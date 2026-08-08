package org.yanbwe.modularshootammo.creative;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.yanbwe.modularshootammo.ModularShootAmmo;
import org.yanbwe.modularshootammo.item.ModularAmmoItems;

/**
 * 模组创造模式标签页：收纳全部默认物品（弹药、demo 枪与插件）。
 */
public final class ModularAmmoCreativeTabs {

    /** 创造标签页注册表。 */
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModularShootAmmo.MODID);

    /** 模组主标签页，图标为步枪弹药。 */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register("ammo", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.modularshootammo"))
                    .icon(() -> new ItemStack(ModularAmmoItems.RIFLE_AMMO))
                    .displayItems((params, output) -> {
                        output.accept(ModularAmmoItems.RIFLE_AMMO.get());
                        output.accept(ModularAmmoItems.SHOTGUN_AMMO.get());
                        output.accept(ModularAmmoItems.DEMO_GUN.get());
                        output.accept(ModularAmmoItems.DEMO_EXTENDED_MAG.get());
                    })
                    .build());

    private ModularAmmoCreativeTabs() {
    }
}
