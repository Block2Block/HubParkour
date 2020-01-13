package me.block2block.hubparkour.entities;

import org.bukkit.Location;

public abstract class PressurePlate {

    protected Location location;
    protected Parkour parkour;

    public PressurePlate(Location location) {
        this.location =  location;
    }

    public void setParkour(Parkour parkour) {
        this.parkour = parkour;
    }

    public Location getLocation() {
        return location;
    }

    public abstract int getType();
}
