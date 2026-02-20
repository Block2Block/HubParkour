package me.block2block.hubparkour.entities.hologram;

import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.hologram.HologramFactory;
import me.block2block.hubparkour.api.hologram.IHologram;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FHHologramFactory extends HologramFactory {

    private final Map<Integer, List<IHologram>> holograms = new HashMap<>();
    private static FHHologramFactory instance;

    public static FHHologramFactory getInstance() {
        return instance;
    }

    public FHHologramFactory() {
        instance = this;
    }

    @Override
    public IHologram createHologram(IParkour parkour, String name, Location location) {
        IHologram hologram = new FHHologram(parkour, name, location);
        holograms.putIfAbsent(parkour.getId(), new ArrayList<>());
        holograms.get(parkour.getId()).add(hologram);
        return hologram;
    }

    public void removeHologram(IHologram hologram) {
        holograms.get(hologram.getParkour().getId()).remove(hologram);
        if (holograms.get(hologram.getParkour().getId()).isEmpty()) {
            holograms.remove(hologram.getParkour().getId());
        }
    }

    @Override
    public void removeHologramsForParkour(IParkour parkour) {
        if (holograms.containsKey(parkour.getId())) {
            holograms.remove(parkour.getId()).forEach(IHologram::remove);
        }
    }

}
