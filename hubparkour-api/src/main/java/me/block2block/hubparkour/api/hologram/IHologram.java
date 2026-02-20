package me.block2block.hubparkour.api.hologram;

import me.block2block.hubparkour.api.IParkour;
import org.bukkit.Location;

import java.util.List;

/**
 * Represents a generic Hologram.
 */
public interface IHologram {

    /**
     * Removes the hologram from the world.
     */
    void remove();

    /**
     * Gets the location of the hologram in the world.
     *
     * @return the location of the hologram.
     */
    Location getLocation();

    /**
     * Retrieves the parkour associated with this hologram.
     *
     * @return the instance of {@code IParkour} associated with this hologram,
     * or {@code null} if no parkour is linked.
     */
    IParkour getParkour();

    /**
     * Sets the lines of text to be displayed in the hologram.
     *
     * @param lines the list of strings representing the lines of text to display in the hologram.
     */
    void setLines(List<String> lines);

    /**
     * Sets the location of the hologram in the world.
     *
     * @param location the new location of the hologram.
     */
    void setLocation(Location location);

    /**
     * Get the internal name of the hologram.
     * @return the internal name of the hologram.
     */
    String getName();

}
