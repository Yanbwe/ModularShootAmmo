package org.yanbwe.modularshootammo.ammo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 纯逻辑换弹数学测试（无 MC 依赖，JUnit 5）。覆盖：
 * deduct 扣弹 clamp 到 0、isOutOfAmmo 边界（mag == perShotCost 不视为空）、
 * fillAmount 各分支（充足/不足/已满/超容量）、isMagFull 边界。
 */
class ReloadMathTest {

    @Test
    void deductClampsToZero() {
        assertEquals(29, ReloadMath.deduct(30, 1));   // 常规扣一发
        assertEquals(0, ReloadMath.deduct(0, 1));     // 空弹匣不扣成负数
        assertEquals(0, ReloadMath.deduct(3, 5));     // 消耗大于弹匣不扣成负数
    }

    @Test
    void isOutOfAmmoBoundary() {
        // mag == perShotCost：正好够一发，不算空
        assertFalse(ReloadMath.isOutOfAmmo(1, 1));
        assertFalse(ReloadMath.isOutOfAmmo(3, 3));
        // mag < perShotCost：不够一发，算空
        assertTrue(ReloadMath.isOutOfAmmo(0, 1));
        assertTrue(ReloadMath.isOutOfAmmo(2, 3));
        // mag > perShotCost：充足
        assertFalse(ReloadMath.isOutOfAmmo(5, 2));
    }

    @Test
    void fillAmountWithSufficientReserve() {
        // 需要 20，背包有 100 → 补 20
        assertEquals(20, ReloadMath.fillAmount(10, 30, 100));
    }

    @Test
    void fillAmountWithInsufficientReserve() {
        // 需要 20，背包只有 5 → 补 5
        assertEquals(5, ReloadMath.fillAmount(10, 30, 5));
    }

    @Test
    void fillAmountWhenMagFull() {
        // 弹匣已满 → 补 0
        assertEquals(0, ReloadMath.fillAmount(30, 30, 100));
    }

    @Test
    void fillAmountWhenMagExceedsCapacity() {
        // 弹匣超过容量（异常数据）→ 补 0，不扣回
        assertEquals(0, ReloadMath.fillAmount(40, 30, 100));
    }

    @Test
    void fillAmountWithZeroAvailable() {
        assertEquals(0, ReloadMath.fillAmount(10, 30, 0));
    }

    @Test
    void isMagFullBoundary() {
        assertTrue(ReloadMath.isMagFull(30, 30));   // 等于容量算满
        assertFalse(ReloadMath.isMagFull(29, 30));  // 差一发不满
        assertTrue(ReloadMath.isMagFull(31, 30));   // 超容量（异常数据）视为满
    }
}
