package me.frog.playerstats.event;

import me.frog.playerstats.database.DatabaseManager;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Events implements Listener {

    private DatabaseManager manager = new DatabaseManager();

    private Map<UUID, Long> startTime = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void NewPlayer(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Checks if the player exists in the database.
        if(!manager.playerExist(playerUUID)) {
            manager.addNewPlayer(player.getName(), playerUUID);

            startTime.put(playerUUID, System.currentTimeMillis());

            return;
        }

        /*
           Searches the user's UUID in the database and checks
           if the user changed their name.
         */
        if(!manager.checkPlayerNameChange(playerUUID).equals(player.getName()))
            manager.changePlayerName(player.getName(), playerUUID);

        startTime.put(playerUUID, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void LogTime(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        long endTime = System.currentTimeMillis();

        // Gets the total time the player spent online in seconds.
        long timeOnline = (endTime - startTime.get(player.getUniqueId())) / 1000;

        // Used to update the last time a player logged on.
        Date lastSeen = new Date(endTime);

        manager.logTimeOnline(player.getUniqueId(), timeOnline);
        manager.updateLastSeen(player.getUniqueId(), lastSeen);
    }
}