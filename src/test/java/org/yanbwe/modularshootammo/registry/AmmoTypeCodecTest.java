package org.yanbwe.modularshootammo.registry;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.ResourceLocation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Codec round-trip tests for {@link AmmoType#CODEC} (JsonOps 直接往返，仿框架
 * TraitCodecTest 风格). 覆盖：全字段往返、必填字段默认值、color 十六进制解析
 * （含非法回退）、缺必填字段报错。
 */
class AmmoTypeCodecTest {

    private static AmmoType decode(JsonObject json) {
        return AmmoType.CODEC.decode(JsonOps.INSTANCE, json)
                .getOrThrow(m -> new RuntimeException(m))
                .getFirst();
    }

    private static JsonObject encode(AmmoType ammoType) {
        return AmmoType.CODEC.encodeStart(JsonOps.INSTANCE, ammoType)
                .getOrThrow(m -> new RuntimeException(m))
                .getAsJsonObject();
    }

    private static JsonObject fullJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "Rifle Ammo");
        json.addProperty("color", "#FFAA00");
        json.addProperty("item", "modularshootammo:rifle_ammo");
        json.addProperty("reserve_limit", 128);
        json.addProperty("per_shot_cost", 2);
        json.addProperty("reload_sound", "modularshootammo:reload_start");
        return json;
    }

    @Test
    void fullFieldJsonDecodesToAllFields() {
        AmmoType decoded = decode(fullJson());
        assertEquals("Rifle Ammo", decoded.name());
        assertEquals(0xFFFFAA00, decoded.color());
        assertEquals(
                ResourceLocation.fromNamespaceAndPath("modularshootammo", "rifle_ammo"),
                decoded.item());
        assertEquals(128, decoded.reserveLimit());
        assertEquals(2, decoded.perShotCost());
        assertEquals(
                ResourceLocation.fromNamespaceAndPath("modularshootammo", "reload_start"),
                decoded.reloadSound());
    }

    @Test
    void fullFieldJsonRoundTrips() {
        AmmoType decoded = decode(fullJson());
        // 编码后各键值应与原始 JSON 完全一致（含 color 回写为 "#RRGGBB"）
        assertEquals(fullJson(), encode(decoded));
        // 二次解码字段相等（编码-解码稳定）
        AmmoType redecoded = decode(encode(decoded));
        assertEquals(decoded, redecoded);
    }

    @Test
    void minimalJsonAppliesDefaults() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "Shotgun Ammo");
        json.addProperty("color", "#AAFF00");
        json.addProperty("item", "modularshootammo:shotgun_ammo");

        AmmoType decoded = decode(json);
        assertEquals(1, decoded.perShotCost());
        assertNull(decoded.reserveLimit());
        assertNull(decoded.reloadSound());
    }

    @Test
    void colorParsesToArgb() {
        // "#RRGGBB" → ARGB（alpha 0xFF）
        assertEquals(0xFFFFAA00, AmmoType.parseHexColor("#FFAA00"));
        // 无 "#" 前缀同样支持
        assertEquals(0xFFFFAA00, AmmoType.parseHexColor("FFAA00"));
        // 非法 hex 回退白色 ARGB
        assertEquals(0xFFFFFFFF, AmmoType.parseHexColor("not-a-color"));
        assertEquals(0xFFFFFFFF, AmmoType.parseHexColor(""));
        // 数据包 color 字段经 codec 解析同样产出 ARGB
        JsonObject json = fullJson();
        json.addProperty("color", "#112233");
        assertEquals(0xFF112233, decode(json).color());
    }

    @Test
    void missingNameFails() {
        JsonObject json = new JsonObject();
        json.addProperty("color", "#FFAA00");
        json.addProperty("item", "modularshootammo:rifle_ammo");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> decode(json));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void missingItemFails() {
        JsonObject json = new JsonObject();
        json.addProperty("name", "Rifle Ammo");
        json.addProperty("color", "#FFAA00");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> decode(json));
        assertTrue(ex.getMessage().contains("item"));
    }
}
