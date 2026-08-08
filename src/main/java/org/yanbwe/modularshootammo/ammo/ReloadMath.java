package org.yanbwe.modularshootammo.ammo;

/**
 * 换弹纯数学逻辑（纯静态，无 MC 依赖，可单测）。
 * 弹匣数值语义：mag 为弹匣内当前弹药数，perShotCost 为每发消耗。
 */
public final class ReloadMath {

    private ReloadMath() {}

    /** 扣弹：clamp 到 0，不扣成负数 */
    public static int deduct(int mag, int perShotCost) {
        return Math.max(0, mag - perShotCost);
    }

    /** 弹药不足判定：弹匣 < 每发消耗（等于消耗正好够一发，不算空） */
    public static boolean isOutOfAmmo(int mag, int perShotCost) {
        return mag < perShotCost;
    }

    /** 换弹应补充的数量：min(弹匣容量 - 当前, 背包可用)，两侧均 ≥ 0 */
    public static int fillAmount(int mag, int magSize, int available) {
        int need = Math.max(0, magSize - mag);
        return Math.min(need, Math.max(0, available));
    }

    /** 弹匣是否已满（等于或超过容量均视为满） */
    public static boolean isMagFull(int mag, int magSize) {
        return mag >= magSize;
    }
}
