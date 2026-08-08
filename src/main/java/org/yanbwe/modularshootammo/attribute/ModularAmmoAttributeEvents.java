package org.yanbwe.modularshootammo.attribute;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

import org.yanbwe.modularshootammo.ModularShootAmmo;

/**
 * Mounts ammo attributes onto the player entity so that
 * {@code player.getAttributeValue(ModularAmmoAttributes.MAG_SIZE)} succeeds.
 *
 * <p>Without this event handler the ammo attributes — although registered in the vanilla
 * {@code ATTRIBUTE} registry via {@code DeferredRegister} — are never added to the player's
 * {@code AttributeMap}. Calling {@code getAttributeValue()} on a player whose map does not
 * contain the attribute throws {@code IllegalArgumentException("Can't find attribute …")}
 * and crashes the shooting pipeline.
 *
 * <p>{@code EntityAttributeModificationEvent} is an {@code IModBusEvent}, hence the
 * {@code Bus.MOD} subscriber.
 */
@EventBusSubscriber(modid = ModularShootAmmo.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class ModularAmmoAttributeEvents {

    private ModularAmmoAttributeEvents() {
    }

    @SubscribeEvent
    public static void onAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ModularAmmoAttributes.MAG_SIZE);
        event.add(EntityType.PLAYER, ModularAmmoAttributes.RELOAD_TIME);
    }
}
