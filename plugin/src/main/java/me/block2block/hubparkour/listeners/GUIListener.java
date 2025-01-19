package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.gui.GUI;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class GUIListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (CacheManager.getGUI((Player) e.getPlayer()) != null) {
            CacheManager.closeGUI((Player) e.getPlayer());
            e.getPlayer().closeInventory();
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        GUI gui = CacheManager.getGUI((Player) e.getWhoClicked());
        if (gui != null) {
            if (e.getClickedInventory() instanceof PlayerInventory) {
                return;
            }
            if (e.getSlot() < 0 || e.getSlot() >= e.getInventory().getSize()) {
                return;
            }
            if (e.getInventory() == null) {
                return;
            }
            ItemStack itemStack = e.getClickedInventory().getItem(e.getSlot());
            if (e.getCursor() != null && itemStack == null) {
                itemStack = e.getCursor();
            }
            if (itemStack != null) {
                if (itemStack.getType() != Material.AIR) {
                    if (e.getInventory().getType() != InventoryType.PLAYER && e.getInventory().getType() != InventoryType.CREATIVE) {
                        if (gui.cancelEvent()) {
                            e.setCancelled(true);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    ((Player) e.getWhoClicked()).updateInventory();
                                }
                            }.runTaskLater(HubParkour.getInstance(), 3);
                        }
                        int row = e.getSlot() / 9;
                        int column = e.getSlot() % 9;
                        gui.onClick(row, column, itemStack, e.getClick());
                    }
                }
            }
        }
    }

}
