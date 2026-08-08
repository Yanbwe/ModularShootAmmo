package org.yanbwe.modularshootammo.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.yanbwe.modularshoot.ModularShootAPI;
import org.yanbwe.modularshoot.attribute.AttributeResolver;
import org.yanbwe.modularshoot.component.GunData;
import org.yanbwe.modularshoot.state.GunState;
import org.yanbwe.modularshoot.state.PlayerState;
import org.yanbwe.modularshootammo.ModularAmmoAPI;
import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.ammo.AmmoInventoryHelper;
import org.yanbwe.modularshootammo.ammo.AmmoStateIds;
import org.yanbwe.modularshootammo.ammo.AmmoText;
import org.yanbwe.modularshootammo.ammo.ReloadMath;
import org.yanbwe.modularshootammo.attribute.ModularAmmoAttributes;
import org.yanbwe.modularshootammo.registry.AmmoType;
import org.yanbwe.modularshootammo.registry.AmmoTypeRegistry;
import org.yanbwe.modularshootammo.registry.GunAmmoBindingRegistry;
import org.yanbwe.modularshootammo.sound.ModularAmmoSounds;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 服务端公共逻辑中枢：射击扣弹、手动/自动换弹、换弹倒计时与中断的完整闭环。
 *
 * <p>仅服务端调用（PreShootEvent/ActionEvent/PlayerTickEvent 均在服务端触发）；
 * 客户端 HUD 不走本类，直接查询注册表（见任务 10 设计）。</p>
 */
public final class AmmoService {

    /** 绑定缺失 WARN 节流间隔：6000 tick = 5 分钟 */
    private static final long UNBOUND_WARN_INTERVAL_TICKS = 6000;
    /** 动作栏提示节流间隔（开始/完成提示） */
    private static final int ACTION_BAR_THROTTLE_TICKS = 5;

    /** 绑定缺失 WARN 节流记录：枪械 id → 上次 WARN 的 gameTime */
    private static final Map<ResourceLocation, Long> LAST_UNBOUND_WARN_TICK = new HashMap<>();

    private AmmoService() {}

    // ------------------------------------------------------------------
    // 特性判断
    // ------------------------------------------------------------------

    /** 枪械是否启用弹药系统（最终特性值，含插件合并；未声明一律 false）。 */
    public static boolean isUsesAmmo(ItemStack gun, RegistryAccess ra) {
        return ModularAmmoAPI.isUsesAmmo(gun, ra);
    }

    /** 枪械是否豁免扣弹（无限弹药，最终特性值，含插件合并）。 */
    public static boolean isInfinite(ItemStack gun, RegistryAccess ra) {
        return ModularAmmoAPI.isInfiniteAmmo(gun, ra);
    }

    // ------------------------------------------------------------------
    // 绑定与弹药类型查询
    // ------------------------------------------------------------------

    /**
     * 查询枪械绑定的弹药类型 id（Java 绑定优先 + 数据包绑定兜底）。
     *
     * <p>绑定缺失且枪械已启用弹药系统时输出 WARN 日志（按枪械 id 节流：
     * 6000 tick = 5 分钟一次，key 形如 {@code "warn_unbound:"+gunId}）。</p>
     *
     * @return 绑定命中时的弹药类型 id，否则 {@link Optional#empty()}
     */
    public static Optional<ResourceLocation> resolveAmmoTypeId(ItemStack gun, RegistryAccess ra, Level level) {
        Optional<ResourceLocation> gunIdOpt = ModularShootAPI.resolveGunId(gun, ra);
        if (gunIdOpt.isEmpty()) {
            return Optional.empty();
        }
        ResourceLocation gunId = gunIdOpt.get();
        Optional<ResourceLocation> bound = GunAmmoBindingRegistry.get(ra, gunId);
        if (bound.isEmpty() && isUsesAmmo(gun, ra)) {
            warnUnboundOnce(level, gunId);
        }
        return bound;
    }

    /** 查询枪械绑定的弹药类型定义（未绑定/类型已移除均返回空）。 */
    public static Optional<AmmoType> resolveAmmoType(ItemStack gun, RegistryAccess ra, Level level) {
        return resolveAmmoTypeId(gun, ra, level).flatMap(id -> AmmoTypeRegistry.get(ra, id));
    }

