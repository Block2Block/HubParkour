package me.block2block.hubparkour.api.gui;

import me.block2block.hubparkour.api.HubParkourAPI;
import me.block2block.hubparkour.api.gui.exception.InvalidColumnException;
import me.block2block.hubparkour.api.gui.exception.InvalidRowException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a graphical user interface (GUI) that can be displayed to a player in a Minecraft environment.
 * This abstract class handles the creation, management, and interaction with GUI items and inventory slots.
 * <p>
 * This GUI framework relies on an implementing listener that handles inventory clicks, closes and opens.
 */
public abstract class GUI {

    private final int rows;
    private final String name;
    private final Map<Integer, GUIRow> inventory;
    private final boolean cancelEvent;
    protected final Player player;
    private Inventory inv;

    /**
     * Constructs a new GUI instance with the specified parameters.
     * The GUI is designed to represent a graphical interface for players, comprised of rows, columns, and optional
     * event cancellation behavior. Rows must be within the range of 0 to 5 inclusive.
     *
     * @param player The player who will interact with the GUI.
     * @param name The display name of the GUI.
     * @param rows The number of rows in the GUI, which must be between 0 and 5 inclusive.
     * @param cancelEvent Determines whether the click events in the GUI will be cancelled.
     * @throws InvalidRowException If the number of rows is outside the valid range (0 to 5).
     */
    public GUI(Player player, String name, int rows, boolean cancelEvent) {
        this.player = player;
        this.name = name;
        this.cancelEvent = cancelEvent;
        if (rows > 5 || rows < 0) {
            throw new InvalidRowException("row must be between 0 and 5, gave: " + rows);
        }

        this.rows = rows;
        inventory = new HashMap<>();
        for (int i = 0;i <= rows;i++) {
            inventory.put(i, new GUIRow(i));
        }
    }

    /**
     * Sets a {@link GUIItem} at the specified row and column in the GUI.
     * The row and column indices must be within their respective valid ranges.
     * This will not update the GUI if it is already open.
     *
     * @param row The row index where the item will be placed. Must be between 0 and 5 inclusive.
     * @param column The column index where the item will be placed. Must be between 0 and 8 inclusive.
     * @param item The {@link GUIItem} to be placed at the specified position. Can be null to clear the slot.
     * @throws InvalidRowException If the row index is not within the range 0-5.
     * @throws InvalidColumnException If the column index is not within the range 0-8.
     */
    public void setItem(int row, int column, GUIItem item) {
        if (row > 5 || row < 0 || row > rows) {
            throw new InvalidRowException("row must be between 0 and 5, gave:" + row);
        }

        if (column > 8 || column < 0) {
            throw new InvalidColumnException("column must be between 0 and 8, gave:" + column);
        }

        inventory.get(row).setItem(column, item);
    }

    /**
     * Updates the item in the specified row and column of the GUI. If the provided item is null, the slot in the
     * inventory is cleared. Otherwise, the item is set in the specified location within the inventory.
     * This will update the GUI if its open.
     *
     * @param row The row index to update. Must be between 0 and 5 inclusive.
     * @param column The column index to update. Must be between 0 and 8 inclusive.
     * @param item The {@link GUIItem} to set at the specified position. If null, the corresponding slot
     *             in the inventory will be cleared.
     */
    public void updateItem(int row, int column, GUIItem item) {
        setItem(row, column, item);
        if (item == null) {
            inv.clear((row * 9) + column);
        } else {
            inv.setItem((row * 9) + column, item.getItemStack());
        }
    }

    /**
     * Opens the GUI for the player associated with this instance, initializing its content based on the provided configuration.
     * If the player already has a GUI open, it will close the existing one before opening the new GUI.
     * <p>
     * The method creates an inventory with dimensions determined by the rows and columns defined in this instance.
     * Each slot of the inventory is populated with items from the internal inventory data structure,
     * retaining the layout specified during the GUI's construction or modification.
     * <p>
     * Once the inventory is prepared, it is displayed to the player and registered for tracking in the
     * {@link HubParkourAPI}. If the player's open inventory is null after attempting to open, the method exits early.
     */
    public void open() {
        if (HubParkourAPI.getGUI(player) != null) {
            HubParkourAPI.closeGUI(player);
        }
        Inventory inventory = Bukkit.createInventory(null, (rows + 1) * 9, ChatColor.translateAlternateColorCodes('&', name));
        for (int row = 0;row <= rows;row++) {
            for (int column = 0;column <=8;column++) {
                if (this.inventory.get(row).getItem(column) != null) {
                    inventory.setItem((row * 9) + column, this.inventory.get(row).getItem(column).getItemStack());
                }
            }
        }
        player.openInventory(inventory);
        inv = inventory;
        if (player.getOpenInventory() == null) {
            return;
        }
        HubParkourAPI.openGUI(player, this);
    }

    /**
     * Calculates the total size of the GUI's inventory based on the number of rows.
     * The size is determined as the product of the rows and 9, where 9 represents
     * the number of columns per row in the GUI.
     *
     * @return The total number of slots in the GUI, calculated as rows * 9.
     */
    public int getSize() {
        return rows * 9;
    }

    /**
     * Abstract method invoked when a click action occurs within the GUI at a specified row and column.
     * This method is intended to handle specific user interactions with items present in the GUI.
     * This is automatically called by the plugin.
     *
     * @param row The row index of the clicked slot. Must be between 0 and 5 inclusive.
     * @param column The column index of the clicked slot. Must be between 0 and 8 inclusive.
     * @param item The ItemStack present in the clicked slot. Can be null if the slot is empty.
     * @param clickType The type of click action performed (e.g., left-click, right-click, shift-click).
     */
    public abstract void onClick(int row, int column, ItemStack item, ClickType clickType);

    /**
     * Determines whether events, such as clicks, within the GUI should be cancelled.
     *
     * @return true if GUI events should be cancelled; false otherwise.
     */
    public boolean cancelEvent() {
        return cancelEvent;
    }

    public void border(String name, List<String> lore) {
        GUIItem item;

        if (HubParkourAPI.isPre1_13()) {
            item = new GUIItem(Material.getMaterial("STAINED_GLASS_PANE"), name, 1, lore, (short) 7);
        } else {
            item = new GUIItem(Material.GRAY_STAINED_GLASS_PANE, name, 1, lore);
        }

        for (int i = 0; i <= 8; i++) {
            if (i <= rows) {
                this.setItem(i, 0, item);
                this.setItem(i, 8, item);
            }
            this.setItem(0, i, item);
            this.setItem(rows, i, item);
        }
    }

    /**
     * Checks if the GUI is currently open.
     *
     * @return true if the GUI is marked as open.
     */
    public boolean isOpen() {
        return inv != null;
    }

}
