package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HungerListener implements Listener {

    @EventHandler
    public void onHungerChange(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (CacheManager.isParkour(p)) {
                if (ConfigUtil.getBoolean("Settings.Hunger.Disable-Hunger", true)) {
                    if (e.getFoodLevel() != 30) {
                        e.setCancelled(true);
                        p.setFoodLevel(30);
                    }
                }
            }
        }
    }

}