    /** 绑定缺失 WARN（按 gunId 节流，6000 tick 一次）。 */
    private static void warnUnboundOnce(Level level, ResourceLocation gunId) {
        long now = level.getGameTime();
        Long last = LAST_UNBOUND_WARN_TICK.get(gunId);
        if (last != null && now - last < UNBOUND_WARN_INTERVAL_TICKS) {
            return;
        }
        LAST_UNBOUND_WARN_TICK.put(gunId, now);
        ModularShootAmmo.LOGGER.warn(
                "Gun {} uses ammo (modularshootammo:uses_ammo) but has no ammo type binding; "
                        + "skipping ammo checks. Bind it via modularammo:gun_ammo_bindings datapack or ModularAmmoAPI.bindGun",
                gunId);
    }

    // ------------------------------------------------------------------
    // 属性最终值
    // ------------------------------------------------------------------

    /** 弹匣容量（mag_size 属性最终值，round 取整，兜底 ≥ 1）。 */
    public static int magSizeOf(LivingEntity entity, RegistryAccess ra) {
        return Math.max(1, (int) Math.round(
                AttributeResolver.readFinalValue(entity, ModularAmmoAttributes.MAG_SIZE_ID, ra)));
    }

    /** 换弹时间 tick 数（reload_time 属性最终值，round 取整，兜底 ≥ 1）。 */
    public static int reloadTimeOf(LivingEntity entity, RegistryAccess ra) {
        return Math.max(1, (int) Math.round(
                AttributeResolver.readFinalValue(entity, ModularAmmoAttributes.RELOAD_TIME_ID, ra)));
    }

    // ------------------------------------------------------------------
    // 枪实例
    // ------------------------------------------------------------------

    /** 当前主手枪械实例 uuid（无 GunData 组件时返回 null）。 */
    public static @Nullable UUID gunInstanceUuid(ItemStack gun) {
        return ModularShootAPI.getGunData(gun).map(GunData::gunInstanceUuid).orElse(null);
    }

    // ------------------------------------------------------------------
    // 射击扣弹
    // ------------------------------------------------------------------

    /**
     * 扣一发（服务端，PreShootEvent 调用；调用方已判定启用/非豁免/非创造）。
     *
     * @param perShotCost 每发消耗
     * @return 扣弹后的弹匣余量
     */
    public static int deductOneShot(ServerPlayer player, ItemStack gun, int perShotCost) {
        GunState gs = ModularShootAPI.getState(gun, player);
        if (gs == null) {
            return 0;
        }
        int newMag = ReloadMath.deduct(gs.getInt(AmmoStateIds.MAG_AMMO), perShotCost);
        gs.setInt(AmmoStateIds.MAG_AMMO, newMag);
        return newMag;
    }

    // ------------------------------------------------------------------
    // 换弹
    // ------------------------------------------------------------------

    /**
     * 开始换弹。
     *
     * <p>{@code manual=true}（R 键）时条件不满足给出动作栏提示原因：
     * 弹匣已满 / 背包无对应弹药（带弹药名参数）；{@code manual=false}
     * （自动换弹）时静默返回。</p>
     */
    public static void tryStartReload(ServerPlayer player, ItemStack gun, boolean manual) {
        PlayerState ps = ModularShootAPI.getPlayerState(player);
        if (ps.getInt(AmmoStateIds.RELOAD_TICK) > 0) {
            return; // 已换弹中
        }
        RegistryAccess ra = player.registryAccess();
        Optional<AmmoType> typeOpt = resolveAmmoType(gun, ra, player.level());
        if (typeOpt.isEmpty()) {
            return; // 未绑定（服务端已 WARN）
        }
        AmmoType type = typeOpt.get();
        GunState gs = ModularShootAPI.getState(gun, player);
        if (gs == null) {
            return;
        }
        int mag = gs.getInt(AmmoStateIds.MAG_AMMO);
        int magSize = magSizeOf(player, ra);
        if (ReloadMath.isMagFull(mag, magSize)) {
            if (manual) {
                player.displayClientMessage(AmmoText.resolve("lang:modularshootammo.reload.mag_full"), true);
            }
            return;
        }
        int reserve = AmmoInventoryHelper.countAmmo(
                player.getInventory().items, BuiltInRegistries.ITEM.get(type.item()));
        if (reserve <= 0) {
            if (manual) {
                player.displayClientMessage(
                        AmmoText.resolve("lang:modularshootammo.reload.no_ammo", AmmoText.resolve(type.name())), true);
            }
            return;
        }
        UUID uuid = gunInstanceUuid(gun);
        if (uuid == null) {
            return;
        }
        ps.setUuid(AmmoStateIds.RELOAD_GUN, uuid);
        ps.setInt(AmmoStateIds.RELOAD_TICK, reloadTimeOf(player, ra));
        playReloadSound(player, type, true);
        sendActionBar(player, "lang:modularshootammo.reload.start");
    }

