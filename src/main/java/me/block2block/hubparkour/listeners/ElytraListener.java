package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;

public class ElytraListener implements Listener {

    @EventHandler
    public void onElytraFly(EntityToggleGlideEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) e.getEntity();
            if (e.isGliding()) {
                if (CacheManager.isParkour(player)) {
                    if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Fail.On-Elytra-Use", true)) {
                        CacheManager.getPlayer(player).end(ParkourPlayerFailEvent.FailCause.ELYTRA_USE);
                    }
                }
            }
        }
    }

}
