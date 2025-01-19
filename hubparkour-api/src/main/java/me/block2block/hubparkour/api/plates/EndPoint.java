package me.block2block.hubparkour.api.plates;

import org.bukkit.Location;

/**
 * Pressure plate to symbolize the end of the parkour.
 */
public class EndPoint extends PressurePlate {
    @SuppressWarnings("unused")
    public EndPoint(Location location) {
        super(location, null);
    }

    @Override
    public int getType() {
        return 1;
    }
}
