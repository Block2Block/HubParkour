package me.block2block.hubparkour.managers;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.entities.plates.PressurePlate;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class CacheManager {

    private static Map<UUID, HubParkourPlayer> players = new HashMap<>();
    private static Map<Location, PressurePlate> points = new HashMap<>();
    private static Map<Integer, Material> types = new HashMap<>();
    private static List<Parkour> parkours = new ArrayList<>();
    private static int setupStage = -1;
    private static Player setupPlayer;

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

    public static boolean isPoint(Location location) {
        return points.containsKey(location);
    }

    public static PressurePlate getPoint(Location location) {
        return points.get(location);
    }

    public static void removePlate(PressurePlate p) {
        p.removeMaterial();
        points.remove(p.getLocation());
    }

    public static void addPoint(PressurePlate p) {
        points.put(p.getLocation(), p);
    }

    public static void playerStart(HubParkourPlayer p) {
        players.put(p.getPlayer().getUniqueId(), p);
    }

    public static void playerEnd(HubParkourPlayer p) {
        players.remove(p.getPlayer().getUniqueId());
    }

}
