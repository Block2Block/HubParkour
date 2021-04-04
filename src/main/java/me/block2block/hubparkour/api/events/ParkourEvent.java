package me.block2block.hubparkour.api.events;

import me.block2block.hubparkour.api.IParkour;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * A generic Parkour event.
 */
public abstract class ParkourEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final IParkour parkour;

    @SuppressWarnings("unused")
    public ParkourEvent(IParkour parkour, boolean isAsync) {
        super(isAsync);
        this.parkour = parkour;
    }

    @SuppressWarnings("unused")
    public IParkour getParkour() {
        return parkour;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
