package org.dmg.vortexiaPaper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandVortex implements CommandExecutor {

    private final VortexManager manager;

    public CommandVortex(VortexManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can toggle the vortex.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            manager.toggle(player.getUniqueId());
            player.sendMessage(Component.text("Vortex state toggled!").color(NamedTextColor.AQUA));
            return true;
        }

        player.sendMessage(Component.text("Usage: /vortexia toggle").color(NamedTextColor.RED));
        return true;
    }
}
