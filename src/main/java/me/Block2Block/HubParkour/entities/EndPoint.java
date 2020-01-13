package me.block2block.hubparkour.entities;

import org.bukkit.Location;

public class EndPoint extends PressurePlate {
    public EndPoint(Location location) {
        super(location);
    }

    @Override
    public int getType() {
        return 1;
    }
}
