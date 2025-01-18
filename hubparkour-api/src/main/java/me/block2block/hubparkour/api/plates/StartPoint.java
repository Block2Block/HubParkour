package me.block2block.hubparkour.api.plates;

import org.bukkit.Location;

/**
 * Pressure plate to symbolize the starting point of the parkour.
 */
public class StartPoint extends PressurePlate {

    @SuppressWarnings("unused")
    public StartPoint(Location location) {
        super(location);
    }

    @Override
    public int getType() {
        return 0;
    }
}
