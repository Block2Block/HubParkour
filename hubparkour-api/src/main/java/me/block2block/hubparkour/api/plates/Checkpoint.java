package me.block2block.hubparkour.api.plates;

import org.bukkit.Location;

/**
 * Pressure plate to symbolize the parkour checkpoints.
 */
public class Checkpoint extends PressurePlate {

    private int checkpointNo;

    @SuppressWarnings("unused")
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

    public void setCheckpointNo(int checkpointNo) {
        this.checkpointNo = checkpointNo;
    }
}
