package me.Block2Block.HubParkour.Entities;


import org.bukkit.entity.Player;

public class HubParkourPlayer {

    private Player player;
    private Parkour parkour;
    private long startTime;

    public HubParkourPlayer(Player p, Parkour parkour) {
        this.parkour = parkour;
        this.player = p;
        startTime = System.currentTimeMillis();
    }

}
