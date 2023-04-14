package me.block2block.hubparkour.api.events.player;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;

/**
 * Called when a user starts a parkour by standing on a start pressure plate. This is called before the items and state variables have been set, so methods like HubParkourPlayer#getPrevHealth() have not yet been initialised.
 */
public class ParkourPlayerStartEvent extends ParkourPlayerEvent {

    private final long startTimeStamp;

    @SuppressWarnings("unused")
    public ParkourPlayerStartEvent(IParkour parkour, IHubParkourPlayer player, long startTimeStamp) {
        super(parkour, player);
        this.startTimeStamp = startTimeStamp;
    }

    /**
     * Gets the time of which the player started the parkour.
     * @return the start timestamp.
     */
    @SuppressWarnings("unused")
    public long getStartTimeStamp() {
        return startTimeStamp;
    }
}
