package org.yanbwe.modularshootammo.client;

import java.util.Set;

/**
 * 客户端 HUD 配置单例（由 {@link HudConfigReloadListener} 从
 * {@code data/<ns>/modularammo/hud_config.json} 热重载更新，缺省字段用默认值）。
 *
 * <p>字段均为 volatile 静态字段，读多写少（渲染每帧读，/reload 时写），
 * 无需加锁。</p>
 */
public final class HudConfig {

    /** 默认锚点：右下角。 */
    public static final String DEFAULT_ANCHOR = "bottom_right";

    /** 合法锚点值：bottom_right / bottom_left / top_right / top_left。 */
    private static final Set<String> VALID_ANCHORS =
            Set.of("bottom_right", "bottom_left", "top_right", "top_left");

    private static final int DEFAULT_OFFSET_X = 8;
    private static final int DEFAULT_OFFSET_Y = 8;
    private static final double DEFAULT_SCALE = 1.0;
    private static final boolean DEFAULT_SHOW_RESERVE = true;
    private static final boolean DEFAULT_SHOW_RELOAD_PROGRESS = true;

    private static volatile int offsetX = DEFAULT_OFFSET_X;
    private static volatile int offsetY = DEFAULT_OFFSET_Y;
    private static volatile double scale = DEFAULT_SCALE;
    private static volatile boolean showReserve = DEFAULT_SHOW_RESERVE;
    private static volatile boolean showReloadProgress = DEFAULT_SHOW_RELOAD_PROGRESS;
    private static volatile String anchor = DEFAULT_ANCHOR;

    private HudConfig() {}

    // ------------------------------------------------------------------
    // 访问器（渲染直接读取）
    // ------------------------------------------------------------------

    /** 右下角水平偏移（像素，未缩放坐标系）。 */
    public static int offsetX() {
        return offsetX;
    }

    /** 右下角垂直偏移（像素，未缩放坐标系）。 */
    public static int offsetY() {
        return offsetY;
    }

    /** HUD 缩放倍率（非法值在解析/更新时兜底 ≥ 0.25）。 */
    public static double scale() {
        return scale;
    }

    /** 是否显示备弹数（false 时只显示弹匣数）。 */
    public static boolean showReserve() {
        return showReserve;
    }

    /** 是否显示换弹中提示与进度条。 */
    public static boolean showReloadProgress() {
        return showReloadProgress;
    }

    /** 锚点（bottom_right / bottom_left / top_right / top_left）。 */
    public static String anchor() {
        return anchor;
    }

    // ------------------------------------------------------------------
    // 配置写入（仅 ReloadListener 调用）
    // ------------------------------------------------------------------

    /** 由 reload listener 用解析后的字段值更新单例；无效值兜底。 */
    static void update(int newOffsetX, int newOffsetY, double newScale,
                       boolean newShowReserve, boolean newShowReloadProgress, String newAnchor) {
        offsetX = newOffsetX;
        offsetY = newOffsetY;
        scale = Double.isFinite(newScale) ? Math.max(0.25, newScale) : DEFAULT_SCALE;
        showReserve = newShowReserve;
        showReloadProgress = newShowReloadProgress;
        anchor = VALID_ANCHORS.contains(newAnchor) ? newAnchor : DEFAULT_ANCHOR;
    }

    /** 重置为默认值（reload 时配置文件缺失/解析失败调用）。 */
    static void reset() {
        offsetX = DEFAULT_OFFSET_X;
        offsetY = DEFAULT_OFFSET_Y;
        scale = DEFAULT_SCALE;
        showReserve = DEFAULT_SHOW_RESERVE;
        showReloadProgress = DEFAULT_SHOW_RELOAD_PROGRESS;
        anchor = DEFAULT_ANCHOR;
    }
}
