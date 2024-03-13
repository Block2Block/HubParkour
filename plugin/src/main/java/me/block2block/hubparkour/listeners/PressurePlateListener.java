package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.events.player.ParkourPlayerCheckpointEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerStartEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerTeleportEvent;
import me.block2block.hubparkour.api.plates.Checkpoint;
import me.block2block.hubparkour.api.plates.PressurePlate;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ALL")
public class PressurePlateListener implements Listener {

    final double STILL = -0.0784000015258789;

    @SuppressWarnings("unused")
    @EventHandler
    public void onPressurePlate(PlayerMoveEvent e) {
        if (e.getFrom().getBlock().getType().equals(e.getTo().getBlock().getType())) {
            if (CacheManager.isParkour(e.getPlayer())) {
                if (e.getPlayer().isOnGround()) {
                    Player p = e.getPlayer();
                    HubParkourPlayer player = CacheManager.getPlayer(p);
                    player.touchedGround();
                }
                if (e.getPlayer().getVelocity().getY() > STILL) {
                    Player p = e.getPlayer();
                    HubParkourPlayer player = CacheManager.getPlayer(p);
                    if (player.hasTouchedGround() && !player.getPlayer().isOnGround()) {
                        player.leftGround();
                        player.getParkourRun().jumped();
                    }
                }
                Location from = e.getFrom().clone();
                Location to = e.getTo().clone();
                from.setY(0);
                to.setY(0);
                double distance = Math.abs(from.distance(to));
                {
                    Player p = e.getPlayer();
                    HubParkourPlayer player = CacheManager.getPlayer(p);
                    player.getParkourRun().addTravel(distance);
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
                            }.runTaskLater(HubParkour.getInstance(), 5);
                            return;
                        }
                    }
                }

            }
            return;
        }
        if (CacheManager.isParkour(e.getPlayer())) {
            if (e.getTo().getBlock().isLiquid()) {
                boolean tpwater = ConfigUtil.getBoolean("Settings.Teleport.On-Water", true);
                boolean tplava = ConfigUtil.getBoolean("Settings.Teleport.On-Lava", true);
                ParkourPlayerTeleportEvent.TeleportReason reason =
                        ParkourPlayerTeleportEvent.TeleportReason.Unknow;
                if (e.getTo().getBlock().getType() == Material.WATER) {
                    reason = ParkourPlayerTeleportEvent.TeleportReason.Water;
                    if (!tpwater) {
                        return;
                    }
                } else if (e.getTo().getBlock().getType() == Material.LAVA) {
                    reason = ParkourPlayerTeleportEvent.TeleportReason.Lava;
                    if (!tplava) {
                        return;
                    }
                } else if (HubParkour.isPre1_13()) {
                    if (e.getTo().getBlock().getType() == Material.getMaterial("STATIONARY_WATER")) {
                        reason = ParkourPlayerTeleportEvent.TeleportReason.Water;
                        if (!tpwater) {
                            return;
                        }
                    } else {
                        reason = ParkourPlayerTeleportEvent.TeleportReason.Lava;
                        if (!tplava) {
                            return;
                        }
                    }
                }
                if (tpwater || tplava) {
                    Player p = e.getPlayer();
                    ParkourPlayerTeleportEvent event = new ParkourPlayerTeleportEvent(CacheManager.getPlayer(p).getParkour(), CacheManager.getPlayer(p), CacheManager.getPlayer(p).getParkour().getRestartPoint(), reason);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }

                    HubParkourPlayer player = CacheManager.getPlayer(p);
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
            } else if (!HubParkour.isPre1_13()) {
                if (e.getTo().getBlock().getBlockData() instanceof Waterlogged) {
                    Waterlogged waterlogged = (Waterlogged) e.getTo().getBlock().getBlockData();
                    if (waterlogged.isWaterlogged() && ConfigUtil.getBoolean("Settings.Teleport.On-Water", true)) {
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
            }
            {
                Player p = e.getPlayer();
                HubParkourPlayer player = CacheManager.getPlayer(p);
                Parkour parkour = player.getParkour();
                if (player.getParkour().getBorders().size() == 2) {
                    Location borderA = parkour.getBorders().get(0).getLocation(), borderB = parkour.getBorders().get(1).getLocation();


                    double highX = 0, lowX = 0, highY = 0, lowY = 0, highZ = 0, lowZ = 0;
                    if (borderA.getX() > borderB.getX()) {
                        highX = borderA.getX();
                        lowX = borderB.getX();
                    } else {
                        highX = borderB.getX();
                        lowX = borderA.getX();
                    }

                    if (borderA.getY() > borderB.getY()) {
                        highY = borderA.getY();
                        lowY = borderB.getY();
                    } else {
                        highY = borderB.getY();
                        lowY = borderA.getY();
                    }

                    if (borderA.getZ() > borderB.getZ()) {
                        highZ = borderA.getZ();
                        lowZ = borderB.getZ();
                    } else {
                        highZ = borderB.getZ();
                        lowZ = borderA.getZ();
                    }
                    if ((highX < p.getLocation().getX() || lowX > p.getLocation().getX()) || (highY < p.getLocation().getY() || lowY > p.getLocation().getY()) || (highZ < p.getLocation().getZ() || lowZ > p.getLocation().getZ())) {
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
                    }
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
                        }.runTaskLater(HubParkour.getInstance(), 5);
                        return;
                    }
                }
            }
        }
        if (CacheManager.isPoint(e.getTo().getBlock().getLocation())) {
            PressurePlate pp = CacheManager.getPoint(e.getTo().getBlock().getLocation());
            Player p = e.getPlayer();
            if (CacheManager.isSomeoneEdit()) {
                if (pp.getParkour().equals(CacheManager.getEditWizard().getParkour())) {
                    ConfigUtil.sendMessageOrDefault(e.getPlayer(), "Messages.Parkour.Currently-Being-Edited", "This parkour is currently in being modified by an admin. Please wait to attempt this parkour!", true, Collections.emptyMap());
                    return;
                }
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
                                HubParkourPlayer old = CacheManager.getPlayer(p);
                                old.end(ParkourPlayerFailEvent.FailCause.NEW_PARKOUR);

                                //Start the new parkour
                                Parkour parkour = (Parkour) pp.getParkour();
                                HubParkourPlayer player = new HubParkourPlayer(old, parkour);
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
                        player.startParkour();

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
