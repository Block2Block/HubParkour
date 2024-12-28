package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerLeaveEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerTeleportEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerTogglePlayersEvent;
import me.block2block.hubparkour.api.items.HideItem;
import me.block2block.hubparkour.api.items.ParkourItem;
import me.block2block.hubparkour.api.items.ShowItem;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.Parkour;
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

import java.util.*;

public class ItemClickListener implements Listener {

    private final List<Player> cancelNextEvent = new ArrayList<>();
    private final Map<Player, Integer> confirmationRequied = new HashMap<>();

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
                        Parkour parkour = player.getParkour();
                        switch (type) {
                            case 0:
                                //Reset.
                                if (FallListener.getHasTeleported().contains(p)) {
                                    return;
                                }
                                ParkourPlayerTeleportEvent event = new ParkourPlayerTeleportEvent(parkour, player, parkour.getRestartPoint(), ParkourPlayerTeleportEvent.TeleportReason.ITEM_CLICK_RESET);
                                Bukkit.getPluginManager().callEvent(event);
                                if (event.isCancelled()) {
                                    return;
                                }
                                if ((confirmationRequied.containsKey(p) && confirmationRequied.get(p) == 0) || !ConfigUtil.getBoolean("Settings.Parkour-Items.Reset.Confirmation", true)) {
                                    confirmationRequied.remove(p);
                                    p.setFallDistance(0);
                                    Location l = parkour.getRestartPoint().getLocation().clone();
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
                                    }.runTaskLater(HubParkour.getInstance(), 5);
                                } else {
                                    confirmationRequied.put(p, 0);
                                    ConfigUtil.sendMessage(p, "Messages.Parkour.Confirm-Action", "Please click the item again to confirm your action.", true, Collections.emptyMap());
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            confirmationRequied.remove(p);
                                        }
                                    }.runTaskLater(HubParkour.getInstance(), 100);
                                }
                                return;
                            case 1:
                                //Checkpoint.
                                if (FallListener.getHasTeleported().contains(p)) {
                                    return;
                                }
                                ParkourPlayerTeleportEvent event2 = new ParkourPlayerTeleportEvent(parkour, player, (player.getLastReached() != 0)?parkour.getCheckpoint(player.getLastReached()):parkour.getRestartPoint(), ParkourPlayerTeleportEvent.TeleportReason.ITEM_CLICK_CHECKPOINT);
                                Bukkit.getPluginManager().callEvent(event2);
                                if (event2.isCancelled()) {
                                    return;
                                }

                                if ((confirmationRequied.containsKey(p) && confirmationRequied.get(p) == 1) || !ConfigUtil.getBoolean("Settings.Parkour-Items.Checkpoint.Confirmation", true)) {
                                    confirmationRequied.remove(p);
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
                                    }.runTaskLater(HubParkour.getInstance(), 5);
                                } else {
                                    confirmationRequied.put(p, 1);
                                    ConfigUtil.sendMessage(p, "Messages.Parkour.Confirm-Action", "Please click the item again to confirm your action.", true, Collections.emptyMap());
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            confirmationRequied.remove(p);
                                        }
                                    }.runTaskLater(HubParkour.getInstance(), 100);
                                }
                                return;
                            case 2:
                                //Cancel.
                                ParkourPlayerLeaveEvent leaveEvent = new ParkourPlayerLeaveEvent(parkour, player);
                                Bukkit.getPluginManager().callEvent(leaveEvent);
                                if (leaveEvent.isCancelled()) {
                                    return;
                                }
                                if ((confirmationRequied.containsKey(p) && confirmationRequied.get(p) == 2) || !ConfigUtil.getBoolean("Settings.Parkour-Items.Cancel.Confirmation", true)) {
                                    confirmationRequied.remove(p);
                                    //Delay to avoid clientside visual glitch
                                    new BukkitRunnable(){
                                        @Override
                                        public void run() {
                                            player.end(ParkourPlayerFailEvent.FailCause.LEAVE);
                                        }
                                    }.runTaskLater(HubParkour.getInstance(), 1);
                                    ConfigUtil.sendMessage(p, "Messages.Commands.Leave.Left", "You have left the parkour and your progress has been reset.", true, Collections.emptyMap());
                                } else {
                                    confirmationRequied.put(p, 2);
                                    ConfigUtil.sendMessage(p, "Messages.Parkour.Confirm-Action", "Please click the item again to confirm your action.", true, Collections.emptyMap());
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            confirmationRequied.remove(p);
                                        }
                                    }.runTaskLater(HubParkour.getInstance(), 100);
                                }
                                return;
                            case 3: {
                                ParkourPlayerTogglePlayersEvent toggleEvent = new ParkourPlayerTogglePlayersEvent(player.getParkour(), player, false);
                                Bukkit.getPluginManager().callEvent(toggleEvent);
                                if (toggleEvent.isCancelled()) {
                                    return;
                                }
                                ParkourItem item = null;
                                for (ParkourItem item2 : player.getParkourItems()) {
                                    if (item2.getType() == 3) {
                                        item = item2;
                                        break;
                                    }
                                }
                                player.getParkourItems().remove(item);
                                if (item != null) {
                                    ShowItem showItem = new ShowItem((HideItem) item);
                                    player.getParkourItems().add(showItem);
                                    player.getPlayer().getInventory().setItem(showItem.getSlot(), showItem.getItem());
                                    for (Player pl : Bukkit.getOnlinePlayers()) {
                                        if (player.getPlayer().canSee(pl)) {
                                            showItem.getHiddenPlayers().add(pl);
                                            p.hidePlayer(pl);
                                        }
                                    }
                                }
                                break;
                            }
                            case 4: {
                                ParkourPlayerTogglePlayersEvent toggleEvent = new ParkourPlayerTogglePlayersEvent(parkour, player, true);
                                Bukkit.getPluginManager().callEvent(toggleEvent);
                                if (toggleEvent.isCancelled()) {
                                    return;
                                }
                                ParkourItem item = null;
                                for (ParkourItem item2 : player.getParkourItems()) {
                                    if (item2.getType() == 4) {
                                        item = item2;
                                        break;
                                    }
                                }
                                player.getParkourItems().remove(item);
                                if (item != null) {
                                    HideItem hideItem = new HideItem((ShowItem) item);
                                    player.getParkourItems().add(hideItem);
                                    player.getPlayer().getInventory().setItem(hideItem.getSlot(), hideItem.getItem());
                                    for (Player player1 : ((ShowItem) item).getHiddenPlayers()) {
                                        if (player1.isOnline()) {
                                            player.getPlayer().showPlayer(player1);
                                        }
                                    }
                                }
                                break;
                            }
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
