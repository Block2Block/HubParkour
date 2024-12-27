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
        UNKNOW,
        /**
         * Use command /parkour reset
         */
        COMMAND_RESET,
        /**
         * Use command /parkour checkpoint
         */
        COMMAND_CHECK_POINT,
        /**
         * Click gui item reset
         */
        ITEM_CLICK_RESET,
        /**
         * Click gui item checkpoint
         */
        ITEM_CLICK_CHECK_POINT,
        /**
         * Player dropped from platform
         */
        FALL,
        /**
         * Player fall in water
         */
        WATER,
        /**
         * Player fall in lava
         */
        LAVA
    }
}
