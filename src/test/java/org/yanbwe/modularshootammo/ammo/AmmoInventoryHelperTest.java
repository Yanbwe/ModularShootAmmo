package org.yanbwe.modularshootammo.ammo;

import java.util.List;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 背包弹药统计/扣取测试（JUnit 5，用原版静态物品构造 ItemStack，无需游戏实例）。
 * 覆盖：countAmmo 单堆叠/跨堆叠/混排/空列表；
 * consumeAmmo 充足跨堆叠、不足（返回实际扣除数且未波及堆保留）、恰好耗尽一堆、物品不匹配不误扣。
 *
 * <p>JUnit 环境桥接（探针已验证，仿框架 ModularShootAPIItemBindingTest）：
 * {@code ItemStack.<clinit>} 触发 {@code BuiltInRegistries.<clinit>}，要求原版
 * Bootstrap 先运行；FML shim + 游戏版本 + bootStrap 三步缺一不可。本测试只读
 * 用现成物品（铁锭/金锭），无需 unfreezeData 注册新条目。</p>
 */
class AmmoInventoryHelperTest {

    static {
        // FML shim: FeatureFlags.<clinit> -> FeatureFlagLoader 需要非 null LoadingModList
        net.neoforged.fml.loading.LoadingModList.of(
                List.of(), List.of(), List.of(), List.of(), Map.of());
        // DataFixers.<clinit> 需要当前游戏版本
        net.minecraft.SharedConstants.setVersion(net.minecraft.DetectedVersion.BUILT_IN);
        // 完整原版注册表 bootstrap
        net.minecraft.server.Bootstrap.bootStrap();
    }

    private static ItemStack iron(int count) {
        return new ItemStack(Items.IRON_INGOT, count);
    }

    private static ItemStack gold(int count) {
        return new ItemStack(Items.GOLD_INGOT, count);
    }

    @Test
    void countAmmoSingleStack() {
        assertEquals(64, AmmoInventoryHelper.countAmmo(List.of(iron(64)), Items.IRON_INGOT));
    }

    @Test
    void countAmmoAcrossStacks() {
        assertEquals(96, AmmoInventoryHelper.countAmmo(
                List.of(iron(64), iron(32)), Items.IRON_INGOT));
    }

    @Test
    void countAmmoIgnoresOtherItems() {
        // 混排：铁锭 64+16，金锭 32 不参与统计
        assertEquals(80, AmmoInventoryHelper.countAmmo(
                List.of(iron(64), gold(32), iron(16)), Items.IRON_INGOT));
    }

    @Test
    void countAmmoEmptyList() {
        assertEquals(0, AmmoInventoryHelper.countAmmo(List.of(), Items.IRON_INGOT));
    }

    @Test
    void countAmmoEmptyStacksSkipped() {
        // 空堆叠（ItemStack.EMPTY）跳过，不贡献数量
        assertEquals(64, AmmoInventoryHelper.countAmmo(
                List.of(ItemStack.EMPTY, iron(64)), Items.IRON_INGOT));
    }

    @Test
    void consumeAmmoSufficientAcrossStacks() {
        // 64 + 32，扣 80：第一堆 64 清空，第二堆 32→16
        List<ItemStack> stacks = new java.util.ArrayList<>(List.of(iron(64), iron(32)));
        int consumed = AmmoInventoryHelper.consumeAmmo(stacks, Items.IRON_INGOT, 80);
        assertEquals(80, consumed);
        assertEquals(0, stacks.get(0).getCount());
        assertEquals(16, stacks.get(1).getCount());
    }

    @Test
    void consumeAmmoInsufficientReturnsActual() {
        // 64 + 10，扣 100：只够 74，返回实际扣除数，未波及堆（金锭）保留
        List<ItemStack> stacks = new java.util.ArrayList<>(List.of(iron(64), iron(10), gold(5)));
        int consumed = AmmoInventoryHelper.consumeAmmo(stacks, Items.IRON_INGOT, 100);
        assertEquals(74, consumed);
        assertEquals(0, stacks.get(0).getCount());
        assertEquals(0, stacks.get(1).getCount());
        assertEquals(5, stacks.get(2).getCount());
    }

    @Test
    void consumeAmmoExactlyDepletesOneStack() {
        // 恰好耗尽第一堆，第二堆金锭不受影响
        List<ItemStack> stacks = new java.util.ArrayList<>(List.of(iron(64), gold(64)));
        int consumed = AmmoInventoryHelper.consumeAmmo(stacks, Items.IRON_INGOT, 64);
        assertEquals(64, consumed);
        assertEquals(0, stacks.get(0).getCount());
        assertEquals(64, stacks.get(1).getCount());
    }

    @Test
    void consumeAmmoMismatchedItemNotTouched() {
        // 背包只有金锭，扣铁锭 → 返回 0，金锭原样保留
        List<ItemStack> stacks = new java.util.ArrayList<>(List.of(gold(64)));
        int consumed = AmmoInventoryHelper.consumeAmmo(stacks, Items.IRON_INGOT, 10);
        assertEquals(0, consumed);
        assertEquals(64, stacks.get(0).getCount());
    }

    @Test
    void consumeAmmoZeroCountReturnsZero() {
        List<ItemStack> stacks = new java.util.ArrayList<>(List.of(iron(64)));
        assertEquals(0, AmmoInventoryHelper.consumeAmmo(stacks, Items.IRON_INGOT, 0));
        assertEquals(64, stacks.get(0).getCount());
    }
}
