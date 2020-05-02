package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@SuppressWarnings("unused")
public class BreakListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (CacheManager.isPoint(e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Break-Disallowed")));
        }
    }

}
