package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.events.player.ParkourPlayerTeleportEvent;
import me.block2block.hubparkour.api.plates.PressurePlate;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ALL")
public class FallListener implements Listener {

    private static List<Player> hasTeleported = new ArrayList<>();

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onFall(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {

            Player p = (Player) e.getEntity();
            if (CacheManager.isParkour(p)) {
                if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                    if (!ConfigUtil.getBoolean("Settings.Teleport.On-Fall.Enabled", true) || p.getFallDistance() < ConfigUtil.getDouble("Settings.Teleport.On-Fall.Minimum-Distance", 3.0)) {
                        if (ConfigUtil.getBoolean("Settings.Cancel-Fall-Damage", false)) {
                            e.setCancelled(true);
                        }
                        return;
                    }
                } else if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (!ConfigUtil.getBoolean("Settings.Teleport.On-Void", true)) {
                        return;
                    } else {
                        if (hasTeleported.contains(p)) {
                            return;
                        }
                    }
                } else {
                    if (ConfigUtil.getBoolean("Settings.Health.Disable-Damage", true)) {
                        e.setCancelled(true);
                    }
                    return;
                }

                e.setCancelled(true);
                p.setFallDistance(0);
                HubParkourPlayer player = CacheManager.getPlayer(p);
                Parkour parkour = player.getParkour();
                PressurePlate point = (player.getLastReached() != 0) ? parkour.getCheckpoint(player.getLastReached())
                        : parkour.getRestartPoint();
                ParkourPlayerTeleportEvent event = new ParkourPlayerTeleportEvent(parkour,
                        player, point, ParkourPlayerTeleportEvent.TeleportReason.FALL);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                }

                Location l = point.getLocation().clone();
                l.setX(l.getX() + 0.5);
                l.setY(l.getY() + 0.5);
                l.setZ(l.getZ() + 0.5);
                p.setVelocity(new Vector(0, 0, 0));
                p.teleport(l);
                ConfigUtil.sendMessage(p, "Messages.Parkour.Teleport", "You have been teleported to your last checkpoint.", true, Collections.emptyMap());
                if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    hasTeleported.add(p);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            hasTeleported.remove(p);
                        }
                    }.runTaskLater(HubParkour.getInstance(), 5);
                }
            }
        }
    }

    public static List<Player> getHasTeleported() {
        return hasTeleported;
    }
}
