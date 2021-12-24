package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.api.items.ParkourItem;
import me.block2block.hubparkour.api.items.ShowItem;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for (HubParkourPlayer player : CacheManager.getPlayers()) {
            for (ParkourItem item : player.getParkourItems()) {
                if (item.getType() == 4) {
                    if (player.getPlayer().canSee(e.getPlayer())) {
                        ShowItem showItem = (ShowItem) item;
                        showItem.getHiddenPlayers().add(e.getPlayer());
                        player.getPlayer().hidePlayer(e.getPlayer());
                    }
                }
            }
        }
    }

}
