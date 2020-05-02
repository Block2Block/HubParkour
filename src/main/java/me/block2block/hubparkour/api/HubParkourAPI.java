package me.block2block.hubparkour.api;

import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.entity.Player;

/**
 * The HubParkour API.
 */
public class HubParkourAPI {

    /**
     * Use to check if a player is in a parkour.
     * @param player the player to check.
     * @return if the player is in a parkour.
     * @throws NullPointerException if <code>player</code> is null.
     */
    @SuppressWarnings("unused")
    public static boolean isInParkour(Player player) throws NullPointerException {
        if (player == null) {
            throw new NullPointerException("player cannot be null");
        }

        return CacheManager.isParkour(player);
    }


    /**
     * Get a player that is currently in parkour.
     * @param player The player to retrieve.
     * @return the player, or if the player is not in a parkour, null.
     * @throws NullPointerException if <code>player</code> is null.
     */
    @SuppressWarnings("unused")
    public static IHubParkourPlayer getPlayer(Player player) throws NullPointerException {
        if (player == null) {
            throw new NullPointerException("Player cannot be null");
        }
        return CacheManager.getPlayer(player);
    }

    /**
     * Get a specific parkour.
     * @param id the id of the parkour.
     * @return The parkour, if it does not exist, null.
     */
    @SuppressWarnings("unused")
    public static IParkour getParkour(int id) {
        return CacheManager.getParkour(id);
    }

    /**
     * Get a specific parkour.
     * @param name the name of the parkour
     * @return The parkour, if it does not exist, null.
     * @throws NullPointerException if <code>name</code> is null.
     */
    @SuppressWarnings("unused")
    public static IParkour getParkour(String name) throws NullPointerException {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        return CacheManager.getParkour(name);
    }

}
