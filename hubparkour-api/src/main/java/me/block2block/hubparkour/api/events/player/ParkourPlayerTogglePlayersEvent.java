package me.block2block.hubparkour.api.events.player;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;

/**
 * Fired when a player has toggled whether players are visible.
 */
public class ParkourPlayerTogglePlayersEvent extends ParkourPlayerEvent {

    public final boolean visible;

    public ParkourPlayerTogglePlayersEvent(IParkour parkour, IHubParkourPlayer player, boolean visible) {
        super(parkour, player);
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
}
