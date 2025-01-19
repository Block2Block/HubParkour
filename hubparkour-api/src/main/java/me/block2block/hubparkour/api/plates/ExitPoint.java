package me.block2block.hubparkour.api.plates;

import org.bukkit.Location;

/**
 * Pressure plate to symbolize where the player should be teleported to after parkour ends.
 * Handles multiple end types.
 */
public class ExitPoint extends PressurePlate {

    public ExitPoint(Location location) {
        super(location, null);
    }

    @Override
    public int getType() {
        return 5;
    }

}
