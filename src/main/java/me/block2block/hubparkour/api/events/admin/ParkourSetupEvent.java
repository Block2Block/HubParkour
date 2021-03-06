package me.block2block.hubparkour.api.events.admin;

import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.events.ParkourEvent;
import org.bukkit.entity.Player;

/**
 * Called when an admin sets up a parkour.
 */
public class ParkourSetupEvent extends ParkourEvent {

    private final Player player;

    @SuppressWarnings("unused")
    public ParkourSetupEvent(IParkour parkour, Player player) {
        super(parkour, true);
        this.player = player;
    }

    @SuppressWarnings("unused")
    public Player getPlayer() {
        return player;
    }
}
