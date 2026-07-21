package org.dmg.vortexiaPaper;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class VortexiaPaper extends JavaPlugin {

    @Override
    public void onEnable() {
        VortexManager _vortexManager = new VortexManager(this);

        if (getCommand("vortexia") != null)
            Objects.requireNonNull(getCommand("vortexia")).setExecutor(new CommandVortex(_vortexManager));
        if (getCommand("text") != null) 
            Objects.requireNonNull(getCommand("text")).setExecutor(new CommandText(this));

        getLogger().info("Vortexia Paper MVP has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Vortexia Paper MVP disabled.");
    }
}
