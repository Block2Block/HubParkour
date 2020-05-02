package me.block2block.hubparkour.api.events.player;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.plates.PressurePlate;

/**
 * Called when a user uses /parkour reset or /parkour checkpoint.
 */
@SuppressWarnings("ALL")
public class ParkourPlayerTeleportEvent extends ParkourPlayerEvent {

    private final PressurePlate plate;

    @SuppressWarnings("unused")
    public ParkourPlayerTeleportEvent(IParkour parkour, IHubParkourPlayer player, PressurePlate pressurePlate) {
        super(parkour, player);
        this.plate = pressurePlate;
    }

    /**
     * Get the pressure plate that the user teleported to.
     * @return the pressure plate
     */
    @SuppressWarnings("unused")
    public PressurePlate getPressurePlate() {
        return plate;
    }
}
