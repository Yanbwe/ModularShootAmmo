package org.yanbwe.modularshootammo;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ModularShootAmmo.MODID, dist = Dist.CLIENT)
public class ModularShootAmmoClient {
    public ModularShootAmmoClient(ModContainer container) {
        // Client-side initialization goes here.
    }
}
