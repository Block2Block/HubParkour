package me.block2block.hubparkour.api;


import org.bukkit.Location;

/**
 * Represents a leaderboard hologram.
 */
public interface ILeaderboardHologram {

    /**
     * Generate the hologram. Only ever call this once, unless <code>remove()</code> has been called since the last time it was generated.
     */
    void generate();

    /**
     * Remove the hologram from the world.
     */
    void remove();

    /**
     * Refresh the leaderboard statistics.
     */
    void refresh();

    /**
     * Get the location of the hologram.
     * @return the location of the hologram.
     */
    Location getLocation();

    /**
     * Get the parkour that this hologram belongs to.
     * @return the parkour this hologram belongs to, or <i>null</i> if this is an overall leaderboard hologram.
     */
    IParkour getParkour();

    /**
     * Get the ID of this hologram.
     * @return the ID of the hologram.
     */
    int getId();
}
