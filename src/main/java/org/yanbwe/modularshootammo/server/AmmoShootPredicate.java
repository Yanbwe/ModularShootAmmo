package org.yanbwe.modularshootammo.server;

import java.util.Optional;

import org.yanbwe.modularshoot.ModularShootAPI;
import org.yanbwe.modularshoot.shooting.ShootPredicate;
import org.yanbwe.modularshoot.shooting.ShootPredicateResult;
import org.yanbwe.modularshoot.state.GunState;
import org.yanbwe.modularshoot.state.PlayerState;
import org.yanbwe.modularshootammo.ammo.AmmoInventoryHelper;
import org.yanbwe.modularshootammo.ammo.AmmoStateIds;
import org.yanbwe.modularshootammo.ammo.ReloadMath;
import org.yanbwe.modularshootammo.registry.AmmoType;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 射击判断器（纯判断无副作用，注册由主类完成）。
 *
 * <p>判断顺序：未启用/豁免/未绑定/创造 → 放行；换弹中 → 阻止（提示
 * {@code reload.in_progress}）；弹匣不足 → 阻止（背包有弹提示自动换弹
 * {@code empty_auto}，无弹提示 {@code empty}）。</p>
 */
public final class AmmoShootPredicate implements ShootPredicate {

    @Override
    public ShootPredicateResult test(Player player, ItemStack gun) {
        RegistryAccess ra = player.registryAccess();
        if (!AmmoService.isUsesAmmo(gun, ra)) {
            return ShootPredicateResult.success(); // 未启用不介入
        }
        if (AmmoService.isInfinite(gun, ra)) {
            return ShootPredicateResult.success(); // 豁免
        }
        Optional<AmmoType> type = AmmoService.resolveAmmoType(gun, ra, player.level());
        if (type.isEmpty()) {
            return ShootPredicateResult.success(); // 未绑定→放行（服务端已 WARN）
        }
        if (player.isCreative()) {
            return ShootPredicateResult.success(); // 创造模式不检查
        }
        PlayerState ps = ModularShootAPI.getPlayerState(player);
        if (ps.getInt(AmmoStateIds.RELOAD_TICK) > 0) {
            return ShootPredicateResult.failure("lang:modularshootammo.reload.in_progress");
        }
        GunState gs = ModularShootAPI.getState(gun, player);
        if (gs == null) {
            return ShootPredicateResult.success();
        }
        int mag = gs.getInt(AmmoStateIds.MAG_AMMO);
        if (ReloadMath.isOutOfAmmo(mag, type.get().perShotCost())) {
            int reserve = AmmoInventoryHelper.countAmmo(player.getInventory().items,
                    BuiltInRegistries.ITEM.get(type.get().item()));
            // 背包有备弹：空仓扣扳机 → 打"待自动换弹"内存信号（PlayerTick 消费一次）
            // （框架仅从服务端 ShootingEngine 调用本 predicate，player 必为 ServerPlayer；
            //   instanceof 仅为防御，避免非预期调用路径下抛错）
            if (reserve > 0 && player instanceof ServerPlayer sp) {
                AmmoService.markPendingAutoReload(sp);
            }
            return ShootPredicateResult.failure(reserve > 0
                    ? "lang:modularshootammo.ammo.empty_auto"  // 弹药不足，自动换弹中...
                    : "lang:modularshootammo.ammo.empty");     // 弹药不足
        }
        return ShootPredicateResult.success();
    }
}
