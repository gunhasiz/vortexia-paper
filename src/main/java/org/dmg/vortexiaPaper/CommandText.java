package org.dmg.vortexiaPaper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

public class CommandText implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /text <player_name> <message...>").color(NamedTextColor.RED));
            return true;
        }
        
        var target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
            return true;
        }
        
        var message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        Component actionBarText = Component.text(message).color(NamedTextColor.GOLD);
        target.sendActionBar(actionBarText);

        sender.sendMessage(Component.text("Message sent to " + target.getName()).color(NamedTextColor.GREEN));
        return true;
    }
}