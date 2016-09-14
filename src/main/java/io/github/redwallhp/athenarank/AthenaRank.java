package io.github.redwallhp.athenarank;

import io.github.redwallhp.athenagm.AthenaGM;
import io.github.redwallhp.athenarank.leaderboard.Leaderboard;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class AthenaRank extends JavaPlugin {


    public static AthenaRank instance;
    private MySQLPool pool;
    private Leaderboard leaderboard;
    private AthenaGM athena;


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        pool = new MySQLPool(getConfig());
        createTables();
        checkAthena();
        new RankEventListener();
        leaderboard = new Leaderboard();
        new CommandHandler();
    }


    @Override
    public void onDisable() {
        if (pool != null) {
            pool.close();
        }
    }


    /**
     * Get a connection from the MySQL pool
     * @throws SQLException
     */
    public Connection getSQLConnection() throws SQLException {
        return pool.getConnection();
    }


    /**
     * Initialize the database tables
     */
    private void createTables() {
        try {
            Connection conn = getSQLConnection();
            if (conn == null) return;
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS `rankings` (" +
                    "`uuid` VARCHAR(36) NOT NULL," +
                    "`name` VARCHAR(16)," +
                    "`kills` INTEGER NOT NULL DEFAULT '0'," +
                    "`captures` INTEGER NOT NULL DEFAULT '0'," +
                    "`deaths` INTEGER NOT NULL DEFAULT '0'," +
                    "`distance` INTEGER NOT NULL DEFAULT '0'," +
                    "PRIMARY KEY (`uuid`)" +
                    ");";
            stmt.executeUpdate(sql);
            conn.close();
        } catch (SQLException ex) {
            getLogger().warning("Error creating database tables: " + ex.getMessage());
        }
    }


    /**
     * Returns the Leaderboard handler
     */
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }


    /**
     * Load the server's AthenaGM instance, returning false if AthenaGM is not installed.
     * @return true if AthenaGM is installed and active, false otherwise
     */
    private boolean checkAthena() {
        Plugin plugin = getServer().getPluginManager().getPlugin("AthenaGM");
        if (plugin == null || !(plugin instanceof AthenaGM)) {
            getLogger().warning("AthenaGM is not present. Disabling.");
            this.setEnabled(false);
            return false;
        } else {
            athena = (AthenaGM) plugin;
            return true;
        }
    }


    /**
     * Get the server's AthenaGM instance
     * @return AthenaGM instance
     */
    public AthenaGM getAthena() {
        return athena;
    }


}
