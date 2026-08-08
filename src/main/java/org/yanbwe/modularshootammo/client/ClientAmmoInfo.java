package org.yanbwe.modularshootammo.client;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.yanbwe.modularshoot.ModularShootAPI;
import org.yanbwe.modularshoot.client.ClientGunDataStore;
import org.yanbwe.modularshoot.component.GunData;
import org.yanbwe.modularshoot.state.GunState;
import org.yanbwe.modularshootammo.ammo.AmmoInventoryHelper;
import org.yanbwe.modularshootammo.ammo.AmmoStateIds;
import org.yanbwe.modularshootammo.registry.AmmoType;
import org.yanbwe.modularshootammo.registry.AmmoTypeRegistry;
import org.yanbwe.modularshootammo.registry.GunAmmoBindingRegistry;
import org.yanbwe.modularshootammo.server.AmmoService;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 客户端 HUD 数据装配：从客户端可见的注册表/同步快照/物品组件读取
 * 弹药显示所需的全部字段。
 *
 * <p>与 {@link AmmoService} 服务端逻辑的区别：绑定查询<strong>不走</strong>
 * {@code AmmoService.resolveAmmoType}（那是服务端 WARN 节流版），
 * 直接查 {@link GunAmmoBindingRegistry} + {@link AmmoTypeRegistry}；其余
 * 特性判断（uses_ammo / infinite）复用服务端同款静态方法（纯查询，无副作用）。</p>
 */
public final class ClientAmmoInfo {

    private ClientAmmoInfo() {}

    /** HUD 单帧渲染数据。 */
    public record AmmoHudData(int mag, int magSize, int reserve, int reloadTick, int reloadTotal,
                              boolean infinite, @Nullable AmmoType type) {
        /** 是否正在换弹（换弹进度 > 0）。 */
        public boolean reloading() {
            return reloadTick > 0;
        }
    }

    /**
     * 主手为已启用弹药系统且已绑定弹药类型的枪时返回 HUD 数据，否则
     * {@code null}（不显示 HUD）。
     */
    public static @Nullable AmmoHudData collect(Player player, ItemStack gun) {
        RegistryAccess ra = player.registryAccess();
        if (!AmmoService.isUsesAmmo(gun, ra)) {
            return null;
        }
        boolean infinite = AmmoService.isInfinite(gun, ra);
        // 客户端查询不走服务端 WARN 版 resolveAmmoType，直接查注册表
        Optional<ResourceLocation> gunIdOpt = ModularShootAPI.resolveGunId(gun, ra);
        Optional<ResourceLocation> typeIdOpt = gunIdOpt.flatMap(gid -> GunAmmoBindingRegistry.get(ra, gid));
        Optional<AmmoType> typeOpt = typeIdOpt.flatMap(tid -> AmmoTypeRegistry.get(ra, tid));
        if (typeOpt.isEmpty()) {
            return null; // 启用但未绑定 → 无 HUD（与服务端静默回退一致）
        }
        AmmoType type = typeOpt.get();
        // 弹匣：优先 ClientGunDataStore 同步快照，回退主手栈组件
        int mag = readMag(player, gun);
        int magSize = ClientAttributeValues.magSize(gun);
        int reserve = AmmoInventoryHelper.countAmmo(player.getInventory().items,
                BuiltInRegistries.ITEM.get(type.item()));
        int reloadTick = ModularShootAPI.getPlayerState(player).getInt(AmmoStateIds.RELOAD_TICK);
        int reloadTotal = ClientAttributeValues.reloadTime(gun);
        return new AmmoHudData(mag, magSize, reserve, reloadTick, reloadTotal, infinite, type);
    }

    /** 弹匣读数：同步快照优先，回退主手栈 GUN_DATA 组件，都没有 → 0。 */
    private static int readMag(Player player, ItemStack gun) {
        ClientGunDataStore store = ClientGunDataStore.getInstance();
        RegistryAccess ra = player.registryAccess();
        if (store.hasSyncData()) {
            GunState synced = GunState.of(store.getState(), ra);
            return synced.getInt(AmmoStateIds.MAG_AMMO);
        }
        Optional<GunData> gd = ModularShootAPI.getGunData(gun);
        if (gd.isPresent()) {
            return GunState.of(gd.get().state(), ra).getInt(AmmoStateIds.MAG_AMMO);
        }
        return 0;
    }
}
