package org.yanbwe.modularshootammo.registry;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Codec round-trip tests for {@link GunAmmoBinding#CODEC}.
 */
class GunAmmoBindingCodecTest {

    private static GunAmmoBinding decode(JsonObject json) {
        return GunAmmoBinding.CODEC.decode(JsonOps.INSTANCE, json)
                .getOrThrow(m -> new RuntimeException(m))
                .getFirst();
    }

    private static JsonObject encode(GunAmmoBinding binding) {
        return GunAmmoBinding.CODEC.encodeStart(JsonOps.INSTANCE, binding)
                .getOrThrow(m -> new RuntimeException(m))
                .getAsJsonObject();
    }

    @Test
    void validJsonRoundTrips() {
        JsonObject json = new JsonObject();
        json.addProperty("ammo_type", "modularshootammo:rifle_ammo");

        GunAmmoBinding decoded = decode(json);
        assertEquals(
                ResourceLocation.fromNamespaceAndPath("modularshootammo", "rifle_ammo"),
                decoded.ammoType());
        assertEquals(json, encode(decoded));
    }

    @Test
    void missingAmmoTypeFails() {
        JsonObject json = new JsonObject();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> decode(json));
        assertTrue(ex.getMessage().contains("ammo_type"));
    }
}
