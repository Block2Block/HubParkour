package me.block2block.hubparkour.api.events.admin;

import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.events.ParkourEvent;
import org.bukkit.entity.Player;

/**
 * Called when an admin deletes a parkour.
 */
public class ParkourDeleteEvent extends ParkourEvent {

    private final Player player;

    @SuppressWarnings("unused")
    public ParkourDeleteEvent(IParkour parkour, Player player) {
        super(parkour);
        this.player = player;
    }

    @SuppressWarnings("unused")
    public Player getPlayer() {
        return player;
    }
}
