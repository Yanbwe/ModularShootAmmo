package org.yanbwe.modularshootammo.client;

import java.util.Optional;

import org.yanbwe.modularshootammo.attribute.ModularAmmoAttributes;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * 客户端属性最终值计算（纯组件读取，不依赖实体/注册表）。
 *
 * <p>从枪械物品的 {@code ATTRIBUTE_MODIFIERS} 组件中按属性 id 收集修饰符，
 * 采用<strong>原版</strong> {@code AttributeInstance.getValue()} 叠加公式
 * （设计文档系统三/八明确要求，与框架 tooltip 的求和单因子近似不同）：</p>
 *
 * <pre>{@code
 * final = (Σ ADD_VALUE) × (1 + Σ ADD_MULTIPLIED_BASE) × Π(1 + ADD_MULTIPLIED_TOTAL_i)
 * }</pre>
 *
 * <p>注意 {@code ADD_MULTIPLIED_TOTAL} 是<strong>逐项相乘</strong>
 * （原版多修饰符相乘），而非框架 {@code AttributeTooltipBuilder} 的求和单因子。</p>
 */
public final class ClientAttributeValues {

    private ClientAttributeValues() {}

    /**
     * 按原版公式计算枪械上某属性的最终值。
     *
     * @param gun          枪械物品（含 GUN_DATA 与 ATTRIBUTE_MODIFIERS 组件）
     * @param attributeId  属性 id（如 {@code modularshootammo:mag_size}）
     * @return 最终值；无修饰符/无组件时为 {@code 0.0}
     */
    public static double computeFinalValue(ItemStack gun, ResourceLocation attributeId) {
        ItemAttributeModifiers modifiers = gun.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return 0.0;
        }
        double addSum = 0.0;
        double mulBaseSum = 0.0;
        double mulTotalProduct = 1.0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            Optional<ResourceLocation> id = entry.attribute().unwrapKey().map(ResourceKey::location);
            if (id.isEmpty() || !id.get().equals(attributeId)) {
                continue;
            }
            switch (entry.modifier().operation()) {
                case ADD_VALUE -> addSum += entry.modifier().amount();
                case ADD_MULTIPLIED_BASE -> mulBaseSum += entry.modifier().amount();
                case ADD_MULTIPLIED_TOTAL -> mulTotalProduct *= 1.0 + entry.modifier().amount();
            }
        }
        return addSum * (1.0 + mulBaseSum) * mulTotalProduct;
    }

    /** 弹匣容量最终值（round 取整，兜底 ≥ 1）。 */
    public static int magSize(ItemStack gun) {
        return Math.max(1, (int) Math.round(computeFinalValue(gun, ModularAmmoAttributes.MAG_SIZE_ID)));
    }

    /** 换弹时间最终值（tick，round 取整，兜底 ≥ 1）。 */
    public static int reloadTime(ItemStack gun) {
        return Math.max(1, (int) Math.round(computeFinalValue(gun, ModularAmmoAttributes.RELOAD_TIME_ID)));
    }
}
