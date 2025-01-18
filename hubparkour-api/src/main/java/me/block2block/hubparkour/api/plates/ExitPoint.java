package me.block2block.hubparkour.api.plates;

import org.bukkit.Location;

public class ExitPoint extends PressurePlate {

    public ExitPoint(Location location) {
        super(location);
    }

    @Override
    public int getType() {
        return 5;
    }

}
