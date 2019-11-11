package me.Block2Block.HubParkour.Managers;

import me.Block2Block.HubParkour.Main;
import me.Block2Block.HubParkour.Managers.Database.MySQL;
import me.Block2Block.HubParkour.Managers.Database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;


public class DatabaseManager {

    public static DatabaseManager i;

    private static MySQL dbMySql;
    private static SQLite dbSqlite;
    private static boolean isMysql;
    private static Connection connection;

    private static boolean error;

    public DatabaseManager() {
        i = this;
    }

    public void setup(boolean isMySql) throws SQLException, ClassNotFoundException {
        this.isMysql = isMySql;
        if (isMysql) {
            dbMySql = new MySQL(Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Hostname"), Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Port"),Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Database"), Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Username"), Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Password"));
            connection = dbMySql.openConnection();
        } else {
            dbSqlite = new SQLite(Main.getInstance().getConfig().getString("Settings.Database.Details.SQLite.File-Name"));
            connection = dbSqlite.openConnection();
        }
        createTables();

    }

    private void createTables() {
            try {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_playertimes (`uuid` varchar(36) NOT NULL PRIMARY KEY, `time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                boolean set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_locations (`type` tinyint(3) NOT NULL,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `checkno` tinyint(64) NULL, `world` varchar(64) NOT NULL, PRIMARY KEY (`type`, x, y, z))");
                set = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error creating the tables. Database functionality has been disabled until the server is restarted. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void addLocation(Location location, int type, int checkNo) {
        if (error) return;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_locations VALUES (?,?,?,?,?,?)");
            statement.setInt(1, type);
            statement.setInt(2, x);
            statement.setInt(3, y);
            statement.setInt(4, z);
            statement.setString(6, location.getWorld().getName());

            if (checkNo == -1) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, checkNo);
            }

            statement.execute();

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error creating the tables. Database functionality has been disabled until the server is restarted. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }

    }

    public void setLocation(Location location, int type, int checkNo) {
        if (error) return;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        try {
            PreparedStatement statement;

            if (type!=3) {
                statement = connection.prepareStatement("UPDATE hp_locations SET x = ?,y = ?, z = ?, world = ? WHERE type = ?");

                statement.setInt(1, x);
                statement.setInt(2, y);
                statement.setInt(3, z);
                statement.setString(4, location.getWorld().getName());
                statement.setInt(5, type);
            } else {
                statement = connection.prepareStatement("UPDATE hp_locations SET x = ?,y = ?, z = ?, world = ? WHERE checkno = ?");

                statement.setInt(1, x);
                statement.setInt(2, y);
                statement.setInt(3, z);
                statement.setString(4, location.getWorld().getName());
                statement.setInt(5, checkNo);

            }

            statement.execute();

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error creating the tables. Database functionality has been disabled until the server is restarted. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }

    }

    public HashMap<Integer, List<String>> getLeaderboard() {
        HashMap<Integer, List<String>> leaderboard = new HashMap<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_playertimes ORDER BY `time` ASC ");


            ResultSet results = statement.executeQuery();
            if (results.next()) {
                List<String> record = new ArrayList<>();
                record.add(results.getString(3));
                record.add(results.getString(2));
                record.add(results.getString(1));
                leaderboard.put(1, record);
            }
            if (results.next()) {
                List<String> record = new ArrayList<>();
                record.add(results.getString(3));
                record.add(results.getString(2));
                record.add(results.getString(1));
                leaderboard.put(2, record);
            }
            if (results.next()) {
                List<String> record = new ArrayList<>();
                record.add(results.getString(3));
                record.add(results.getString(2));
                record.add(results.getString(1));
                leaderboard.put(3, record);
            }


        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Database functionality has been disabled until the server is restarted. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
        return leaderboard;
    }

    public List<List<String>> getLocations() {
        List<List<String>> locations = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_locations ORDER BY `type`, `checkno` ASC");

            ResultSet results = statement.executeQuery();

            while (results.next()) {
                List<String> location = new ArrayList<>();
                location.add(results.getString(1));
                location.add(results.getString(2));
                location.add(results.getString(3));
                location.add(results.getString(4));
                location.add(results.getString(5));
                location.add(results.getString(6));

                locations.add(location);
            }

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Database functionality has been disabled until the server is restarted. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }

        return locations;
    }

    public boolean beatBefore(Player player) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_playertimes WHERE uuid = ?");
            statement.setString(1, player.getUniqueId().toString());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Database functionality has been disabled until the server is restarted. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
        return false;
    }

    public boolean beatOldRecord(Player player, long time) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT time FROM hp_playertimes WHERE uuid = ?");
            statement.setString(1, player.getUniqueId().toString());

            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                return false;
            }

            if (resultSet.getLong(1) > time) {
                return true;
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Database functionality has been disabled until the server is restarted. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
        return false;
    }

    public void newTime(Player player, long time, boolean beatBefore) {
        if (beatBefore) {
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_playertimes SET time = ? WHERE uuid = ?");

                statement.setLong(1, time);
                statement.setString(2, player.getUniqueId().toString());

                boolean result = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Database functionality has been disabled until the server is restarted. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_playertimes(uuid, time, name) values (?,?,?)");

                statement.setString(1, player.getUniqueId().toString());
                statement.setLong(2, time);
                statement.setString(3, player.getName());

                boolean result = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Database functionality has been disabled until the server is restarted. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }

        }
    }

    public void closeConnection() {
        try {
            if (isMysql) {
                dbMySql.closeConnection();
            } else {
                dbSqlite.closeConnection();
            }
        } catch (Exception e) {

        }
    }


}
