package org.yanbwe.modularshootammo;

import org.yanbwe.modularshootammo.attribute.ModularAmmoAttributes;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ModularShootAmmo.MODID)
public class ModularShootAmmo {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "modularshootammo";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ModularShootAmmo(IEventBus modEventBus) {
        // Register DeferredRegisters to the mod event bus here as features are added.
        ModularAmmoAttributes.ATTRIBUTES.register(modEventBus);
    }
}
