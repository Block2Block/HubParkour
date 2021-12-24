package me.block2block.hubparkour.api;

/**
 * A parkour run to track player statistics.
 */
public class ParkourRun {

    private final IHubParkourPlayer player;
    private int jumps;
    private int checkpointsHit;
    private double totalDistanceTravelled;

    /**
     * Initiates a brand-new parkour run with empty statistics.
     * @param player the player who is doing this run.
     */
    public ParkourRun(IHubParkourPlayer player) {
        this.player = player;
        jumps = 0;
        checkpointsHit = 0;
        totalDistanceTravelled = 0.0d;
    }

    /**
     * Get the total distance travelled this run.
     * @return the total distance travelled this run.
     */
    public double getTotalDistanceTravelled() {
        return totalDistanceTravelled;
    }

    /**
     * Get the player who is doing this parkour run.
     * @return the player doing this parkour run.
     */
    public IHubParkourPlayer getPlayer() {
        return player;
    }

    /**
     * Get the amount of checkpoints the player has hit this run.
     * @return the amount of checkpoints.
     */
    public int getCheckpointsHit() {
        return checkpointsHit;
    }

    /**
     * Get the amount of times the player has jumped in this run.
     * @return the amount of jumps.
     */
    public int getJumps() {
        return jumps;
    }

    /**
     * Executed when a checkpoint is hit.
     */
    public void checkpointHit() {
        this.checkpointsHit++;
    }

    /**
     * Executed when the player jumps.
     */
    public void jumped() {
        this.jumps++;
    }

    /**
     * Executed when the player travels when doing the parkour.
     * @param distance the distance travelled.
     */
    public void addTravel(double distance) {
        this.totalDistanceTravelled += distance;
    }
}
