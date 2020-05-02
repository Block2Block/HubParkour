package me.block2block.hubparkour.api.plates;

import org.bukkit.Location;

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
