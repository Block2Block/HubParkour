package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.events.player.ParkourPlayerCheckpointEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerStartEvent;
import me.block2block.hubparkour.api.plates.Checkpoint;
import me.block2block.hubparkour.api.plates.PressurePlate;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class PressurePlateListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onPressurePlate(PlayerMoveEvent e) {
        if (e.getFrom().getBlock().getType().equals(e.getTo().getBlock().getType())) {
            if (CacheManager.isParkour(e.getPlayer())) {
                if (ConfigUtil.getBoolean("Settings.Incompatibility-Workarounds.VoidSpawn.Enabled", false)) {
                    if (ConfigUtil.getBoolean("Settings.Teleport.On-Void", true)) {
                        if (ConfigUtil.getInt("Settings.Incompatibility-Workarounds.VoidSpawn.Min-Y", -1) > e.getTo().getY()) {
                            Player p = e.getPlayer();
                            p.setFallDistance(0);
                            HubParkourPlayer player = CacheManager.getPlayer(p);

                            Location l = player.getParkour().getRestartPoint().getLocation().clone();
                            if (player.getLastReached() != 0) {
                                l = player.getParkour().getCheckpoint(player.getLastReached()).getLocation().clone();
                            }
                            l.setX(l.getX() + 0.5);
                            l.setY(l.getY() + 0.5);
                            l.setZ(l.getZ() + 0.5);
                            double health = p.getHealth();
                            p.setVelocity(new Vector(0, 0, 0));
                            p.teleport(l);
                            ConfigUtil.sendMessage(p, "Messages.Parkour.Teleport", "You have been teleported to your last checkpoint.", true, Collections.emptyMap());
                            FallListener.getHasTeleported().add(p);
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    FallListener.getHasTeleported().remove(p);
                                }
                            }.runTaskLater(Main.getInstance(), 5);
                            return;
                        }
                    }
                }
            }
            return;
        }
        if (CacheManager.isParkour(e.getPlayer())) {
            if (e.getTo().getBlock().isLiquid()) {
                if (e.getTo().getBlock().getType() == Material.WATER) {
                    if (!ConfigUtil.getBoolean("Settings.Teleport.On-Water", true)) {
                        return;
                    }
                } else if (e.getTo().getBlock().getType() == Material.LAVA) {
                    if (!ConfigUtil.getBoolean("Settings.Teleport.On-Lava", true)) {
                        return;
                    }
                } else if (Main.isPre1_13()) {
                    if (e.getTo().getBlock().getType() == Material.getMaterial("STATIONARY_WATER")) {
                        if (!ConfigUtil.getBoolean("Settings.Teleport.On-Water", true)) {
                            return;
                        }
                    } else {
                        if (!ConfigUtil.getBoolean("Settings.Teleport.On-Lava", true)) {
                            return;
                        }
                    }
                }
                if (ConfigUtil.getBoolean("Settings.Teleport.On-Water", true)) {
                    HubParkourPlayer player = CacheManager.getPlayer(e.getPlayer());

                    Location l = player.getParkour().getRestartPoint().getLocation().clone();
                    if (player.getLastReached() != 0) {
                        l = player.getParkour().getCheckpoint(player.getLastReached()).getLocation().clone();
                    }
                    l.setX(l.getX() + 0.5);
                    l.setY(l.getY() + 0.5);
                    l.setZ(l.getZ() + 0.5);
                    e.getPlayer().setFallDistance(0);
                    e.getPlayer().setVelocity(new Vector(0, 0, 0));
                    e.getPlayer().teleport(l);
                    ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Teleport", "You have been teleported to your last checkpoint.", true, Collections.emptyMap());
                    return;
                }
            }
            if (ConfigUtil.getBoolean("Settings.Incompatibility-Workarounds.VoidSpawn.Enabled", false)) {
                if (ConfigUtil.getBoolean("Settings.Teleport.On-Void", true)) {
                    if (ConfigUtil.getInt("Settings.Incompatibility-Workarounds.VoidSpawn.Min-Y", -1) > e.getTo().getY()) {
                        Player p = e.getPlayer();
                        p.setFallDistance(0);
                        HubParkourPlayer player = CacheManager.getPlayer(p);

                        Location l = player.getParkour().getRestartPoint().getLocation().clone();
                        if (player.getLastReached() != 0) {
                            l = player.getParkour().getCheckpoint(player.getLastReached()).getLocation().clone();
                        }
                        l.setX(l.getX() + 0.5);
                        l.setY(l.getY() + 0.5);
                        l.setZ(l.getZ() + 0.5);
                        double health = p.getHealth();
                        p.setVelocity(new Vector(0, 0, 0));
                        p.teleport(l);
                        ConfigUtil.sendMessage(p, "Messages.Parkour.Teleport", "You have been teleported to your last checkpoint.", true, Collections.emptyMap());
                        FallListener.getHasTeleported().add(p);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                FallListener.getHasTeleported().remove(p);
                            }
                        }.runTaskLater(Main.getInstance(), 5);
                        return;
                    }
                }
            }
        }
        if (CacheManager.getTypes().containsValue(e.getTo().getBlock().getType())) {
            if (CacheManager.isPoint(e.getTo().getBlock().getLocation())) {
                PressurePlate pp = CacheManager.getPoint(e.getTo().getBlock().getLocation());
                Player p = e.getPlayer();
                if (pp.getParkour().equals(CacheManager.alreadySetup())) {
                    ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Currently-Being-Edited", "This parkour is currently in being modified by an admin. Please wait to attempt this parkour!", true, Collections.emptyMap());
                    return;
                }
                switch (pp.getType()) {
                    case 0:
                        //StartPoint
                        if (CacheManager.isParkour(p)) {
                            if (CacheManager.getPlayer(p).getParkour().getId() == pp.getParkour().getId()) {
                                //Restart the parkour.

                                CacheManager.getPlayer(p).restart();
                                ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Restarted", "You have restarted the parkour! Your time has been reset to 0!", true, Collections.emptyMap());
                                return;
                            } else {
                                //Do nothing, is doing a different parkour.
                                if (ConfigUtil.getBoolean("Settings.Start-When-In-Parkour", false)) {
                                    CacheManager.getPlayer(p).end(ParkourPlayerFailEvent.FailCause.NEW_PARKOUR);

                                    //Start the new parkour
                                    Parkour parkour = (Parkour) pp.getParkour();
                                    HubParkourPlayer player = new HubParkourPlayer(p, parkour);
                                    ParkourPlayerStartEvent event = new ParkourPlayerStartEvent(parkour, player, player.getStartTime());
                                    Bukkit.getPluginManager().callEvent(event);
                                    if (event.isCancelled()) {
                                        return;
                                    }
                                    parkour.playerStart(player);
                                    CacheManager.playerStart(player);
                                    if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Remove-Potion-Effects", true)) {
                                        for (PotionEffect effect : p.getActivePotionEffects()) {
                                            p.removePotionEffect(effect.getType());
                                        }
                                    }
                                    if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Remove-Fly", true)) {
                                        p.setFlying(false);
                                        if (Material.getMaterial("ELYTRA") != null) {
                                            p.setGliding(false);
                                        }
                                    }
                                    player.giveItems();

                                    Map<String, String> bindings = new HashMap<>();
                                    bindings.put("parkour-name", parkour.getName());

                                    ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Started", "You have started the &a{parkour-name} &rparkour!", true, bindings);
                                    return;
                                } else {
                                    ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Already-In-Parkour", "You are already doing a parkour. If you wish to leave the current parkour and start a new one, do /parkour leave.", true, Collections.emptyMap());
                                    return;
                                }
                            }
                        } else {
                            //Start the parkour
                            Parkour parkour = (Parkour) pp.getParkour();
                            HubParkourPlayer player = new HubParkourPlayer(p, parkour);
                            ParkourPlayerStartEvent event = new ParkourPlayerStartEvent(parkour, player, player.getStartTime());
                            Bukkit.getPluginManager().callEvent(event);
                            if (event.isCancelled()) {
                                return;
                            }
                            parkour.playerStart(player);
                            CacheManager.playerStart(player);
                            if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Remove-Potion-Effects", true)) {
                                for (PotionEffect effect : p.getActivePotionEffects()) {
                                    p.removePotionEffect(effect.getType());
                                }
                            }
                            if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Remove-Fly", true)) {
                                p.setFlying(false);
                                if (Material.getMaterial("ELYTRA") != null) {
                                    p.setGliding(false);
                                }
                            }
                            player.giveItems();

                            Map<String, String> bindings = new HashMap<>();
                            bindings.put("parkour-name", parkour.getName());

                            ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Started", "You have started the &a{parkour-name} &rparkour!", true, bindings);


                        }
                        break;
                    case 1:
                        //EndPoint
                        if (CacheManager.isParkour(p)) {
                            if (CacheManager.getPlayer(p).getParkour().getId() == pp.getParkour().getId()) {
                                //End the parkour.
                                CacheManager.getPlayer(p).end(null);
                                return;
                            } else {
                                //Do nothing, is doing a different parkour.
                                ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Already-In-Parkour", "You are already doing a parkour. If you wish to leave the current parkour and start a new one, do /parkour leave.", true, Collections.emptyMap());
                                return;
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.End.Not-Started", "You must start a parkour in order to finish it.", true, Collections.emptyMap());
                        }
                        break;
                    case 2:
                        return;
                    case 3:
                        //Checkpoint
                        if (CacheManager.isParkour(p)) {
                            if (CacheManager.getPlayer(p).getParkour().getId() == pp.getParkour().getId()) {
                                //Checkpoint the parkour.
                                Checkpoint checkpoint = (Checkpoint) pp;
                                ParkourPlayerCheckpointEvent event = new ParkourPlayerCheckpointEvent(pp.getParkour(), CacheManager.getPlayer(p), checkpoint);
                                Bukkit.getPluginManager().callEvent(event);
                                if (event.isCancelled()) {
                                    return;
                                }

                                CacheManager.getPlayer(p).checkpoint(checkpoint);
                                return;
                            } else {
                                //Do nothing, is doing a different parkour.
                                ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Already-In-Parkour", "You are already doing a parkour. If you wish to leave the current parkour and start a new one, do /parkour leave.", true, Collections.emptyMap());
                                return;
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Checkpoints.Not-Started", "You must start a parkour in order to reach checkpoints!", true, Collections.emptyMap());
                        }
                        break;
                }
            }
        }
    }

}
