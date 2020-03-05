package me.block2block.hubparkour.entities.plates;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public abstract class PressurePlate {

    protected Location location;
    protected Parkour parkour;
    protected Material material;

    public PressurePlate(Location location) {
        this.location =  location;
        this.material = CacheManager.getTypes().get(this.getType());
    }

    public void setParkour(Parkour parkour) {
        this.parkour = parkour;
    }

    public Location getLocation() {
        return location;
    }

    public abstract int getType();

    public Material getMaterial() {
        return material;
    };

    public void placeMaterial() {
        if (material != Material.AIR) {
            if (location == null) {
                Bukkit.getLogger().warning("A location that one of your parkour points is in does not exist. Please delete the " + parkour.getName() + " parkour and set it up again.");
                return;
            }
            if (location.getWorld() == null) {
                Bukkit.getLogger().warning("A location that one of your parkour points is in does not exist. Please delete the " + parkour.getName() + " parkour and set it up again.");
                return;
            }
            location.getBlock().setType(material);
        }
    };

    public void removeMaterial() {
        location.getBlock().setType(Material.AIR);
    }

    public Parkour getParkour() {
        return parkour;
    }
}
