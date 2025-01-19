package me.block2block.hubparkour.api.gui;

import me.block2block.hubparkour.api.HubParkourAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class GUIItem {

    private final ItemStack item;

    public GUIItem(Material material, String name) {
        this(material, name, 1);
    }

    public GUIItem(Material material, String name, int amount) {
        this(material, name, amount, null);
    }

    public GUIItem(Material material, String name, int amount, List<String> lore) {
        this(material, name, amount, lore, (short) 0);
    }

    public GUIItem(Material material, String name, int amount, List<String> lore, short data) {
        this(material, name, amount, lore, data, false);
    }

    /**
     * Constructs a new GUI item with the specified properties.
     *
     * @param material the material of the item
     * @param name the display name of the item, may be null
     * @param amount the quantity of the item
     * @param lore the lore (description) for the item, may be null
     * @param data the data value of the item
     * @param glowing whether the item has a glowing effect
     */
    public GUIItem(Material material, String name, int amount, List<String> lore, short data, boolean glowing) {
        ItemStack item = new ItemStack(material, amount, data);
        ItemMeta im = item.getItemMeta();

        if (HubParkourAPI.isPre1_13()) {
            im.spigot().setUnbreakable(true);
        } else {
            im.setUnbreakable(true);
        }

        im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        im.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        if (name != null) {
            im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        }
        if (lore != null) {
            im.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
        }
        if (glowing) {
            im.addEnchant(Enchantment.DURABILITY, 1, true);
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(im);

        this.item = item;
    }

    /**
     * Retrieves the underlying ItemStack associated with this GUIItem.
     *
     * @return the ItemStack object representing the item.
     */
    public ItemStack getItemStack() {
        return item;
    }
}
