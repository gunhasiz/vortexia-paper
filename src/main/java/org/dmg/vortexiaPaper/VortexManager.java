package org.dmg.vortexiaPaper;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VortexManager {
    private static final int ARMS = 5;
    private static final int POINTS_PER_ARM = 3;
    private static final double SPIRAL_TWIST = Math.PI * 1.5;
    private static final double FLOW_SPEED = 0.006;

    private final Map<UUID, VortexData> activeVortexes = new ConcurrentHashMap<>();
    private final JavaPlugin plugin;

    public VortexManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startAsyncEngine();
    }

    public void toggle(UUID playerId) {
        activeVortexes.compute(playerId, (id, data) -> {
            if (data == null) {
                return new VortexData(VortexState.FADING_IN, 0.0f);
            } else {
                data.state = VortexState.FADING_OUT;
                return data;
            }
        });
    }

    private void startAsyncEngine() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (var entry : activeVortexes.entrySet()) {
                    var player = Bukkit.getPlayer(entry.getKey());
                    var data = entry.getValue();

                    if (player == null || !player.isOnline()) {
                        activeVortexes.remove(entry.getKey());
                        continue;
                    }

                    if (data.state == VortexState.FADING_IN) {
                        data.intensity = Math.min(1.0f, data.intensity + 0.05f);
                        if (data.intensity >= 1.0f) {
                            data.state = VortexState.ACTIVE;
                        }
                    } else if (data.state == VortexState.FADING_OUT) {
                        data.intensity = Math.max(0.0f, data.intensity - 0.05f);
                        if (data.intensity <= 0.0f) {
                            activeVortexes.remove(entry.getKey());
                            continue;
                        }
                    }

                    data.theta += 0.02;
                    data.orbitTheta += 0.01;
                    data.flowPhase += FLOW_SPEED;

                    if (data.theta > Math.PI * 2) data.theta -= Math.PI * 2;
                    if (data.orbitTheta > Math.PI * 2) data.orbitTheta -= Math.PI * 2;
                    if (data.flowPhase > 1.0) data.flowPhase -= 1.0;

                    var center = player.getLocation().add(0, 1.0, 0);
                    var maxRadius = 1.5;
                    var currentRadius = maxRadius * data.intensity;

                    var cosOrbit = Math.cos(data.orbitTheta);
                    var sinOrbit = Math.sin(data.orbitTheta);

                    for (var arm = 0; arm < ARMS; arm++) {
                        var angleOffset = (Math.PI * 2 / ARMS) * arm;

                        for (var p = 1; p <= POINTS_PER_ARM; p++) {
                            var t = ((double) p / POINTS_PER_ARM + data.flowPhase) % 1.0;

                            var radius = currentRadius * t;
                            var angle = data.theta + angleOffset + (t * SPIRAL_TWIST);

                            var localX = radius * Math.cos(angle);
                            var localY = radius * Math.sin(angle);

                            var x = localX * cosOrbit;
                            var z = localX * sinOrbit;
                            var y = localY;

                            var particleLoc = center.clone().add(x, y, z);
                            var oppositeParticleLoc = center.clone().add(y, x, z);
                            var polarParticleLoc = center.clone().add(z, x, y);
                            player.getWorld().spawnParticle(Particle.SOUL, particleLoc, 1, 0, 0, 0, 0);
                            player.getWorld().spawnParticle(Particle.SOUL, oppositeParticleLoc, 1, 0, 0, 0, 0);
                            player.getWorld().spawnParticle(Particle.SOUL, polarParticleLoc, 1, 0, 0, 0, 0);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1L);
    }
}