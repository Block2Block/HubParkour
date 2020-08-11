package me.block2block.hubparkour.managers;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.plates.PressurePlate;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.LeaderboardHologram;
import me.block2block.hubparkour.entities.Parkour;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@SuppressWarnings("unused")
public class CacheManager {

    private static final Map<UUID, HubParkourPlayer> players;
    private static final Map<Location, PressurePlate> points;
    private static final Map<Location, PressurePlate> restartPoints;
    private static final Map<Integer, Material> types;
    private static final Map<Integer, ItemStack> items;
    private static final List<Parkour> parkours;
    private static final List<LeaderboardHologram> leaderboards;
    private static int setupStage = -1;
    private static Player setupPlayer;

    static {
        players = new HashMap<>();
        points = new HashMap<>();
        types = new HashMap<>();
        items = new HashMap<>();
        parkours = new ArrayList<>();
        leaderboards = new ArrayList<>();
        restartPoints = new HashMap<>();
    }

    public static boolean isParkour(Player p) {
        return players.containsKey(p.getUniqueId());
    }

    public static HubParkourPlayer getPlayer(Player p) {
        return players.get(p.getUniqueId());
    }

    public static boolean isSetup(Player p) {
        return p.equals(setupPlayer);
    }

    public static void setSetupPlayer(Player p) {
        setupPlayer = p;
    }

    public static void exitSetup() {
        setupPlayer = null;
        setupStage = -1;
    }

    public static int getSetupStage() {
        return setupStage;
    }

    public static void nextStage() {setupStage++;}

    public static void addParkour(Parkour parkour) {
        parkours.add(parkour);
    }

    public static List<Parkour> getParkours() {
        return parkours;
    }

    public static Parkour getParkour(String name) {
        for (Parkour parkour : parkours) {
            if (ChatColor.stripColor(Main.c(false, name)).equalsIgnoreCase(ChatColor.stripColor(Main.c(false, parkour.getName())))) {
                return parkour;
            }
        }
        return null;
    }

    public static Parkour getParkour(int id) {
        for (Parkour parkour : parkours) {
            if (id == parkour.getId()) {
                return parkour;
            }
        }
        return null;
    }

    public static void setType(int type, Material material) {
        types.put(type, material);
    }

    public static Map<Integer, Material> getTypes() {
        return types;
    }

    public static void setItem(int type, ItemStack material) {
        items.put(type, material);
    }

    public static Map<Integer, ItemStack> getItems() {
        return items;
    }

    public static boolean isPoint(Location location) {
        location = location.clone();
        location.setPitch(0);
        location.setYaw(0);
        return points.containsKey(location);
    }

    public static boolean isRestartPoint(Location location) {
        location = location.clone();
        location.setPitch(0);
        location.setYaw(0);
        return restartPoints.containsKey(location);
    }

    public static PressurePlate getPoint(Location location) {
        location = location.clone();
        location.setPitch(0);
        location.setYaw(0);
        return points.get(location);
    }

    public static PressurePlate getRestartPoint(Location location) {
        location = location.clone();
        location.setPitch(0);
        location.setYaw(0);
        return restartPoints.get(location);
    }

    public static void removePlate(PressurePlate p) {
        p.removeMaterial();
        points.remove(p.getLocation());
    }

    public static void addPoint(PressurePlate p) {
        Location location = p.getLocation().clone();
        location.setYaw(0);
        location.setPitch(0);
        points.put(location, p);
    }

    public static void addRestartPoint(PressurePlate p) {
        Location location = p.getLocation().clone();
        location.setYaw(0);
        location.setPitch(0);
        restartPoints.put(location, p);
    }

    public static void playerStart(HubParkourPlayer p) {
        players.put(p.getPlayer().getUniqueId(), p);
    }

    public static void playerEnd(HubParkourPlayer p) {
        players.remove(p.getPlayer().getUniqueId());
    }

    public static void addHologram(LeaderboardHologram hologram) {
        leaderboards.add(hologram);
    }

    public static void removeHologram(LeaderboardHologram hologram) {
        leaderboards.remove(hologram);
    }

    public static LeaderboardHologram getHologram(int id) {
        for (LeaderboardHologram hologram : leaderboards) {
            if (id == hologram.getId()) {
                return hologram;
            }
        }
        return null;
    }

    public static List<LeaderboardHologram> getLeaderboards() {
        return leaderboards;
    }

}
