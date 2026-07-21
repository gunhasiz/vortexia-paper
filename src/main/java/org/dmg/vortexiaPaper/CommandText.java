package org.dmg.vortexiaPaper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CommandText implements CommandExecutor {

    private static final long TICKS_PER_CHAR = 2L;

    private final JavaPlugin plugin;
    private final Map<UUID, Queue<String>> messageQueues = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> activeTypewriters = new ConcurrentHashMap<>();

    public CommandText(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can text other players.");
            return true;
        }

        if (player.getGameMode() != GameMode.SPECTATOR) {
            player.sendMessage(Component.text("You must be in Spectator mode to use this!").color(NamedTextColor.RED));
            return true;
        }

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

        queueMessage(target, message);

        sender.sendMessage(Component.text("Message sent to " + target.getName()).color(NamedTextColor.GREEN));
        return true;
    }

    private void queueMessage(Player target, String message) {
        var uuid = target.getUniqueId();
        var queue = messageQueues.computeIfAbsent(uuid, id -> new ConcurrentLinkedQueue<>());
        queue.add(message);
        
        if (activeTypewriters.putIfAbsent(uuid, Boolean.TRUE) == null) {
            processNextInQueue(target);
        }
    }

    private void processNextInQueue(Player target) {
        var uuid = target.getUniqueId();
        var queue = messageQueues.get(uuid);

        if (queue == null || queue.isEmpty()) {
            activeTypewriters.remove(uuid);
            return;
        }

        var message = queue.poll();
        runTypewriter(target, message, () -> processNextInQueue(target));
    }

    private void runTypewriter(Player target, String message, Runnable onFinished) {
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!target.isOnline()) {
                    messageQueues.remove(target.getUniqueId());
                    activeTypewriters.remove(target.getUniqueId());
                    cancel();
                    return;
                }

                if (index > message.length()) {
                    cancel();
                    onFinished.run();
                    return;
                }

                var visiblePart = message.substring(0, index);
                target.sendActionBar(Component.text(visiblePart).color(NamedTextColor.GOLD));

                index++;
            }
        }.runTaskTimer(plugin, 0L, TICKS_PER_CHAR);
    }
}