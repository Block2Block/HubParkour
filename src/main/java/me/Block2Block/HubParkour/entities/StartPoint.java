package me.block2block.hubparkour.entities;

import org.bukkit.Location;

public class StartPoint extends PressurePlate {

    public StartPoint(Location location) {
        super(location);
    }

    @Override
    public int getType() {
        return 0;
    }
}
