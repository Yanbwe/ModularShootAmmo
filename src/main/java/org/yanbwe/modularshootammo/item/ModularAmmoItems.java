package org.yanbwe.modularshootammo.item;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.yanbwe.modularshootammo.ModularShootAmmo;

/**
 * 默认弹药物品与 demo 物品注册。
 *
 * <p>弹药物品可堆叠且耐火；demo 枪不可堆叠（枪械物品惯例），
 * demo 扩容弹匣插件物品可堆叠 64。
 */
public final class ModularAmmoItems {

    /** 物品注册表（绑定到 vanilla ITEM registry）。 */
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ModularShootAmmo.MODID);

    /** 步枪弹药（弹药类型 {@code modularshootammo:rifle_ammo} 的消耗品）。 */
    public static final DeferredHolder<Item, Item> RIFLE_AMMO =
            ITEMS.registerSimpleItem("rifle_ammo", new Item.Properties().stacksTo(64).fireResistant());

    /** 霰弹枪弹药（弹药类型 {@code modularshootammo:shotgun_ammo} 的消耗品）。 */
    public static final DeferredHolder<Item, Item> SHOTGUN_AMMO =
            ITEMS.registerSimpleItem("shotgun_ammo", new Item.Properties().stacksTo(64).fireResistant());

    /** demo 枪物品（绑定到 {@code modularshootammo:demo_rifle} 枪械，不可堆叠）。 */
    public static final DeferredHolder<Item, Item> DEMO_GUN =
            ITEMS.registerSimpleItem("demo_gun", new Item.Properties().stacksTo(1));

    /** demo 扩容弹匣插件物品（绑定到 {@code modularshootammo:demo_extended_mag} 插件）。 */
    public static final DeferredHolder<Item, Item> DEMO_EXTENDED_MAG =
            ITEMS.registerSimpleItem("demo_extended_mag", new Item.Properties().stacksTo(64));

    private ModularAmmoItems() {
    }
}
