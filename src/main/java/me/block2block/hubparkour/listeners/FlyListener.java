package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class FlyListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onFly(PlayerToggleFlightEvent e) {
        if (e.isFlying()) {
            if (CacheManager.isParkour(e.getPlayer())) {
                CacheManager.getPlayer(e.getPlayer()).end(true);
            }
        }
    }


}
