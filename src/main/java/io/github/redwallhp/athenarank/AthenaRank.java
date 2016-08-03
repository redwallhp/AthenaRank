package io.github.redwallhp.athenarank;

import io.github.redwallhp.athenarank.leaderboard.Leaderboard;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class AthenaRank extends JavaPlugin {


    public static AthenaRank instance;
    private MySQLPool pool;
    private Leaderboard leaderboard;


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        pool = new MySQLPool(getConfig());
        createTables();
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


    public Connection getSQLConnection() throws SQLException {
        return pool.getConnection();
    }


    private void createTables() {
        try {
            Connection conn = getSQLConnection();
            if (conn == null) return;
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE `rankings` (" +
                    "`uuid` VARCHAR(36) NOT NULL," +
                    "`name` VARCHAR(16)," +
                    "`kills` INTEGER NOT NULL DEFAULT '0'," +
                    "`captures` INTEGER NOT NULL DEFAULT '0'," +
                    "`deaths` INTEGER NOT NULL DEFAULT '0'," +
                    "PRIMARY KEY (`uuid`)" +
                    ");";
            stmt.executeUpdate(sql);
            conn.close();
            getLogger().info("Created initial database tables.");
        } catch (SQLException ex) {
            getLogger().warning("Error creating database tables: " + ex.getMessage());
        }
    }


    public Leaderboard getLeaderboard() {
        return leaderboard;
    }


}
