package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;

public class PotionListener implements Listener {

    @EventHandler
    public void onPotion(EntityPotionEffectEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getAction() == EntityPotionEffectEvent.Action.ADDED) {
                Player p = (Player) e.getEntity();
                if (CacheManager.isParkour(p)) {
                    if (Main.getInstance().getConfig().getBoolean("Settings.Stop-Potion-Effects")) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

}
