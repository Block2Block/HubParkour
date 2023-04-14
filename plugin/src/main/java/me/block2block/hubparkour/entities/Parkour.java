package me.block2block.hubparkour.entities;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.ILeaderboardHologram;
import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.plates.*;
import me.block2block.hubparkour.api.signs.ClickableSign;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Parkour implements IParkour {

    private final int id;
    private UUID server;
    private String name;
    private StartPoint start;
    private EndPoint endPoint;
    private final List<Checkpoint> checkpoints;
    private final List<IHubParkourPlayer> players;
    private String checkpointCommand;
    private String endCommand;
    private RestartPoint restartPoint;
    private int rewardCooldown;
    private final Map<PressurePlate, Hologram> holograms = new HashMap<>();
    private final List<ILeaderboardHologram> leaderboardHolograms = new ArrayList<>();
    private final List<BorderPoint> borderPoints;

    @SuppressWarnings("unused")
    public Parkour(int id, UUID server, String name, StartPoint start, EndPoint end, List<Checkpoint> checkpoints, RestartPoint restartPoint, List<BorderPoint> borderPoints, String checkpointCommand, String endCommand, int rewardCooldown) {
        this.id = id;
        this.server = server;
        this.start = start;
        this.start.setParkour(this);
        this.endPoint = end;
        this.endPoint.setParkour(this);
        this.checkpoints = checkpoints;
        for (Checkpoint checkpoint : checkpoints) {
            checkpoint.setParkour(this);
        }
        this.players = new ArrayList<>();
        this.checkpointCommand = checkpointCommand;
        this.endCommand = endCommand;
        this.name = name;
        this.restartPoint = restartPoint;
        this.borderPoints = borderPoints;
        for (BorderPoint borderPoint : borderPoints) {
            borderPoint.setParkour(this);
        }
        this.rewardCooldown = rewardCooldown;
    }

    @SuppressWarnings("unused")
    public Parkour(Parkour parkour, int id) {
        this.id = id;
        this.start = parkour.getStart();
        this.start.setParkour(this);
        this.endPoint = parkour.getEndPoint();
        this.endPoint.setParkour(this);
        this.checkpoints = parkour.getCheckpoints();
        for (Checkpoint checkpoint : checkpoints) {
            checkpoint.setParkour(this);
        }
        this.players = new ArrayList<>();
        this.checkpointCommand = parkour.getCheckpointCommand();
        this.endCommand = parkour.getEndCommand();
        this.name = parkour.getName();
        this.restartPoint = parkour.getRestartPoint();
        this.borderPoints = parkour.getBorders();
        this.rewardCooldown = parkour.getRewardCooldown();
    }

    public int getNoCheckpoints() {
        return checkpoints.size();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public List<Checkpoint> getCheckpoints() {
        return new ArrayList<>(checkpoints);
    }

    public List<IHubParkourPlayer> getPlayers() {
        return new ArrayList<>(players);
    }

    public RestartPoint getRestartPoint() {
        return restartPoint;
    }

    public StartPoint getStart() {
        return start;
    }

    public String getCheckpointCommand() {
        return checkpointCommand;
    }

    public String getEndCommand() {
        return endCommand;
    }

    public List<PressurePlate> getAllPoints() {
        List<PressurePlate> pressurePlates = new ArrayList<>(checkpoints);
        pressurePlates.add(endPoint);
        pressurePlates.add(start);
        pressurePlates.add(restartPoint);
        pressurePlates.addAll(borderPoints);
        return pressurePlates;
    }

    @SuppressWarnings("unused")
    public void generateHolograms() {
        for (PressurePlate p : getAllPoints()) {
            List<String> defaultValues = new ArrayList<>();
            String configKey = "";
            switch (p.getType()) {
                case 0:
                    if (!ConfigUtil.getBoolean("Settings.Holograms.Start", true)) {
                        continue;
                    }
                    configKey = "Start";
                    defaultValues.add("&9&l&n{parkour-name}");
                    defaultValues.add("&9&lParkour Start");
                    break;
                case 1:
                    if (!ConfigUtil.getBoolean("Settings.Holograms.End", true)) {
                        continue;
                    }
                    configKey = "End";
                    defaultValues.add("&9&l&n{parkour-name}");
                    defaultValues.add("&9&lParkour End");
                    break;
                case 2:
                    continue;
                case 3:
                    if (!ConfigUtil.getBoolean("Settings.Holograms.Checkpoint", true)) {
                        continue;
                    }
                    configKey = "Checkpoint";
                    defaultValues.add("&9&l&n{parkour-name}");
                    defaultValues.add("&9&lCheckpoint #{checkpoint}");
                    break;
                default:
            }
            Location l = p.getLocation().clone();
            l.setX(l.getX() + 0.5);
            l.setZ(l.getZ() + 0.5);
            l.setY(l.getY() + 2);
            if (l.getWorld() == null) {
                continue;
            }
            Hologram hologram = DHAPI.getHologram("hp_" + id + "-" + p.getType() + ((p instanceof Checkpoint)?"-" + ((Checkpoint) p).getCheckpointNo():""));
            if (hologram == null) {
                hologram = DHAPI.createHologram("hp_" + id + "-" + p.getType() + ((p instanceof Checkpoint)?"-" + ((Checkpoint) p).getCheckpointNo():""), l);
            } else {
                DHAPI.moveHologram(hologram, l);
            }
            int counter = 0;

            List<String> lines = new ArrayList<>();

            for (String s : ConfigUtil.getStringList("Messages.Holograms." + configKey, defaultValues)) {
                s = ChatColor.translateAlternateColorCodes('&', s.replace("{parkour-name}",name).replace("{checkpoint}",((p instanceof Checkpoint)?((Checkpoint)p).getCheckpointNo() + "":"")));
                if (HubParkour.isPlaceholders()) {
                    s = PlaceholderAPI.setPlaceholders(null, s);
                }
                lines.add(s);
                counter++;
            }
            DHAPI.setHologramLines(hologram, lines);
            holograms.put(p, hologram);
        }

    }

    public void removeHolograms() {
        for (Hologram h : holograms.values()) {
            h.delete();
        }
    }

    public void playerStart(IHubParkourPlayer p) {
        players.add(p);
        for (ClickableSign sign : CacheManager.getSigns().values()) {
            if (sign.getParkour().equals(this)) {
                sign.refresh();
            }
        }
    }

    public void playerEnd(IHubParkourPlayer p) {
        players.remove(p);
        for (ClickableSign sign : CacheManager.getSigns().values()) {
            if (sign.getParkour().equals(this)) {
                sign.refresh();
            }
        }
    }

    public Checkpoint getCheckpoint(int checkpointNo) {
        for (Checkpoint cp : checkpoints) {
            if (cp.getCheckpointNo() == checkpointNo) {
                return cp;
            }
        }
        return null;
    }

    public void addHologram(ILeaderboardHologram hologram) {
        leaderboardHolograms.add(hologram);
    }

    public void removeHologram(ILeaderboardHologram hologram) {
        leaderboardHolograms.remove(hologram);
    }

    public List<ILeaderboardHologram> getLeaderboards() {
        return leaderboardHolograms;
    }

    public void setName(String name) {
        this.name = name;
        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setName(id, name);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
        new BukkitRunnable(){
            @Override
            public void run() {
                if (HubParkour.isHolograms()) {
                    generateHolograms();
                    for (ILeaderboardHologram hologram : leaderboardHolograms) {
                        hologram.refresh();
                    }
                }
            }
        }.runTask(HubParkour.getInstance());
    }

    public void setEndCommand(String endCommand) {
        this.endCommand = endCommand;
        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setEndCommand(id, endCommand);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }

    public void setCheckpointCommand(String checkpointCommand) {
        this.checkpointCommand = checkpointCommand;
        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setCheckpointCommand(id, checkpointCommand);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }

    public void setStartPoint(StartPoint point) {
        CacheManager.removePlate(this.start);
        this.start = point;
        CacheManager.addPoint(point);
        this.start.setParkour(this);
        point.placeMaterial();
        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setStartPoint(id, point);
                HubParkour.getInstance().getDbManager().resetSplitTimes(id);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
        if (HubParkour.isHolograms()) {
            generateHolograms();
        }
    }

    public void setEndPoint(EndPoint point) {
        CacheManager.removePlate(this.endPoint);
        this.endPoint = point;
        CacheManager.addPoint(point);
        this.endPoint.setParkour(this);
        point.placeMaterial();
        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setEndPoint(id, point);
                HubParkour.getInstance().getDbManager().resetSplitTimes(id);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
        if (HubParkour.isHolograms()) {
            generateHolograms();
        }
    }

    public void setRestartPoint(RestartPoint point) {
        CacheManager.removeRestartPoint(this.restartPoint);
        this.restartPoint = point;
        this.restartPoint.setParkour(this);
        CacheManager.addRestartPoint(point);
        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setRestartPoint(id, point);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }

    public void addCheckpoint(Checkpoint point, int checkNo) {
        point.setParkour(this);
        CacheManager.addPoint(point);
        List<Checkpoint> checkpoints = new ArrayList<>(this.checkpoints);
        this.checkpoints.clear();
        for (Checkpoint checkpoint : checkpoints) {
            if (checkpoint.getCheckpointNo() >= checkNo) {
                if (checkpoint.getCheckpointNo() == checkNo) {
                    this.checkpoints.add(point);
                }
                checkpoint.setCheckpointNo(checkpoint.getCheckpointNo() + 1);
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        HubParkour.getInstance().getDbManager().updateCheckpointNumber(id, checkpoint);
                    }
                }.runTaskAsynchronously(HubParkour.getInstance());
            }
            this.checkpoints.add(checkpoint);
        }
        if (!this.checkpoints.contains(point)) {
            this.checkpoints.add(point);
        }

        point.placeMaterial();

        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().addCheckpoint(id, point);
                HubParkour.getInstance().getDbManager().resetSplitTimes(id);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());

        if (HubParkour.isHolograms()) {
            generateHolograms();
        }
    }

    public void deleteCheckpoint(Checkpoint point) {
        new BukkitRunnable(){
            @Override
            public void run() {
                CacheManager.removePlate(point);
            }
        }.runTask(HubParkour.getInstance());
        this.checkpoints.remove(point);
        for (Checkpoint checkpoint : checkpoints) {
            if (checkpoint.getCheckpointNo() > point.getCheckpointNo()) {
                checkpoint.setCheckpointNo(checkpoint.getCheckpointNo() - 1);
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        HubParkour.getInstance().getDbManager().updateCheckpointNumber(id, checkpoint);
                    }
                }.runTaskAsynchronously(HubParkour.getInstance());
            }
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().deleteCheckpoint(id, point);
                HubParkour.getInstance().getDbManager().resetSplitTimes(id);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());

        if (HubParkour.isHolograms()) {
            generateHolograms();
        }
    }

    public void setBorders(List<BorderPoint> borderPoints) {
        this.borderPoints.clear();
        this.borderPoints.addAll(borderPoints);

        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setBorders(id, borderPoints);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }

    public List<BorderPoint> getBorders() {
        return borderPoints;
    }

    public int getRewardCooldown() {
        return rewardCooldown;
    }

    @Override
    public UUID getServer() {
        return server;
    }

    public void setRewardCooldown(int rewardCooldown) {
        this.rewardCooldown = rewardCooldown;
        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setRewardCooldown(id, rewardCooldown);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }
}
