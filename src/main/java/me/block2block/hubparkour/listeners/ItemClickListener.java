package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerLeaveEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerTeleportEvent;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemClickListener implements Listener {

    private final List<Player> cancelNextEvent = new ArrayList<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (CacheManager.isParkour(e.getPlayer())) {
            if (cancelNextEvent.contains(e.getPlayer())) {
                cancelNextEvent.remove(e.getPlayer());
                return;
            }
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                cancelNextEvent.add(e.getPlayer());
            }
            if (e.getItem() != null) {
                for (int type : CacheManager.getItems().keySet()) {
                    if (CacheManager.getItems().get(type).equals(e.getItem())) {
                        e.setCancelled(true);
                        Player p = e.getPlayer();
                        HubParkourPlayer player = CacheManager.getPlayer(p);
                        switch (type) {
                            case 0:
                                //Reset.
                                if (FallListener.getHasTeleported().contains(p)) {
                                    return;
                                }
                                ParkourPlayerTeleportEvent event = new ParkourPlayerTeleportEvent(CacheManager.getPlayer(p).getParkour(), CacheManager.getPlayer(p), CacheManager.getPlayer(p).getParkour().getRestartPoint());
                                Bukkit.getPluginManager().callEvent(event);
                                if (event.isCancelled()) {
                                    return;
                                }
                                p.setFallDistance(0);
                                Location l = CacheManager.getPlayer(p).getParkour().getRestartPoint().getLocation().clone();
                                l.setX(l.getX() + 0.5);
                                l.setY(l.getY() + 0.5);
                                l.setZ(l.getZ() + 0.5);
                                p.setVelocity(new Vector(0, 0, 0));
                                p.teleport(l);
                                ConfigUtil.sendMessage(p, "Messages.Commands.Reset.Successful", "You have been teleported to the start.", true, Collections.emptyMap());
                                FallListener.getHasTeleported().add(p);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        FallListener.getHasTeleported().remove(p);
                                    }
                                }.runTaskLater(Main.getInstance(), 5);
                                return;
                            case 1:
                                //Checkpoint.
                                if (FallListener.getHasTeleported().contains(p)) {
                                    return;
                                }
                                ParkourPlayerTeleportEvent event2 = new ParkourPlayerTeleportEvent(player.getParkour(), player, (player.getLastReached() != 0)?player.getParkour().getCheckpoint(player.getLastReached()):player.getParkour().getRestartPoint());
                                Bukkit.getPluginManager().callEvent(event2);
                                if (event2.isCancelled()) {
                                    return;
                                }
                                p.setFallDistance(0);
                                Location l2 = player.getParkour().getRestartPoint().getLocation().clone();
                                if (player.getLastReached() != 0) {
                                    l2 = player.getParkour().getCheckpoint(player.getLastReached()).getLocation().clone();
                                }

                                l2.setX(l2.getX() + 0.5);
                                l2.setY(l2.getY() + 0.5);
                                l2.setZ(l2.getZ() + 0.5);
                                p.setVelocity(new Vector(0, 0, 0));
                                p.teleport(l2);
                                ConfigUtil.sendMessage(p, "Messages.Commands.Checkpoint.Successful", "You have been teleported to your last checkpoint.", true, Collections.emptyMap());
                                FallListener.getHasTeleported().add(p);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        FallListener.getHasTeleported().remove(p);
                                    }
                                }.runTaskLater(Main.getInstance(), 5);
                                return;
                            case 2:
                                //Cancel.
                                ParkourPlayerLeaveEvent leaveEvent = new ParkourPlayerLeaveEvent(player.getParkour(), player);
                                Bukkit.getPluginManager().callEvent(leaveEvent);
                                if (leaveEvent.isCancelled()) {
                                    return;
                                }
                                //Delay to avoid clientside visual glitch
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        player.end(ParkourPlayerFailEvent.FailCause.LEAVE);
                                    }
                                }.runTaskLater(Main.getInstance(), 1);
                                ConfigUtil.sendMessage(p, "Messages.Commands.Leave.Left", "You have left the parkour and your progress has been reset.", true, Collections.emptyMap());
                                return;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBuild(BlockPlaceEvent e) {
        if (CacheManager.isParkour(e.getPlayer())) {
            if (e.getBlockPlaced() != null) {
                for (int type : CacheManager.getItems().keySet()) {
                    if (CacheManager.getItems().get(type).getType().equals(e.getBlockPlaced().getType())) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }
}
