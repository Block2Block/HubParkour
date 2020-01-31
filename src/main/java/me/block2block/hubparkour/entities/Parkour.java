package me.block2block.hubparkour.entities;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import me.block2block.hubparkour.entities.plates.Checkpoint;
import me.block2block.hubparkour.entities.plates.EndPoint;
import me.block2block.hubparkour.entities.plates.PressurePlate;
import me.block2block.hubparkour.entities.plates.StartPoint;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class Parkour {

    private int id;
    private String name;
    private StartPoint start;
    private EndPoint endPoint;
    private List<Checkpoint> checkpoints;
    private List<HubParkourPlayer> players;
    private String checkpointCommand;
    private String endCommand;
    private Location restartPoint;
    private List<Hologram> holograms = new ArrayList<>();

    public Parkour(int id, String name, StartPoint start, EndPoint end, List<Checkpoint> checkpoints, Location restartPoint, String checkpointCommand, String endCommand) {
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

    public Location getRestartPoint() {
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
        return pressurePlates;
    }

    public void generateHolograms() {

    }

}
