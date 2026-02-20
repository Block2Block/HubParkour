package me.block2block.hubparkour.entities.hologram;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.HologramData;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.hologram.IHologram;
import org.bukkit.Location;

import java.util.List;

public class FHHologram implements IHologram {

    private boolean removed;

    private final IParkour parkour;
    private final String name;
    private Location location;

    private Hologram hologram;

    public FHHologram(IParkour parkour, String name, Location location) {
        TextHologramData data = new TextHologramData(name, location);
        data.setPersistent(false);

        HologramManager manager = FancyHologramsPlugin.get().getHologramManager();
        hologram = manager.getHologram(name).orElse(null);
        if (hologram == null) {
            hologram = manager.create(data);
        }
        manager.addHologram(hologram);


        this.parkour = parkour;
        this.name = name;
        this.location = location;

        this.removed = false;
    }

    @Override
    public void remove() {
        if (removed) {
            return;
        }
        hologram.deleteHologram();
        this.removed = true;
        FHHologramFactory.getInstance().removeHologram(this);
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
        HologramData data = hologram.getData();
        if (data instanceof TextHologramData) {
            ((TextHologramData)data).setText(lines);
        }
        hologram.forceUpdate();
    }

    @Override
    public void setLocation(Location location) {
        if (removed) {
            return;
        }
        HologramData data = hologram.getData();
        data.setLocation(location);
        hologram.forceUpdate();

        this.location = location;
    }

    @Override
    public String getName() {
        return name;
    }
}
