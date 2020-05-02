package me.block2block.hubparkour.api.events.player;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.plates.Checkpoint;

/**
 * Called when a user reaches a checkpoint.
 */
public class ParkourPlayerCheckpointEvent extends ParkourPlayerEvent {

    private final Checkpoint checkpoint;

    @SuppressWarnings("unused")
    public ParkourPlayerCheckpointEvent(IParkour parkour, IHubParkourPlayer player, Checkpoint checkpoint) {
        super(parkour, player);
        this.checkpoint = checkpoint;
    }

    /**
     * Get the checkpoint that the player reached.
     * @return the checkpoint object
     */
    @SuppressWarnings("unused")
    public Checkpoint getCheckpoint() {
        return checkpoint;
    }
}
