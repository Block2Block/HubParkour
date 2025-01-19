package me.block2block.hubparkour.api;

import me.block2block.hubparkour.api.plates.*;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

/**
 * Represents a parkour.
 */
public interface IParkour {

    /**
     * Gets the number of checkpoints this parkour has.
     * @return the number of checkpoints.
     */
    @SuppressWarnings("unused")
    int getNoCheckpoints();

    /**
     * Gets the ID of this parkour.
     * @return the parkour's id.
     */
    @SuppressWarnings("unused")
    int getId();

    /**
     * Get the name of this parkour. This includes colour codes.
     * @return the parkour's name.
     */
    @SuppressWarnings("unused")
    String getName();

    /**
     * Get the parkour end point pressure plate.
     * @return the end plate.
     */
    @SuppressWarnings("unused")
    EndPoint getEndPoint();

    /**
     * Get the exit point pressure plate.
     * @return the exit point.
     */
    ExitPoint getExitPoint();

    /**
     * Get the list of checkpoints active in this parkour.
     * @return list of all checkpoints.
     */
    @SuppressWarnings("unused")
    List<Checkpoint> getCheckpoints();

    /**
     * Gets the list of all players currently playing in the parkour.
     * @return list of active players.
     */
    @SuppressWarnings("unused")
    List<IHubParkourPlayer> getPlayers();

    /**
     * Gets the restart point of this parkour.
     * @return the restart point of this parkour.
     */
    @SuppressWarnings("unused")
    RestartPoint getRestartPoint();

    /**
     * Gets the start point of this parkour.
     * @return the start point of this parkour.
     */
    @SuppressWarnings("unused")
    StartPoint getStart();

    /**
     * Returns a list commands to execute for every checkpoint the user gets to.
     * @return the commands.
     */
    @SuppressWarnings("unused")
    List<String> getGlobalCheckpointCommands();

    /**
     * Get the list of commands to execute for the end of the parkour.
     * @return the commands.
     */
    @SuppressWarnings("unused")
    List<String> getEndCommands();

    /**
     * Get a list of all points for this parkour.
     * @return list of all points.
     */
    @SuppressWarnings("unused")
    List<PressurePlate> getAllPoints();

    /**
     * Generates all of the holograms for points. Requires holographic displays to be installed.
     */
    @SuppressWarnings("unused")
    void generateHolograms();

    /**
     * Remove all of the holograms for points. Requires holographic displays to be installed.
     */
    @SuppressWarnings("unused")
    void removeHolograms();

    /**
     * Start a player on this parkour.
     * @param p the player to start.
     */
    @SuppressWarnings("unused")
    void playerStart(IHubParkourPlayer p);

    /**
     * End the player on this parkour.
     * @param p the player to end.
     */
    @SuppressWarnings("unused")
    void playerEnd(IHubParkourPlayer p);

    /**
     * Get a specific checkpoint for this parkour.
     * @param checkpointNo the checkpoint to get.
     * @return the checkpoint.
     */
    @SuppressWarnings("unused")
    Checkpoint getCheckpoint(int checkpointNo);

    /**
     * Add a hologram as a part of this parkour.
     * @param hologram the hologram
     */
    void addHologram(ILeaderboardHologram hologram);

    /**
     * Remove a hologram that is a part of this parkour.
     * @param hologram the hologram.
     */
    void removeHologram(ILeaderboardHologram hologram);

    /**
     * Retrieves a list of leaderboard holograms attached to this parkour.
     * @return a list of leaderboards attached to this parkour.
     */
    List<ILeaderboardHologram> getLeaderboards();

    /**
     * Set the name of the parkour.
     * @param name The new name for the parkour.
     */
    void setName(String name);

    /**
     * Set the end commands for the parkour.
     * @param endCommands The new end commands for the parkour.
     */
    void setEndCommands(List<String> endCommands);

    /**
     * Sets the commands that will be executed globally for every checkpoint in this parkour.
     * @param globalCheckpointCommands The list of commands to be executed for global checkpoints.
     */
    void setGlobalCheckpointCommands(List<String> globalCheckpointCommands);

    /**
     * Set the start point of the parkour.
     * @param point The new start point for the parkour.
     */
    void setStartPoint(StartPoint point);

    /**
     * Set the end point of the parkour.
     * @param point The new end point for the parkour.
     */
    void setEndPoint(EndPoint point);

    /**
     * Set the exit point of the parkour.
     * @param exitPoint The new exit point for the parkour.
     * @param alreadyExists Whether the point already exists.
     */
    void setExitPoint(ExitPoint exitPoint, boolean alreadyExists);

    /**
     * Set the restart point of the parkour.
     * @param point The new restart point for the parkour.
     */
    void setRestartPoint(RestartPoint point);

    /**
     * Adds a new checkpoint to the parkour.
     * @param point The new start point for the parkour.
     * @param checkNo The checkpoint number this should become.
     */
    void addCheckpoint(Checkpoint point, int checkNo);

    /**
     * Deletes a checkpoint from the parkour.
     * @param point The checkpoint to remove.
     */
    void deleteCheckpoint(Checkpoint point);

    /**
     * Removes the exit point.
     */
    void deleteExitPoint();

    /**
     * Get a list of the border points setup for the map. Should only ever have length of 2.
     * @return the list of 2 border points.
     */
    List<BorderPoint> getBorders();

    /**
     * Get the configured reward cooldown.
     * @return the current reward cooldown.
     */
    int getRewardCooldown();

    /**
     * Get the UUID of the server this parkour is attached to.
     * @return the UUID of the server, or <i>null</i> if this parkour is global.
     */
    UUID getServer();

    /**
     * Retrieves the material of the item associated with this parkour.
     *
     * @return the material of the associated item.
     */
    Material getItemMaterial();

    /**
     * Retrieves the data for the item associated with this parkour.
     *
     * @return the data of the associated item.
     */
    short getItemData();

    /**
     * Sets the item associated with this parkour.
     *
     * @param material The material of the item to set.
     * @param data The data value of the item to set.
     */
    void setItem(Material material, short data);
}

