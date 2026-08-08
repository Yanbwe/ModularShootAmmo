package org.yanbwe.modularshootammo;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.yanbwe.modularshootammo.client.config.ModularAmmoClientConfig;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ModularShootAmmo.MODID, dist = Dist.CLIENT)
public class ModularShootAmmoClient {
    public ModularShootAmmoClient(ModContainer container) {
        // 配置界面：Options → Mods → ModularShootAmmo → Config（仿框架 ModularShootClient）
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        // 客户端配置：modularshootammo-client.toml（HUD 显示配置）
        container.registerConfig(ModConfig.Type.CLIENT, ModularAmmoClientConfig.SPEC);
    }
}
