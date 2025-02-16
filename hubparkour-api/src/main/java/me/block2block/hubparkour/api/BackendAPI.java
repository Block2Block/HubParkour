package me.block2block.hubparkour.api;

import me.block2block.hubparkour.api.gui.GUI;
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

    /**
     * Retrieves a GUI instance associated with the provided player.
     *
     * @param player the player for whom the GUI is being retrieved.
     *               Must not be null.
     * @return the GUI instance corresponding to the specified player, or null if no GUI is associated.
     * @throws NullPointerException if the provided player is null.
     */
    public abstract GUI getGUI(Player player);

    /**
     * Closes the GUI associated with the specified player, if any.
     *
     * @param player the player whose GUI should be closed. Must not be null.
     * @throws NullPointerException if the provided player is null.
     */
    public abstract void closeGUI(Player player);

    /**
     * Opens a specified GUI for the given player. This method associates the player with
     * the provided GUI instance and displays the interface.
     *
     * @param player the player for whom the GUI will be opened. Must not be null.
     * @param gui the GUI instance to be opened for the player. Must not be null.
     * @throws NullPointerException if either the player or GUI is null.
     */
    public abstract void openGUI(Player player, GUI gui);

    /**
     * Determines whether the server is before 1.13.
     *
     * @return true if the server is 1.12 or below.
     */
    public abstract boolean isPre1_13();

    /**
     * Determines whether the server version is 1.14 or newer.
     *
     * @return true if the server version is 1.14 or above
     */
    public abstract boolean isPost1_14();

}
