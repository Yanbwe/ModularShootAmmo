package org.yanbwe.modularshootammo.client;

import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.ammo.AmmoText;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * 弹药 HUD 层：弹匣/备弹文本 + 换弹中提示与进度条（四角锚点可配置）。
 *
 * <p>注册为 {@code registerAboveAll}（mod bus，Dist.CLIENT），绘制数据由
 * {@link ClientAmmoInfo#collect} 装配，位置/缩放/锚点由 {@link HudConfig} 控制
 * （数据包 {@code modularammo/hud_config.json} 热重载）。</p>
 *
 * <p>缩放实现：pushPose 后先 scale，再按放大后的逻辑屏幕尺寸做锚点计算，
 * {@code anchor=bottom_right} 时与旧版右下角行为完全一致（实际像素位置为
 * {@code screenWidth - offset - textWidth*scale}）。</p>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AmmoHudLayer {

    /** 弹匣空（不足一发）时的警示红。 */
    private static final int COLOR_OUT_OF_AMMO = 0xFFFF5555;
    /** 换弹提示文本颜色。 */
    private static final int COLOR_RELOAD_TEXT = 0xFFFFFFFF;
    /** 进度条底色（半透明黑）。 */
    private static final int COLOR_BAR_BG = 0xAA000000;
    /** 进度条前景色（半透明白）。 */
    private static final int COLOR_BAR_FG = 0xAAFFFFFF;

    private AmmoHudLayer() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "ammo_hud"),
                AmmoHudLayer::render);
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        if (mc.options.hideGui) {
            return;
        }
        ItemStack gun = player.getMainHandItem();
        ClientAmmoInfo.AmmoHudData data = ClientAmmoInfo.collect(player, gun);
        if (data == null) {
            return;
        }

        Font font = mc.font;
        boolean creative = player.isCreative();
        boolean infinite = data.infinite();

        // 行 1：弹匣/备弹（∞ 用翻译键，方便本地化；创造/无限均显示 ∞）
        MutableComponent magText = infinite
                ? AmmoText.resolve("lang:modularshootammo.hud.unbounded")
                : Component.literal(String.valueOf(data.mag()));
        MutableComponent line1 = magText;
        if (HudConfig.showReserve()) {
            MutableComponent reserveText = (creative || infinite)
                    ? AmmoText.resolve("lang:modularshootammo.hud.unbounded")
                    : Component.literal(String.valueOf(data.reserve()));
            line1 = magText.copy().append(Component.literal("/")).append(reserveText);
        }
        // 弹匣空（mag < perShotCost 且非无限）→ 红，否则弹药类型色
        int color = (data.mag() < data.type().perShotCost() && !infinite)
                ? COLOR_OUT_OF_AMMO
                : data.type().color();

        boolean showReload = data.reloading() && HudConfig.showReloadProgress();
        double scale = HudConfig.scale();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale((float) scale, (float) scale, 1.0f);
        // 锚点定位：先 scale 再按逻辑坐标计算；offset 始终是"距屏幕边缘的
        // 像素距离"（scale 后逻辑坐标），四角由 anchor 决定贴合方向。
        int textWidth = font.width(line1);
        int totalHeight = font.lineHeight + (showReload ? font.lineHeight + 6 : 0);
        boolean right = HudConfig.anchor().endsWith("right");
        boolean bottom = HudConfig.anchor().startsWith("bottom");
        int x = right
                ? (int) ((screenWidth - HudConfig.offsetX()) / scale) - textWidth
                : (int) (HudConfig.offsetX() / scale);
        int y = bottom
                ? (int) ((screenHeight - HudConfig.offsetY()) / scale) - totalHeight
                : (int) (HudConfig.offsetY() / scale);

        guiGraphics.drawString(font, line1, x, y, color);

        // 行 2（换弹中）：文字 + 进度条
        if (showReload) {
            MutableComponent reloadText = AmmoText.resolve("lang:modularshootammo.reload.in_progress");
            int reloadY = y + font.lineHeight + 2;
            guiGraphics.drawString(font, reloadText, x, reloadY, COLOR_RELOAD_TEXT);
            int barY = reloadY + font.lineHeight + 2;
            int barWidth = Math.max(textWidth, font.width(reloadText));
            guiGraphics.fill(x, barY, x + barWidth, barY + 2, COLOR_BAR_BG);
            // 进度 = 剩余换弹 tick 占比的反向（reloadTotal ≥ 1 由 ClientAttributeValues 兜底）
            float progress = Mth.clamp(
                    1.0f - (float) data.reloadTick() / (float) data.reloadTotal(), 0.0f, 1.0f);
            int fgWidth = Math.round(barWidth * progress);
            if (fgWidth > 0) {
                guiGraphics.fill(x, barY, x + fgWidth, barY + 2, COLOR_BAR_FG);
            }
        }

        guiGraphics.pose().popPose();
    }
}
