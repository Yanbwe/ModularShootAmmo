package org.yanbwe.modularshootammo.sound;

import org.yanbwe.modularshootammo.ModularShootAmmo;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 换弹音效事件注册（sounds.json 资源文件由任务 7 补写；
 * 运行期缺音频资源仅静音，不报错）。
 */
public final class ModularAmmoSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ModularShootAmmo.MODID);

    /** 开始换弹音效（默认，弹药类型 reload_sound 可覆盖） */
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_START =
            SOUNDS.register("reload_start", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "reload_start")));

    /** 换弹完成音效（默认，弹药类型 reload_sound 可覆盖） */
    public static final DeferredHolder<SoundEvent, SoundEvent> RELOAD_FINISH =
            SOUNDS.register("reload_finish", () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ModularShootAmmo.MODID, "reload_finish")));

    private ModularAmmoSounds() {}
}
