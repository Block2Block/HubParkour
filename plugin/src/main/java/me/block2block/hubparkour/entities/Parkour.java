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
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Parkour implements IParkour {

    private final int id;
    private UUID server;
    private String name;
    private StartPoint start;
    private EndPoint endPoint;
    private ExitPoint exitPoint = null;
    private final List<Checkpoint> checkpoints;
    private final List<IHubParkourPlayer> players;
    private List<String> globalCheckpointCommands;
    private List<String> endCommands;
    private RestartPoint restartPoint;
    private int rewardCooldown;
    private final Map<PressurePlate, Hologram> holograms = new HashMap<>();
    private final List<ILeaderboardHologram> leaderboardHolograms = new ArrayList<>();
    private final List<BorderPoint> borderPoints;

    private Material material;
    private short data;
    private int customModelData;

    @SuppressWarnings("unused")
    public Parkour(int id, UUID server, String name, StartPoint start, EndPoint end, ExitPoint exit, List<Checkpoint> checkpoints, RestartPoint restartPoint, List<BorderPoint> borderPoints, List<String> globalCheckpointCommands, List<String> endCommands, int rewardCooldown, Material material, short data, int customModelData) {
        this.id = id;
        this.server = server;
        this.start = start;
        this.start.setParkour(this);
        this.endPoint = end;
        this.endPoint.setParkour(this);
        if (exit != null) {
            this.exitPoint = exit;
            this.exitPoint.setParkour(this);
        }
        this.checkpoints = checkpoints;
        for (Checkpoint checkpoint : checkpoints) {
            checkpoint.setParkour(this);
        }
        this.players = new ArrayList<>();
        this.endCommands = endCommands;
        this.globalCheckpointCommands = globalCheckpointCommands;
        this.name = name;
        this.restartPoint = restartPoint;
        this.borderPoints = borderPoints;
        for (BorderPoint borderPoint : borderPoints) {
            borderPoint.setParkour(this);
        }
        this.rewardCooldown = rewardCooldown;

        this.material = material;
        this.data = data;
        this.customModelData = customModelData;
    }

    @SuppressWarnings("unused")
    public Parkour(Parkour parkour, int id) {
        this.id = id;
        this.start = parkour.getStart();
        this.start.setParkour(this);
        this.endPoint = parkour.getEndPoint();
        this.endPoint.setParkour(this);
        if (parkour.getExitPoint() != null) {
            this.exitPoint = parkour.getExitPoint();
            this.exitPoint.setParkour(this);
        }
        this.checkpoints = parkour.getCheckpoints();
        for (Checkpoint checkpoint : checkpoints) {
            checkpoint.setParkour(this);
        }
        this.players = new ArrayList<>();
        this.endCommands = parkour.getEndCommands();
        this.globalCheckpointCommands = parkour.getGlobalCheckpointCommands();
        this.name = parkour.getName();
        this.restartPoint = parkour.getRestartPoint();
        this.borderPoints = parkour.getBorders();
        this.rewardCooldown = parkour.getRewardCooldown();
        this.material = parkour.getItemMaterial();
        this.data = parkour.getItemData();
        this.customModelData = parkour.getCustomModelData();
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

    public ExitPoint getExitPoint() { return exitPoint; }

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

    public List<String> getGlobalCheckpointCommands() {
        return globalCheckpointCommands;
    }

    public List<String> getEndCommands() {
        return endCommands;
    }

    public void setGlobalCheckpointCommands(List<String> globalCheckpointCommands) {
        this.globalCheckpointCommands = globalCheckpointCommands;

        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setGlobalCheckpointCommands(id, globalCheckpointCommands);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }

    public void setEndCommands(List<String> endCommands) {
        this.endCommands = endCommands;

        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setEndCommands(id, endCommands);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }

    public List<PressurePlate> getAllPoints() {
        List<PressurePlate> pressurePlates = new ArrayList<>(checkpoints);
        pressurePlates.add(endPoint);
        pressurePlates.add(start);
        if (exitPoint != null) {
            pressurePlates.add(exitPoint);
        }
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
                    continue;
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
        for (PressurePlate p : getAllPoints()) {
            switch (p.getType()) {
                case 0:
                    if (!ConfigUtil.getBoolean("Settings.Holograms.Start", true)) {
                        continue;
                    }
                    break;
                case 1:
                    if (!ConfigUtil.getBoolean("Settings.Holograms.End", true)) {
                        continue;
                    }
                    break;
                case 3:
                    if (!ConfigUtil.getBoolean("Settings.Holograms.Checkpoint", true)) {
                        continue;
                    }
                    break;
                default:
                    continue;
            }

            Hologram hologram = DHAPI.getHologram("hp_" + id + "-" + p.getType() + ((p instanceof Checkpoint)?"-" + ((Checkpoint) p).getCheckpointNo():""));
            if (hologram != null) {
                hologram.delete();
            }
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

        if (getExitPoint() != null) {
            HubParkour.getInstance().getLogger().info("Teleporting to exit point");
            Location location = getExitPoint().getLocation().clone();
            location.setY(location.getY() + 0.5);
            location.setZ(location.getZ() + 0.5);
            location.setX(location.getX() + 0.5);
            p.getPlayer().setVelocity(new Vector(0, 0, 0));
            p.getPlayer().teleport(location);
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

    public void deleteExitPoint() {
        CacheManager.removePlate(this.exitPoint);
        this.exitPoint = null;
        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().deleteExitPoint(id);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }

    public void setExitPoint(ExitPoint point, boolean alreadyExists) {
        if (alreadyExists) {
            CacheManager.removePlate(this.exitPoint);
        }
        this.exitPoint = point;
        CacheManager.addPoint(point);
        this.exitPoint.setParkour(this);
        point.placeMaterial();
        if (alreadyExists) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    HubParkour.getInstance().getDbManager().updateExitPoint(id, point);
                }
            }.runTaskAsynchronously(HubParkour.getInstance());
        } else {
            new BukkitRunnable(){
                @Override
                public void run() {
                    HubParkour.getInstance().getDbManager().setExitPoint(id, point);
                }
            }.runTaskAsynchronously(HubParkour.getInstance());
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

    public Material getItemMaterial() {
        return material;
    }

    public short getItemData() {
        return data;
    }

    @Override
    public int getCustomModelData() {
        return customModelData;
    }

    @Override
    public void setItem(Material material, short data, int customModelData) {
        this.material = material;
        this.data = data;
        this.customModelData = customModelData;
        new BukkitRunnable(){
            @Override
            public void run() {
                HubParkour.getInstance().getDbManager().setItem(id, material, data, customModelData);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }
}

