package me.block2block.hubparkour.entities.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.hologram.IHologram;
import org.bukkit.Location;

import java.util.List;

public class DHHologram implements IHologram {

    private boolean removed;

    private final IParkour parkour;
    private final String name;
    private Location location;

    private Hologram hologram;

    public DHHologram(IParkour parkour, String name, Location location) {
        hologram = DHAPI.getHologram(name);
        if (hologram == null) {
            hologram = DHAPI.createHologram(name, location);
        } else {
            DHAPI.moveHologram(hologram, location);
        }

        this.name = name;
        this.parkour = parkour;
        this.location = location;

        this.removed = false;
    }

    @Override
    public void remove() {
        if (removed) {
            return;
        }
        if (hologram != null) {
            hologram.delete();
        }
        DHHologramFactory.getInstance().removeHologram(this);
        removed = true;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public IParkour getParkour() {
        return parkour;
    }

    @Override
    public void setLines(List<String> lines) {
        if (removed) {
            return;
        }
        DHAPI.setHologramLines(hologram, lines);
    }

    @Override
    public void setLocation(Location location) {
        if (removed) {
            return;
        }
        DHAPI.moveHologram(hologram, location);
        this.location = location;
    }

    @Override
    public String getName() {
        return name;
    }
}
