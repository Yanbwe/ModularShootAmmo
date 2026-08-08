package org.yanbwe.modularshootammo.client;

import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.ammo.AmmoText;
import org.yanbwe.modularshootammo.registry.AmmoType;
import org.yanbwe.modularshootammo.registry.ModularAmmoRegistries;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 弹药物品 tooltip：追加一行归属弹药类型（设计文档系统七）。
 *
 * <p>遍历 {@code modularammo:ammo_types} 注册表，找 {@code item} 字段等于
 * 当前物品的弹药类型；命中则在 tooltip 末尾追加一行
 * {@code 弹药类型显示名}（以弹药类型 {@code color} 着色），未命中不追加。
 * 注册表条目量小（demo 仅 2 个），逐条遍历无性能问题。</p>
 *
 * <p>仿框架 TooltipBuilder 模式：注册在 NeoForge 游戏事件总线且
 * {@code value = Dist.CLIENT}，仅物理客户端加载；{@link ItemTooltipEvent}
 * 在无玩家上下文时（如主菜单搜索树填充）{@code getEntity()} 为
 * {@code null}，此时直接跳过。</p>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID, value = Dist.CLIENT)
public final class AmmoItemTooltip {
    private AmmoItemTooltip() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return; // 主菜单预览场景：无玩家上下文，不注入 tooltip
        }
        ItemStack stack = event.getItemStack();
        RegistryAccess ra = player.registryAccess();
        ra.registry(ModularAmmoRegistries.AMMO_TYPES_KEY)
                .flatMap(reg -> reg.stream()
                        .filter(type -> type.item().equals(BuiltInRegistries.ITEM.getKey(stack.getItem())))
                        .findFirst())
                .ifPresent(type -> appendTypeLine(event, type));
    }

    /** 追加归属弹药类型行：显示名以弹药类型颜色着色。 */
    private static void appendTypeLine(ItemTooltipEvent event, AmmoType type) {
        Component line = AmmoText.resolve(type.name()).withColor(type.color());
        event.getToolTip().add(line);
    }
}
