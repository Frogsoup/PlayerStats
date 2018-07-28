package me.frog.playerstats.database;

import me.frog.playerstats.PlayerStats;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.UUID;

public class DatabaseManager {

    private static PlayerStats instance = PlayerStats.getInstance();
    private static Connection connection;

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXIST user_stats (user_id INT(25) NOT NULL AUTO_INCREMENT PRIMARY KEY, user_uuid VARCHAR(64) NOT NULL, user VARCHAR(16) NOT NULL, time_online LONG NOT NULL, joined_on DATE NOT NULL, last_seen DATE NOT NULL);";
    private static final String INSERT_NEW_PLAYER = "INSERT INTO user_stats (user_uuid, user, time_online, joined_on, last_seen) values (?, ?, 0, now(), now());";

    /**
     * Called to establish a connection to the SQL
     * Server.
     *
     * @param address  The address of the SQL Server.
     * @param username The username of the SQL Server.
     * @param password The password of the SQL Server.
     * @param database The name of the database.
     */
    public synchronized static void establishConnection(String address, String username, String password, String database) {
        try {
            String url = "jdbc:mysql://" + address + ":3306/" + database;
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error establishing the connection!");
        }
    }

    /**
     * Called to check if the database exists, If it
     *  does not exist it will create it. It will do
     *  the same for the table.
     */
    public static void createDatabaseIfNotExist() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(CREATE_TABLE);
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error creating the database and/or table!");
        }
    }

    /**
     * Called to check if a player already exists in
     *  the database.
     *
     * @param playerUUID The UUID of the player.
     * @return Whether or not they exist. True if they do.
     */
    public boolean playerExist(UUID playerUUID) {
        boolean flag = false;

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_stats WHERE user_uuid=?");
            statement.setString(1, playerUUID.toString());

            ResultSet result = statement.executeQuery();
            if (result.next())
                flag = true;

            result.close();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error checking if a player exists!");
        }

        return flag;
    }

    /**
     * Called to check if a player already exists in
     *  the database.
     *
     * @param playerName The name of the player.
     * @return True if the player exists.
     */
    public boolean playerExist(String playerName) {
        boolean flag = false;

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_stats WHERE user=?");
            statement.setString(1, playerName);

            ResultSet result = statement.executeQuery();
            if (result.next())
                flag = true;

            result.close();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error checking if a player exists!");
        }

        return flag;
    }

    /**
     * Called to create a new player within the
     *  `user_stats` database.
     *
     * @param player     The name of the player.
     * @param playerUUID The UUID of the player.
     */
    public void addNewPlayer(String player, UUID playerUUID) {
        try {
            PreparedStatement statement = connection.prepareStatement(INSERT_NEW_PLAYER);
            statement.setString(1, playerUUID.toString());
            statement.setString(2, player);

            statement.executeUpdate();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error creating a new player!");
        }
    }

    /**
     * Called to get the UUID of a player
     *
     * @param player
     */
    public UUID getPlayerUUID(String player) {
        UUID playerUUID = null;
        String UUIDstr = null;

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_stats WHERE user=?");
            statement.setString(1, player);

            ResultSet result = statement.executeQuery();
            if (result.next())
                UUIDstr = result.getString("user_uuid");

            playerUUID = UUID.fromString(UUIDstr);

            result.close();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error getting a player's total time online!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Player's Name: " + player);
        }

        return playerUUID;
    }

    /**
     * Called to get the name of the user in
     *  the table. Used to check if a player
     *  has changed their name,
     *
     * @param playerUUID The UUID of the player.
     * @return The name of the player in the table.
     */
    public String checkPlayerNameChange(UUID playerUUID) {
        String user = null;

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_stats WHERE user_uuid=?");
            statement.setString(1, playerUUID.toString());

            ResultSet result = statement.executeQuery();
            if (result.next())
                user = result.getString("user");

            result.close();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error getting a player's name!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Player UUID: " + playerUUID);
        }

        return user;
    }

    /**
     * Called to change the player's name in
     *  the table.
     *
     * @param newName    The new name of the player.
     * @param playerUUID The UUID of the player.
     */
    public void changePlayerName(String newName, UUID playerUUID) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE user_stats SET user=? WHERE user_uuid=?");
            statement.setString(1, newName);
            statement.setString(2, playerUUID.toString());

            statement.executeUpdate();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error updating a player's name!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Player UUID: " + playerUUID);
        }
    }

    /**
     * Called to get the total time a player has been
     *  online, in seconds.
     *
     * @param playerUUID The UUID of the player.
     * @return The time online in seconds.
     */
    public long getTotalLogTime(UUID playerUUID) {
        long timeOnline = 0;

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_stats WHERE user_uuid=?");
            statement.setString(1, playerUUID.toString());

            ResultSet result = statement.executeQuery();
            if (result.next())
                timeOnline = result.getLong("time_online");

            result.close();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error getting a player's total time online!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Player UUID: " + playerUUID);
        }

        return timeOnline;
    }

    /**
     * Called to log/update the total time a player
     *  has spent online.
     *
     * @param playerUUID The UUID of the player.
     * @param timeOnline The time the player spent online in their current session.
     */
    public void logTimeOnline(UUID playerUUID, long timeOnline) {
        long totalTime = getTotalLogTime(playerUUID) + timeOnline;

        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE user_stats SET time_online=? WHERE user_uuid=?");
            statement.setLong(1, totalTime);
            statement.setString(2, playerUUID.toString());

            statement.executeUpdate();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error updating a player's total time online!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Player UUID: " + playerUUID);
        }
    }

    /**
     * Gets the join date of the player.
     *
     * @param playerUUID The UUID of the player.
     * @return The date as a string. Customize the format in the config.yml
     */
    public String getJoinedDate(UUID playerUUID) {
        String date = "null";

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_stats WHERE user_uuid=?");
            statement.setString(1, playerUUID.toString());

            ResultSet result = statement.executeQuery();
            if (result.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(instance.getConfigurationFile().getString("date-format"));
                Date unformatted = result.getDate("date_joined");
                date = dateFormat.format(unformatted);
            }

            result.close();
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error getting a player's join date!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Player UUID: " + playerUUID);
        }

        return date;
    }

    /**
     * Gets the date the player was last seen
     *  on the server.
     *
     * @param playerUUID The UUID of the player.
     * @return The date as a string. Customize the format in the config.yml
     */
    public String getLastSeen(UUID playerUUID) {
        String date = "null";

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM user_stats WHERE user_uuid=?");
            statement.setString(1, playerUUID.toString());

            ResultSet result = statement.executeQuery();
            if(result.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(instance.getConfigurationFile().getString("date-format"));
                Date unformatted = result.getDate("last_seen");
                date = dateFormat.format(unformatted);
            }

            result.close();
            statement.close();
        }catch(SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error getting a player's last seen date!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Player UUID: " + playerUUID);
        }

        return date;
    }

    /**
     * Called on disconnect to update the date the
     *  player was last online.
     *
     * @param playerUUID The UUID of the player.
     * @param lastSeen The date the player was last seen.
     */
    public void updateLastSeen(UUID playerUUID, Date lastSeen) {
        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE user_stats SET last_seen=? WHERE user_uuid=?");
            statement.setDate(1, lastSeen);
            statement.setString(2, playerUUID.toString());

            statement.executeUpdate();
            statement.close();
        }catch(SQLException exception) {
            exception.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error updating a player's last seen date!");
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Player UUID: " + playerUUID);
        }
    }

    /**
     * Gets the connection to the database.
     *
     * @return The connection.
     */
    public static Connection getConnection() { return connection; }
}
