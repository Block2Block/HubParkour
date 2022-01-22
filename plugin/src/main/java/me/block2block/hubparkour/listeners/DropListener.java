package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.api.HubParkourAPI;
import me.block2block.hubparkour.api.items.ParkourItem;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class DropListener implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (HubParkourAPI.isInParkour(e.getPlayer())) {
            for (ParkourItem item : HubParkourAPI.getPlayer(e.getPlayer()).getParkourItems()) {
                if (e.getItemDrop().getItemStack().equals(item.getItem())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
            Player p = e.getPlayer();
            if (ConfigUtil.getBoolean("Settings.Parkour-Items.Prevent-Item-Pickup", true)) {
                if (HubParkourAPI.isInParkour(p)) {
                    e.setCancelled(true);
                }
            }
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            if (CacheManager.isParkour(player)) {
                for (ParkourItem item : HubParkourAPI.getPlayer(player).getParkourItems()) {
                    if (item != null) {
                        if (item.getItem() != null) {
                            if (e.getCurrentItem() != null) {
                                if (item.getItem().equals(e.getCurrentItem())) {
                                    e.setCancelled(true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

        }
    }

}
