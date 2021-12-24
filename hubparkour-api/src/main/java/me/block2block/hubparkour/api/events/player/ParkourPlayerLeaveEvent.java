package me.block2block.hubparkour.api.events.player;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;

/**
 * Called when a user leaves a parkour using /parkour leave.
 */
public class ParkourPlayerLeaveEvent extends ParkourPlayerEvent {

    @SuppressWarnings("unused")
    public ParkourPlayerLeaveEvent(IParkour parkour, IHubParkourPlayer player) {
        super(parkour, player);
    }
}
