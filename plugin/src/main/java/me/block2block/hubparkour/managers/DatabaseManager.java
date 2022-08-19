package me.block2block.hubparkour.managers;

import me.block2block.hubparkour.api.plates.*;
import me.block2block.hubparkour.api.signs.ClickableSign;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.LeaderboardHologram;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.entities.Statistics;
import me.block2block.hubparkour.signs.StartClickableSign;
import me.block2block.hubparkour.signs.StatsClickableSign;
import me.block2block.hubparkour.signs.TeleportClickableSign;
import me.block2block.hubparkour.utils.ConfigUtil;
import me.block2block.hubparkour.utils.database.MySQLConnectionPool;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;


@SuppressWarnings({"ALL", "UnusedAssignment"})
public class DatabaseManager {

    @SuppressWarnings("unused")
    public static DatabaseManager i;

    private static MySQLConnectionPool dbMySql;
    private static boolean isMysql;
    private static boolean error;

    @SuppressWarnings("unused")
    public DatabaseManager() {
        i = this;
    }

    public void setup(boolean isMySql) throws SQLException, ClassNotFoundException {
        isMysql = isMySql;
        if (isMysql) {
            dbMySql = new MySQLConnectionPool(ConfigUtil.getString("Settings.Database.Details.MySQL.Hostname", "localhost"), ConfigUtil.getString("Settings.Database.Details.MySQL.Port", "3306"),ConfigUtil.getString("Settings.Database.Details.MySQL.Database", "HubParkour"), ConfigUtil.getString("Settings.Database.Details.MySQL.Username", "root"), ConfigUtil.getString("Settings.Database.Details.MySQL.Password", ""));
        } else {
            setupSQLite(ConfigUtil.getString("Settings.Database.Details.SQLite.File-Name", "hp-storage.db"));
        }
        createTables();
        loadParkours();
        loadHolograms();
        loadSigns();
    }

