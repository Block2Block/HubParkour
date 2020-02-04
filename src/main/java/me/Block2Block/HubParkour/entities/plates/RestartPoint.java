package me.block2block.hubparkour.entities.plates;

import org.bukkit.Location;

public class RestartPoint extends PressurePlate {

    public RestartPoint(Location location) {
        super(location);
    }

    @Override
    public int getType() {
        return 2;
    }
}
