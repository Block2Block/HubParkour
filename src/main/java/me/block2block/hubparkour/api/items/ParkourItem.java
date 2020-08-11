package me.block2block.hubparkour.api.items;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a generic parkour item.
 *
 * <strong>WARNING:</strong> never create ParkourItem objects. Always extend this class.
 */
public abstract class ParkourItem {

    protected final IHubParkourPlayer player;
    protected ItemStack originalItem;
    protected final int slot;
    protected final ItemStack item;

    public ParkourItem(IHubParkourPlayer player, int slot) {
        this.player = player;
        this.slot = slot;
        item = CacheManager.getItems().get(this.getType());
    }

    public abstract int getType();

    public void giveItem() {
        if (slot >= 0 && slot < 9) {
            originalItem = player.getPlayer().getInventory().getItem(slot);
            if (originalItem == null) {
                originalItem = new ItemStack(Material.AIR);
            }
            player.getPlayer().getInventory().setItem(slot, item);
        }
    }

    public void removeItem() {
        if (slot >= 0 && slot < 9 && originalItem != null) {
            player.getPlayer().getInventory().setItem(slot, originalItem);
            originalItem = null;
        }
    }

}
