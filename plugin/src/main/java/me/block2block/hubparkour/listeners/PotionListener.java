package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
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
                    if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Stop-Potion-Effects", true)) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

}
