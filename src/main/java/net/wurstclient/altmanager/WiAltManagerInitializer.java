package net.wurstclient.altmanager;

import net.fabricmc.api.ModInitializer;

public class WiAltManagerInitializer implements ModInitializer
{
    private static boolean initialized;

    @Override
    public void onInitialize()
    {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        if (initialized)
            throw new RuntimeException(
                    "WiAltManagerInitializer.onInitialize() ran twice!");

        WiAltManager.INSTANCE.initialize();
        initialized = true;
    }
}
