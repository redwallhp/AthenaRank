package io.github.redwallhp.athenarank.leaderboard;

import io.github.redwallhp.athenarank.AthenaRank;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
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

        new BukkitRunnable() {
            public void run() {
                buildHubLeaderboard();
            }
        }.runTaskTimer(plugin, 20L, 1220L);

    }


    /**
     * Load the top players from the database
     */
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


    /**
     * Return the cached top players list
     */
    public List<LeaderboardEntry> getTopEntries() {
        return topEntries;
    }


    /**
     * Convert a "0,0,0" (x,y,z) string into a vector object
     */
    private Vector parseVectorString(String vectorString) {
        vectorString = vectorString.replaceAll("\\s+",""); //strip spaces
        String[] components = vectorString.split(",");
        try {
            double x = Double.parseDouble(components[0]);
            double y = Double.parseDouble(components[1]);
            double z = Double.parseDouble(components[2]);
            return new Vector(x, y, z);
        } catch(NumberFormatException ex) {
            return null;
        }
    }


    /**
     * Add leaderboard heads/signs to the Hub
     */
    private void buildHubLeaderboard() {

        // Return if this feature, or the Hub, is disabled
        if (!plugin.getConfig().getBoolean("leaderboard.enabled", false)) return;
        if (plugin.getAthena().getHub().getWorld() == null) return;

        // Load the coordinates
        List<Vector> headVectors = new ArrayList<Vector>();
        List<Vector> signVectors = new ArrayList<Vector>();
        for (String vs : plugin.getConfig().getStringList("leaderboard.heads")) {
            Vector vec = parseVectorString(vs);
            if (vec != null) {
                headVectors.add(vec);
            }
        }
        for (String vs : plugin.getConfig().getStringList("leaderboard.signs")) {
            Vector vec = parseVectorString(vs);
            if (vec != null) {
                signVectors.add(vec);
            }
        }

        // Set the heads
        World world = plugin.getAthena().getHub().getWorld();
        for (int i=0; i<headVectors.size(); i++) {
            Vector vec = headVectors.get(i);
            LeaderboardEntry entry = getEntryAtIndex(i);
            Block headBlock = world.getBlockAt(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
            if (headBlock.getType().equals(Material.SKULL)) {
                Skull skull = (Skull) headBlock.getState();
                BlockFace facing = skull.getRotation();
                headBlock.setData((byte) SkullType.PLAYER.ordinal());
                skull.setOwningPlayer(plugin.getServer().getOfflinePlayer(entry.getUuid()));
                skull.setRotation(facing);
                skull.update();
            }
        }

        // Set the signs
        for (int i=0; i<signVectors.size(); i++) {
            Vector vec = signVectors.get(i);
            LeaderboardEntry entry = getEntryAtIndex(i);
            Block signBlock = world.getBlockAt(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
            int rank = i + 1;
            float kdr = calculateKDR(entry);
            if (signBlock.getType().equals(Material.WALL_SIGN)) {
                Sign sign = (Sign) signBlock.getState();
                sign.setLine(1, String.format("%s%s", ChatColor.BOLD, entry.getName()));
                sign.setLine(2, String.format("Rank: #%d  KDR: %.1f", rank, kdr));
                sign.update();
            }
        }

    }


    private LeaderboardEntry getEntryAtIndex(int i) {
        if (i < topEntries.size()) {
            return topEntries.get(i);
        } else {
            //MHF_Steve
            return new LeaderboardEntry(UUID.fromString("c06f8906-4c8a-4911-9c29-ea1dbd1aab82"), "Nobody", 0, 0, 0, 0);
        }
    }


    private float calculateKDR(LeaderboardEntry entry) {
        float kdr;
        try {
            kdr = entry.getKills() / entry.getDeaths();
        } catch (ArithmeticException ex) {
            kdr = entry.getKills();
        }
        return kdr;
    }


}
