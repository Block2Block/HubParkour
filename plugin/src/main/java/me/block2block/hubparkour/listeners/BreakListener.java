package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Collections;
import java.util.HashMap;

@SuppressWarnings("unused")
public class BreakListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (CacheManager.isPoint(e.getBlock().getLocation()) && CacheManager.getPoint(e.getBlock().getLocation()).getMaterial() != Material.AIR) {
            e.setCancelled(true);
            ConfigUtil.sendMessage(e.getPlayer(), "Messages.Break-Disallowed", "You are not allowed to break HubParkour Pressure Plates. In order to remove HubParkour Pressure Plates, please delete the parkour with /parkour delete [id].", true, Collections.emptyMap());
            return;
        }
        if (e.getBlock().getType().name().toLowerCase().contains("sign")) {
            if (CacheManager.getSigns().containsKey(e.getBlock().getLocation())) {
                if (!e.getPlayer().hasPermission("hubparkour.admin.signs")) {
                    e.setCancelled(true);
                    return;
                }
                HubParkour.getInstance().getDbManager().removeSign(CacheManager.getSigns().get(e.getBlock().getLocation()));
                CacheManager.getSigns().remove(e.getBlock().getLocation());
                ConfigUtil.sendMessage(e.getPlayer(), "Messages.Signs.Sign-Deleted", "The sign has been successfully deleted.", true, new HashMap<>());
            }
        }
    }

}
