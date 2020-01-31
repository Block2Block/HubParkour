package me.block2block.hubparkour.entities.plates;

import org.bukkit.Location;
import org.bukkit.Material;

public class Checkpoint extends PressurePlate {

    private int checkpointNo;

    public Checkpoint(Location location, int checkpointNo) {
        super(location);
        this.checkpointNo = checkpointNo;
    }

    public int getType() {
        return 3;
    }

    public int getCheckpointNo() {
        return checkpointNo;
    }
}
