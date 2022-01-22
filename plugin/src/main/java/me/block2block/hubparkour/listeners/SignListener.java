package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.signs.ClickableSign;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.signs.StartClickableSign;
import me.block2block.hubparkour.signs.StatsClickableSign;
import me.block2block.hubparkour.signs.TeleportClickableSign;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (e.getClickedBlock().getType().name().toLowerCase().contains("sign")){
                Sign bukkitSign = (Sign) e.getClickedBlock().getState();
                if (CacheManager.getSigns().containsKey(bukkitSign.getLocation())) {
                    ClickableSign clickableSign = CacheManager.getSigns().get(bukkitSign.getLocation());
                    clickableSign.onClick(e.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void signChangeListener(SignChangeEvent e) {
        if (e.getLines()[0].equals("[HubParkour]")) {
            if (!e.getPlayer().hasPermission("hubparkour.admin.signs")) {
                return;
            }
            e.setCancelled(true);
            Sign sign = (Sign) e.getBlock().getState();
            switch (e.getLines()[1]) {
                case "teleport": {
                    Parkour parkour = CacheManager.getParkour(e.getLines()[2]);
                    if (parkour == null) {
                        int i;
                        try {
                            i = Integer.parseInt(e.getLines()[2]);
                        } catch (NumberFormatException ex) {
                            sign.setLine(1, "§c§lInvalid Parkour");
                            sign.update(true);
                            return;
                        }
                        parkour = CacheManager.getParkour(i);
                        if (parkour == null) {
                            sign.setLine(1, "§c§lInvalid Parkour");
                            sign.update(true);
                            return;
                        }
                    }
                    Location location = sign.getLocation();
                    TeleportClickableSign sign1 = new TeleportClickableSign(parkour, sign);
                    HubParkour.getInstance().getDbManager().addSign(sign1);
                    CacheManager.getSigns().put(location, sign1);
                    sign1.refresh();
                    break;
                }
                case "start": {
                    Parkour parkour = CacheManager.getParkour(e.getLines()[2]);
                    if (parkour == null) {
                        int i;
                        try {
                            i = Integer.parseInt(e.getLines()[2]);
                        } catch (NumberFormatException ex) {
                            sign.setLine(1, "§c§lInvalid Parkour");
                            sign.setLine(2, "");
                            sign.setLine(3, "");
                            sign.setLine(4, "");
                            sign.update(true);
                            return;
                        }
                        parkour = CacheManager.getParkour(i);
                        if (parkour == null) {
                            sign.setLine(1, "§c§lInvalid Parkour");
                            sign.setLine(2, "");
                            sign.setLine(3, "");
                            sign.setLine(4, "");
                            sign.update(true);
                            return;
                        }
                    }
                    Location location = sign.getLocation();
                    StartClickableSign sign1 = new StartClickableSign(parkour, sign);
                    HubParkour.getInstance().getDbManager().addSign(sign1);
                    CacheManager.getSigns().put(location, sign1);
                    sign1.refresh();
                    break;
                }
                case "stats": {
                    Parkour parkour = CacheManager.getParkour(e.getLines()[2]);
                    if (parkour == null) {
                        int i;
                        try {
                            i = Integer.parseInt(e.getLines()[2]);
                        } catch (NumberFormatException ex) {
                            sign.setLine(1, "§c§lInvalid Parkour");
                            sign.setLine(2, "");
                            sign.setLine(3, "");
                            sign.setLine(4, "");
                            sign.update(true);
                            return;
                        }
                        parkour = CacheManager.getParkour(i);
                        if (parkour == null) {
                            sign.setLine(1, "§c§lInvalid Parkour");
                            sign.setLine(2, "");
                            sign.setLine(3, "");
                            sign.setLine(4, "");
                            sign.update(true);
                            return;
                        }
                    }
                    Location location = sign.getLocation();
                    StatsClickableSign sign1 = new StatsClickableSign(parkour, sign);
                    HubParkour.getInstance().getDbManager().addSign(sign1);
                    CacheManager.getSigns().put(location, sign1);
                    sign1.refresh();
                    break;
                }
            }
        }
    }

}
