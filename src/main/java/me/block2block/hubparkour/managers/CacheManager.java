package me.block2block.hubparkour.managers;

import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.entities.plates.StartPoint;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class CacheManager {

    private static Map<UUID, HubParkourPlayer> players = new HashMap<>();
    private static Map<Location, StartPoint> startPoints = new HashMap<>();
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

    public static void setSetupPlayer(Player p) {setupPlayer = p;}

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

    public static void setType(int type, Material material) {
        types.put(type, material);
    }

    public static Map<Integer, Material> getTypes() {
        return types;
    }

    public static boolean isStartPoint(Location location) {
        return startPoints.containsKey(location);
    }

    public static void removePlayer(Player p) {
        players.remove(p.getUniqueId());
    }

}
