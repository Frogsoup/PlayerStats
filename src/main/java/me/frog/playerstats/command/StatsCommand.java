package me.frog.playerstats.command;

import me.frog.playerstats.PlayerStats;
import me.frog.playerstats.database.DatabaseManager;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class StatsCommand extends Command {

    private static PlayerStats instance = PlayerStats.getInstance();
    private DatabaseManager database = new DatabaseManager();

    public StatsCommand(PlayerStats instance) { super("stats", "playerstats.view"); }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;

            if(player.hasPermission("playerstats.view")) {
                if(args.length == 1) {
                    String targetPlayer = args[0];

                    if(database.playerExist(targetPlayer)) {
                        sendStatsMessage(sender, targetPlayer);
                    }else
                        player.sendMessage(new ComponentBuilder("The player '" + targetPlayer + "' does not exist!").color(ChatColor.RED).create());
                }else
                    player.sendMessage(new ComponentBuilder("Usage: /stats <PlayerName>").color(ChatColor.RED).create());
            }else
                player.sendMessage(new ComponentBuilder("You do not have permission to execute this command.").color(ChatColor.RED).create());
        }else {
            if(args.length == 1) {
                String targetPlayer = args[0];

                if(database.playerExist(targetPlayer)) {
                    sendStatsMessage(sender, targetPlayer);
                }else
                    sender.sendMessage(new ComponentBuilder("The player '" + targetPlayer + "' does not exist!").color(ChatColor.RED).create());
            }else
                sender.sendMessage(new ComponentBuilder("Usage: /stats <PlayerName>").color(ChatColor.RED).create());
        }
    }

    /**
     * Called to format the stats of the targetPlayer to
     *  the command sender.
     *
     * @param sender The player who executed the command.
     * @param targetPlayer The player whose stats are being looked up.
     */
    private void sendStatsMessage(CommandSender sender, String targetPlayer) {
        ProxyServer proxyServer = instance.getProxy();

        UUID targetUUID = database.getPlayerUUID(targetPlayer);

        String dateJoined = database.getJoinedDate(targetUUID);
        long timeOnline = database.getTotalLogTime(targetUUID);
        String lastSeen = database.getLastSeen(targetUUID);
        boolean onlineStatus = proxyServer.getPlayer(targetUUID).isConnected();
        String status = "§cOffline";

        if(onlineStatus)
            status = "§aOnline";

        sender.sendMessage(new ComponentBuilder("-------------------------").color(ChatColor.GREEN).create());
        sender.sendMessage(new ComponentBuilder("§7Player: §e" + targetPlayer).create());
        sender.sendMessage(new ComponentBuilder("§7Date Joined: §e" + dateJoined).create());
        sender.sendMessage(new ComponentBuilder("§7Time Online: §e" + formatTimeOnline(timeOnline)).create());
        sender.sendMessage(new ComponentBuilder("§7Last Seen: §e" + lastSeen).create());
        sender.sendMessage(new ComponentBuilder("§7Status: §e" + status).create());
        sender.sendMessage(new ComponentBuilder("-------------------------").color(ChatColor.GREEN).create());
    }

    /**
     * Called to format the time a player has been
     *  online based on the settings in the config.
     *
     * @param timeOnline The time online in seconds.
     * @return The time in the proper format.
     */
    private String formatTimeOnline(long timeOnline) {
        String format = String.format("%,.1d Seconds", timeOnline);
        double conversion;
        String configTimeFormat = instance.getConfigurationFile().getString("time-format");


        switch(configTimeFormat) {
            case "minutes":
                conversion = timeOnline / 60.0;
                format = String.format("%,.1f Minutes", conversion);
                break;
            case "hours":
                conversion = timeOnline / 3600.0;
                format = String.format("%,.1f Hours", conversion);
                break;
            case "days":
                conversion = timeOnline / 86400.0;
                format = String.format("%,.1f Days", conversion);
                break;
            case "weeks":
                conversion = timeOnline / 604800.0;
                format = String.format("%,.1f Weeks", conversion);
                break;
            case "months":
                conversion = timeOnline / 2592000.0;
                format = String.format("%,.1f Months", conversion);
                break;
            case "years":
                conversion = timeOnline / 31556952.0;
                format = String.format("%,.1f Years", conversion);
            default:
        }

        return format;
    }
}