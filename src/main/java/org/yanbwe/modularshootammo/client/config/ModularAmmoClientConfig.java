package org.yanbwe.modularshootammo.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-side {@link ModConfigSpec} —— 模组的客户端配置文件
 * （{@code modularshootammo-client.toml}），可在游戏内配置界面编辑
 * （{@code Options → Mods → ModularShootAmmo}，经
 * {@code ModularShootAmmoClient} 注册的 {@code IConfigScreenFactory}）。
 *
 * <p>当前承载弹药 HUD 的全部显示配置（原数据包 {@code hud_config.json}
 * 迁移而来）：右下角偏移、缩放、是否显示备弹、是否显示换弹进度、
 * 锚点（四角定位）。</p>
 *
 * <p><strong>客户端专属类。</strong>在 {@code @Mod(dist = Dist.CLIENT)}
 * 构造器中以 {@link net.neoforged.neoforge.common.ModConfig.Type#CLIENT}
 * 注册，专用服务器上不加载。</p>
 *
 * <p>所有值经 {@code ModConfigSpec.ConfigValue#get()} 读取实时值——
 * 配置界面修改保存后下一帧即生效，无需重启。</p>
 *
 * <p>本类不可实例化。</p>
 */
public final class ModularAmmoClientConfig {

    /** 构建完成的客户端配置 spec，经 {@code ModContainer#registerConfig} 注册。 */
    public static final ModConfigSpec SPEC;

    /** HUD 锚点：四角定位。 */
    public enum HudAnchor {
        BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT
    }

    /** HUD 距屏幕右边缘的偏移（GUI 缩放后像素）。 */
    private static final ModConfigSpec.IntValue HUD_OFFSET_X;

    /** HUD 距屏幕下边缘的偏移（GUI 缩放后像素）。 */
    private static final ModConfigSpec.IntValue HUD_OFFSET_Y;

    /** HUD 整体缩放倍率。 */
    private static final ModConfigSpec.DoubleValue HUD_SCALE;

    /** 是否显示备弹（弹匣/备弹格式的第二段）。 */
    private static final ModConfigSpec.BooleanValue HUD_SHOW_RESERVE;

    /** 是否显示换弹中提示与进度条。 */
    private static final ModConfigSpec.BooleanValue HUD_SHOW_RELOAD_PROGRESS;

    /** HUD 锚点（四角定位）。 */
    private static final ModConfigSpec.EnumValue<HudAnchor> HUD_ANCHOR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        HUD_OFFSET_X = builder
                .comment(
                        "HUD horizontal offset from the right/left screen edge, in GUI-scaled pixels.",
                        "Default 8.")
                .translation("modularshootammo.configuration.hud.offsetX")
                .defineInRange("hud.offsetX", 8, 0, 2000);

        HUD_OFFSET_Y = builder
                .comment(
                        "HUD vertical offset from the top/bottom screen edge, in GUI-scaled pixels.",
                        "Default 8.")
                .translation("modularshootammo.configuration.hud.offsetY")
                .defineInRange("hud.offsetY", 8, 0, 2000);

        HUD_SCALE = builder
                .comment(
                        "HUD overall scale factor.",
                        "Range 0.25-4.0, default 1.0.")
                .translation("modularshootammo.configuration.hud.scale")
                .defineInRange("hud.scale", 1.0, 0.25, 4.0);

        HUD_SHOW_RESERVE = builder
                .comment(
                        "Show the reserve ammo count (the second segment of 'mag/reserve').",
                        "When disabled, only the magazine count is shown. Default true.")
                .translation("modularshootammo.configuration.hud.showReserve")
                .define("hud.showReserve", true);

        HUD_SHOW_RELOAD_PROGRESS = builder
                .comment(
                        "Show the reloading message and progress bar while reloading.",
                        "Default true.")
                .translation("modularshootammo.configuration.hud.showReloadProgress")
                .define("hud.showReloadProgress", true);

        HUD_ANCHOR = builder
                .comment(
                        "HUD corner anchor: BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT or TOP_LEFT.",
                        "Default BOTTOM_RIGHT.")
                .translation("modularshootammo.configuration.hud.anchor")
                .defineEnum("hud.anchor", HudAnchor.BOTTOM_RIGHT);

        SPEC = builder.build();
    }

    private ModularAmmoClientConfig() {
    }

    /** HUD 距屏幕右/左边缘的偏移（GUI 缩放后像素）。 */
    public static int getOffsetX() {
        return HUD_OFFSET_X.get();
    }

    /** HUD 距屏幕下/上边缘的偏移（GUI 缩放后像素）。 */
    public static int getOffsetY() {
        return HUD_OFFSET_Y.get();
    }

    /** HUD 整体缩放倍率。 */
    public static double getScale() {
        return HUD_SCALE.get();
    }

    /** 是否显示备弹。 */
    public static boolean isShowReserve() {
        return HUD_SHOW_RESERVE.get();
    }

    /** 是否显示换弹中提示与进度条。 */
    public static boolean isShowReloadProgress() {
        return HUD_SHOW_RELOAD_PROGRESS.get();
    }

    /** HUD 锚点。 */
    public static HudAnchor getAnchor() {
        return HUD_ANCHOR.get();
    }
}
