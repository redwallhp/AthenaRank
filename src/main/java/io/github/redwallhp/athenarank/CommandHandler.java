package io.github.redwallhp.athenarank;

import io.github.redwallhp.athenarank.leaderboard.LeaderboardEntry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;


public class CommandHandler implements CommandExecutor {


    private AthenaRank plugin;
    private HashMap<String, String> resetConfirmation;


    public CommandHandler() {
        plugin = AthenaRank.instance;
        resetConfirmation = new HashMap<String, String>();
        plugin.getCommand("athenarank").setExecutor(this);
        plugin.getCommand("rankings").setExecutor(this);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("athenarank")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Valid subcommands: reload, resetplayer");
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                reloadCommand(sender);
            }
            else if (args[0].equalsIgnoreCase("resetplayer")) {
                resetCommand(sender, args);
            }
            else if (args[0].equalsIgnoreCase("confirmreset")) {
                confirmResetCommand(sender);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("rankings")) {
            rankingsCommand(sender);
            return true;
        }

        return false;

    }


    /**
     * Reload the configuration
     */
    private void reloadCommand(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage("Configuration reloaded");
    }


    /**
     * Display the top 10 players
     */
    private void rankingsCommand(CommandSender sender) {
        int i = 1;
        float kdr;
        String who;
        String stats;
        ChatColor color;
        sender.sendMessage(ChatColor.DARK_AQUA + "--- Top 10 Ranked Players ---");
        for (LeaderboardEntry e : plugin.getLeaderboard().getTopEntries()) {
            kdr = plugin.getLeaderboard().calculateKDR(e);
            who = String.format("%d. %s", i, e.getName());
            stats = String.format("[Kills: %d Caps: %d, Deaths: %d, KDR: %.2f]", e.getKills(), e.getCaptures(), e.getDeaths(), kdr);
            if (i % 2 == 0) {
                color = ChatColor.YELLOW;
            } else {
                color = ChatColor.GOLD;
            }
            sender.sendMessage(color + who);
            sender.sendMessage(ChatColor.GRAY + stats);
            i++;
        }
    }


    /**
     * Reset a player's stats (e.g. if caught cheating)
     */
    private void resetCommand(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /athenarank resetplayer <player>");
            return;
        }
        String str = String.format("Type %s/athenarank confirmreset%s to reset %s's ranking.", ChatColor.GREEN, ChatColor.RESET, args[1]);
        sender.sendMessage(ChatColor.RED + "Are you sure you want to potentially look very stupid?");
        sender.sendMessage(str);
        resetConfirmation.put(sender.getName(), args[1]);
    }


    /**
     * Actually reset a player's stats
     */
    private void confirmResetCommand(final CommandSender sender) {
        if (!resetConfirmation.containsKey(sender.getName())) {
            sender.sendMessage(ChatColor.RED + "You haven't specified a player to reset!");
            return;
        }
        final String name = resetConfirmation.get(sender.getName()); //player to reset
        new BukkitRunnable() {
            public void run() {
                try {
                    Connection conn = plugin.getSQLConnection();
                    String sql = "DELETE FROM `rankings` WHERE name=?;";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    int rows = stmt.executeUpdate();
                    conn.close();
                    if (rows > 0) {
                        sender.sendMessage(String.format("Stats for player '%s' successfully reset.", name));
                    } else {
                        sender.sendMessage(String.format("Could not find player '%s'", name));
                    }
                } catch (SQLException ex) {
                    plugin.getLogger().warning("Error resetting player stats: " + ex.getMessage());
                }
            }
        }.runTaskAsynchronously(plugin);
    }


}
