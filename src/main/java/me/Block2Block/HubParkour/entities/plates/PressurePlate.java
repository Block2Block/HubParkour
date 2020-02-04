package me.block2block.hubparkour.entities.plates;

import me.block2block.hubparkour.entities.Parkour;
import org.bukkit.Location;
import org.bukkit.Material;

public abstract class PressurePlate {

    protected Location location;
    protected Parkour parkour;
    protected static Material material;

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

    public static Material getMaterial() {
        return material;
    };

    public void placeMaterial() {
        location.getBlock().setType(material);
    };

    public void removeMaterial() {
        location.getBlock().setType(Material.AIR);
    }

    public Parkour getParkour() {
        return parkour;
    }
}
