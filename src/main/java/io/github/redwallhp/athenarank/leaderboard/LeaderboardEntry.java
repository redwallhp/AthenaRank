package io.github.redwallhp.athenarank.leaderboard;

import java.util.UUID;


public class LeaderboardEntry {

    private UUID uuid;
    private String name;
    private int kills;
    private int captures;
    private int deaths;
    private double rank;

    public LeaderboardEntry(UUID uuid, String name, int kills, int captures, int deaths, double rank) {
        this.uuid = uuid;
        this.name = name;
        this.kills = kills;
        this.captures = captures;
        this.deaths = deaths;
        this.rank = rank;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getKills() {
        return kills;
    }

    public int getCaptures() {
        return captures;
    }

    public int getDeaths() {
        return deaths;
    }

    public double getRank() {
        return rank;
    }

}
