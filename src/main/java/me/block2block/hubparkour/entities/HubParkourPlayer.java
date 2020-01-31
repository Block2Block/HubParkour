package me.block2block.hubparkour.entities;


import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.entities.plates.Checkpoint;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.entity.Player;

import java.util.List;

public class HubParkourPlayer {

    private Player player;
    private Parkour parkour;
    private List<Checkpoint> checkpoints;
    private long startTime;

    public HubParkourPlayer(Player p, Parkour parkour) {
        this.parkour = parkour;
        this.player = p;
        startTime = System.currentTimeMillis();
    }

    public long end(boolean fly) {
        if (fly) {
            player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Failed.Not-Enough-Checkpoints")));
            CacheManager.removePlayer(player);
        } else {
            if (Main.getInstance().getConfig().getBoolean("Settings.Must-Complete-All-Checkpoints")) {
                if (checkpoints.size() != parkour.getNoCheckpoints()) {
                    player.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Parkour.End.Failed.Not-Enough-Checkpoints")));
                    CacheManager.removePlayer(player);
                }
            }

            long finishMili = System.currentTimeMillis() - startTime;
            float finishTime = finishMili/1000f;




        }
        return -1;
    }

}
