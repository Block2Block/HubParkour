package me.block2block.hubparkour.api.events.player;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.events.ParkourEvent;
import org.bukkit.event.Cancellable;

/**
 * Events relating to parkour players.
 */
public abstract class ParkourPlayerEvent extends ParkourEvent implements Cancellable {

    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private final IHubParkourPlayer player;

    @SuppressWarnings("unused")
    public ParkourPlayerEvent(IParkour parkour, IHubParkourPlayer player) {
        super(parkour, false);
        this.player = player;
        this.cancelled = false;
    }

    /**
     * Get the player for which this event was called from.
     * @return the player
     */
    @SuppressWarnings("unused")
    public IHubParkourPlayer getPlayer() {
        return player;
    }
}
