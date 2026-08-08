package org.yanbwe.modularshootammo.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.yanbwe.modularshoot.ModularShootAPI;
import org.yanbwe.modularshoot.component.ModularShootDataComponents;
import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.ammo.AmmoText;
import org.yanbwe.modularshootammo.registry.AmmoType;
import org.yanbwe.modularshootammo.registry.AmmoTypeRegistry;
import org.yanbwe.modularshootammo.registry.GunAmmoBindingRegistry;
import org.yanbwe.modularshootammo.server.AmmoService;

import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 枪械 tooltip 弹药信息行（客户端，设计文档 2026-08-08-gun-ammo-tooltip）。
 *
 * <p>在属性栏之后显示「弹药: <类型名>」（类型色）+「每发消耗: N」（仅消耗 > 1）；
 * 未绑定显示灰色「弹药: 未绑定」；无限弹药/未启用弹药系统的枪不显示。
 * 以 {@code EventPriority.LOWEST} 订阅，保证框架 TooltipBuilder（NORMAL）
 * 先注入四栏，再按框架标题翻译键锚点扫描插入。</p>
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID, value = Dist.CLIENT)
public final class GunAmmoTooltip {

    private static final String KEY_ATTRIBUTE_HEADER = "modularshoot.tooltip.attribute_header";
    private static final String KEY_TRAIT_HEADER = "modularshoot.tooltip.trait_header";
    private static final String KEY_STATE_HEADER = "modularshoot.tooltip.state_header";
    private static final String KEY_PLUGIN_HEADER = "modularshoot.tooltip.plugin_header";
    private static final String HINT_PREFIX = "modularshoot.tooltip.hint_";

    private GunAmmoTooltip() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        if (player == null) {
            return; // 主菜单搜索树预览：无玩家上下文，不注入
        }
        ItemStack stack = event.getItemStack();
        RegistryAccess ra = player.registryAccess();
        if (!ModularShootAPI.isGun(stack, ra)) {
            return;
        }
        if (!stack.has(ModularShootDataComponents.GUN_DATA.get())) {
            return; // 未转化绑定通道枪：框架只显示定义预览，无属性栏可贴
        }
        if (!AmmoService.isUsesAmmo(stack, ra)) {
            return; // 未启用弹药系统
        }
        if (AmmoService.isInfinite(stack, ra)) {
            return; // 无限弹药：不显示
        }
        List<Component> lines = buildAmmoLines(stack, ra);
        if (lines.isEmpty()) {
            return;
        }
        insertAfterAttributeBar(event.getToolTip(), lines);
    }

    /** 组装弹药行：绑定命中 → 类型名（类型色）+ 消耗行；绑定缺失 → 灰色「未绑定」。 */
    private static List<Component> buildAmmoLines(ItemStack gun, RegistryAccess ra) {
        Optional<ResourceLocation> typeIdOpt = ModularShootAPI.resolveGunId(gun, ra)
                .flatMap(gid -> GunAmmoBindingRegistry.get(ra, gid));
        Optional<AmmoType> typeOpt = typeIdOpt.flatMap(tid -> AmmoTypeRegistry.get(ra, tid));
        List<Component> lines = new ArrayList<>(2);
        if (typeOpt.isEmpty()) {
            lines.add(buildAmmoLine(Component.translatable("modularshootammo.tooltip.unbound")
                    .withStyle(ChatFormatting.GRAY)));
            return lines;
        }
        AmmoType type = typeOpt.get();
        lines.add(buildAmmoLine(AmmoText.resolve(type.name()).withColor(type.color())));
        if (type.perShotCost() > 1) {
            lines.add(buildPerShotLine(type.perShotCost()));
        }
        return lines;
    }

    /** 行格式与属性栏一致：「  <弹药>: <值>」，「弹药」灰色。 */
    private static MutableComponent buildAmmoLine(Component ammoName) {
        return Component.empty()
                .append(Component.literal("  "))
                .append(Component.translatable("modularshootammo.tooltip.ammo").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(ammoName);
    }

    /** 消耗行：「  <每发消耗>: <N>」，全部灰色。 */
    private static MutableComponent buildPerShotLine(int cost) {
        return Component.empty()
                .append(Component.literal("  "))
                .append(Component.translatable("modularshootammo.tooltip.per_shot").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(cost)).withStyle(ChatFormatting.GRAY));
    }

    /** 插入定位：属性栏标题后扫描到下一栏标题/hint 行插入；无属性栏兜底 hint 行前/末尾。 */
    private static void insertAfterAttributeBar(List<Component> toolTip, List<Component> lines) {
        int headerIdx = findTranslationIndex(toolTip, KEY_ATTRIBUTE_HEADER);
        if (headerIdx < 0) {
            int hintIdx = findFirstHintIndex(toolTip);
            toolTip.addAll(hintIdx >= 0 ? hintIdx : toolTip.size(), lines);
            return;
        }
        int insertAt = headerIdx + 1;
        while (insertAt < toolTip.size() && !isSectionBoundary(toolTip.get(insertAt))) {
            insertAt++;
        }
        toolTip.addAll(insertAt, lines);
    }

    /** 是否后续栏标题（特性/状态/插件）或修饰键 hint 行。 */
    private static boolean isSectionBoundary(Component c) {
        if (c.getContents() instanceof TranslatableContents tc) {
            String key = tc.getKey();
            return key.equals(KEY_TRAIT_HEADER) || key.equals(KEY_STATE_HEADER)
                    || key.equals(KEY_PLUGIN_HEADER) || key.startsWith(HINT_PREFIX);
        }
        return false;
    }

    private static int findTranslationIndex(List<Component> toolTip, String key) {
        for (int i = 0; i < toolTip.size(); i++) {
            if (toolTip.get(i).getContents() instanceof TranslatableContents tc && tc.getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    private static int findFirstHintIndex(List<Component> toolTip) {
        for (int i = 0; i < toolTip.size(); i++) {
            if (toolTip.get(i).getContents() instanceof TranslatableContents tc
                    && tc.getKey().startsWith(HINT_PREFIX)) {
                return i;
            }
        }
        return -1;
    }
}
