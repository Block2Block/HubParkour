package me.block2block.hubparkour.entities;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.ILeaderboardHologram;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LeaderboardHologram implements ILeaderboardHologram {

    private int id;
    private final Location location;
    private final Parkour parkour;
    private Hologram hologram;

    public LeaderboardHologram(Location location, Parkour parkour, int id) {
        this.location = location;
        this.parkour = parkour;
        this.id = id;
    }

    public LeaderboardHologram(Location location, Parkour parkour) {
        this.parkour = parkour;
        this.location = location;
        LeaderboardHologram instance = this;

        //Get ID and insert into database.
        new BukkitRunnable() {
            @Override
            public void run() {
                id = HubParkour.getInstance().getDbManager().addHologram(instance);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }

    public void generate() {
        if (location == null) {
            HubParkour.getInstance().getLogger().info("The location of one of your leaderboard holograms for parkour " + parkour.getName() + " no longer exists. Please delete leaderboard hologram " + this.id + ".");
            return;
        }

        if (location.getWorld() == null) {
            HubParkour.getInstance().getLogger().info("The location of one of your leaderboard holograms for parkour " + parkour.getName() + " no longer exists. Please delete leaderboard hologram " + this.id + ".");
            return;
        }
        hologram = DHAPI.getHologram("hp:leaderboard." + parkour.getId() + "." + id);
        if (hologram == null) {
            hologram = DHAPI.createHologram("hp:leaderboard." + parkour.getId() + "." + id, location);
        } else {
            DHAPI.moveHologram(hologram, location);
        }
        refresh();
    }

    public void remove() {
        if (hologram != null) {
            hologram.delete();
        }
    }

    public void refresh() {
        List<String> lines = new ArrayList<>();
        lines.add(ChatColor.translateAlternateColorCodes('&', ConfigUtil.getString("Messages.Holograms.Leaderboard.Header-" + ((parkour == null)?"Overall":"Parkour"), (parkour == null)?"&9&lOverall Leaderboard for All Parkours":"&9&lLeaderboard for {parkour-name}").replace("{parkour-name}", ((parkour == null)?"":parkour.getName()))));
        Map<Integer, List<String>> leaderboard = HubParkour.getInstance().getDbManager().getLeaderboard(parkour, ConfigUtil.getInt("Settings.Leaderboard.Limit", 10));
        for (int place : leaderboard.keySet()) {
            List<String> record = leaderboard.get(place);
            lines.add(ChatColor.translateAlternateColorCodes('&', ConfigUtil.getString("Messages.Holograms.Leaderboard.Line", "&3#{place}&r - &b{player-name}&r - &b{player-time}").replace("{player-name}", record.get(0)).replace("{player-time}", ConfigUtil.formatTime(Long.parseLong(record.get(1)))).replace("{place}", "" + place)));
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                DHAPI.setHologramLines(hologram, lines);
            }
        }.runTask(HubParkour.getInstance());
    }

    public Location getLocation() {
        return location.clone();
    }

    public Parkour getParkour() {
        return parkour;
    }

    public int getId() {
        return id;
    }
}