    /**
     * 换弹完成结算：按背包可用弹药补弹、扣背包、清换弹状态、音效与提示。
     *
     * <p>弹药类型已被移除（如数据包变更）时仅 WARN，弹匣保持现状。</p>
     */
    public static void completeReload(ServerPlayer player, ItemStack gun) {
        PlayerState ps = ModularShootAPI.getPlayerState(player);
        RegistryAccess ra = player.registryAccess();
        AmmoType type = null;
        GunState gs = ModularShootAPI.getState(gun, player);
        if (gs != null) {
            int mag = gs.getInt(AmmoStateIds.MAG_AMMO);
            int magSize = magSizeOf(player, ra);
            Optional<AmmoType> typeOpt = resolveAmmoType(gun, ra, player.level());
            if (typeOpt.isEmpty()) {
                ModularShootAmmo.LOGGER.warn("lang:modularshootammo.reload.warn_no_type: ammo type removed while reloading (player={})",
                        player.getScoreboardName());
            } else {
                type = typeOpt.get();
                Item item = BuiltInRegistries.ITEM.get(type.item());
                int need = ReloadMath.fillAmount(mag, magSize,
                        AmmoInventoryHelper.countAmmo(player.getInventory().items, item));
                int consumed = AmmoInventoryHelper.consumeAmmo(player.getInventory().items, item, need);
                gs.setInt(AmmoStateIds.MAG_AMMO, mag + consumed);
            }
        }
        ps.clearState(AmmoStateIds.RELOAD_TICK);
        ps.clearState(AmmoStateIds.RELOAD_GUN);
        playReloadSound(player, type, false);
        sendActionBar(player, "lang:modularshootammo.reload.done");
    }

    /** 中断换弹：清 reload_tick / reload_gun（无提示）。 */
    public static void interruptReload(ServerPlayer player) {
        PlayerState ps = ModularShootAPI.getPlayerState(player);
        ps.clearState(AmmoStateIds.RELOAD_TICK);
        ps.clearState(AmmoStateIds.RELOAD_GUN);
    }

    // ------------------------------------------------------------------
    // 音效与提示
    // ------------------------------------------------------------------

    /**
     * 换弹音效：弹药类型 {@code reload_sound} 覆盖优先，否则 addon 默认音效。
     *
     * @param type  弹药类型（可为 null → 默认音效）
     * @param start {@code true} 开始音效，{@code false} 完成音效
     */
    public static void playReloadSound(ServerPlayer player, @Nullable AmmoType type, boolean start) {
        ResourceLocation sound = (type != null && type.reloadSound() != null)
                ? type.reloadSound()
                : (start ? ModularAmmoSounds.RELOAD_START.getId() : ModularAmmoSounds.RELOAD_FINISH.getId());
        player.level().playSound(null, player.blockPosition(),
                SoundEvent.createVariableRangeEvent(sound), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /** 动作栏提示（MessageThrottle 节流：5 tick 内同文本不重复）。 */
    private static void sendActionBar(ServerPlayer player, String text) {
        if (MessageThrottle.shouldSend(player, text, ACTION_BAR_THROTTLE_TICKS)) {
            player.displayClientMessage(AmmoText.resolve(text), true);
        }
    }
}
