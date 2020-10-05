package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.api.HubParkourAPI;
import me.block2block.hubparkour.api.items.ParkourItem;
import me.block2block.hubparkour.entities.Parkour;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

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

}
