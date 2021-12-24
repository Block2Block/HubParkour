package me.block2block.hubparkour.signs;

import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerStartEvent;
import me.block2block.hubparkour.api.signs.ClickableSign;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class StartClickableSign extends ClickableSign {

    public StartClickableSign(Parkour parkour, Sign signState) {
        super(parkour, signState);
    }

    public void refresh() {
        List<String> defaultList = new ArrayList<>();
        defaultList.add("&2&l[PARKOUR]");
        defaultList.add("&a{parkour-name}");
        defaultList.add("{amount-of-players} players");
        defaultList.add("Click to start!");
        int counter = 0;
        for (String s : ConfigUtil.getStringList("Messages.Signs.Start", defaultList)) {
            signState.setLine(counter, ChatColor.translateAlternateColorCodes('&', s.replace("{parkour-name}", parkour.getName()).replace("{amount-of-players}", parkour.getPlayers().size() + "")));
            counter++;
            if (counter == 4) {
                break;
            }
        }
        signState.update(true);
    }

    public int getType() {
        return 2;
    }

    public void onClick(Player p) {
        if (CacheManager.isSomeoneEdit()) {
            if (parkour.equals(CacheManager.getEditWizard().getParkour())) {
                ConfigUtil.sendMessageOrDefault(p, "Messages.Parkour.Currently-Being-Edited", "This parkour is currently in being modified by an admin. Please wait to attempt this parkour!", true, Collections.emptyMap());
                return;
            }
        }

        if (CacheManager.isParkour(p)) {
            if (CacheManager.getPlayer(p).getParkour().getId() == parkour.getId()) {
                //Restart the parkour.

                CacheManager.getPlayer(p).restart();

                ConfigUtil.sendMessageOrDefault(p, "Messages.Parkour.Restarted", "You have restarted the parkour! Your time has been reset to 0!", true, Collections.emptyMap());
                return;
            } else {
                //Do nothing, is doing a different parkour.
                if (ConfigUtil.getBoolean("Settings.Start-When-In-Parkour", false)) {
                    CacheManager.getPlayer(p).end(ParkourPlayerFailEvent.FailCause.NEW_PARKOUR);

                    p.teleport(parkour.getRestartPoint().getLocation());
                    //Start the new parkour
                    HubParkourPlayer player = new HubParkourPlayer(p, (Parkour) parkour);
                    ParkourPlayerStartEvent event = new ParkourPlayerStartEvent(parkour, player, player.getStartTime());
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    parkour.playerStart(player);
                    CacheManager.playerStart(player);
                    if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Remove-Potion-Effects", true)) {
                        for (PotionEffect effect : p.getActivePotionEffects()) {
                            p.removePotionEffect(effect.getType());
                        }
                    }
                    if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Remove-Fly", true)) {
                        p.setFlying(false);
                        if (Material.getMaterial("ELYTRA") != null) {
                            p.setGliding(false);
                        }
                    }
                    player.giveItems();

                    Map<String, String> bindings = new HashMap<>();
                    bindings.put("parkour-name", parkour.getName());

                    ConfigUtil.sendMessageOrDefault(p, "Messages.Parkour.Started", "You have started the &a{parkour-name} &rparkour!", true, bindings);
                    return;
                } else {
                    ConfigUtil.sendMessageOrDefault(p, "Messages.Parkour.Already-In-Parkour", "You are already doing a parkour. If you wish to leave the current parkour and start a new one, do /parkour leave.", true, Collections.emptyMap());
                    return;
                }
            }
        } else {
            //Start the parkour
            p.teleport(parkour.getRestartPoint().getLocation());
            Parkour parkour = (Parkour) this.parkour;
            HubParkourPlayer player = new HubParkourPlayer(p, parkour);
            ParkourPlayerStartEvent event = new ParkourPlayerStartEvent(parkour, player, player.getStartTime());
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            parkour.playerStart(player);
            CacheManager.playerStart(player);
            if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Remove-Potion-Effects", true)) {
                for (PotionEffect effect : p.getActivePotionEffects()) {
                    p.removePotionEffect(effect.getType());
                }
            }
            if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Remove-Fly", true)) {
                p.setFlying(false);
                if (Material.getMaterial("ELYTRA") != null) {
                    p.setGliding(false);
                }
            }
            player.giveItems();

            Map<String, String> bindings = new HashMap<>();
            bindings.put("parkour-name", parkour.getName());

            ConfigUtil.sendMessageOrDefault(p, "Messages.Parkour.Started", "You have started the &a{parkour-name} &rparkour!", true, bindings);


        }
    }
}
