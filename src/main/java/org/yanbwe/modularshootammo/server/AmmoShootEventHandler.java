package org.yanbwe.modularshootammo.server;

import java.util.Optional;

import org.yanbwe.modularshoot.shooting.PreShootEvent;
import org.yanbwe.modularshootammo.ModularShootAmmo;
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
 * <p>创造/未启用/豁免/未绑定 → 不介入；否则每发扣弹。最后一发打空后
 * 不在此处自动换弹：自动换弹只由"空仓扣扳机"（下一发被 predicate 阻止并
 * 打信号后，PlayerTick 检测）触发，见 {@link AmmoReloadTickHandler}。</p>
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
        AmmoService.deductOneShot(sp, gun, perShot); // 只扣弹，不触发换弹
    }

    private AmmoShootEventHandler() {}
}
