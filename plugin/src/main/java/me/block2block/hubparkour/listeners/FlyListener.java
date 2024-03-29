package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class FlyListener implements Listener {

    @SuppressWarnings("unused")
    @EventHandler
    public void onFly(PlayerToggleFlightEvent e) {
        if (e.isFlying()) {
            if (CacheManager.isParkour(e.getPlayer())) {
                if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Fail.On-Toggle-Fly", true)) {
                    CacheManager.getPlayer(e.getPlayer()).end(ParkourPlayerFailEvent.FailCause.FLY);
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (CacheManager.isParkour(e.getPlayer())) {
            Location l = e.getTo().clone();
            l.setX(l.getX() - 0.5);
            l.setY(l.getY() - 0.5);
            l.setZ(l.getZ() - 0.5);
            if (!CacheManager.isPoint(l)) {
                if (!CacheManager.isRestartPoint(l)) {
                    if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Fail.On-Teleport", true)) {
                        CacheManager.getPlayer(e.getPlayer()).end(ParkourPlayerFailEvent.FailCause.TELEPORTATION);
                    }
                }
            }
        }
    }


}
