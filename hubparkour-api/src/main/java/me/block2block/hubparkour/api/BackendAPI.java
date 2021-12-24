package me.block2block.hubparkour.api;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * The backend API class which the API uses to execute methods.
 */
public abstract class BackendAPI {

    private static BackendAPI implementation;

    public static BackendAPI getImplementation() {
        if (implementation == null) {
            throw new IllegalStateException("No API implementation set. Is HubParkour enabled?");
        } else {
            return implementation;
        }
    }

    public static void setImplementation(BackendAPI implementation) {
        BackendAPI.implementation = implementation;
    }

    /**
     * Use to check if a player is in a parkour.
     * @param player the player to check.
     * @return if the player is in a parkour.
     * @throws NullPointerException if <code>player</code> is null.
     */
    @SuppressWarnings("unused")
    public abstract boolean isInParkour(Player player) throws NullPointerException;


    /**
     * Get a player that is currently in parkour.
     * @param player The player to retrieve.
     * @return the player, or if the player is not in a parkour, null.
     * @throws NullPointerException if <code>player</code> is null.
     */
    @SuppressWarnings("unused")
    public abstract IHubParkourPlayer getPlayer(Player player) throws NullPointerException;

    /**
     * Get a specific parkour.
     * @param id the id of the parkour.
     * @return The parkour, if it does not exist, null.
     */
    @SuppressWarnings("unused")
    public abstract IParkour getParkour(int id);

    /**
     * Get a specific parkour.
     * @param name the name of the parkour
     * @return The parkour, if it does not exist, null.
     * @throws NullPointerException if <code>name</code> is null.
     */
    @SuppressWarnings("unused")
    public abstract IParkour getParkour(String name) throws NullPointerException;

    /**
     * Gets the material type of a specific pressure plate type.
     * @param type the type of pressure plate.
     * @return the material.
     */
    public abstract Material getPressurePlateType(int type);

    /**
     * Gets the ItemStack of a specific item type.
     * @param type the type of item.
     * @return the ItemStack.
     */
    public abstract ItemStack getItem(int type);

}
