package me.block2block.hubparkour.api;

import me.block2block.hubparkour.api.plates.Checkpoint;
import org.bukkit.entity.Player;

/**
 * Represents a player in a parkour.
 */
@SuppressWarnings("ALL")
public interface IHubParkourPlayer {

    /**
     * What happens when a user reaches a checkpoint.
     * @param checkpoint the checkpoint reached.
     */
    @SuppressWarnings("unused")
    void checkpoint(Checkpoint checkpoint);

    /**
     * What happens when the player has finished the parkour
     * @param fly if the parkour was failed because of flying.
     */
    @SuppressWarnings("unused")
    void end(boolean fly);

    /**
     * Gets the last reached checkpoint
     * @return the number of the last reached checkpoint.
     */
    @SuppressWarnings("unused")
    int getLastReached();

    /**
     * Gets the parkour this player is currently in.
     * @return the parkour.
     */
    @SuppressWarnings("unused")
    IParkour getParkour();

    /**
     * Get the <code>org.bukkit.entity.Player</code> object of the player.
     * @return the Bukkit Player object.
     */
    Player getPlayer();

    /**
     * When happens when the player restarts the parkour.
     */
    @SuppressWarnings("unused")
    void restart();

    /**
     * Get the time they got on the last time they completed the parkour.
     * @return the time they received in ms.
     */
    @SuppressWarnings("unused")
    long getPrevious();

    /**
     * Get the timestamp of when they started the parkour.
     * @return the timestamp in ms.
     */
    @SuppressWarnings("unused")
    long getStartTime();

}
