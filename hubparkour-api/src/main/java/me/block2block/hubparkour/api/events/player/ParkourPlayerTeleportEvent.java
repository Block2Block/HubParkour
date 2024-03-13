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
    private final TeleportReason teleportReason;

    @SuppressWarnings("unused")
    public ParkourPlayerTeleportEvent(IParkour parkour, IHubParkourPlayer player,
                                      PressurePlate pressurePlate, TeleportReason teleportReason) {
        super(parkour, player);
        this.plate = pressurePlate;
        this.teleportReason = teleportReason;
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
     *
     * @return the reason of teleport
     */
    @SuppressWarnings("unused")
    public TeleportReason getTeleportReason() {
        return teleportReason;
    }

    /**
     * The reason of teleport
     */
    public enum TeleportReason {
        /**
         * Unknow reason
         */
        Unknow,
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
        Fall,
        /**
         * Player fall in water
         */
        Water,
        /**
         * Player fall in lava
         */
        Lava
    }
}
