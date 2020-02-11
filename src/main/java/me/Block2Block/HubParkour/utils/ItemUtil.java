package me.block2block.hubparkour.utils;

import me.block2block.hubparkour.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class ItemUtil {

    public static ItemStack ci(Material type, String name, int amount, String lore, short data, String skullName) {
        ItemStack is = new ItemStack(type, amount, data);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(Main.c(false, name));
        if (lore != null) {
            im.setLore(Arrays.asList(Main.c(false, lore).split(";")));
        }
        if (skullName != null) {
            SkullMeta sm = (SkullMeta) im;
            sm.setOwner(skullName);
            im = sm;
        }
        im.setUnbreakable(true);
        im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        is.setItemMeta(im);
        return is;
    }
    public static ItemStack ci(Material type, String name, int amount, String lore, short data) {
        return ci(type, name, amount, lore, data, null);
    }
    public static ItemStack ci(Material type, String name, int amount, String lore) {
        return ci(type, name, amount, lore, (short)0);
    }
    public static ItemStack ci(Material type, String name, int amount) {
        return ci(type, name, amount, null);
    }
    public static ItemStack ci(Material type, String name) {
        return ci(type, name, 1);
    }
    public static ItemStack ci(Material type) {
        return ci(type, "");
    }

}
