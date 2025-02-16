package me.block2block.hubparkour.utils;

import me.block2block.hubparkour.HubParkour;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

@SuppressWarnings("deprecation")
public class ItemUtil {

    public static ItemStack ci(Material type, String name, int amount, String lore, short data, int customModelData, String skullName) {
        ItemStack is = new ItemStack(type, amount, data);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(HubParkour.c(false, name));
        if (lore != null) {
            im.setLore(Arrays.asList(HubParkour.c(false, lore).split(";")));
        }
        if (skullName != null) {
            SkullMeta sm = (SkullMeta) im;
            sm.setOwner(skullName);
            im = sm;
        }

        if (HubParkour.isPre1_13()) {
            im.spigot().setUnbreakable(true);
        } else {
            im.setUnbreakable(true);
        }

        if (HubParkour.isPost1_14()) {
            if (customModelData != -1) {
                im.setCustomModelData(customModelData);
            } else {
                im.setCustomModelData(null);
            }
        }


        im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        is.setItemMeta(im);
        return is;
    }

    public static ItemStack ci(Material type, String name, int amount, String lore, short data, int customModelData) {
        return ci(type, name, amount, lore, data, customModelData, null);
    }

    public static ItemStack ci(Material type, String name, int amount, String lore, short data) {
        return ci(type, name, amount, lore, data, -1, null);
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
    @SuppressWarnings("unused")
    public static ItemStack ci(Material type) {
        return ci(type, "");
    }

}
