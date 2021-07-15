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
import me.block2block.hubparkour.utils.ConfigUtil;
import me.block2block.hubparkour.utils.TitleUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

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
    private List<Checkpoint> previouslyReachedCheckpoints;
    private boolean lastRunCompleted;
    private ItemStack[] inventory;
    private ItemStack[] extraContents;
    private ItemStack[] armorContents;
    private ItemStack[] storageContents;
    private BukkitTask actionBarTask;
    private final GameMode prevGamemode;
    private final double prevHealth;
    private final int prevHunger;

    @SuppressWarnings("unused")
    public HubParkourPlayer(Player p, Parkour parkour) {
        this.parkour = parkour;
        this.player = p;
        startTime = System.currentTimeMillis();
        currentSplit = startTime;
        prevGamemode = player.getGameMode();
        prevHealth = player.getHealth();
        prevHunger = player.getFoodLevel();
        if (ConfigUtil.getBoolean("Settings.Health.Heal-To-Full", true)) {
            player.setHealth(20);
        }
        if (ConfigUtil.getBoolean("Settings.Hunger.Saturate-To-Full", true)) {
            player.setFoodLevel(30);
        }
        if (ConfigUtil.getBoolean("Settings.Parkour-Gamemode.Enabled", true)) {
            GameMode mode = GameMode.valueOf(ConfigUtil.getString("Settings.Parkour-Gamemode.Gamemode", "ADVENTURE"));
            player.setGameMode(mode);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                lastRunCompleted = Main.getInstance().getDbManager().wasCompletedLastRun(p, parkour);
                if ((ConfigUtil.getBoolean("Settings.Repeat-Rewards", true) || ConfigUtil.getBoolean("Settings.Exploit-Prevention.Checkpoint-Rewards-Everytime", false)) && lastRunCompleted) {
                    //The last run was completed and repeat rewards are enabled, delete all previously reached checkpoints so they can reach them again.
                    previouslyReachedCheckpoints = new ArrayList<>();
                    Main.getInstance().getDbManager().resetReachedCheckpoints(p, parkour);
                } else {
                    previouslyReachedCheckpoints = Main.getInstance().getDbManager().getReachedCheckpoints(p, parkour);
                }
                previous = Main.getInstance().getDbManager().getTime(p, parkour);
                splitTimes = Main.getInstance().getDbManager().getSplitTimes(p, parkour);
                Main.getInstance().getDbManager().resetLastRun(p, parkour);
            }
        }.runTaskAsynchronously(Main.getInstance());
        parkourItems.add(new ResetItem(this, ConfigUtil.getInt("Settings.Parkour-Items.Reset.Slot", 5)));
        parkourItems.add(new CheckpointItem(this, ConfigUtil.getInt("Settings.Parkour-Items.Checkpoint.Slot", 4)));
        parkourItems.add(new CancelItem(this, ConfigUtil.getInt("Settings.Parkour-Items.Cancel.Slot", 6)));
        if (ConfigUtil.getBoolean("Settings.Action-Bar.Enabled", true)) {
            actionBarTask = new BukkitRunnable(){
                @Override
                public void run() {
                    String message = Main.c(false, ConfigUtil.getString("Messages.Parkour.Action-Bar", "&a&lCurrent Time: &r{current-time} - &a&lParkour: &r{parkour-name}&r - &a&lCurrent Checkpoint: &r#{current-checkpoint}").replace("{current-time}", ConfigUtil.formatTime((System.currentTimeMillis() - startTime))).replace("{parkour-name}", parkour.getName()).replace("{current-checkpoint}", lastReached + "").replace("{current-splittime}", "" + ((System.currentTimeMillis() - currentSplit)/1000f)));
                    if (Main.isPlaceholders()) {
                        message = PlaceholderAPI.setPlaceholders(player, message);
                    }
                    TitleUtil.sendActionBar(player, message, ChatColor.WHITE, false);
                }
            }.runTaskTimerAsynchronously(Main.getInstance(), 0, ConfigUtil.getInt("Settings.Action-Bar.Update-Interval", 2));
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
            Map<String, String> bindings = new HashMap<>();
            bindings.put("checkpoint", checkpoint.getCheckpointNo() + "");
            bindings.put("new-time", ConfigUtil.formatTime(ms));
            if (splitTimes.containsKey(checkpoint.getCheckpointNo())) {
                bindings.put("old-time", ConfigUtil.formatTime(splitTimes.get(checkpoint.getCheckpointNo())));
                if (splitTimes.get(checkpoint.getCheckpointNo()) > ms) {

                    ConfigUtil.sendMessage(player, "Messages.Parkour.Checkpoints.Reached.Beat-Split-Time", "You have reached checkpoint &a#{checkpoint}&r in &a{new-time}s&r and beat your personal best of &a{old-time}s&r!", true, bindings);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Main.getInstance().getDbManager().setSplitTime(player, parkour, checkpoint.getCheckpointNo(), ms, true);
                        }
                    }.runTaskAsynchronously(Main.getInstance());
                    splitTimes.put(checkpoint.getCheckpointNo(), ms);
                } else {
                    ConfigUtil.sendMessage(player, "Messages.Parkour.Checkpoints.Reached.Not-Beat-Split-Time", "You have reached checkpoint &a#{checkpoint}&r in &a{new-time}s&r (personal best: {old-time}s)!", true, bindings);
                }
            } else {
                ConfigUtil.sendMessage(player, "Messages.Parkour.Checkpoints.Reached.New-Split-Time", "You have reached checkpoint &a#{checkpoint}&r in &a{new-time}s&r!", true, bindings);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Main.getInstance().getDbManager().setSplitTime(player, parkour, checkpoint.getCheckpointNo(), ms, false);
                    }
                }.runTaskAsynchronously(Main.getInstance());
                splitTimes.put(checkpoint.getCheckpointNo(), ms);
            }
            checkpoints.add(checkpoint);

            //Give checkpoint reward if not already reached.
            if (!previouslyReachedCheckpoints.contains(checkpoint)) {
                if (parkour.getCheckpointCommand() != null) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parkour.getCheckpointCommand().replace("{player-name}",player.getName()).replace("{player-uuid}",player.getUniqueId().toString()));
                }
            } else {
                if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Checkpoint-Rewards-Everytime", false)) {
                    if (parkour.getCheckpointCommand() != null) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parkour.getCheckpointCommand().replace("{player-name}",player.getName()).replace("{player-uuid}",player.getUniqueId().toString()));
                    }
                }
            }

            previouslyReachedCheckpoints.add(checkpoint);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Main.getInstance().getDbManager().reachedCheckpoint(player, parkour, checkpoint);
                }
            }.runTaskAsynchronously(Main.getInstance());

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
                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Failed.Fly", "You are not allowed to fly while doing the parkour. Parkour failed!", true, Collections.emptyMap());
                    break;
                case ELYTRA_USE:
                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Failed.Elytra-Use", "You are not allowed to use an Elytra while doing the parkour. Parkour failed!", true, Collections.emptyMap());
                    break;
                case TELEPORTATION:
                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Failed.Teleportation", "You are not allowed to teleport while doing the parkour. Parkour failed!", true, Collections.emptyMap());
                    break;
                case NEW_PARKOUR:
                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Failed.Parkour-Change", "You have started another parkour, parkour failed!", true, Collections.emptyMap());
                    break;
            }
        } else {
            if (ConfigUtil.getBoolean("Settings.Must-Complete-All-Checkpoints", true)) {
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
                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Failed.Not-Enough-Checkpoints", "You did not reach enough checkpoints, parkour failed!", true, Collections.emptyMap());
                    parkour.playerEnd(this);
                    if (ConfigUtil.getBoolean("Settings.Health.Heal-To-Full", true)) {
                        player.setHealth(prevHealth);
                    }
                    if (ConfigUtil.getBoolean("Settings.Hunger.Saturate-To-Full", true)) {
                        player.setFoodLevel(prevHunger);
                    }
                    if (ConfigUtil.getBoolean("Settings.Parkour-Gamemode.Enabled", true)) {
                        player.setGameMode(prevGamemode);
                    }
                    CacheManager.playerEnd(this);
                    removeItems();
                    return;
                }
            }

            long finishMili = System.currentTimeMillis() - startTime;

            long splitMs = System.currentTimeMillis() - currentSplit;

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

            new BukkitRunnable() {
                @Override
                public void run() {
                    Main.getInstance().getDbManager().completedLastRun(player, parkour);
                }
            }.runTaskAsynchronously(Main.getInstance());

            Map<String, String> bindings = new HashMap<>();
            bindings.put("new-time", ConfigUtil.formatTime(splitMs));
            if (splitTimes.containsKey(check)) {
                long oldSplit = splitTimes.get(check);
                bindings.put("old-time", ConfigUtil.formatTime(oldSplit));
                if (oldSplit > splitMs) {
                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Split-Time.Beat-Split-Time", "You have reached the finish point in &a{new-time}s&r and beat your personal best of &a{old-time}s&r!", true, bindings);
                    int finalCheck = check;
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Main.getInstance().getDbManager().setSplitTime(player, parkour, finalCheck, splitMs, true);
                        }
                    }.runTaskAsynchronously(Main.getInstance());
                } else {
                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Split-Time.Not-Beat-Split-Time", "You have reached the finish point in &a{new-time}s&r (personal best: {old-time}s)!", true, bindings);
                }
            } else {
                ConfigUtil.sendMessage(player, "Messages.Parkour.End.Split-Time.New-Split-Time", "You have reached the finish point in &a{new-time}s&r!", true, bindings);
                int finalCheck = check;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Main.getInstance().getDbManager().setSplitTime(player, parkour, finalCheck, splitMs, false);
                    }
                }.runTaskAsynchronously(Main.getInstance());
            }

            if (previous > 0) {
                if (ConfigUtil.getBoolean("Settings.Repeat-Rewards", true)) {
                    if (parkour.getEndCommand() != null) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parkour.getEndCommand().replace("{player-name}",player.getName()).replace("{player-uuid}",player.getUniqueId().toString()));
                    }
                }
                bindings.clear();
                bindings.put("time",ConfigUtil.formatTime(finishMili));
                bindings.put("parkour-name", parkour.getName());
                if (finishMili < previous) {

                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Beat-Previous-Personal-Best", "You beat your previous record and you managed to complete the &a{parkour-name} &rparkour in &a{time} &rseconds!", true, bindings);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Main.getInstance().getDbManager().newTime(player, finishMili, true, parkour);
                            int position = Main.getInstance().getDbManager().leaderboardPosition(player, parkour);

                            bindings.clear();
                            bindings.put("position","" + position);
                            bindings.put("suffix",((position % 10 == 1)?"st":((position % 10 == 2)?"nd":((position % 10 == 3)?((position == 13)?"th":"rd"):"th"))));
                            bindings.put("parkour-name",parkour.getName());

                            ConfigUtil.sendMessage(player, "Messages.Parkour.Leaderboard.Leaderboard-Place", "You are in &a{position}{suffix} place&r for the &a{parkour-name}&r parkour!", true, bindings);
                            for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        hologram.refresh();
                                    }
                                }.runTask(Main.getInstance());
                            }
                        }
                    }.runTaskAsynchronously(Main.getInstance());
                } else {
                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Not-Beat-Previous-Personal-Best", "You didn't beat your previous record, but you managed to complete the &a{parkour-name} &rparkour in &a{time} &rseconds!", true, bindings);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int position = Main.getInstance().getDbManager().leaderboardPosition(player, parkour);
                            bindings.clear();
                            bindings.put("position","" + position);
                            bindings.put("suffix",((position % 10 == 1)?"st":((position % 10 == 2)?"nd":((position % 10 == 3)?((position == 13)?"th":"rd"):"th"))));
                            bindings.put("parkour-name",parkour.getName());

                            ConfigUtil.sendMessage(player, "Messages.Parkour.Leaderboard.Leaderboard-Place", "You are in &a{position}{suffix} place&r for the &a{parkour-name}&r parkour!", true, bindings);
                        }
                    }.runTaskAsynchronously(Main.getInstance());
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

                    bindings.clear();
                    bindings.put("time",ConfigUtil.formatTime(finishMili));
                    bindings.put("parkour-name", parkour.getName());

                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.First-Time", "Well done! You completed the &a{parkour-name}&r parkour in &a{time}&r seconds! Your reward will be applied shortly!", true, bindings);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Main.getInstance().getDbManager().newTime(player, finishMili, false, parkour);
                            int position = Main.getInstance().getDbManager().leaderboardPosition(player, parkour);
                            bindings.clear();
                            bindings.put("position","" + position);
                            bindings.put("suffix",((position % 10 == 1)?"st":((position % 10 == 2)?"nd":((position % 10 == 3)?"rd":"th"))));
                            bindings.put("parkour-name",parkour.getName());

                            ConfigUtil.sendMessage(player, "Messages.Parkour.Leaderboard.Leaderboard-Place", "You are in &a{position}{suffix} place&r for the &a{parkour-name}&r parkour!", true, bindings);
                            for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        hologram.refresh();
                                    }
                                }.runTask(Main.getInstance());
                            }
                        }
                    }.runTaskAsynchronously(Main.getInstance());
                } else {
                    ConfigUtil.sendMessage(player, "Messages.Parkour.End.Failed.Too-Quick", "You completed the parkour too quickly, parkour failed!", true, Collections.emptyMap());
                }
            }

        }
        removeItems();
        parkour.playerEnd(this);
        if (ConfigUtil.getBoolean("Settings.Health.Heal-To-Full", true)) {
            player.setHealth(prevHealth);
        }
        if (ConfigUtil.getBoolean("Settings.Hunger.Saturate-To-Full", true)) {
            player.setFoodLevel(prevHunger);
        }
        if (ConfigUtil.getBoolean("Settings.Parkour-Gamemode.Enabled", true)) {
            player.setGameMode(prevGamemode);
        }
        CacheManager.playerEnd(this);

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
        if (ConfigUtil.getBoolean("Settings.Parkour-Items.Clear-Inventory-On-Parkour-Start", true)) {
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
        if (ConfigUtil.getBoolean("Settings.Parkour-Items.Clear-Inventory-On-Parkour-Start", true)) {
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
        if (ConfigUtil.getBoolean("Settings.Parkour-Gamemode.Enabled", true)) {
            player.setGameMode(prevGamemode);
        }
        if (ConfigUtil.getBoolean("Settings.Health.Heal-To-Full", true)) {
            player.setHealth(prevHealth);
        }
        if (ConfigUtil.getBoolean("Settings.Hunger.Saturate-To-Full", true)) {
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
