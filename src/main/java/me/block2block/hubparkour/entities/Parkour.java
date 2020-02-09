package me.block2block.hubparkour.entities;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.entities.plates.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parkour {

    private int id;
    private String name;
    private StartPoint start;
    private EndPoint endPoint;
    private List<Checkpoint> checkpoints;
    private List<HubParkourPlayer> players;
    private String checkpointCommand;
    private String endCommand;
    private RestartPoint restartPoint;
    private Map<PressurePlate, Hologram> holograms = new HashMap<>();

    public Parkour(int id, String name, StartPoint start, EndPoint end, List<Checkpoint> checkpoints, RestartPoint restartPoint, String checkpointCommand, String endCommand) {
        this.id = id;
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
    }

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
        return checkpoints;
    }

    public List<HubParkourPlayer> getPlayers() {
        return players;
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
        List<PressurePlate> pressurePlates = new ArrayList<>();
        pressurePlates.addAll(checkpoints);
        pressurePlates.add(endPoint);
        pressurePlates.add(start);
        pressurePlates.add(restartPoint);
        return pressurePlates;
    }

    public void generateHolograms() {
        for (PressurePlate p : getAllPoints()) {
            String configKey = "";
            switch (p.getType()) {
                case 0:
                    configKey = "Start";
                    break;
                case 1:
                    configKey = "End";
                    break;
                case 2:
                    continue;
                case 3:
                    configKey = "Checkpoint";
                    break;
                default:
            }
            Location l = p.getLocation().clone();
            l.setX(l.getX() + 0.5);
            l.setZ(l.getZ() + 0.5);
            l.setY(l.getY() + 2);
            Hologram hologram = HologramsAPI.createHologram(Main.getInstance(), l);
            int counter = 0;
            for (String s : Main.getInstance().getConfig().getStringList("Messages.Holograms." + configKey)) {
                TextLine textLine = hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', s.replace("{parkour-name}",name).replace("{checkpoint}",((p instanceof Checkpoint)?((Checkpoint)p).getCheckpointNo() + "":""))));
                holograms.put(p, hologram);
                counter++;
            }
        }

    }

    public void removeHolograms() {
        for (Hologram h : holograms.values()) {
            h.delete();
        }
    }

    public void playerStart(HubParkourPlayer p) {
        players.add(p);
    }

    public void playerEnd(HubParkourPlayer p) {
        players.remove(p);
    }

    public Checkpoint getCheckpoint(int checkpointNo) {
        for (Checkpoint cp : checkpoints) {
            if (cp.getCheckpointNo() == checkpointNo) {
                return cp;
            }
        }
        return null;
    }

}
