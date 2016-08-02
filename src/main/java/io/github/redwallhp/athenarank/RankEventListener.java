package io.github.redwallhp.athenarank;

import io.github.redwallhp.athenagm.events.PlayerMurderPlayerEvent;
import io.github.redwallhp.athenagm.events.PlayerScorePointEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class RankEventListener implements Listener {


    private AthenaRank plugin;


    public RankEventListener() {
        plugin = AthenaRank.instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    /**
     * Create a new record for the player when they first join.
     * Also, update their cached name.
     */
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        new BukkitRunnable() {
            public void run() {
                try {
                    Connection conn = plugin.getSQLConnection();
                    String sql = "INSERT INTO `rankings` (uuid,name) VALUES (?,?) ON DUPLICATE KEY UPDATE name=?;";
                    PreparedStatement addPlayer = conn.prepareStatement(sql);
                    addPlayer.setString(1, event.getPlayer().getUniqueId().toString());
                    addPlayer.setString(2, event.getPlayer().getName());
                    addPlayer.setString(3, event.getPlayer().getName());
                    addPlayer.executeUpdate();
                    conn.close();
                } catch (SQLException ex) {
                    plugin.getLogger().warning("Error adding player to database: " + ex.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }


    /**
     * Update the killer and victim's scores after a PvP encounter
     */
    @EventHandler
    public void onPlayerMurderEvent(final PlayerMurderPlayerEvent event) {
        new BukkitRunnable() {
            public void run() {
                try {
                    Connection conn = plugin.getSQLConnection();
                    conn.setAutoCommit(false);
                    String killerSQL = "UPDATE `rankings` SET kills=kills+1 WHERE uuid=?;";
                    String victimSQL = "UPDATE `rankings` SET deaths=deaths+1 WHERE uuid=?;";
                    PreparedStatement updateKiller = conn.prepareStatement(killerSQL);
                    PreparedStatement updateVictim = conn.prepareStatement(victimSQL);
                    updateKiller.setString(1, event.getKiller().getUniqueId().toString());
                    updateVictim.setString(1, event.getVictim().getUniqueId().toString());
                    updateKiller.executeUpdate();
                    updateVictim.executeUpdate();
                    conn.commit();
                    conn.close();
                } catch (SQLException ex) {
                    plugin.getLogger().warning("Error updating record: " + ex.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }


    /**
     * Update the player's score when they obtain a point
     */
    @EventHandler
    public void onPlayerScoreEvent(final PlayerScorePointEvent event) {
        new BukkitRunnable() {
            public void run() {
                try {
                    Connection conn = plugin.getSQLConnection();
                    String sql = "UPDATE `rankings` SET captures=captures+1 WHERE uuid=?;";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, event.getPlayer().getUniqueId().toString());
                    stmt.executeUpdate();
                    conn.close();
                } catch (SQLException ex) {
                    plugin.getLogger().warning("Error updating record: " + ex.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }


}
