package me.block2block.hubparkour.entities;


import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.ILeaderboardHologram;
import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerFinishEvent;
import me.block2block.hubparkour.api.items.CancelItem;
import me.block2block.hubparkour.api.items.CheckpointItem;
import me.block2block.hubparkour.api.items.ParkourItem;
import me.block2block.hubparkour.api.items.ResetItem;
import me.block2block.hubparkour.api.plates.Checkpoint;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
public class HubParkourPlayer implements IHubParkourPlayer {

    private final Player player;
    private final Parkour parkour;
    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private final List<ParkourItem> parkourItems = new ArrayList<>();
    private int lastReached = 0;
    private long startTime;
    private long previous = -2;

    @SuppressWarnings("unused")
    public HubParkourPlayer(Player p, Parkour parkour) {
        this.parkour = parkour;
        this.player = p;
        startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            @Override
            public void run() {
                previous = Main.getInstance().getDbManager().getTime(p, parkour);
            }
        }.runTaskAsynchronously(Main.getInstance());
        parkourItems.add(new ResetItem(this, Main.getInstance().getConfig().getInt("Settings.Parkour-Items.Reset.Slot")));
        parkourItems.add(new CheckpointItem(this, Main.getInstance().getConfig().getInt("Settings.Parkour-Items.Checkpoint.Slot")));
        parkourItems.add(new CancelItem(this, Main.getInstance().getConfig().getInt("Settings.Parkour-Items.Cancel.Slot")));
    }

    public void checkpoint(Checkpoint checkpoint) {
        lastReached = checkpoint.getCheckpointNo();
        if (!checkpoints.contains(checkpoint)) {
            checkpoints.add(checkpoint);
        }
    }

    public void end(ParkourPlayerFailEvent.FailCause cause) {
        if (cause != null) {
            ParkourPlayerFailEvent failEvent = new ParkourPlayerFailEvent(this.parkour, this, cause);
            Bukkit.getPluginManager().callEvent(failEvent);
            if (failEvent.isCancelled()) {
                return;
            }
            switch (cause) {
                case FLY:
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Failed.Fly")));
                    break;
                case ELYTRA_USE:
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Failed.Elytra-Use")));
                    break;
                case TELEPORTATION:
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Failed.Teleportation")));
                    break;
                case NEW_PARKOUR:
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Failed.Parkour-Change")));
                    break;
            }
        } else {
            if (Main.getInstance().getConfig().getBoolean("Settings.Must-Complete-All-Checkpoints")) {
                if (checkpoints.size() != parkour.getNoCheckpoints()) {
                    ParkourPlayerFailEvent failEvent = new ParkourPlayerFailEvent(this.parkour, this, ParkourPlayerFailEvent.FailCause.NOT_ENOUGH_CHECKPOINTS);
                    Bukkit.getPluginManager().callEvent(failEvent);
                    if (failEvent.isCancelled()) {
                        return;
                    }
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Failed.Not-Enough-Checkpoints")));
                    parkour.playerEnd(this);
                    CacheManager.playerEnd(this);
                    removeItems();
                    return;
                }
            }

            long finishMili = System.currentTimeMillis() - startTime;
            float finishTime = finishMili/1000f;
            ParkourPlayerFinishEvent finishEvent = new ParkourPlayerFinishEvent(this.parkour, this, finishMili, finishMili + startTime, startTime);
            Bukkit.getPluginManager().callEvent(finishEvent);
            if (finishEvent.isCancelled()) {
                return;
            }
            if (previous > 0) {
                if (finishMili < previous) {
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Beat-Previous-Personal-Best").replace("{time}","" + finishTime).replace("{parkour-name}",parkour.getName())));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Main.getInstance().getDbManager().newTime(player, finishMili, true, parkour);
                            int position = Main.getInstance().getDbManager().leaderboardPosition(player, parkour);
                            player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Leaderboard.Leaderboard-Place").replace("{position}", "" + position).replace("{parkour-name}",parkour.getName()).replace("{suffix}",((position % 10 == 1)?"st":((position % 10 == 2)?"nd":((position % 10 == 3)?"rd":"th"))))));
                            for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                hologram.refresh();
                            }
                        }
                    }.runTaskAsynchronously(Main.getInstance());
                } else {
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Not-Beat-Previous-Personal-Best").replace("{time}","" + finishTime).replace("{parkour-name}",parkour.getName())));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int position = Main.getInstance().getDbManager().leaderboardPosition(player, parkour);
                            player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Leaderboard.Leaderboard-Place").replace("{position}", "" + position).replace("{parkour-name}", parkour.getName()).replace("{suffix}", ((position % 10 == 1) ? "st" : ((position % 10 == 2) ? "nd" : ((position % 10 == 3) ? "rd" : "th"))))));
                        }
                    }.runTaskAsynchronously(Main.getInstance());
                    parkour.playerEnd(this);
                    CacheManager.playerEnd(this);
                    removeItems();
                    return;
                }
            } else {
                if (previous == -1) {
                    if (parkour.getEndCommand() != null) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parkour.getEndCommand().replace("{player-name}",player.getName()).replace("{player-uuid}",player.getUniqueId().toString()));
                    }
                    if (parkour.getCheckpointCommand() != null) {
                        for (int i = 0;i < checkpoints.size();i++) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parkour.getCheckpointCommand().replace("{player-name}",player.getName()).replace("{player-uuid}",player.getUniqueId().toString()));
                        }
                    }
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.First-Time").replace("{time}","" + finishTime).replace("{parkour-name}",parkour.getName())));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Main.getInstance().getDbManager().newTime(player, finishMili, false, parkour);
                            int position = Main.getInstance().getDbManager().leaderboardPosition(player, parkour);
                            player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Leaderboard.Leaderboard-Place").replace("{position}", "" + position).replace("{parkour-name}",parkour.getName()).replace("{suffix}",((position % 10 == 1)?"st":((position % 10 == 2)?"nd":((position % 10 == 3)?"rd":"th"))))));
                            for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                hologram.refresh();
                            }
                        }
                    }.runTaskAsynchronously(Main.getInstance());
                } else {
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Failed.Too-Quick")));
                    parkour.playerEnd(this);
                    CacheManager.playerEnd(this);
                    removeItems();
                    return;
                }
            }

        }
        parkour.playerEnd(this);
        CacheManager.playerEnd(this);
        removeItems();
    }

    public int getLastReached() {
        return lastReached;
    }

    public Parkour getParkour() {
        return parkour;
    }

    public Player getPlayer() {
        return player;
    }

    public void restart() {
        startTime = System.currentTimeMillis();
        checkpoints.clear();
        lastReached = 0;
    }

    public long getPrevious() {
        return previous;
    }

    public long getStartTime() {
        return startTime;
    }

    public List<ParkourItem> getParkourItems() {
        return parkourItems;
    }

    public void giveItems() {
        for (ParkourItem item : parkourItems) {
            item.giveItem();
        }
    }

    public void removeItems(){
        for (ParkourItem item : parkourItems) {
            item.removeItem();
        }
    }
}
