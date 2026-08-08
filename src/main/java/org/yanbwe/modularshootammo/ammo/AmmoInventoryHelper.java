package org.yanbwe.modularshootammo.ammo;

import java.util.List;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 背包弹药操作（纯静态，只依赖 {@link List}{@code <ItemStack>} + {@link Item}）。
 * 不改动堆叠顺序，不新增/删除堆叠槽位，仅对现有堆叠 shrink。
 */
public final class AmmoInventoryHelper {

    private AmmoInventoryHelper() {}

    /** 统计列表中该物品的总数量（空堆叠跳过） */
    public static int countAmmo(List<ItemStack> items, Item ammoItem) {
        int total = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem() == ammoItem) {
                total += stack.getCount();
            }
        }
        return total;
    }

    /**
     * 跨堆叠扣除 count 个，返回实际扣除数（不足则扣完可用部分）。
     * 仅对匹配 ammoItem 的非空堆叠生效，其余堆叠原样保留。
     */
    public static int consumeAmmo(List<ItemStack> items, Item ammoItem, int count) {
        int remaining = count;
        for (ItemStack stack : items) {
            if (remaining <= 0) break;
            if (stack.isEmpty() || stack.getItem() != ammoItem) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
        }
        return count - remaining;
    }
}
