package me.block2block.hubparkour.api.events.player;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;

/**
 * Called when a user finishes the parkour successfully.
 */
public class ParkourPlayerFinishEvent extends ParkourPlayerEvent {
    final long parkourTime;
    final long parkourStartTimestamp;
    final long parkourFinishTimestamp;

    @SuppressWarnings("unused")
    public ParkourPlayerFinishEvent(IParkour parkour, IHubParkourPlayer player, long parkourTime, long parkourFinishTimestamp, long parkourStartTimestamp) {
        super(parkour, player);
        this.parkourFinishTimestamp = parkourFinishTimestamp;
        this.parkourStartTimestamp = parkourStartTimestamp;
        this.parkourTime = parkourTime;
    }

    /**
     * Get the timestamp the user finished. This is in milliseconds.
     * @return the finish timestamp.
     */
    @SuppressWarnings("unused")
    public long getParkourFinishTimestamp() {
        return parkourFinishTimestamp;
    }

    /**
     * Get the timestamp the user started the parkour. This is in milliseconds.
     * @return the start timestamp.
     */
    @SuppressWarnings("unused")
    public long getParkourStartTimestamp() {
        return parkourStartTimestamp;
    }

    /**
     * Get the time, in milliseconds, of the time the user took to finish the parkour.
     * @return the time in ms.
     */
    @SuppressWarnings("unused")
    public long getParkourTime() {
        return parkourTime;
    }
}
