package me.block2block.hubparkour.api;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
        return BackendAPI.getImplementation().isInParkour(player);
    }


    /**
     * Get a player that is currently in parkour.
     * @param player The player to retrieve.
     * @return the player, or if the player is not in a parkour, null.
     * @throws NullPointerException if <code>player</code> is null.
     */
    @SuppressWarnings("unused")
    public static IHubParkourPlayer getPlayer(Player player) throws NullPointerException {
        return BackendAPI.getImplementation().getPlayer(player);
    }

    /**
     * Get a specific parkour.
     * @param id the id of the parkour.
     * @return The parkour, if it does not exist, null.
     */
    @SuppressWarnings("unused")
    public static IParkour getParkour(int id) {
        return BackendAPI.getImplementation().getParkour(id);
    }

    /**
     * Get a specific parkour.
     * @param name the name of the parkour
     * @return The parkour, if it does not exist, null.
     * @throws NullPointerException if <code>name</code> is null.
     */
    @SuppressWarnings("unused")
    public static IParkour getParkour(String name) throws NullPointerException {
        return BackendAPI.getImplementation().getParkour(name);
    }

    /**
     * Gets the material type of a specific pressure plate type.
     * @param type the type of pressure plate.
     * @return the material.
     */
    @SuppressWarnings("unused")
    public static Material getPressurePlateType(int type) throws NullPointerException {
        return BackendAPI.getImplementation().getPressurePlateType(type);
    }

    /**
     * Gets the ItemStack of a specific item type.
     * @param type the type of item.
     * @return the ItemStack.
     */
    @SuppressWarnings("unused")
    public static ItemStack getItem(int type) throws NullPointerException {
        return BackendAPI.getImplementation().getItem(type);
    }

}
