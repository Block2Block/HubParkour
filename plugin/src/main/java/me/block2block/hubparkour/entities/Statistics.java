package me.block2block.hubparkour.entities;

import java.util.Map;

public class Statistics {

    private final String playerName;
    private final Map<Integer, Integer> jumps;
    private final Map<Integer, Integer> completions;
    private final Map<Integer, Integer> attempts;
    private final Map<Integer, Integer> checkpointsHit;
    private final Map<Integer, Double> totalDistanceTravelled;
    private final Map<Integer, Long> totalTime;


    public Statistics(String name, Map<Integer, Integer> jumps, Map<Integer, Integer> completions, Map<Integer, Integer> attempts, Map<Integer, Integer> checkpointsHit, Map<Integer, Double> totalDistanceTravelled, Map<Integer, Long> totalTime) {
        this.playerName = name;
        this.jumps = jumps;
        this.completions = completions;
        this.attempts = attempts;
        this.checkpointsHit = checkpointsHit;
        this.totalDistanceTravelled = totalDistanceTravelled;
        this.totalTime = totalTime;
    }

    public Map<Integer, Double> getTotalDistanceTravelled() {
        return totalDistanceTravelled;
    }

    public Map<Integer, Integer> getAttempts() {
        return attempts;
    }

    public Map<Integer, Integer> getCheckpointsHit() {
        return checkpointsHit;
    }

    public Map<Integer, Integer> getCompletions() {
        return completions;
    }

    public Map<Integer, Integer> getJumps() {
        return jumps;
    }

    public Map<Integer, Long> getTotalTime() {
        return totalTime;
    }

    public String getPlayerName() {
        return playerName;
    }
}
