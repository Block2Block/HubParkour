package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.api.items.ParkourItem;
import me.block2block.hubparkour.api.items.ShowItem;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class LeaveListener implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (CacheManager.isParkour(e.getPlayer())) {
            HubParkourPlayer player = CacheManager.getPlayer(e.getPlayer());
            player.removeItems();
            player.getParkour().playerEnd(player);
            player.setToPrevState();
            CacheManager.playerEnd(player);
        }
        if (CacheManager.isEdit(e.getPlayer())) {
            CacheManager.leaveEditMode();
        }
        if (CacheManager.isSetup(e.getPlayer())) {
            CacheManager.exitSetup();
        }
        for (HubParkourPlayer player : CacheManager.getPlayers()) {
            for (ParkourItem item : player.getParkourItems()) {
                if (item.getType() == 4) {
                    if (player.getPlayer().canSee(e.getPlayer())) {
                        ShowItem showItem = (ShowItem) item;
                        showItem.getHiddenPlayers().remove(e.getPlayer());
                    }
                }
            }
        }
    }

}
