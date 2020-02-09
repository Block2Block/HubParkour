package me.block2block.hubparkour.managers;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.entities.plates.*;
import me.block2block.hubparkour.managers.database.MySQL;
import me.block2block.hubparkour.managers.database.SQLite;
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
        isMysql = isMySql;
        if (isMysql) {
            dbMySql = new MySQL(Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Hostname"), Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Port"),Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Database"), Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Username"), Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Password"));
            connection = dbMySql.openConnection();
        } else {
            dbSqlite = new SQLite(Main.getInstance().getConfig().getString("Settings.Database.Details.SQLite.File-Name"));
            connection = dbSqlite.openConnection();
        }
        createTables();
        loadParkours();
    }

    private void createTables() throws SQLException {
            try {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_playertimes (`uuid` varchar(36) NOT NULL, `parkour_id` " + ((isMysql)?"INT":"INTEGER") +  " NOT NULL,`time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                boolean set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_parkours (`id` " + ((isMysql)?"INT NOT NULL AUTO_INCREMENT":"INTEGER PRIMARY KEY AUTOINCREMENT") +  ",`name` TEXT NOT NULL,`finish_reward` TEXT DEFAULT NULL,`checkpoint_reward` TEXT DEFAULT NULL" + ((isMysql)?", PRIMARY KEY (id)":"") + ")");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_locations (`parkour_id` " + ((isMysql)?"INT":"INTEGER") +  ",`type` tinyint(3) NOT NULL,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `checkno` tinyint(64) NULL, `world` varchar(64) NOT NULL)");
                set = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error creating the tables. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
                throw e;
            }
    }

    public Parkour addParkour(Parkour parkour) {
        if (error) return null;
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_parkours(`name`,`finish_reward`,`checkpoint_reward`) VALUES (?,?,?)");
            statement.setString(1, parkour.getName());
            statement.setString(2, parkour.getEndCommand());
            statement.setString(3, parkour.getCheckpointCommand());
            statement.execute();

            statement = connection.prepareStatement("SELECT id FROM hp_parkours WHERE name = ?");
            statement.setString(1, parkour.getName());
            ResultSet results = statement.executeQuery();

            results.next();
            int id = results.getInt(1);
            statement = connection.prepareStatement("INSERT INTO hp_locations VALUES (?, ?, ?, ?, ?, ?, ?)");
            statement.setInt(1, id);
            statement.setInt(2, 2);
            statement.setInt(3, parkour.getRestartPoint().getLocation().getBlockX());
            statement.setInt(4, parkour.getRestartPoint().getLocation().getBlockY());
            statement.setInt(5, parkour.getRestartPoint().getLocation().getBlockZ());
            statement.setString(7, parkour.getRestartPoint().getLocation().getWorld().getName());
            statement.setNull(6, Types.INTEGER);

            statement.execute();

            for (PressurePlate plate : parkour.getAllPoints()) {
                statement = connection.prepareStatement("INSERT INTO hp_locations VALUES (?, ?, ?, ?, ?, ?, ?)");
                statement.setInt(1, id);
                statement.setInt(2, plate.getType());
                statement.setInt(3, plate.getLocation().getBlockX());
                statement.setInt(4, plate.getLocation().getBlockY());
                statement.setInt(5, plate.getLocation().getBlockZ());
                statement.setString(7, plate.getLocation().getWorld().getName());

                if (plate.getType() == 3) {
                    Checkpoint checkpoint = (Checkpoint) plate;
                    statement.setInt(6, checkpoint.getCheckpointNo());
                } else {
                    statement.setNull(6, Types.INTEGER);
                }

                statement.execute();
            }

            parkour = new Parkour(parkour, id);

            return parkour;


        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error loading parkours. Database functionality has been disabled until the server is restarted. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
            return null;
        }

    }

    public HashMap<Integer, List<String>> getLeaderboard(Parkour parkour, int limit) {
        HashMap<Integer, List<String>> leaderboard = new HashMap<>();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_playertimes WHERE parkour_id = ? ORDER BY `time` ASC" + ((limit!=-1)?" LIMIT " + limit:""));
            statement.setInt(1, parkour.getId());

            ResultSet results = statement.executeQuery();
            int counter = 1;
            while (results.next()) {
                List<String> record = new ArrayList<>();
                record.add(results.getString(4));
                record.add(results.getString(3));
                record.add(results.getString(1));
                leaderboard.put(counter, record);
                counter++;
            }

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
        return leaderboard;
    }

    public int leaderboardPosition(Player player, Parkour parkour) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_playertimes WHERE parkour_id = ? ORDER BY `time` ASC ");
            statement.setInt(1, parkour.getId());

            ResultSet results = statement.executeQuery();
            int counter = 1;
            while (results.next()) {
                if (results.getString(1).equals(player.getUniqueId().toString())) {
                    return counter;
                } else {
                    counter++;
                }
            }
        } catch (SQLException e) {
            return -1;
        }
        return -1;
    }

    public List<List<String>> getLocations(Parkour parkour) {
        List<List<String>> locations = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_locations WHERE parkour_id = ? ORDER BY `type`, `checkno` ASC");
            statement.setInt(1, parkour.getId());

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
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }

        return locations;
    }

    public long getTime(Player player, Parkour parkour) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_playertimes WHERE parkour_id = ? AND uuid = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(1, parkour.getId());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getLong(2);
            } else {
                return -1;
            }
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            e.printStackTrace();
        }
        return -1;
    }

    public void newTime(Player player, long time, boolean beatBefore, Parkour parkour) {
        if (beatBefore) {
            try {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_playertimes SET time = ? WHERE uuid = ? AND parkour_id = ?");

                statement.setLong(1, time);
                statement.setString(2, player.getUniqueId().toString());
                statement.setInt(3, parkour.getId());

                boolean result = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_playertimes(uuid, parkour_id, time, name) values (?,?,?,?)");

                statement.setString(1, player.getUniqueId().toString());
                statement.setInt(2, parkour.getId());
                statement.setLong(3, time);
                statement.setString(4, player.getName());

                boolean result = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }

        }
    }

    public void loadParkours() {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_parkours");

            ResultSet result = statement.executeQuery();
            List<Parkour> parkours = new ArrayList<>();
            while (result.next()) {
                statement = connection.prepareStatement("SELECT * FROM hp_locations WHERE parkour_id = " + result.getInt(1));
                ResultSet parkourPoints = statement.executeQuery();

                StartPoint start = null;
                EndPoint end = null;
                List<Checkpoint> checkpoints = new ArrayList<>();
                RestartPoint restart = null;
                String endCommand = result.getString(3);
                String checkCommand = result.getString(4);

                while (parkourPoints.next()) {
                    switch(parkourPoints.getInt(2)) {
                        case 0:
                            //3457
                            start = new StartPoint(new Location(Bukkit.getWorld(parkourPoints.getString(7)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5)));
                            break;
                        case 1:
                            end = new EndPoint(new Location(Bukkit.getWorld(parkourPoints.getString(7)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5)));
                            break;
                        case 2:
                            restart = new RestartPoint(new Location(Bukkit.getWorld(parkourPoints.getString(7)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5)));
                            break;
                        case 3:
                            checkpoints.add(new Checkpoint(new Location(Bukkit.getWorld(parkourPoints.getString(7)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5)), parkourPoints.getInt(6)));
                            break;
                    }
                }

                CacheManager.addParkour(new Parkour(result.getInt(1),result.getString(2), start, end, checkpoints, restart, checkCommand, endCommand));
            }

        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }

    }

    public void deleteParkour(Parkour parkour) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_playertimes WHERE `parkour_id` = ?");
            statement.setInt(1, parkour.getId());
            boolean result = statement.execute();

            statement = connection.prepareStatement("DELETE FROM hp_locations WHERE `parkour_id` = ?");
            statement.setInt(1, parkour.getId());
            result = statement.execute();

            statement = connection.prepareStatement("DELETE FROM hp_parkours WHERE `id` = ?");
            statement.setInt(1, parkour.getId());
            result = statement.execute();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
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