    @SuppressWarnings("unused")
    private void createTables() throws SQLException {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_playertimes (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL,`time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                boolean set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_parkours (`id` INT NOT NULL AUTO_INCREMENT,`name` TEXT NOT NULL,`finish_reward` TEXT DEFAULT NULL,`checkpoint_reward` TEXT DEFAULT NULL, `reward_cooldown` INT NOT NULL DEFAULT -1, PRIMARY KEY (id))");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_locations (`parkour_id` INT,`type` tinyint(3) NOT NULL,`x` bigint(64) NOT NULL, `y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `pitch` FLOAT NOT NULL, `yaw` FLOAT NOT NULL, `checkno` tinyint(64) NULL, `world` varchar(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_holograms (`hologram_id` INT NOT NULL AUTO_INCREMENT, `parkour_id` INT,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL, PRIMARY KEY (hologram_id))");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_splittimes (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL, `checkpoint` INT NOT NULL, `time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_reachedcheckpoints (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL, `checkpoint` INT NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_lastruncompleted (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_rewardtimestamps (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL, `checkpoint` INT NOT NULL, `timestamp` TIMESTAMP NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_stats (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL, `completions` INT NOT NULL, `attempts` INT NOT NULL, `jumps` INT NOT NULL, `checkpoints` INT NOT NULL, `distance` DOUBLE NOT NULL, `total_time` bigint(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_signs (`sign_id` INT NOT NULL AUTO_INCREMENT, `parkour_id` INT,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL, `type` INT NOT NULL, PRIMARY KEY (sign_id))");
                set = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error creating the tables. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
                throw e;
            }
        } else {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_playertimes (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL,`time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                boolean set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_parkours (`id` INTEGER PRIMARY KEY AUTOINCREMENT,`name` TEXT NOT NULL,`finish_reward` TEXT DEFAULT NULL,`checkpoint_reward` TEXT DEFAULT NULL, `reward_cooldown` INTEGER NOT NULL DEFAULT -1)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_locations (`parkour_id` INTEGER,`type` tinyint(3) NOT NULL,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `pitch` FLOAT NOT NULL, `yaw` FLOAT NOT NULL, `checkno` tinyint(64) NULL, `world` varchar(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_holograms (`hologram_id` INTEGER PRIMARY KEY AUTOINCREMENT, `parkour_id` INTEGER,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_splittimes (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL, `checkpoint` INTEGER NOT NULL, `time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_reachedcheckpoints (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL, `checkpoint` INTEGER NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_rewardtimestamps (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL, `checkpoint` INTEGER NOT NULL, `timestamp` TIMESTAMP NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_lastruncompleted (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_stats (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL, `completions` INTEGER NOT NULL, `attempts` INTEGER NOT NULL, `jumps` INTEGER NOT NULL, `checkpoints` INTEGER NOT NULL, `distance` DOUBLE NOT NULL, `total_time` bigint(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS hp_signs (`sign_id` INTEGER PRIMARY KEY AUTOINCREMENT, `parkour_id` INTEGER,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL, `type` INTEGER NOT NULL)");
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
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_parkours(`name`,`finish_reward`,`checkpoint_reward`, `reward_cooldown`) VALUES (?,?,?,?)");
                statement.setString(1, parkour.getName());
                statement.setString(2, parkour.getEndCommand());
                statement.setString(3, parkour.getCheckpointCommand());
                statement.setInt(4, parkour.getRewardCooldown());
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

    public HashMap<Integer, List<String>> getLeaderboard(Parkour parkour, int limit) {
        HashMap<Integer, List<String>> leaderboard = new HashMap<>();

            try (Connection connection = getConnection()) {
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
            try (Connection connection = getConnection()) {
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

    @SuppressWarnings("unused")
    public List<List<String>> getLocations(Parkour parkour) {
        List<List<String>> locations = new ArrayList<>();
            try (Connection connection = getConnection()) {
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
        return locations;
    }

    public long getTime(Player player, Parkour parkour) {
            try (Connection connection = getConnection()) {
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
        return -1;
    }

    public List<Checkpoint> getReachedCheckpoints(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_reachedcheckpoints WHERE parkour_id = ? AND uuid = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(1, parkour.getId());

            ResultSet resultSet = statement.executeQuery();

            List<Checkpoint> checkpoints = new ArrayList<>();
            while (resultSet.next()) {
                checkpoints.add(parkour.getCheckpoint(resultSet.getInt(3)));
            }
            return checkpoints;
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public void resetReachedCheckpoints(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_reachedcheckpoints WHERE parkour_id = ? AND uuid = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(1, parkour.getId());

            statement.execute();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            e.printStackTrace();
        }
    }


    public void reachedCheckpoint(Player player, Parkour parkour, Checkpoint checkpoint) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_reachedcheckpoints VALUES (?, ?, ?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.setInt(2, parkour.getId());
            statement.setInt(3, checkpoint.getCheckpointNo());

            statement.execute();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            e.printStackTrace();
        }
    }

    public boolean wasCompletedLastRun(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_lastruncompleted WHERE parkour_id = ? AND uuid = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(1, parkour.getId());

            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            e.printStackTrace();
        }
        return false;
    }

    public void resetLastRun(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_lastruncompleted WHERE parkour_id = ? AND uuid = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(1, parkour.getId());

            statement.execute();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            e.printStackTrace();
        }
    }

    public void completedLastRun(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_lastruncompleted VALUES (?,?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.setInt(2, parkour.getId());

            statement.execute();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            e.printStackTrace();
        }
    }

    public long getTime(String player, Parkour parkour) {
            try (Connection connection = getConnection()) {
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
        return -1;
    }

    @SuppressWarnings("unused")
    public void newTime(Player player, long time, boolean beatBefore, Parkour parkour) {
        if (beatBefore) {
                try (Connection connection = getConnection()) {
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
                try (Connection connection = getConnection()) {
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

    @SuppressWarnings("unused")
    public void loadParkours() {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_parkours");

                ResultSet result = statement.executeQuery();
                List<Parkour> parkours = new ArrayList<>();
                while (result.next()) {
                    statement = connection.prepareStatement("SELECT * FROM hp_locations WHERE parkour_id = " + result.getInt(1));
                    ResultSet parkourPoints = statement.executeQuery();

                    StartPoint start = null;
                    EndPoint end = null;
                    List<Checkpoint> checkpoints = new ArrayList<>();
                    List<BorderPoint> borderPoints = new ArrayList<>();
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
                            case 4:
                                borderPoints.add(new BorderPoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6))));
                                break;
                        }
                    }

                    CacheManager.addParkour(new Parkour(result.getInt(1),result.getString(2), start, end, checkpoints, restart, borderPoints, checkCommand, endCommand, result.getInt(5)));
                }

            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void loadHolograms() {
            try (Connection connection = getConnection()) {
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

    public void loadSigns() {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_signs");

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                World world = Bukkit.getWorld(result.getString(6));
                if (world == null) {
                    Bukkit.getLogger().info("A world that a sign was in does not exist.");
                    continue;
                }

                Parkour parkour = CacheManager.getParkour(result.getInt(2));
                if (parkour == null) {
                    Bukkit.getLogger().info("A parkour that a sign was for does not exist.");
                    continue;
                }
                switch (result.getInt(7)) {
                    case 0: {
                        TeleportClickableSign sign = new TeleportClickableSign(parkour, (Sign) (new Location(world, result.getInt(3), result.getInt(4), result.getInt(5))).getBlock().getState());
                        CacheManager.getSigns().put(sign.getSignState().getLocation(), sign);
                        sign.refresh();
                        break;
                    }
                    case 1: {
                        StatsClickableSign sign = new StatsClickableSign(parkour, (Sign) (new Location(world, result.getInt(3), result.getInt(4), result.getInt(5))).getBlock().getState());
                        CacheManager.getSigns().put(sign.getSignState().getLocation(), sign);
                        sign.refresh();
                        break;
                    }
                    case 2: {
                        StartClickableSign sign = new StartClickableSign(parkour, (Sign) (new Location(world, result.getInt(3), result.getInt(4), result.getInt(5))).getBlock().getState());
                        CacheManager.getSigns().put(sign.getSignState().getLocation(), sign);
                        sign.refresh();
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public int addHologram(LeaderboardHologram hologram) {
            try (Connection connection = getConnection()) {
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

    public void removeHologram(LeaderboardHologram hologram) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_holograms WHERE hologram_id = ?");
                statement.setInt(1, hologram.getId());

                boolean success = statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void deleteParkour(Parkour parkour) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_playertimes WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                boolean result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM hp_splittimes WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM hp_reachedcheckpoints WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM hp_locations WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM hp_parkours WHERE `id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM hp_holograms WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM hp_lastruncompleted WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void resetTime(String name, int parkourId) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_playertimes WHERE `name` = ? AND `parkour_id` = ?");
                statement.setString(1, name);
                statement.setInt(2, parkourId);
                statement.execute();

                statement = connection.prepareStatement("DELETE FROM hp_splittimes WHERE `name` = ? AND `parkour_id` = ?");
                statement.setString(1, name);
                statement.setInt(2, parkourId);
                statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void resetTimes(int parkourId) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_playertimes WHERE `parkour_id` = ?");
                statement.setInt(1, parkourId);
                statement.execute();
                statement = connection.prepareStatement("DELETE FROM hp_splittimes WHERE `parkour_id` = ?");
                statement.setInt(1, parkourId);
                statement.execute();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void resetTimes(String name) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_playertimes WHERE `name` = ?");
            statement.setString(1, name);
            statement.execute();
            statement = connection.prepareStatement("DELETE FROM hp_splittimes WHERE `name` = ?");
            statement.setString(1, name);
            statement.execute();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public void resetTimes() {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_playertimes");
            statement.execute();
            statement = connection.prepareStatement("DELETE FROM hp_splittimes");
            statement.execute();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public void setName(int id, String name) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_parkours SET name = ? WHERE id = ?");
                statement.setString(1, name);
                statement.setInt(2, id);
                statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void setRewardCooldown(int id, int cooldown) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE hp_parkours SET reward_cooldown = ? WHERE id = ?");
            statement.setInt(1, cooldown);
            statement.setInt(2, id);
            statement.execute();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public void setEndCommand(int id, String command) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_parkours SET finish_reward = ? WHERE id = ?");
                if (command == null) {
                    statement.setNull(1, Types.VARCHAR);
                } else {
                    statement.setString(1, command);
                }
                statement.setInt(2, id);
                statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void setCheckpointCommand(int id, String command) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_parkours SET checkpoint_reward = ? WHERE id = ?");
                if (command != null) {
                    statement.setString(1, command);
                } else {
                    statement.setNull(1, Types.VARCHAR);
                }
                statement.setInt(2, id);
                statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void setStartPoint(int id, StartPoint point) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_locations SET x = ?, y = ?, z = ?, pitch = ?, yaw = ?, world = ? WHERE parkour_id = ? AND type = 0");
                statement.setInt(1, point.getLocation().getBlockX());
                statement.setInt(2, point.getLocation().getBlockY());
                statement.setInt(3, point.getLocation().getBlockZ());
                statement.setFloat(4, point.getLocation().getPitch());
                statement.setFloat(5, point.getLocation().getYaw());
                statement.setString(6, point.getLocation().getWorld().getName());
                statement.setInt(7, id);
                statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void setEndPoint(int id, EndPoint point) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_locations SET x = ?, y = ?, z = ?, pitch = ?, yaw = ?, world = ? WHERE parkour_id = ? AND type = 1");
                statement.setInt(1, point.getLocation().getBlockX());
                statement.setInt(2, point.getLocation().getBlockY());
                statement.setInt(3, point.getLocation().getBlockZ());
                statement.setFloat(4, point.getLocation().getPitch());
                statement.setFloat(5, point.getLocation().getYaw());
                statement.setString(6, point.getLocation().getWorld().getName());
                statement.setInt(7, id);
                statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void setRestartPoint(int id, RestartPoint point) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_locations SET x = ?, y = ?, z = ?, pitch = ?, yaw = ?, world = ? WHERE parkour_id = ? AND type = 2");
                statement.setInt(1, point.getLocation().getBlockX());
                statement.setInt(2, point.getLocation().getBlockY());
                statement.setInt(3, point.getLocation().getBlockZ());
                statement.setFloat(4, point.getLocation().getPitch());
                statement.setFloat(5, point.getLocation().getYaw());
                statement.setString(6, point.getLocation().getWorld().getName());
                statement.setInt(7, id);
                statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void updateCheckpointNumber(int id, Checkpoint point) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE hp_locations SET checkno = ? WHERE parkour_id = ? AND type = 3 AND x = ? AND y = ? AND z = ? AND pitch = ? AND yaw = ? AND world = ?");
                statement.setInt(1, point.getCheckpointNo());
                statement.setInt(2, id);
                statement.setInt(3, point.getLocation().getBlockX());
                statement.setInt(4, point.getLocation().getBlockY());
                statement.setInt(5, point.getLocation().getBlockZ());
                statement.setFloat(6, point.getLocation().getPitch());
                statement.setFloat(7, point.getLocation().getYaw());
                statement.setString(8, point.getLocation().getWorld().getName());
                statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void addCheckpoint(int id, Checkpoint checkpoint) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_locations VALUES (?, 3, ?, ?, ?, ?, ?, ?, ?)");
                statement.setInt(1, id);
                statement.setInt(2, checkpoint.getLocation().getBlockX());
                statement.setInt(3, checkpoint.getLocation().getBlockY());
                statement.setInt(4, checkpoint.getLocation().getBlockZ());
                statement.setFloat(5, checkpoint.getLocation().getPitch());
                statement.setFloat(6, checkpoint.getLocation().getYaw());
                statement.setInt(7, checkpoint.getCheckpointNo());
                statement.setString(8, checkpoint.getLocation().getWorld().getName());
                statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void deleteCheckpoint(int id, Checkpoint checkpoint) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_locations WHERE parkour_id = ? AND type = 3 AND checkno = ?");
                statement.setInt(1, id);
                statement.setInt(2, checkpoint.getCheckpointNo());
                statement.execute();
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
                error = true;
                e.printStackTrace();
            }
    }

    public void setBorders(int id, List<BorderPoint> borderPoints) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_locations WHERE parkour_id = ? AND type = 4");
            statement.setInt(1, id);
            statement.execute();
            for (BorderPoint point : borderPoints) {
                statement = connection.prepareStatement("INSERT INTO hp_locations VALUES (?, 4, ?, ?, ?, ?, ?, NULL, ?)");
                statement.setInt(1, id);
                statement.setInt(2, point.getLocation().getBlockX());
                statement.setInt(3, point.getLocation().getBlockY());
                statement.setInt(4, point.getLocation().getBlockZ());
                statement.setFloat(5, point.getLocation().getPitch());
                statement.setFloat(6, point.getLocation().getYaw());
                statement.setString(7, point.getLocation().getWorld().getName());
                statement.execute();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public Map<Integer, Long> getSplitTimes(Player player, Parkour parkour) {
        HashMap<Integer, Long> splitTimes = new HashMap<>();
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_splittimes WHERE uuid = ? AND parkour_id = ?");
            statement.setString(1, player.getUniqueId().toString());
            statement.setInt(2, parkour.getId());

            ResultSet set = statement.executeQuery();
            while (set.next()) {
                splitTimes.put(set.getInt(3), set.getLong(4));
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
        return splitTimes;
    }

    public void setSplitTime(Player player, Parkour parkour, int checkpoint, long time, boolean reachedBefore) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement;
            if (reachedBefore) {
                statement = connection.prepareStatement("UPDATE hp_splittimes SET time = ? WHERE uuid = ? AND parkour_id = ? AND checkpoint = ?");
                statement.setLong(1, time);
                statement.setString(2, player.getUniqueId().toString());
                statement.setInt(3, parkour.getId());
                statement.setInt(4, checkpoint);
            } else {
                statement = connection.prepareStatement("INSERT INTO hp_splittimes VALUES(?, ?, ?, ?, ?)");
                statement.setString(1, player.getUniqueId().toString());
                statement.setInt(2, parkour.getId());
                statement.setInt(3, checkpoint);
                statement.setLong(4, time);
                statement.setString(5, player.getName());
            }

            statement.execute();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public void resetSplitTimes(int parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_splittimes WHERE parkour_id = ?");
            statement.setInt(1, parkour);
            statement.execute();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public long getRecordTime(Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `time` FROM hp_playertimes WHERE parkour_id = ? ORDER BY `time` ASC LIMIT 1");
            statement.setInt(1, parkour.getId());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
        return -1;
    }

    public String getRecordHolder(Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `name` FROM hp_playertimes WHERE parkour_id = ? ORDER BY `time` ASC LIMIT 1");
            statement.setInt(1, parkour.getId());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
        return null;
    }

    public long getPositionTime(Parkour parkour, int position) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `time` FROM hp_playertimes WHERE parkour_id = ? ORDER BY `time` ASC LIMIT ?,1");
            statement.setInt(1, parkour.getId());
            statement.setInt(2, position - 1);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
        return -1;
    }

    public String getPositionHolder(Parkour parkour, int position) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `name` FROM hp_playertimes WHERE parkour_id = ? ORDER BY `time` ASC LIMIT ?,1");
            statement.setInt(1, parkour.getId());
            statement.setInt(2, position - 1);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
        return null;
    }


    public void addCompletion(HubParkourPlayer player, Parkour parkour, long time) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_stats WHERE parkour_id = ? AND uuid = ?");
            statement.setInt(1, parkour.getId());
            statement.setString(2, player.getPlayer().getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                statement = connection.prepareStatement("UPDATE hp_stats SET completions = completions + 1, attempts = attempts + 1, total_time = (total_time + ?), checkpoints = checkpoints + ?, jumps = jumps + ?, distance = distance + ? WHERE parkour_id = ? AND uuid = ?");
                statement.setLong(1, time);
                statement.setInt(2, player.getParkourRun().getCheckpointsHit());
                statement.setInt(3, player.getParkourRun().getJumps());
                statement.setDouble(4, player.getParkourRun().getTotalDistanceTravelled());
                statement.setInt(5, parkour.getId());
                statement.setString(6, player.getPlayer().getUniqueId().toString());
                statement.execute();
            } else {
                statement = connection.prepareStatement("INSERT INTO hp_stats VALUES (?, ?, 1, 1, ?, ?, ?, ?)");
                statement.setString(1, player.getPlayer().getUniqueId().toString());
                statement.setInt(2, parkour.getId());
                statement.setLong(6, time);
                statement.setInt(4, player.getParkourRun().getCheckpointsHit());
                statement.setInt(3, player.getParkourRun().getJumps());
                statement.setDouble(5, player.getParkourRun().getTotalDistanceTravelled());
                statement.execute();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public void addAttempt(HubParkourPlayer player, Parkour parkour, long time) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_stats WHERE parkour_id = ? AND uuid = ?");
            statement.setInt(1, parkour.getId());
            statement.setString(2, player.getPlayer().getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                statement = connection.prepareStatement("UPDATE hp_stats SET attempts = attempts + 1, total_time = (total_time + ?), checkpoints = checkpoints + ?, jumps = jumps + ?, distance = distance + ? WHERE parkour_id = ? AND uuid = ?");
                statement.setLong(1, time);
                statement.setInt(2, player.getParkourRun().getCheckpointsHit());
                statement.setInt(3, player.getParkourRun().getJumps());
                statement.setDouble(4, player.getParkourRun().getTotalDistanceTravelled());
                statement.setInt(5, parkour.getId());
                statement.setString(6, player.getPlayer().getUniqueId().toString());
                statement.execute();
            } else {
                statement = connection.prepareStatement("INSERT INTO hp_stats VALUES (?, ?, 0, 1, ?, ?, ?, ?)");
                statement.setString(1, player.getPlayer().getUniqueId().toString());
                statement.setInt(2, parkour.getId());
                statement.setLong(6, time);
                statement.setInt(4, player.getParkourRun().getCheckpointsHit());
                statement.setInt(3, player.getParkourRun().getJumps());
                statement.setDouble(5, player.getParkourRun().getTotalDistanceTravelled());
                statement.execute();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public Statistics getParkourStatistics(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_stats WHERE parkour_id = ? AND uuid = ?");
            statement.setInt(1, parkour.getId());
            statement.setString(2, player.getPlayer().getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            Map<Integer, Integer> jumps = new HashMap<>();
            Map<Integer, Integer> completions = new HashMap<>();
            Map<Integer, Integer> attempts = new HashMap<>();
            Map<Integer, Integer> checkpointsHit = new HashMap<>();
            Map<Integer, Double> totalDistanceTravelled = new HashMap<>();
            Map<Integer, Long> totalTime = new HashMap<>();
            while (set.next()) {
                jumps.put(parkour.getId(), set.getInt(5));
                completions.put(parkour.getId(), set.getInt(3));
                attempts.put(parkour.getId(), set.getInt(4));
                checkpointsHit.put(parkour.getId(), set.getInt(6));
                totalDistanceTravelled.put(parkour.getId(), set.getDouble(7));
                totalTime.put(parkour.getId(), set.getLong(8));
            }
            return new Statistics(player.getName(), jumps, completions, attempts, checkpointsHit, totalDistanceTravelled, totalTime);
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
            return null;
        }
    }

    public Statistics getGeneralStats(Player player) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_stats WHERE uuid = ?");
            statement.setString(1, player.getPlayer().getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            Map<Integer, Integer> jumps = new HashMap<>();
            Map<Integer, Integer> completions = new HashMap<>();
            Map<Integer, Integer> attempts = new HashMap<>();
            Map<Integer, Integer> checkpointsHit = new HashMap<>();
            Map<Integer, Double> totalDistanceTravelled = new HashMap<>();
            Map<Integer, Long> totalTime = new HashMap<>();
            while (set.next()) {
                jumps.put(set.getInt(2), set.getInt(5));
                completions.put(set.getInt(2), set.getInt(3));
                attempts.put(set.getInt(2), set.getInt(4));
                checkpointsHit.put(set.getInt(2), set.getInt(6));
                totalDistanceTravelled.put(set.getInt(2), set.getDouble(7));
                totalTime.put(set.getInt(2), set.getLong(8));
            }
            return new Statistics(player.getName(), jumps, completions, attempts, checkpointsHit, totalDistanceTravelled, totalTime);
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
            return null;
        }
    }


    private Connection getConnection() throws SQLException {
        if (isMysql) {
            return dbMySql.getConnection();
        } else {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (Exception e) {
                e.printStackTrace();
            }
            File dataFolder = Bukkit.getPluginManager().getPlugin("HubParkour").getDataFolder();
            String dbLocation = ConfigUtil.getString("Settings.Database.Details.SQLite.File-Name", "hp-storage.db");
            Connection dbSqlite = DriverManager
                    .getConnection("jdbc:sqlite:"
                            + dataFolder + "/"
                            + dbLocation);
            return dbSqlite;
        }
    }

    private void setupSQLite(String dbLocation) {
        try {
            File dataFolder = Bukkit.getPluginManager().getPlugin("HubParkour").getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File file = new File(dataFolder, dbLocation);
            if (!(file.exists())) {
                try {
                    file.createNewFile();
                    Bukkit.getLogger().info("Database file " + dbLocation + " successfully created!");
                } catch (IOException e) {
                    Bukkit.getLogger().info("Unable to create database. Stack Trace:");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().info("Unable to initialise SQLite connection. Stack Trace:");
            e.printStackTrace();
        }
    }

    public void updateTimestamp(UUID uuid, int id, int checkpoint, long timestamp) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM hp_rewardtimestamps WHERE parkour_id = ? AND uuid = ? AND checkpoint = ?");
            statement.setInt(1, id);
            statement.setString(2, uuid.toString());
            statement.setInt(3, checkpoint);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                statement = connection.prepareStatement("UPDATE hp_rewardtimestamps SET timestamp = ? WHERE uuid = ? AND parkour_id = ? AND checkpoint = ?");
                statement.setTimestamp(1, new Timestamp(timestamp));
                statement.setString(2, uuid.toString());
                statement.setInt(3, id);
                statement.setInt(4, checkpoint);
                statement.execute();
            } else {
                statement = connection.prepareStatement("INSERT INTO hp_rewardtimestamps VALUES (?, ?, ?, ?)");
                statement.setString(1, uuid.toString());
                statement.setInt(2, id);
                statement.setInt(3, checkpoint);
                statement.setTimestamp(4, new Timestamp(timestamp));
                statement.execute();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public long getTimestamp(UUID uuid, int id, int checkpoint) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `timestamp` FROM hp_rewardtimestamps WHERE parkour_id = ? AND uuid = ? AND checkpoint = ?");
            statement.setInt(1, id);
            statement.setString(2, uuid.toString());
            statement.setInt(3, checkpoint);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getTimestamp(1).getTime();
            } else {
                return -1;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
            return -1;
        }
    }

    public void addSign(ClickableSign sign) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO hp_signs(parkour_id, x, y, z, world, type) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setInt(1, sign.getParkour().getId());
            statement.setInt(2, sign.getSignState().getLocation().getBlockX());
            statement.setInt(3, sign.getSignState().getLocation().getBlockY());
            statement.setInt(4, sign.getSignState().getLocation().getBlockZ());
            statement.setString(5, sign.getSignState().getLocation().getWorld().getName());
            statement.setInt(6, sign.getType());

            boolean success = statement.execute();

            statement = connection.prepareStatement("SELECT sign_id FROM hp_signs WHERE parkour_id = ? AND x = ? AND y = ? AND z = ? AND world = ? AND type = ?");
            statement.setInt(1, sign.getParkour().getId());
            statement.setInt(2, sign.getSignState().getLocation().getBlockX());
            statement.setInt(3, sign.getSignState().getLocation().getBlockY());
            statement.setInt(4, sign.getSignState().getLocation().getBlockZ());
            statement.setString(5, sign.getSignState().getLocation().getWorld().getName());
            statement.setInt(6, sign.getType());
            ResultSet results = statement.executeQuery();

            results.next();
            sign.setId(results.getInt(1));
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }

    public void removeSign(ClickableSign sign) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM hp_signs WHERE sign_id = ?");
            statement.setInt(1, sign.getId());
            statement.execute();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            error = true;
            e.printStackTrace();
        }
    }
}
