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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;

@SuppressWarnings("ALL")
public class PressurePlateListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onPressurePlate(PlayerMoveEvent e) {
        if (e.getFrom().getBlock().getType().equals(e.getTo().getBlock().getType())) {
            return;
        }
        if (CacheManager.isParkour(e.getPlayer())) {
            if (e.getTo().getBlock().isLiquid()) {
                if (e.getTo().getBlock().getType() == Material.WATER) {
                    if (!Main.getInstance().getConfig().getBoolean("Settings.Teleport.On-Water")) {
                        return;
                    }
                } else if (e.getTo().getBlock().getType() == Material.LAVA) {
                    if (!Main.getInstance().getConfig().getBoolean("Settings.Teleport.On-Lava")) {
                        return;
                    }
                } else if (Main.isPre1_13()) {
                    if (e.getTo().getBlock().getType() == Material.getMaterial("STATIONARY_WATER")) {
                        if (!Main.getInstance().getConfig().getBoolean("Settings.Teleport.On-Water")) {
                            return;
                        }
                    } else {
                        if (!Main.getInstance().getConfig().getBoolean("Settings.Teleport.On-Lava")) {
                            return;
                        }
                    }
                }
                if (Main.getInstance().getConfig().getBoolean("Settings.Teleport.On-Water")) {
                    HubParkourPlayer player = CacheManager.getPlayer(e.getPlayer());

                    Location l = player.getParkour().getRestartPoint().getLocation().clone();
                    if (player.getLastReached() != 0) {
                        l = player.getParkour().getCheckpoint(player.getLastReached()).getLocation().clone();
                    }
                    l.setX(l.getX() + 0.5);
                    l.setY(l.getY() + 0.5);
                    l.setZ(l.getZ() + 0.5);
                    e.getPlayer().teleport(l);
                    e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Teleport")));
                    return;
                }
            }
        }
        if (CacheManager.getTypes().containsValue(e.getTo().getBlock().getType())) {
            if (CacheManager.isPoint(e.getTo().getBlock().getLocation())) {
                PressurePlate pp = CacheManager.getPoint(e.getTo().getBlock().getLocation());
                Player p = e.getPlayer();
                if (pp.getParkour().equals(CacheManager.getEditParkour())) {
                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Currently-Being-Edited")));
                    return;
                }
                switch (pp.getType()) {
                    case 0:
                        //StartPoint
                        if (CacheManager.isParkour(p)) {
                            if (CacheManager.getPlayer(p).getParkour().getId() == pp.getParkour().getId()) {
                                //Restart the parkour.

                                CacheManager.getPlayer(p).restart();
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Restarted")));
                                return;
                            } else {
                                //Do nothing, is doing a different parkour.
                                if (Main.getInstance().getConfig().getBoolean("Settings.Start-When-In-Parkour")) {
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
                                    if (Main.getInstance().getConfig().getBoolean("Settings.Remove-Potion-Effects")) {
                                        for (PotionEffect effect : p.getActivePotionEffects()) {
                                            p.removePotionEffect(effect.getType());
                                        }
                                    }
                                    if (Main.getInstance().getConfig().getBoolean("Settings.Remove-Fly")) {
                                        p.setFlying(false);
                                        if (Material.getMaterial("ELYTRA") != null) {
                                            p.setGliding(false);
                                        }
                                    }
                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Started").replace("{parkour-name}",parkour.getName())));
                                    return;
                                } else {
                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Already-In-Parkour")));
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
                            if (Main.getInstance().getConfig().getBoolean("Settings.Remove-Potion-Effects")) {
                                for (PotionEffect effect : p.getActivePotionEffects()) {
                                    p.removePotionEffect(effect.getType());
                                }
                            }
                            if (Main.getInstance().getConfig().getBoolean("Settings.Remove-Fly")) {
                                p.setFlying(false);
                                if (Material.getMaterial("ELYTRA") != null) {
                                    p.setGliding(false);
                                }
                            }
                            player.giveItems();
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Started").replace("{parkour-name}",parkour.getName())));


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
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Already-In-Parkour")));
                                return;
                            }
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Not-Started")));
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
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Checkpoints.Reached").replace("{checkpoint}","" + checkpoint.getCheckpointNo())));
                                return;
                            } else {
                                //Do nothing, is doing a different parkour.
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Already-In-Parkour")));
                                return;
                            }
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Checkpoints.Not-Started")));
                        }
                        break;
                }
            }
        }
    }

}
