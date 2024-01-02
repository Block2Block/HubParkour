package me.block2block.hubparkour.api.events.player;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.plates.PressurePlate;

/**
 * Called when a user uses /parkour reset or /parkour checkpoint or player fall.
 */
@SuppressWarnings("ALL")
public class ParkourPlayerTeleportEvent extends ParkourPlayerEvent {

    private final PressurePlate plate;
    private final TeleportType teleportType;

    @SuppressWarnings("unused")
    public ParkourPlayerTeleportEvent(IParkour parkour, IHubParkourPlayer player,
                                      PressurePlate pressurePlate, TeleportType teleportType) {
        super(parkour, player);
        this.plate = pressurePlate;
        this.teleportType = teleportType;
    }

    /**
     * Get the pressure plate that the user teleported to.
     *
     * @return the pressure plate
     */
    @SuppressWarnings("unused")
    public PressurePlate getPressurePlate() {
        return plate;
    }

    /**
     * Get reason of player teleport
     * @return
     */
    public TeleportType getTeleportType() {
        return teleportType;
    }

    public enum TeleportType {
        /**
         * Use command /parkour reset
         */
        CommandReset,
        /**
         * Use command /parkour checkpoint
         */
        CommandCheckPoint,
        /**
         * Click gui item reset
         */
        ItemClickReset,
        /**
         * Click gui item checkpoint
         */
        ItemClickCheckPoint,
        /**
         * Player dropped from platform
         */
        Fall
    }
}
