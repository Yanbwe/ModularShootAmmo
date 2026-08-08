package org.yanbwe.modularshootammo.server;

import java.util.Optional;
import java.util.UUID;

import org.yanbwe.modularshoot.ModularShootAPI;
import org.yanbwe.modularshoot.state.GunState;
import org.yanbwe.modularshoot.state.PlayerState;
import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.ammo.AmmoStateIds;
import org.yanbwe.modularshootammo.ammo.ReloadMath;
import org.yanbwe.modularshootammo.registry.AmmoType;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 换弹倒计时与自动换弹检测（PlayerTickEvent.Post，服务端）+ 死亡/登出清理。
 *
 * <p>倒计时中：主手枪实例与 {@code reload_gun} 不符 → 中断；tick 归零 →
 * 完成结算；否则递减。倒计时外：弹匣不足且背包有弹 → 静默自动换弹
 * （射击被阻止后的下一 tick 触发）。</p>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID)
public final class AmmoReloadTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        PlayerState ps = ModularShootAPI.getPlayerState(sp);
        int tick = ps.getInt(AmmoStateIds.RELOAD_TICK);
        if (tick > 0) {
            ItemStack mainHand = sp.getMainHandItem();
            UUID reloadGun = ps.getUuid(AmmoStateIds.RELOAD_GUN);
            UUID current = AmmoService.gunInstanceUuid(mainHand);
            if (current == null || !current.equals(reloadGun)) {
                AmmoService.interruptReload(sp); // 切枪/丢枪/收枪 → 中断
                return;
            }
            if (tick <= 1) {
                AmmoService.completeReload(sp, mainHand);
            } else {
                ps.setInt(AmmoStateIds.RELOAD_TICK, tick - 1);
            }
            return;
        }
        // 自动换弹检测：弹匣不足且背包有对应弹药（射击被阻止后的下一 tick 触发）
        ItemStack gun = sp.getMainHandItem();
        RegistryAccess ra = sp.registryAccess();
        if (!AmmoService.isUsesAmmo(gun, ra)) {
            return;
        }
        if (AmmoService.isInfinite(gun, ra)) {
            return;
        }
        Optional<AmmoType> type = AmmoService.resolveAmmoType(gun, ra, sp.level());
        if (type.isEmpty()) {
            return;
        }
        GunState gs = ModularShootAPI.getState(gun, sp);
        if (gs == null) {
            return;
        }
        int mag = gs.getInt(AmmoStateIds.MAG_AMMO);
        if (ReloadMath.isOutOfAmmo(mag, type.get().perShotCost())) {
            AmmoService.tryStartReload(sp, gun, false); // 静默
        }
    }

    /** 死亡 → 中断换弹（NeoForge 1.21.1 无 PlayerDeathEvent，用 LivingDeathEvent）。 */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            AmmoService.interruptReload(sp);
        }
    }

    /** 登出 → 中断换弹 + 清理该玩家的提示节流记录。 */
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            AmmoService.interruptReload(sp);
            MessageThrottle.clear(sp.getUUID());
        }
    }

    private AmmoReloadTickHandler() {}
}
