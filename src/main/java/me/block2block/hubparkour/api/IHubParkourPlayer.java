package me.block2block.hubparkour.api;

import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.api.items.ParkourItem;
import me.block2block.hubparkour.api.plates.Checkpoint;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

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
     * @param cause The cause of the end. Null if this was a parkour completed.
     */
    @SuppressWarnings("unused")
    void end(ParkourPlayerFailEvent.FailCause cause);

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

    /**
     * Get a list of all of the parkour items. Even if items are disabled, they will still be present in this list, but will not give the item if disabled.
     * @return the list of parkour items.
     */
    @SuppressWarnings("unused")
    List<ParkourItem> getParkourItems();

    /**
     * Give a player all of the parkour items.
     */
    void giveItems();

    /**
     * Remove all of the parkour items and give them their original inventory.
     */
    void removeItems();

    /**
     * Get the active action bar task.
     * @return the BukkitTask object if it is active, null if not.
     */
    BukkitTask getActionBarTask();
}
