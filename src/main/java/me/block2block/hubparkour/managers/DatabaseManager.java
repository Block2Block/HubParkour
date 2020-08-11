package me.block2block.hubparkour.managers;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.plates.*;
import me.block2block.hubparkour.entities.LeaderboardHologram;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.utils.database.MySQLConnectionPool;
import me.block2block.hubparkour.utils.database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;


@SuppressWarnings({"ALL", "UnusedAssignment"})
public class DatabaseManager {

    @SuppressWarnings("unused")
    public static DatabaseManager i;

    private static MySQLConnectionPool dbMySql;
    private static SQLite dbSqlite;
    private static boolean isMysql;
    private static Connection connection;

    private static boolean error;

    @SuppressWarnings("unused")
    public DatabaseManager() {
        i = this;
    }

    public void setup(boolean isMySql) throws SQLException, ClassNotFoundException {
        isMysql = isMySql;
        if (isMysql) {
            dbMySql = new MySQLConnectionPool(Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Hostname"), Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Port"),Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Database"), Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Username"), Main.getInstance().getConfig().getString("Settings.Database.Details.MySQL.Password"));
        } else {
            dbSqlite = new SQLite(Main.getInstance().getConfig().getString("Settings.Database.Details.SQLite.File-Name"));
            connection = dbSqlite.openConnection();
        }
        createTables();
        loadParkours();
        loadHolograms();
    }

    @SuppressWarnings("unused")
    private void createTables() throws SQLException {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_playertimes (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL,`time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                boolean set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_parkours (`id` INT NOT NULL AUTO_INCREMENT,`name` TEXT NOT NULL,`finish_reward` TEXT DEFAULT NULL,`checkpoint_reward` TEXT DEFAULT NULL, PRIMARY KEY (id))");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_locations (`parkour_id` INT,`type` tinyint(3) NOT NULL,`x` bigint(64) NOT NULL, `y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `pitch` FLOAT NOT NULL, `yaw` FLOAT NOT NULL, `checkno` tinyint(64) NULL, `world` varchar(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_holograms (`hologram_id` INT NOT NULL AUTO_INCREMENT, `parkour_id` INT,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL, PRIMARY KEY (hologram_id))");
                set = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error creating the tables. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
                throw e;
            }
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_playertimes (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL,`time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                boolean set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_parkours (`id` INTEGER PRIMARY KEY AUTOINCREMENT,`name` TEXT NOT NULL,`finish_reward` TEXT DEFAULT NULL,`checkpoint_reward` TEXT DEFAULT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_locations (`parkour_id` INTEGER,`type` tinyint(3) NOT NULL,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `pitch` FLOAT NOT NULL, `yaw` FLOAT NOT NULL, `checkno` tinyint(64) NULL, `world` varchar(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_holograms (`hologram_id` INTEGER PRIMARY KEY AUTOINCREMENT, `parkour_id` INTEGER,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL)");
                set = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error creating the tables. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
                throw e;
            }
        }

    }

    public Parkour addParkour(Parkour parkour) {
        if (error) return null;
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
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
                for (PressurePlate plate : parkour.getAllPoints()) {
                    statement = connection.prepareStatement("INSERT INTO hp_locations VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    statement.setInt(1, id);
                    statement.setInt(2, plate.getType());
                    statement.setInt(3, plate.getLocation().getBlockX());
                    statement.setInt(4, plate.getLocation().getBlockY());
                    statement.setInt(5, plate.getLocation().getBlockZ());
                    statement.setFloat(6, plate.getLocation().getPitch());
                    statement.setFloat(7, plate.getLocation().getYaw());
                    statement.setString(9, plate.getLocation().getWorld().getName());

                    if (plate.getType() == 3) {
                        Checkpoint checkpoint = (Checkpoint) plate;
                        statement.setInt(8, checkpoint.getCheckpointNo());
                    } else {
                        statement.setNull(8, Types.INTEGER);
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
        } else {
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

                for (PressurePlate plate : parkour.getAllPoints()) {
                    statement = connection.prepareStatement("INSERT INTO hp_locations VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    statement.setInt(1, id);
                    statement.setInt(2, plate.getType());
                    statement.setInt(3, plate.getLocation().getBlockX());
                    statement.setInt(4, plate.getLocation().getBlockY());
                    statement.setInt(5, plate.getLocation().getBlockZ());
                    statement.setFloat(6, plate.getLocation().getPitch());
                    statement.setFloat(7, plate.getLocation().getYaw());
                    statement.setString(9, plate.getLocation().getWorld().getName());

                    if (plate.getType() == 3) {
                        Checkpoint checkpoint = (Checkpoint) plate;
                        statement.setInt(8, checkpoint.getCheckpointNo());
                    } else {
                        statement.setNull(8, Types.INTEGER);
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

    }

    public HashMap<Integer, List<String>> getLeaderboard(Parkour parkour, int limit) {
        HashMap<Integer, List<String>> leaderboard = new HashMap<>();

        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
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
        } else {
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
        }
        return leaderboard;
    }

    public int leaderboardPosition(Player player, Parkour parkour) {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
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
        } else {
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
        }
        return -1;
    }

    @SuppressWarnings("unused")
    public List<List<String>> getLocations(Parkour parkour) {
        List<List<String>> locations = new ArrayList<>();
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
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
                    location.add(results.getString(7));
                    location.add(results.getString(8));

                    locations.add(location);
                }

            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        } else {
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
                    location.add(results.getString(7));
                    location.add(results.getString(8));

                    locations.add(location);
                }

            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        }

        return locations;
    }

    public long getTime(Player player, Parkour parkour) {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_playertimes WHERE parkour_id = ? AND uuid = ?");
                statement.setString(2, player.getUniqueId().toString());
                statement.setInt(1, parkour.getId());

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getLong(3);
                } else {
                    return -1;
                }
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                e.printStackTrace();
            }
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_playertimes WHERE parkour_id = ? AND uuid = ?");
                statement.setString(2, player.getUniqueId().toString());
                statement.setInt(1, parkour.getId());

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getLong(3);
                } else {
                    return -1;
                }
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                e.printStackTrace();
            }
        }
        return -1;
    }

    public long getTime(String player, Parkour parkour) {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_playertimes WHERE parkour_id = ? AND name = ?");
                statement.setString(2, player);
                statement.setInt(1, parkour.getId());

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getLong(3);
                } else {
                    return -1;
                }
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                e.printStackTrace();
            }
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_playertimes WHERE parkour_id = ? AND name = ?");
                statement.setString(2, player);
                statement.setInt(1, parkour.getId());

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getLong(3);
                } else {
                    return -1;
                }
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                e.printStackTrace();
            }
        }
        return -1;
    }

    @SuppressWarnings("unused")
    public void newTime(Player player, long time, boolean beatBefore, Parkour parkour) {
        if (beatBefore) {
            if (isMysql) {
                try (Connection connection = dbMySql.getConnection()) {
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
                if (isMysql) {
                    try (Connection connection = dbMySql.getConnection()) {
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
                }
            }
        } else {
            if (isMysql) {
                try (Connection connection = dbMySql.getConnection()) {
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
    }

    @SuppressWarnings("unused")
    public void loadParkours() {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
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
                                start = new StartPoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)));
                                break;
                            case 1:
                                end = new EndPoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)));
                                break;
                            case 2:
                                restart = new RestartPoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)));
                                break;
                            case 3:
                                checkpoints.add(new Checkpoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)), parkourPoints.getInt(8)));
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
        } else {
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
                                start = new StartPoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)));
                                break;
                            case 1:
                                end = new EndPoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)));
                                break;
                            case 2:
                                restart = new RestartPoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)));
                                break;
                            case 3:
                                checkpoints.add(new Checkpoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)), parkourPoints.getInt(8)));
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

    }

    public void loadHolograms() {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_holograms");

                ResultSet result = statement.executeQuery();

                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString(6));
                    if (world == null) {
                        Bukkit.getLogger().info("A world that a leaderboard hologram was in does not exist.");
                        continue;
                    }

                    Parkour parkour = CacheManager.getParkour(result.getInt(2));
                    if (parkour == null) {
                        Bukkit.getLogger().info("A parkour that a leaderboard hologram was for does not exist.");
                        continue;
                    }
                    LeaderboardHologram hologram = new LeaderboardHologram(new Location(world, result.getInt(3), result.getInt(4), result.getInt(5)), parkour, result.getInt(1));
                    CacheManager.addHologram(hologram);
                    parkour.addHologram(hologram);
                }
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_holograms");

                ResultSet result = statement.executeQuery();

                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString(6));
                    if (world == null) {
                        Bukkit.getLogger().info("A world that a leaderboard hologram was in does not exist.");
                        continue;
                    }

                    Parkour parkour = CacheManager.getParkour(result.getInt(2));
                    if (parkour == null) {
                        Bukkit.getLogger().info("A parkour that a leaderboard hologram was for does not exist.");
                        continue;
                    }
                    LeaderboardHologram hologram = new LeaderboardHologram(new Location(world, result.getInt(3), result.getInt(4), result.getInt(5)), parkour, result.getInt(1));
                    CacheManager.addHologram(hologram);
                    parkour.addHologram(hologram);
                }
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        }
    }

    public int addHologram(LeaderboardHologram hologram) {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_holograms(parkour_id, x, y, z, world) VALUES (?, ?, ?, ?, ?)");
                statement.setInt(1, hologram.getParkour().getId());
                statement.setInt(2, hologram.getLocation().getBlockX());
                statement.setInt(3, hologram.getLocation().getBlockY());
                statement.setInt(4, hologram.getLocation().getBlockZ());
                statement.setString(5, hologram.getLocation().getWorld().getName());

                boolean success = statement.execute();

                statement = connection.prepareStatement("SELECT hologram_id FROM hp_holograms WHERE parkour_id = ? AND x = ? AND y = ? AND z = ? AND world = ?");
                statement.setInt(1, hologram.getParkour().getId());
                statement.setInt(2, hologram.getLocation().getBlockX());
                statement.setInt(3, hologram.getLocation().getBlockY());
                statement.setInt(4, hologram.getLocation().getBlockZ());
                statement.setString(5, hologram.getLocation().getWorld().getName());
                ResultSet results = statement.executeQuery();

                results.next();
                return results.getInt(1);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
                return -1;
            }
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_holograms(parkour_id, x, y, z, world) VALUES (?, ?, ?, ?, ?)");
                statement.setInt(1, hologram.getParkour().getId());
                statement.setInt(2, hologram.getLocation().getBlockX());
                statement.setInt(3, hologram.getLocation().getBlockY());
                statement.setInt(4, hologram.getLocation().getBlockZ());
                statement.setString(5, hologram.getLocation().getWorld().getName());

                boolean success = statement.execute();

                statement = connection.prepareStatement("SELECT hologram_id FROM hp_holograms WHERE parkour_id = ? AND x = ? AND y = ? AND z = ? AND world = ?");
                statement.setInt(1, hologram.getParkour().getId());
                statement.setInt(2, hologram.getLocation().getBlockX());
                statement.setInt(3, hologram.getLocation().getBlockY());
                statement.setInt(4, hologram.getLocation().getBlockZ());
                statement.setString(5, hologram.getLocation().getWorld().getName());
                ResultSet results = statement.executeQuery();

                results.next();
                return results.getInt(1);
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
                return -1;
            }
        }
    }

    public void removeHologram(LeaderboardHologram hologram) {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_holograms WHERE hologram_id = ?");
                statement.setInt(1, hologram.getId());

                boolean success = statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        } else {
            try {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_holograms WHERE hologram_id = ?");
                statement.setInt(1, hologram.getId());

                boolean success = statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        }
    }

    public void deleteParkour(Parkour parkour) {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
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
        } else {
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
    }

    public void resetTime(String name, int parkourId) {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_playertimes SET `time` = 9223372036854775807 WHERE `name` = ? AND `parkour_id` = ?");
                statement.setString(1, name);
                statement.setInt(2, parkourId);
                statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        } else {
            try {
                //Set it high rather than remove it to prevent the player claiming the reward again.
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_playertimes SET `time` = 9223372036854775807 WHERE `name` = ? AND `parkour_id` = ?");
                statement.setString(1, name);
                statement.setInt(2, parkourId);
                statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
        }
    }

    public void closeConnection() {
        try {
            if (!isMysql) {
                dbSqlite.closeConnection();
            }
        } catch (Exception ignored) {

        }
    }


}
