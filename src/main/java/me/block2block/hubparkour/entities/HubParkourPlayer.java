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
import me.block2block.hubparkour.utils.TitleUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
public class HubParkourPlayer implements IHubParkourPlayer {

    private final Player player;
    private final Parkour parkour;
    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private final List<ParkourItem> parkourItems = new ArrayList<>();
    private long currentSplit;
    private Map<Integer, Long> splitTimes;
    private int lastReached = 0;
    private long startTime;
    private long previous = -2;
    private ItemStack[] inventory;
    private ItemStack[] extraContents;
    private ItemStack[] armorContents;
    private ItemStack[] storageContents;
    private BukkitTask actionBarTask;
    private GameMode prevGamemode;
    private double prevHealth;
    private int prevHunger;

    @SuppressWarnings("unused")
    public HubParkourPlayer(Player p, Parkour parkour) {
        this.parkour = parkour;
        this.player = p;
        startTime = System.currentTimeMillis();
        currentSplit = startTime;
        prevGamemode = player.getGameMode();
        prevHealth = player.getHealth();
        prevHunger = player.getFoodLevel();
        if (Main.getInstance().getConfig().getBoolean("Settings.Health.Heal-To-Full")) {
            player.setHealth(20);
        }
        if (Main.getInstance().getConfig().getBoolean("Settings.Hunger.Saturate-To-Full")) {
            player.setFoodLevel(30);
        }
        if (Main.getInstance().getConfig().getBoolean("Settings.Parkour-Gamemode.Enabled")) {
            GameMode mode = GameMode.valueOf(Main.getInstance().getConfig().getString("Settings.Parkour-Gamemode.Gamemode"));
            player.setGameMode(mode);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                previous = Main.getInstance().getDbManager().getTime(p, parkour);
                splitTimes = Main.getInstance().getDbManager().getSplitTimes(p, parkour);
            }
        }.runTaskAsynchronously(Main.getInstance());
        parkourItems.add(new ResetItem(this, Main.getInstance().getConfig().getInt("Settings.Parkour-Items.Reset.Slot")));
        parkourItems.add(new CheckpointItem(this, Main.getInstance().getConfig().getInt("Settings.Parkour-Items.Checkpoint.Slot")));
        parkourItems.add(new CancelItem(this, Main.getInstance().getConfig().getInt("Settings.Parkour-Items.Cancel.Slot")));
        if (Main.getInstance().getConfig().getBoolean("Settings.Action-Bar.Enabled")) {
            actionBarTask = new BukkitRunnable(){
                @Override
                public void run() {
                    TitleUtil.sendActionBar(player, Main.c(false, Main.getInstance().getConfig().getString("Messages.Parkour.Action-Bar").replace("{current-time}", "" + ((System.currentTimeMillis() - startTime) / 1000f)).replace("{parkour-name}", parkour.getName()).replace("{current-checkpoint}", lastReached + "").replace("{current-splittime}", "" + ((System.currentTimeMillis() - currentSplit)/1000f))), ChatColor.WHITE, false);
                }
            }.runTaskTimerAsynchronously(Main.getInstance(), 0, Main.getInstance().getConfig().getInt("Settings.Action-Bar.Update-Interval"));
        }
    }

    public void checkpoint(Checkpoint checkpoint) {
        if (lastReached == checkpoint.getCheckpointNo()) {
            currentSplit = System.currentTimeMillis();
            return;
        }
        if (!checkpoints.contains(checkpoint)) {
            lastReached = checkpoint.getCheckpointNo();
            long ms = System.currentTimeMillis() - currentSplit;
            float time = ms/1000f;
            if (splitTimes.containsKey(checkpoint.getCheckpointNo())) {
                if (splitTimes.get(checkpoint.getCheckpointNo()) > ms) {
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Checkpoints.Reached.Beat-Split-Time").replace("{checkpoint}","" + checkpoint.getCheckpointNo()).replace("{new-time}","" + time).replace("{old-time}","" + (splitTimes.get(checkpoint.getCheckpointNo())/1000f))));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Main.getInstance().getDbManager().setSplitTime(player, parkour, checkpoint.getCheckpointNo(), ms, true);
                        }
                    }.runTaskAsynchronously(Main.getInstance());
                    splitTimes.put(checkpoint.getCheckpointNo(), ms);
                } else {
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Checkpoints.Reached.Not-Beat-Split-Time").replace("{checkpoint}","" + checkpoint.getCheckpointNo()).replace("{new-time}","" + time).replace("{old-time}","" + (splitTimes.get(checkpoint.getCheckpointNo())/1000f))));
                }
            } else {
                player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.Checkpoints.Reached.New-Split-Time").replace("{checkpoint}","" + checkpoint.getCheckpointNo()).replace("{new-time}","" + time)));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Main.getInstance().getDbManager().setSplitTime(player, parkour, checkpoint.getCheckpointNo(), ms, false);
                    }
                }.runTaskAsynchronously(Main.getInstance());
                splitTimes.put(checkpoint.getCheckpointNo(), ms);
            }
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
            if (actionBarTask != null) {
                actionBarTask.cancel();
                actionBarTask = null;
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
                    if (actionBarTask != null) {
                        actionBarTask.cancel();
                        actionBarTask = null;
                    }
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Failed.Not-Enough-Checkpoints")));
                    parkour.playerEnd(this);
                    if (Main.getInstance().getConfig().getBoolean("Settings.Health.Heal-To-Full")) {
                        player.setHealth(prevHealth);
                    }
                    if (Main.getInstance().getConfig().getBoolean("Settings.Hunger.Saturate-To-Full")) {
                        player.setFoodLevel(prevHunger);
                    }
                    if (Main.getInstance().getConfig().getBoolean("Settings.Parkour-Gamemode.Enabled")) {
                        player.setGameMode(prevGamemode);
                    }
                    CacheManager.playerEnd(this);
                    removeItems();
                    return;
                }
            }

            long finishMili = System.currentTimeMillis() - startTime;
            float finishTime = finishMili/1000f;

            long splitMs = System.currentTimeMillis() - currentSplit;
            float splitTime = splitMs/1000f;

            ParkourPlayerFinishEvent finishEvent = new ParkourPlayerFinishEvent(this.parkour, this, finishMili, finishMili + startTime, startTime);
            Bukkit.getPluginManager().callEvent(finishEvent);
            if (finishEvent.isCancelled()) {
                return;
            }
            if (actionBarTask != null) {
                actionBarTask.cancel();
                actionBarTask = null;
            }

            int check = 0;

            if (checkpoints.size() > 0) {
                check = checkpoints.get(checkpoints.size() - 1).getCheckpointNo() + 1;
            }

            if (splitTimes.containsKey(check)) {
                if (splitTimes.get(check) > splitMs) {
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Split-Time.Beat-Split-Time").replace("{new-time}","" + splitTime).replace("{old-time}","" + (splitTimes.get(check)/1000f))));
                    int finalCheck = check;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Main.getInstance().getDbManager().setSplitTime(player, parkour, finalCheck, splitMs, true);
                        }
                    }.runTaskAsynchronously(Main.getInstance());
                } else {
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Split-Time.Not-Beat-Split-Time").replace("{new-time}","" + splitTime).replace("{old-time}","" + (splitTimes.get(check)/1000f))));
                }
            } else {
                player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Split-Time.New-Split-Time").replace("{new-time}","" + splitTime)));
                int finalCheck = check;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Main.getInstance().getDbManager().setSplitTime(player, parkour, finalCheck, splitMs, false);
                    }
                }.runTaskAsynchronously(Main.getInstance());
            }

            if (previous > 0) {
                if (Main.getInstance().getConfig().getBoolean("Settings.Repeat-Rewards")) {
                    if (parkour.getEndCommand() != null) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parkour.getEndCommand().replace("{player-name}",player.getName()).replace("{player-uuid}",player.getUniqueId().toString()));
                    }
                    if (parkour.getCheckpointCommand() != null) {
                        for (int i = 0;i < checkpoints.size();i++) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parkour.getCheckpointCommand().replace("{player-name}",player.getName()).replace("{player-uuid}",player.getUniqueId().toString()));
                        }
                    }
                }
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
                    if (Main.getInstance().getConfig().getBoolean("Settings.Health.Heal-To-Full")) {
                        player.setHealth(prevHealth);
                    }
                    if (Main.getInstance().getConfig().getBoolean("Settings.Hunger.Saturate-To-Full")) {
                        player.setFoodLevel(prevHunger);
                    }
                    if (Main.getInstance().getConfig().getBoolean("Settings.Parkour-Gamemode.Enabled")) {
                        player.setGameMode(prevGamemode);
                    }
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
                    if (Main.getInstance().getConfig().getBoolean("Settings.Health.Heal-To-Full")) {
                        player.setHealth(prevHealth);
                    }
                    if (Main.getInstance().getConfig().getBoolean("Settings.Hunger.Saturate-To-Full")) {
                        player.setFoodLevel(prevHunger);
                    }
                    if (Main.getInstance().getConfig().getBoolean("Settings.Parkour-Gamemode.Enabled")) {
                        player.setGameMode(prevGamemode);
                    }
                    CacheManager.playerEnd(this);
                    removeItems();
                    return;
                }
            }

        }
        parkour.playerEnd(this);
        if (Main.getInstance().getConfig().getBoolean("Settings.Parkour-Gamemode.Enabled")) {
            player.setGameMode(prevGamemode);
        }
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
        currentSplit = startTime;
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
        inventory = player.getInventory().getContents();
        armorContents = player.getInventory().getArmorContents();
        if (Main.isPost1_8()) {
            extraContents = player.getInventory().getExtraContents();
            storageContents = player.getInventory().getStorageContents();
        }
        if (Main.getInstance().getConfig().getBoolean("Settings.Parkour-Items.Clear-Inventory-On-Parkour-Start")) {
            player.getInventory().clear();
        }
        for (ParkourItem item : parkourItems) {
            item.giveItem();
        }
    }

    public void removeItems(){
        for (ParkourItem item : parkourItems) {
            item.removeItem();
        }
        if (Main.getInstance().getConfig().getBoolean("Settings.Parkour-Items.Clear-Inventory-On-Parkour-Start")) {
            if (inventory != null) {
                player.getInventory().setContents(inventory);
            }
            if (armorContents != null) {
                player.getInventory().setArmorContents(armorContents);
            }
            if (Main.isPost1_8()) {
                if (extraContents != null) {
                    player.getInventory().setExtraContents(extraContents);
                }
                if (storageContents != null) {
                    player.getInventory().setStorageContents(storageContents);
                }
            }

        }
    }

    public BukkitTask getActionBarTask() {
        return actionBarTask;
    }

    public Map<Integer, Long> getSplitTimes() {
        return splitTimes;
    }

    public long getCurrentSplit() {
        return currentSplit;
    }

    public GameMode getPrevGamemode() {
        return prevGamemode;
    }

    public void setToPrevState() {
        if (Main.getInstance().getConfig().getBoolean("Settings.Parkour-Gamemode.Enabled")) {
            player.setGameMode(prevGamemode);
        }
        if (Main.getInstance().getConfig().getBoolean("Settings.Health.Heal-To-Full")) {
            player.setHealth(prevHealth);
        }
        if (Main.getInstance().getConfig().getBoolean("Settings.Hunger.Saturate-To-Full")) {
            player.setFoodLevel(prevHunger);
        }
    }

    public double getPrevHealth() {
        return prevHealth;
    }

    public int getPrevHunger() {
        return prevHunger;
    }
}
