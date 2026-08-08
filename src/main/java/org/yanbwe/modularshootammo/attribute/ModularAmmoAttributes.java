package org.yanbwe.modularshootammo.attribute;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.yanbwe.modularshootammo.ModularShootAmmo;

/**
 * Ammo-related attributes registered to the vanilla {@code ATTRIBUTE} registry.
 *
 * <p>Both attributes use a vanilla base of {@code 0}: a player holding no gun has zero ammo
 * stats, and gun values are applied through attribute modifiers rather than the vanilla base.
 * The hot-reloadable default values live in the {@code attribute_meta} metadata table (task 4)
 * and are distinct from this vanilla base.
 *
 * <p>Every attribute is {@code syncable} so the client receives final values for tooltip display.
 */
public final class ModularAmmoAttributes {

    /** Resource location of the magazine size attribute. */
    public static final ResourceLocation MAG_SIZE_ID =
            ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "mag_size");
    /** Resource location of the reload time attribute. */
    public static final ResourceLocation RELOAD_TIME_ID =
            ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "reload_time");

    /** Deferred register bound to the vanilla {@code ATTRIBUTE} registry under the mod namespace. */
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, ModularShootAmmo.MODID);

    // --- Attributes (base = 0, syncable) ---

    /** Magazine size in rounds; consumed per shot, refilled on reload. */
    public static final DeferredHolder<Attribute, Attribute> MAG_SIZE =
            register("mag_size", 0.0, 0.0, 100000.0);
    /** Reload time in ticks; elapsed while the gun is being reloaded. */
    public static final DeferredHolder<Attribute, Attribute> RELOAD_TIME =
            register("reload_time", 0.0, 0.0, 100000.0);

    private ModularAmmoAttributes() {
    }

    /**
     * Registers a ranged ammo attribute with the given vanilla base, clamped to {@code [min, max]}
     * and synced to clients.
     */
    private static DeferredHolder<Attribute, Attribute> register(String name, double base, double min, double max) {
        return ATTRIBUTES.register(name, () -> new RangedAttribute(
                "attribute.name." + ModularShootAmmo.MODID + "." + name, base, min, max).setSyncable(true));
    }
}
