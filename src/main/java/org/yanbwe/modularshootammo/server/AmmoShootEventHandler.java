package org.yanbwe.modularshootammo.server;

import java.util.Optional;

import org.yanbwe.modularshoot.shooting.PreShootEvent;
import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.ammo.ReloadMath;
import org.yanbwe.modularshootammo.registry.AmmoType;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * PreShootEvent 扣弹（服务端）。
 *
 * <p>创造/未启用/豁免/未绑定 → 不介入；否则每发扣弹，打空（弹匣
 * 不足以支付下一发）后立即触发自动换弹（静默）。</p>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID)
public final class AmmoShootEventHandler {

    @SubscribeEvent
    public static void onPreShoot(PreShootEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        if (player.isCreative()) {
            return; // 创造模式不扣弹
        }
        ItemStack gun = event.getGun();
        RegistryAccess ra = player.registryAccess();
        if (!AmmoService.isUsesAmmo(gun, ra)) {
            return;
        }
        if (AmmoService.isInfinite(gun, ra)) {
            return;
        }
        Optional<AmmoType> type = AmmoService.resolveAmmoType(gun, ra, player.level());
        if (type.isEmpty()) {
            return;
        }
        int perShot = type.get().perShotCost();
        int mag = AmmoService.deductOneShot(sp, gun, perShot);
        if (ReloadMath.isOutOfAmmo(mag, perShot)) {
            AmmoService.tryStartReload(sp, gun, false); // 最后一发打空 → 立即自动换弹
        }
    }

    private AmmoShootEventHandler() {}
}
