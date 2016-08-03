package io.github.redwallhp.athenarank.leaderboard;

import io.github.redwallhp.athenarank.AthenaRank;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handle the loading and calculation of the leaderboard.
 */
public class Leaderboard {


    private AthenaRank plugin;
    private final List<LeaderboardEntry> topEntries;


    public Leaderboard() {

        plugin = AthenaRank.instance;
        topEntries = new CopyOnWriteArrayList<LeaderboardEntry>();

        new BukkitRunnable() {
            public void run() {
                loadTopEntries();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1200L);

    }


    private void loadTopEntries() {
        try {
            Connection conn = plugin.getSQLConnection();
            String sql = "SELECT *, (kills + captures) / GREATEST(30, kills + captures + deaths) AS 'rank' " +
                    "FROM rankings ORDER BY rank DESC LIMIT 10;";
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(sql);
            if (res != null) {
                topEntries.clear();
                while (res.next()) {
                    UUID uuid = UUID.fromString(res.getString("uuid"));
                    String name = res.getString("name");
                    int kills = res.getInt("kills");
                    int captures = res.getInt("captures");
                    int deaths = res.getInt("deaths");
                    double rank = res.getDouble("rank");
                    LeaderboardEntry e = new LeaderboardEntry(uuid, name, kills, captures, deaths, rank);
                    topEntries.add(e);
                }
            }
            conn.close();
        } catch (Exception ex) {
            plugin.getLogger().warning("Error retrieving leaderboard cache: " + ex.getMessage());
        }
    }


    public List<LeaderboardEntry> getTopEntries() {
        return topEntries;
    }


}
