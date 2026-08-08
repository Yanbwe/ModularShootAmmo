package org.yanbwe.modularshootammo.server;

import org.yanbwe.modularshoot.api.event.ActionEvent;
import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.ammo.AmmoText;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * ActionEvent（默认 R 键）手动换弹（服务端）。
 *
 * <p>未启用 → 不介入；豁免（无限弹药）→ 动作栏提示
 * {@code reload.infinite}；否则走 {@link AmmoService#tryStartReload}
 * 完整条件检查（弹匣满/无弹提示原因）。</p>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID)
public final class AmmoReloadHandler {

    @SubscribeEvent
    public static void onAction(ActionEvent event) {
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        ItemStack gun = event.getGun();
        RegistryAccess ra = player.registryAccess();
        if (!AmmoService.isUsesAmmo(gun, ra)) {
            return; // 未启用不介入
        }
        if (AmmoService.isInfinite(gun, ra)) {
            sp.displayClientMessage(AmmoText.resolve("lang:modularshootammo.reload.infinite"), true);
            return;
        }
        AmmoService.tryStartReload(sp, gun, true);
    }

    private AmmoReloadHandler() {}
}
