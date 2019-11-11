package me.Block2Block.HubParkour.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PressurePlateListener implements Listener {

    @EventHandler
    public void onPressurePlate(PlayerMoveEvent e) {
        if (e.getFrom().getBlock().getType().equals(e.getTo().getBlock().getType())) {
            return;
        }

    }

}
