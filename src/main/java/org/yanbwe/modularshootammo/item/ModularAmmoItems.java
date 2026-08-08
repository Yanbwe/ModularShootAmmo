package org.yanbwe.modularshootammo.item;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.yanbwe.modularshootammo.ModularShootAmmo;

/**
 * 默认弹药物品与 demo 插件物品注册。
 *
 * <p>弹药物品可堆叠且耐火。demo 枪不注册专属物品——所有枪械统一使用
 * 框架的通用枪物品（{@code modularshoot:gun}），枪型由栈上的
 * {@code gun_data} 组件决定（见 {@code ModularAmmoCreativeTabs}）。
 * demo 扩容弹匣插件物品可堆叠 64。</p>
 */
public final class ModularAmmoItems {

    /** 物品注册表（绑定到 vanilla ITEM registry）。 */
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ModularShootAmmo.MODID);

    /** 手枪弹药（弹药类型 {@code modularshootammo:pistol_ammo} 的消耗品）。 */
    public static final DeferredHolder<Item, Item> PISTOL_AMMO =
            ITEMS.registerSimpleItem("pistol_ammo", new Item.Properties().stacksTo(64).fireResistant());

    /** 步枪弹药（弹药类型 {@code modularshootammo:rifle_ammo} 的消耗品）。 */
    public static final DeferredHolder<Item, Item> RIFLE_AMMO =
            ITEMS.registerSimpleItem("rifle_ammo", new Item.Properties().stacksTo(64).fireResistant());

    /** 霰弹枪弹药（弹药类型 {@code modularshootammo:shotgun_ammo} 的消耗品）。 */
    public static final DeferredHolder<Item, Item> SHOTGUN_AMMO =
            ITEMS.registerSimpleItem("shotgun_ammo", new Item.Properties().stacksTo(64).fireResistant());

    /** 狙击弹药（弹药类型 {@code modularshootammo:sniper_ammo} 的消耗品）。 */
    public static final DeferredHolder<Item, Item> SNIPER_AMMO =
            ITEMS.registerSimpleItem("sniper_ammo", new Item.Properties().stacksTo(64).fireResistant());

    /** demo 扩容弹匣插件物品（绑定到 {@code modularshootammo:demo_extended_mag} 插件）。 */
    public static final DeferredHolder<Item, Item> DEMO_EXTENDED_MAG =
            ITEMS.registerSimpleItem("demo_extended_mag", new Item.Properties().stacksTo(64));

    private ModularAmmoItems() {
    }
}
