package me.block2block.hubparkour.api.hologram;

import me.block2block.hubparkour.api.IParkour;
import org.bukkit.Location;

public abstract class HologramFactory {

    public abstract IHologram createHologram(IParkour parkour, String name, Location location);

    public abstract void removeHologramsForParkour(IParkour parkour);

}
