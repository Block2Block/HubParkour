package me.block2block.hubparkour.api;

import me.block2block.hubparkour.api.plates.*;

import java.util.List;

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
     * Get the command to execute for every checkpoint the user gets to.
     * @return the command.
     */
    @SuppressWarnings("unused")
    String getCheckpointCommand();

    /**
     * Get the command to execute for the end of the parkour.
     * @return the command.
     */
    @SuppressWarnings("unused")
    String getEndCommand();

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
     * Set the end command for the parkour.
     * @param endCommand The new end command for the parkour.
     */
    void setEndCommand(String endCommand);

    /**
     * Set the checkpoint command for the parkour.
     * @param checkpointCommand The new checkpoint command for the parkour.
     */
    void setCheckpointCommand(String checkpointCommand);

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
}

