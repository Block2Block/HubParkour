package me.block2block.hubparkour.api.plates;

import org.bukkit.Location;

/**
 * Pressure plate to symbolize the restart/respawn location of the parkour.
 * Used when a player "resets" their run.
 */
public class RestartPoint extends PressurePlate {

    @SuppressWarnings("unused")
    public RestartPoint(Location location) {
        super(location, null);
    }

    @Override
    public int getType() {
        return 2;
    }
}
