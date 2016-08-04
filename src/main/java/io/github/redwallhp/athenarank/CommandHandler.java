package io.github.redwallhp.athenarank;

import io.github.redwallhp.athenarank.leaderboard.LeaderboardEntry;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class CommandHandler implements CommandExecutor {


    private AthenaRank plugin;


    public CommandHandler() {
        plugin = AthenaRank.instance;
        plugin.getCommand("athenarank").setExecutor(this);
        plugin.getCommand("rankings").setExecutor(this);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("athenarank")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Valid subcommands: reload");
            }
            else if (args[0].equalsIgnoreCase("reload")) {
                reloadCommand(sender);
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
            try {
                kdr = e.getKills() / e.getDeaths();
            } catch (ArithmeticException ex) {
                kdr = e.getKills();
            }
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


}
