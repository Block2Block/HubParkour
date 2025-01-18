package me.block2block.hubparkour.api.plates;

import org.bukkit.Location;

/**
 * Pressure plates to symbolize the region of the parkour.
 */
public class BorderPoint extends PressurePlate {

    @SuppressWarnings("unused")
    public BorderPoint(Location location) {
        super(location);
    }

    @Override
    public int getType() {
        return 4;
    }
}
