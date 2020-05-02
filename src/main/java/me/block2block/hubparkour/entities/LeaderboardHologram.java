package me.block2block.hubparkour.entities;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.ILeaderboardHologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

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
                id = Main.getInstance().getDbManager().addHologram(instance);
            }
        }.runTaskAsynchronously(Main.getInstance());
    }

    public void generate() {
        if (hologram != null) {
            hologram.delete();
        }
        hologram = HologramsAPI.createHologram(Main.getInstance(), location);
        hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("Messages.Holograms.Leaderboard.Header").replace("{parkour-name}", parkour.getName())));
        refresh();
    }

    public void remove() {
        if (hologram != null) {
            hologram.delete();
        }
    }

    public void refresh() {
        for (int i = 1; i < hologram.size(); i++) {
            hologram.removeLine(i);
        }

        Map<Integer, List<String>> leaderboard = Main.getInstance().getDbManager().getLeaderboard(parkour, Main.getInstance().getConfig().getInt("Settings.Leaderboard.Limit"));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (int place : leaderboard.keySet()) {
                    List<String> record = leaderboard.get(place);
                    hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', Main.getInstance().getConfig().getString("Messages.Holograms.Leaderboard.Line").replace("{player-name}", record.get(0)).replace("{player-time}", "" + Float.parseFloat(record.get(1)) / 1000f).replace("{place}", "" + place)));
                }
            }
        }.runTask(Main.getInstance());
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
