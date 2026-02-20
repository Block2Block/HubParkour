package me.block2block.hubparkour.api;

import me.block2block.hubparkour.api.gui.GUI;
import me.block2block.hubparkour.api.hologram.IHologram;
import org.bukkit.Location;
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

    /**
     * Retrieves the GUI instance associated with the provided player.
     *
     * @param player the player for whom the GUI is being retrieved. Must not be null.
     * @return the GUI instance corresponding to the specified player, or null if no GUI is associated.
     * @throws NullPointerException if the provided player is null.
     */
    public static GUI getGUI(Player player) {
        return BackendAPI.getImplementation().getGUI(player);
    }

    /**
     * Closes the GUI associated with the specified player, if any.
     *
     * @param player the player whose GUI should be closed. Must not be null.
     * @throws NullPointerException if the provided player is null.
     */
    public static void closeGUI(Player player) {
        BackendAPI.getImplementation().closeGUI(player);
    }

    /**
     * Opens the specified GUI for the given player.
     *
     * @param player the player for whom the GUI will be opened. Must not be null.
     * @param gui the GUI instance to open for the specified player. Must not be null.
     * @throws NullPointerException if either the player or the GUI is null.
     */
    public static void openGUI(Player player, GUI gui) {
        BackendAPI.getImplementation().openGUI(player, gui);
    }

    /**
     * Determines whether the server version is before 1.13.
     *
     * @return true if the server version is 1.12 or lower; false otherwise.
     */
    public static boolean isPre1_13() {
        return BackendAPI.getImplementation().isPre1_13();
    }

    /**
     * Determines whether the server version is 1.14 or newer.
     *
     * @return true if the server version is 1.14 or above, false otherwise.
     */
    public static boolean isPost1_14() {
        return BackendAPI.getImplementation().isPost1_14();
    }

    /**
     * Creates a new hologram associated with the specified parkour, name, and location.
     *
     * @param parkour the {@code IParkour} instance to associate with the hologram. Must not be null.
     * @param name the name of the hologram. Must not be null.
     * @param location the {@code Location} where the hologram will be displayed. Must not be null.
     * @return the created {@code IHologram} instance.
     * @throws NullPointerException if {@code parkour}, {@code name}, or {@code location} is null.
     */
    public static IHologram createHologram(IParkour parkour, String name, Location location) {
        return BackendAPI.getImplementation().createHologram(parkour, name, location);
    }

}
