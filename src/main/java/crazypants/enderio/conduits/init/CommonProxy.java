package crazypants.enderio.conduits.init;

import javax.annotation.Nonnull;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import crazypants.enderio.conduits.capability.CapabilityUpgradeHolder;

public class CommonProxy {

    public void init(@Nonnull FMLPreInitializationEvent event) {
        CapabilityUpgradeHolder.register();
    }

    public void init(@Nonnull FMLInitializationEvent event) {
        // CCUtil.init(event);
    }

    public void init(@Nonnull FMLPostInitializationEvent event) {}

    public float getPartialTicks() {
        return 1;
    }
}
