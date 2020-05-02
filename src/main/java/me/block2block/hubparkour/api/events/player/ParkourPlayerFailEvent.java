package me.block2block.hubparkour.api.events.player;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;

/**
 * Called when a user fails the parkour and is kicked from the parkour.
 */
public class ParkourPlayerFailEvent extends ParkourPlayerEvent {

    /**
     * Util enum for the Fail Cause.
     */
    public enum FailCause {FLY, NOT_ENOUGH_CHECKPOINTS, @SuppressWarnings("unused") CUSTOM}

    private final FailCause failCause;

    @SuppressWarnings("unused")
    public ParkourPlayerFailEvent(IParkour parkour, IHubParkourPlayer player, FailCause failCause) {
        super(parkour, player);
        this.failCause = failCause;
    }

    /**
     * Get the reason the user failed
     * @return the reason the user failed.
     */
    @SuppressWarnings("unused")
    public FailCause getFailCause() {
        return failCause;
    }
}
