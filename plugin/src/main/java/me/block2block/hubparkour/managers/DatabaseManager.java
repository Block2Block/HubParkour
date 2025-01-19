package me.block2block.hubparkour.managers;

import me.block2block.hubparkour.HubParkour;
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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;


@SuppressWarnings({"ALL", "UnusedAssignment"})
public class DatabaseManager {

    @SuppressWarnings("unused")
    public static DatabaseManager i;

    private static MySQLConnectionPool dbMySql;
    private static boolean isMysql;
    private static boolean error;
    private static String tablePrefix;

    @SuppressWarnings("unused")
    public DatabaseManager() {
        i = this;
    }

    public boolean setup(boolean isMySql) throws SQLException, ClassNotFoundException {
        isMysql = isMySql;
        if (isMysql) {
            dbMySql = new MySQLConnectionPool(ConfigUtil.getString("Settings.Database.Details.MySQL.Hostname", "localhost"), ConfigUtil.getString("Settings.Database.Details.MySQL.Port", "3306"),ConfigUtil.getString("Settings.Database.Details.MySQL.Database", "HubParkour"), ConfigUtil.getString("Settings.Database.Details.MySQL.Username", "root"), ConfigUtil.getString("Settings.Database.Details.MySQL.Password", ""), ConfigUtil.getString("Settings.Database.Details.MySQL.JDBC-Options","verifyServerCertificate=false&useSSL=false&requireSSL=false"));
        } else {
            setupSQLite(ConfigUtil.getString("Settings.Database.Details.SQLite.File-Name", "hp-storage.db"));
        }

        tablePrefix = ConfigUtil.getString("Settings.Database.Table-Prefix", "hp_");

        boolean tables = hasTables();
        return tables;
    }

    public void load() throws SQLException, ClassNotFoundException {
        createTables();
        loadParkours();
        loadHolograms();
        loadSigns();
    }

    @SuppressWarnings("unused")
    private void createTables() throws SQLException {
        if (isMysql) {
            try (Connection connection = dbMySql.getConnection()) {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "playertimes (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL,`time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                boolean set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "parkours (`id` INT NOT NULL AUTO_INCREMENT,`name` TEXT NOT NULL,`finish_reward` TEXT DEFAULT NULL,`checkpoint_reward` TEXT DEFAULT NULL, `reward_cooldown` INT NOT NULL DEFAULT -1, PRIMARY KEY (id), `server` varchar(36) DEFAULT NULL, `item_material` TEXT NOT NULL DEFAULT 'SLIME_BALL', `item_data` SMALLINT NOT NULL DEFAULT 0)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "locations (`parkour_id` INT,`type` tinyint(3) NOT NULL,`x` bigint(64) NOT NULL, `y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `pitch` FLOAT NOT NULL, `yaw` FLOAT NOT NULL, `checkno` tinyint(64) NULL, `world` varchar(64) NOT NULL, `rewards` TEXT DEFAULT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "holograms (`hologram_id` INT NOT NULL AUTO_INCREMENT, `parkour_id` INT,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL, PRIMARY KEY (hologram_id))");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "splittimes (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL, `checkpoint` INT NOT NULL, `time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "reachedcheckpoints (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL, `checkpoint` INT NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "lastruncompleted (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "rewardtimestamps (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL, `checkpoint` INT NOT NULL, `timestamp` TIMESTAMP NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "stats (`uuid` varchar(36) NOT NULL, `parkour_id` INT NOT NULL, `completions` INT NOT NULL, `attempts` INT NOT NULL, `jumps` INT NOT NULL, `checkpoints` INT NOT NULL, `distance` DOUBLE NOT NULL, `total_time` bigint(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "signs (`sign_id` INT NOT NULL AUTO_INCREMENT, `parkour_id` INT,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL, `type` INT NOT NULL, `facing` TEXT NOT NULL, `wall` BOOLEAN NOT NULL, PRIMARY KEY (sign_id))");
                set = statement.execute();
            } catch (Exception e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error creating the tables. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:", e);
                error = true;
                throw e;
            }
        } else {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "playertimes (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL,`time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                boolean set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "parkours (`id` INTEGER PRIMARY KEY AUTOINCREMENT,`name` TEXT NOT NULL,`finish_reward` TEXT DEFAULT NULL,`checkpoint_reward` TEXT DEFAULT NULL, `reward_cooldown` INTEGER NOT NULL DEFAULT -1, `server` varchar(36) DEFAULT NULL, `item_material` TEXT NOT NULL DEFAULT 'SLIME_BALL', `item_data` INTEGER NOT NULL DEFAULT 0)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "locations (`parkour_id` INTEGER,`type` tinyint(3) NOT NULL,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `pitch` FLOAT NOT NULL, `yaw` FLOAT NOT NULL, `checkno` tinyint(64) NULL, `world` varchar(64) NOT NULL, `rewards` TEXT)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "holograms (`hologram_id` INTEGER PRIMARY KEY AUTOINCREMENT, `parkour_id` INTEGER,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "splittimes (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL, `checkpoint` INTEGER NOT NULL, `time` bigint(64) NOT NULL, `name` varchar(16) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "reachedcheckpoints (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL, `checkpoint` INTEGER NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "rewardtimestamps (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL, `checkpoint` INTEGER NOT NULL, `timestamp` TIMESTAMP NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "lastruncompleted (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "stats (`uuid` varchar(36) NOT NULL, `parkour_id` INTEGER NOT NULL, `completions` INTEGER NOT NULL, `attempts` INTEGER NOT NULL, `jumps` INTEGER NOT NULL, `checkpoints` INTEGER NOT NULL, `distance` DOUBLE NOT NULL, `total_time` bigint(64) NOT NULL)");
                set = statement.execute();

                statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + tablePrefix + "signs (`sign_id` INTEGER PRIMARY KEY AUTOINCREMENT, `parkour_id` INTEGER,`x` bigint(64) NOT NULL,`y` bigint(64) NOT NULL,`z` bigint(64) NOT NULL, `world` varchar(64) NOT NULL, `type` INTEGER NOT NULL, `facing` TEXT NOT NULL, `wall` BOOLEAN NOT NULL)");
                set = statement.execute();
            } catch (Exception e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error creating the tables. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:", e);
                error = true;
                throw e;
            }
        }

    }

    public Parkour addParkour(Parkour parkour) {
        if (error) return null;
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "parkours(`name`,`finish_reward`,`checkpoint_reward`, `reward_cooldown`, `server`, `item_material`, `item_data`) VALUES (?,?,?,?,?,?,?)");
                statement.setString(1, parkour.getName());
                statement.setString(2, String.join(";", parkour.getEndCommands()));
                statement.setString(3, String.join(";", parkour.getGlobalCheckpointCommands()));
                statement.setInt(4, parkour.getRewardCooldown());
                if (parkour.getServer() != null) {
                    statement.setString(5, parkour.getServer().toString());
                } else {
                    statement.setNull(5, Types.VARCHAR);
                }

                statement.setString(6, parkour.getItemMaterial().name());
                statement.setShort(7, parkour.getItemData());

                statement.execute();

                statement = connection.prepareStatement("SELECT id FROM " + tablePrefix + "parkours WHERE name = ?");
                statement.setString(1, parkour.getName());
                ResultSet results = statement.executeQuery();

                results.next();
                int id = results.getInt(1);
                for (PressurePlate plate : parkour.getAllPoints()) {
                    statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "locations VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    statement.setInt(1, id);
                    statement.setInt(2, plate.getType());
                    statement.setInt(3, plate.getLocation().getBlockX());
                    statement.setInt(4, plate.getLocation().getBlockY());
                    statement.setInt(5, plate.getLocation().getBlockZ());
                    statement.setFloat(6, plate.getLocation().getPitch());
                    statement.setFloat(7, plate.getLocation().getYaw());
                    statement.setString(9, plate.getLocation().getWorld().getName());
                    if (plate.getRewards() != null) {
                        statement.setString(10, String.join(";", plate.getRewards()));
                    } else {
                        statement.setNull(10, Types.VARCHAR);
                    }

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
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error loading parkours. Database functionality has been disabled until the server is restarted. Try checking your config file to ensure that all details are correct and that your database is online. Stack trace:", e);
                error = true;
                return null;
            }
    }

    public HashMap<Integer, List<String>> getLeaderboard(Parkour parkour, int limit) {
        if (parkour == null) {
            return getLeaderboard(limit);
        }

        HashMap<Integer, List<String>> leaderboard = new HashMap<>();

            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "playertimes WHERE parkour_id = ? ORDER BY `time` ASC" + ((limit!=-1)?" LIMIT " + limit:""));
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
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
        return leaderboard;
    }

    public HashMap<Integer, List<String>> getLeaderboard(int limit) {

        HashMap<Integer, List<String>> leaderboard = new HashMap<>();

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "playertimes" + ((limit!=-1)?" LIMIT " + limit:""));
            ResultSet results = statement.executeQuery();
            Map<String, List<String>> times = new HashMap<>();
            Map<String, String> uuidToNames = new HashMap<>();
            while (results.next()) {

                uuidToNames.putIfAbsent(results.getString(1), results.getString(4));
                times.putIfAbsent(results.getString(1), new ArrayList<>());
                times.get(results.getString(1)).add(results.getString(3));
            }
            int amount = getNoParkours();
            Map<String, Long> totalTimes = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : times.entrySet()) {
                if (entry.getValue().size() == amount) {
                    long totalTime = 0;
                    for (String val : entry.getValue()) {
                        totalTime += Long.parseLong(val);
                    }
                    totalTimes.put(entry.getKey(), totalTime);
                }
            }

            List<Map.Entry<String, Long>> list = new ArrayList<>(totalTimes.entrySet());
            list.sort(Map.Entry.comparingByValue());
            Collections.reverse(list);
            int counter = 1;
            for (Map.Entry<String, Long> entry : list) {
                List<String> record = new ArrayList<>();

                record.add(uuidToNames.get(entry.getKey()));
                record.add(entry.getValue() + "");
                record.add(entry.getKey());

                leaderboard.put(counter, record);
                counter++;
            }
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
        return leaderboard;
    }

    public int leaderboardPosition(Player player, Parkour parkour) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "playertimes WHERE parkour_id = ? ORDER BY `time` ASC ");
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
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "locations WHERE parkour_id = ? ORDER BY `type`, `checkno` ASC");
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
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
        return locations;
    }

    public long getTime(Player player, Parkour parkour) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "playertimes WHERE parkour_id = ? AND uuid = ?");
                statement.setString(2, player.getUniqueId().toString());
                statement.setInt(1, parkour.getId());

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getLong(3);
                } else {
                    return -1;
                }
            } catch (Exception e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            }
        return -1;
    }

    public List<Checkpoint> getReachedCheckpoints(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "reachedcheckpoints WHERE parkour_id = ? AND uuid = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(1, parkour.getId());

            ResultSet resultSet = statement.executeQuery();

            List<Checkpoint> checkpoints = new ArrayList<>();
            while (resultSet.next()) {
                checkpoints.add(parkour.getCheckpoint(resultSet.getInt(3)));
            }
            return checkpoints;
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }
        return new ArrayList<>();
    }

    public void resetReachedCheckpoints(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "reachedcheckpoints WHERE parkour_id = ? AND uuid = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(1, parkour.getId());

            statement.execute();
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }
    }


    public void reachedCheckpoint(Player player, Parkour parkour, Checkpoint checkpoint) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "reachedcheckpoints VALUES (?, ?, ?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.setInt(2, parkour.getId());
            statement.setInt(3, checkpoint.getCheckpointNo());

            statement.execute();
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }
    }

    public boolean wasCompletedLastRun(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "lastruncompleted WHERE parkour_id = ? AND uuid = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(1, parkour.getId());

            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }
        return false;
    }

    public void resetLastRun(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "lastruncompleted WHERE parkour_id = ? AND uuid = ?");
            statement.setString(2, player.getUniqueId().toString());
            statement.setInt(1, parkour.getId());

            statement.execute();
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }
    }

    public void completedLastRun(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "lastruncompleted VALUES (?,?)");
            statement.setString(1, player.getUniqueId().toString());
            statement.setInt(2, parkour.getId());

            statement.execute();
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }
    }

    public long getTime(String player, Parkour parkour) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "playertimes WHERE parkour_id = ? AND name = ?");
                statement.setString(2, player);
                statement.setInt(1, parkour.getId());

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getLong(3);
                } else {
                    return -1;
                }
            } catch (Exception e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            }
        return -1;
    }

    @SuppressWarnings("unused")
    public void newTime(Player player, long time, boolean beatBefore, Parkour parkour) {
        if (beatBefore) {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "playertimes SET time = ? WHERE uuid = ? AND parkour_id = ?");

                    statement.setLong(1, time);
                    statement.setString(2, player.getUniqueId().toString());
                    statement.setInt(3, parkour.getId());

                    boolean result = statement.execute();
                } catch (Exception e) {
                    HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                    error = true;
                }
        } else {
                try (Connection connection = getConnection()) {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "playertimes(uuid, parkour_id, time, name) values (?,?,?,?)");

                    statement.setString(1, player.getUniqueId().toString());
                    statement.setInt(2, parkour.getId());
                    statement.setLong(3, time);
                    statement.setString(4, player.getName());

                    boolean result = statement.execute();
                } catch (Exception e) {
                    HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                    error = true;
                }
        }
    }

    @SuppressWarnings("unused")
    public void loadParkours() {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "parkours WHERE server IS NULL OR server = ?");
                statement.setString(1, HubParkour.getServerUuid().toString());
                ResultSet result = statement.executeQuery();
                List<Parkour> parkours = new ArrayList<>();
                while (result.next()) {
                    statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "locations WHERE parkour_id = " + result.getInt(1));
                    ResultSet parkourPoints = statement.executeQuery();

                    StartPoint start = null;
                    EndPoint end = null;
                    ExitPoint exit = null;
                    List<Checkpoint> checkpoints = new ArrayList<>();
                    List<BorderPoint> borderPoints = new ArrayList<>();
                    RestartPoint restart = null;
                    String endCommand = result.getString(3);
                    List<String> endCommands = null;
                    if (endCommand != null) {
                        endCommands = new ArrayList<>(Arrays.asList(endCommand.split(";")));
                    }
                    String checkCommand = result.getString(4);
                    List<String> checkCommands = null;
                    if (checkCommand != null) {
                        checkCommands = new ArrayList<>(Arrays.asList(checkCommand.split(";")));
                    }

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
                                String rewards = parkourPoints.getString(10);
                                List<String> commands = null;
                                if (rewards != null) {
                                    commands = new ArrayList<>(Arrays.asList(rewards.split(";")));
                                }
                                checkpoints.add(new Checkpoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)), parkourPoints.getInt(8), commands));
                                break;
                            case 4:
                                borderPoints.add(new BorderPoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6))));
                                break;
                            case 5:
                                exit = new ExitPoint(new Location(Bukkit.getWorld(parkourPoints.getString(9)), parkourPoints.getInt(3), parkourPoints.getInt(4), parkourPoints.getInt(5), parkourPoints.getFloat(7), parkourPoints.getFloat(6)));
                        }
                    }

                    Material material;
                    try {
                        material = Material.valueOf(result.getString(7));
                    } catch (IllegalArgumentException | NullPointerException e) {
                        HubParkour.getInstance().getLogger().log(Level.SEVERE, "One of your parkours has a material that is not valid. We have defaulted the value to a Slime Ball. Please change this using edit mode. Stack trace:", e);
                        material = Material.SLIME_BALL;
                    }
                    CacheManager.addParkour(new Parkour(result.getInt(1), ((result.getString(6) == null)?null:UUID.fromString(result.getString(6))), result.getString(2), start, end, exit, checkpoints, restart, borderPoints, checkCommands, endCommands, result.getInt(5), material, result.getShort(8)));
                }

            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public int getNoParkours() {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT count(*) FROM " + tablePrefix + "parkours");
            ResultSet result = statement.executeQuery();
            result.next();
            return result.getInt(1);
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
            return 0;
        }
    }

    public void loadHolograms() {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "holograms");

                ResultSet result = statement.executeQuery();

                while (result.next()) {
                    World world = Bukkit.getWorld(result.getString(6));
                    if (world == null) {
                        HubParkour.getInstance().getLogger().info("A world that a leaderboard hologram was in does not exist.");
                        continue;
                    }

                    Parkour parkour = CacheManager.getParkour(result.getInt(2));
                    if (parkour == null && result.getInt(2) != 0) {
                        HubParkour.getInstance().getLogger().info("A parkour that a leaderboard hologram was for does not exist.");
                        continue;
                    }
                    LeaderboardHologram hologram = new LeaderboardHologram(new Location(world, result.getInt(3), result.getInt(4), result.getInt(5)), parkour, result.getInt(1));
                    CacheManager.addHologram(hologram);
                    if (parkour != null) {
                        parkour.addHologram(hologram);
                    }
                }
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void loadSigns() {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "signs");

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                World world = Bukkit.getWorld(result.getString(6));
                if (world == null) {
                    HubParkour.getInstance().getLogger().info("A world that a sign was in does not exist.");
                    continue;
                }

                Parkour parkour = CacheManager.getParkour(result.getInt(2));
                if (parkour == null) {
                    HubParkour.getInstance().getLogger().info("A parkour that a sign was for does not exist.");
                    continue;
                }
                Location location = new Location(world, result.getInt(3), result.getInt(4), result.getInt(5));
                if (location.getBlock().getType() != Material.SIGN && location.getBlock().getType() != Material.WALL_SIGN && HubParkour.isPre1_13() && location.getBlock().getType() != Material.matchMaterial("SIGN_POST")) {
                    HubParkour.getInstance().getLogger().info("A registered sign has been removed from the world. Placing a sign back. It is recommended you remove then replace the sign.");
                    location.getBlock().setType(((result.getBoolean(9)?Material.WALL_SIGN: ((HubParkour.isPre1_13())?Material.matchMaterial("SIGN_POST"):Material.SIGN))));
                    org.bukkit.material.Sign sign = (org.bukkit.material.Sign) location.getBlock().getState().getData();
                    String face = result.getString(8);
                    if (face == null || face.equals("")) {
                        face = "NORTH";
                    }
                    sign.setFacingDirection(BlockFace.valueOf(face));

                    if (HubParkour.isPre1_13()) {
                        Method method = Block.class.getMethod("setData", byte.class, boolean.class);
                        method.invoke(location.getBlock(), sign.getData(), true);
                    } else {
                        location.getBlock().getState().setData(sign);
                    }
                    location.getBlock().getState().update(true);

                } else {
                    updateFacingWall(result.getInt(1), ((org.bukkit.material.Sign) location.getBlock().getState().getData()).getFacing(), location.getBlock().getType() == Material.WALL_SIGN);
                }
                switch (result.getInt(7)) {
                    case 0: {
                        TeleportClickableSign sign = new TeleportClickableSign(parkour, (Sign) location.getBlock().getState());
                        sign.setId(result.getInt(1));
                        CacheManager.getSigns().put(location.getBlock().getLocation(), sign);
                        sign.refresh();
                        break;
                    }
                    case 1: {
                        StatsClickableSign sign = new StatsClickableSign(parkour, (Sign) location.getBlock().getState());
                        sign.setId(result.getInt(1));
                        CacheManager.getSigns().put(location.getBlock().getLocation(), sign);
                        sign.refresh();
                        break;
                    }
                    case 2: {
                        StartClickableSign sign = new StartClickableSign(parkour, (Sign) location.getBlock().getState());
                        sign.setId(result.getInt(1));
                        CacheManager.getSigns().put(location.getBlock().getLocation(), sign);
                        sign.refresh();
                        break;
                    }
                }
            }
        } catch (SQLException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public int addHologram(LeaderboardHologram hologram) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "holograms(parkour_id, x, y, z, world) VALUES (?, ?, ?, ?, ?)");
                if (hologram.getParkour() != null) {
                    statement.setInt(1, hologram.getParkour().getId());
                } else {
                    statement.setNull(1, Types.INTEGER);
                }
                statement.setInt(2, hologram.getLocation().getBlockX());
                statement.setInt(3, hologram.getLocation().getBlockY());
                statement.setInt(4, hologram.getLocation().getBlockZ());
                statement.setString(5, hologram.getLocation().getWorld().getName());

                boolean success = statement.execute();

                statement = connection.prepareStatement("SELECT hologram_id FROM " + tablePrefix + "holograms WHERE x = ? AND y = ? AND z = ? AND world = ?");
                statement.setInt(1, hologram.getLocation().getBlockX());
                statement.setInt(2, hologram.getLocation().getBlockY());
                statement.setInt(3, hologram.getLocation().getBlockZ());
                statement.setString(4, hologram.getLocation().getWorld().getName());
                ResultSet results = statement.executeQuery();

                results.next();
                return results.getInt(1);
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
                return -1;
            }
    }

    public void removeHologram(LeaderboardHologram hologram) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "holograms WHERE hologram_id = ?");
                statement.setInt(1, hologram.getId());

                boolean success = statement.execute();
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void deleteParkour(Parkour parkour) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "playertimes WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                boolean result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "splittimes WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "reachedcheckpoints WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "locations WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "parkours WHERE `id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "holograms WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();

                statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "lastruncompleted WHERE `parkour_id` = ?");
                statement.setInt(1, parkour.getId());
                result = statement.execute();
            } catch (Exception e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void resetTime(String name, int parkourId) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "playertimes WHERE `name` = ? AND `parkour_id` = ?");
                statement.setString(1, name);
                statement.setInt(2, parkourId);
                statement.execute();

                statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "splittimes WHERE `name` = ? AND `parkour_id` = ?");
                statement.setString(1, name);
                statement.setInt(2, parkourId);
                statement.execute();
            } catch (Exception e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void resetTimes(int parkourId) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "playertimes WHERE `parkour_id` = ?");
                statement.setInt(1, parkourId);
                statement.execute();
                statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "splittimes WHERE `parkour_id` = ?");
                statement.setInt(1, parkourId);
                statement.execute();
            } catch (Exception e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void resetTimes(String name) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "playertimes WHERE `name` = ?");
            statement.setString(1, name);
            statement.execute();
            statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "splittimes WHERE `name` = ?");
            statement.setString(1, name);
            statement.execute();
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void resetTimes() {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "playertimes");
            statement.execute();
            statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "splittimes");
            statement.execute();
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void setName(int id, String name) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "parkours SET name = ? WHERE id = ?");
                statement.setString(1, name);
                statement.setInt(2, id);
                statement.execute();
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void setRewardCooldown(int id, int cooldown) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "parkours SET reward_cooldown = ? WHERE id = ?");
            statement.setInt(1, cooldown);
            statement.setInt(2, id);
            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void setItem(int id, Material item, short data) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "parkours SET item_material = ?, item_data = ? WHERE id = ?");
            statement.setString(1, item.name());
            statement.setShort(2, data);
            statement.setInt(3, id);
            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void setEndCommands(int id, List<String> commands) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "parkours SET finish_reward = ? WHERE id = ?");
                if (commands != null) {
                    statement.setString(1, String.join(";", commands));
                } else {
                    statement.setNull(1, Types.VARCHAR);
                }
                statement.setInt(2, id);
                statement.execute();
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void setGlobalCheckpointCommands(int id, List<String> commands) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "parkours SET checkpoint_reward = ? WHERE id = ?");
                if (commands != null) {
                    statement.setString(1, String.join(";", commands));
                } else {
                    statement.setNull(1, Types.VARCHAR);
                }
                statement.setInt(2, id);
                statement.execute();
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void setStartPoint(int id, StartPoint point) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "locations SET x = ?, y = ?, z = ?, pitch = ?, yaw = ?, world = ? WHERE parkour_id = ? AND type = 0");
                statement.setInt(1, point.getLocation().getBlockX());
                statement.setInt(2, point.getLocation().getBlockY());
                statement.setInt(3, point.getLocation().getBlockZ());
                statement.setFloat(4, point.getLocation().getPitch());
                statement.setFloat(5, point.getLocation().getYaw());
                statement.setString(6, point.getLocation().getWorld().getName());
                statement.setInt(7, id);
                statement.execute();
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void setEndPoint(int id, EndPoint point) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "locations SET x = ?, y = ?, z = ?, pitch = ?, yaw = ?, world = ? WHERE parkour_id = ? AND type = 1");
                statement.setInt(1, point.getLocation().getBlockX());
                statement.setInt(2, point.getLocation().getBlockY());
                statement.setInt(3, point.getLocation().getBlockZ());
                statement.setFloat(4, point.getLocation().getPitch());
                statement.setFloat(5, point.getLocation().getYaw());
                statement.setString(6, point.getLocation().getWorld().getName());
                statement.setInt(7, id);
                statement.execute();
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void setExitPoint(int id, ExitPoint point) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "locations VALUES(?, 5, ?, ?, ?, ?, ?, ?, ?, NULL)");
            statement.setInt(1, id);


            statement.setInt(2, point.getLocation().getBlockX());
            statement.setInt(3, point.getLocation().getBlockY());
            statement.setInt(4, point.getLocation().getBlockZ());
            statement.setFloat(5, point.getLocation().getPitch());
            statement.setFloat(6, point.getLocation().getYaw());
            statement.setNull(7, Types.INTEGER);
            statement.setString(8, point.getLocation().getWorld().getName());
            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void updateExitPoint(int id, ExitPoint point) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "locations SET x = ?, y = ?, z = ?, pitch = ?, yaw = ?, world = ? WHERE parkour_id = ? AND type = 5");
            statement.setInt(1, point.getLocation().getBlockX());
            statement.setInt(2, point.getLocation().getBlockY());
            statement.setInt(3, point.getLocation().getBlockZ());
            statement.setFloat(4, point.getLocation().getPitch());
            statement.setFloat(5, point.getLocation().getYaw());
            statement.setString(6, point.getLocation().getWorld().getName());
            statement.setInt(7, id);
            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void deleteExitPoint(int id) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "locations WHERE parkour_id = ? AND type = 5");
            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void setRestartPoint(int id, RestartPoint point) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "locations SET x = ?, y = ?, z = ?, pitch = ?, yaw = ?, world = ? WHERE parkour_id = ? AND type = 2");
                statement.setInt(1, point.getLocation().getBlockX());
                statement.setInt(2, point.getLocation().getBlockY());
                statement.setInt(3, point.getLocation().getBlockZ());
                statement.setFloat(4, point.getLocation().getPitch());
                statement.setFloat(5, point.getLocation().getYaw());
                statement.setString(6, point.getLocation().getWorld().getName());
                statement.setInt(7, id);
                statement.execute();
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void updateCheckpointNumber(int id, Checkpoint point) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "locations SET checkno = ? WHERE parkour_id = ? AND type = 3 AND x = ? AND y = ? AND z = ? AND pitch = ? AND yaw = ? AND world = ?");
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
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void addCheckpoint(int id, Checkpoint checkpoint) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "locations VALUES (?, 3, ?, ?, ?, ?, ?, ?, ?, ?)");
                statement.setInt(1, id);
                statement.setInt(2, checkpoint.getLocation().getBlockX());
                statement.setInt(3, checkpoint.getLocation().getBlockY());
                statement.setInt(4, checkpoint.getLocation().getBlockZ());
                statement.setFloat(5, checkpoint.getLocation().getPitch());
                statement.setFloat(6, checkpoint.getLocation().getYaw());
                statement.setInt(7, checkpoint.getCheckpointNo());
                statement.setString(8, checkpoint.getLocation().getWorld().getName());
                if (checkpoint.getRewards() != null && checkpoint.getRewards().size() > 0) {
                    statement.setString(9, String.join(";", checkpoint.getRewards()));
                } else {
                    statement.setNull(9, Types.VARCHAR);
                }
                statement.execute();
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void updateCheckpointRewards(int id, Checkpoint checkpoint) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "locations SET rewards = ? WHERE parkour_id = ? AND type = 3 AND checkno = ?");
            if (checkpoint.getRewards() != null && checkpoint.getRewards().size() > 0) {
                statement.setString(1, String.join(";", checkpoint.getRewards()));
            } else {
                statement.setNull(1, Types.VARCHAR);
            }
            statement.setInt(2, id);
            statement.setInt(3, checkpoint.getCheckpointNo());
            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void deleteCheckpoint(int id, Checkpoint checkpoint) {
            try (Connection connection = getConnection()) {
                PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "locations WHERE parkour_id = ? AND type = 3 AND checkno = ?");
                statement.setInt(1, id);
                statement.setInt(2, checkpoint.getCheckpointNo());
                statement.execute();
            } catch (SQLException e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
                error = true;
            }
    }

    public void setBorders(int id, List<BorderPoint> borderPoints) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "locations WHERE parkour_id = ? AND type = 4");
            statement.setInt(1, id);
            statement.execute();
            for (BorderPoint point : borderPoints) {
                statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "locations VALUES (?, 4, ?, ?, ?, ?, ?, NULL, ?, NULL)");
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
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public Map<Integer, Long> getSplitTimes(Player player, Parkour parkour) {
        HashMap<Integer, Long> splitTimes = new HashMap<>();
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "splittimes WHERE uuid = ? AND parkour_id = ?");
            statement.setString(1, player.getUniqueId().toString());
            statement.setInt(2, parkour.getId());

            ResultSet set = statement.executeQuery();
            while (set.next()) {
                splitTimes.put(set.getInt(3), set.getLong(4));
            }
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
        return splitTimes;
    }

    public void setSplitTime(Player player, Parkour parkour, int checkpoint, long time, boolean reachedBefore) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement;
            if (reachedBefore) {
                statement = connection.prepareStatement("UPDATE " + tablePrefix + "splittimes SET time = ? WHERE uuid = ? AND parkour_id = ? AND checkpoint = ?");
                statement.setLong(1, time);
                statement.setString(2, player.getUniqueId().toString());
                statement.setInt(3, parkour.getId());
                statement.setInt(4, checkpoint);
            } else {
                statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "splittimes VALUES(?, ?, ?, ?, ?)");
                statement.setString(1, player.getUniqueId().toString());
                statement.setInt(2, parkour.getId());
                statement.setInt(3, checkpoint);
                statement.setLong(4, time);
                statement.setString(5, player.getName());
            }

            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void resetSplitTimes(int parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "splittimes WHERE parkour_id = ?");
            statement.setInt(1, parkour);
            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public long getRecordTime(Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `time` FROM " + tablePrefix + "playertimes WHERE parkour_id = ? ORDER BY `time` ASC LIMIT 1");
            statement.setInt(1, parkour.getId());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
        return -1;
    }

    public String getRecordHolder(Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `name` FROM " + tablePrefix + "playertimes WHERE parkour_id = ? ORDER BY `time` ASC LIMIT 1");
            statement.setInt(1, parkour.getId());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
        return null;
    }

    public long getPositionTime(Parkour parkour, int position) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `time` FROM " + tablePrefix + "playertimes WHERE parkour_id = ? ORDER BY `time` ASC LIMIT ?,1");
            statement.setInt(1, parkour.getId());
            statement.setInt(2, position - 1);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getLong(1);
            } else {
                return -1;
            }
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
        return -1;
    }

    public String getPositionHolder(Parkour parkour, int position) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `name` FROM " + tablePrefix + "playertimes WHERE parkour_id = ? ORDER BY `time` ASC LIMIT ?,1");
            statement.setInt(1, parkour.getId());
            statement.setInt(2, position - 1);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                return set.getString(1);
            } else {
                return null;
            }
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
        return null;
    }


    public void addCompletion(HubParkourPlayer player, Parkour parkour, long time) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "stats WHERE parkour_id = ? AND uuid = ?");
            statement.setInt(1, parkour.getId());
            statement.setString(2, player.getPlayer().getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                statement = connection.prepareStatement("UPDATE " + tablePrefix + "stats SET completions = completions + 1, attempts = attempts + 1, total_time = (total_time + ?), checkpoints = checkpoints + ?, jumps = jumps + ?, distance = distance + ? WHERE parkour_id = ? AND uuid = ?");
                statement.setLong(1, time);
                statement.setInt(2, player.getParkourRun().getCheckpointsHit());
                statement.setInt(3, player.getParkourRun().getJumps());
                statement.setDouble(4, player.getParkourRun().getTotalDistanceTravelled());
                statement.setInt(5, parkour.getId());
                statement.setString(6, player.getPlayer().getUniqueId().toString());
                statement.execute();
            } else {
                statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "stats VALUES (?, ?, 1, 1, ?, ?, ?, ?)");
                statement.setString(1, player.getPlayer().getUniqueId().toString());
                statement.setInt(2, parkour.getId());
                statement.setLong(6, time);
                statement.setInt(4, player.getParkourRun().getCheckpointsHit());
                statement.setInt(3, player.getParkourRun().getJumps());
                statement.setDouble(5, player.getParkourRun().getTotalDistanceTravelled());
                statement.execute();
            }
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void addAttempt(HubParkourPlayer player, Parkour parkour, long time) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "stats WHERE parkour_id = ? AND uuid = ?");
            statement.setInt(1, parkour.getId());
            statement.setString(2, player.getPlayer().getUniqueId().toString());
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                statement = connection.prepareStatement("UPDATE " + tablePrefix + "stats SET attempts = attempts + 1, total_time = (total_time + ?), checkpoints = checkpoints + ?, jumps = jumps + ?, distance = distance + ? WHERE parkour_id = ? AND uuid = ?");
                statement.setLong(1, time);
                statement.setInt(2, player.getParkourRun().getCheckpointsHit());
                statement.setInt(3, player.getParkourRun().getJumps());
                statement.setDouble(4, player.getParkourRun().getTotalDistanceTravelled());
                statement.setInt(5, parkour.getId());
                statement.setString(6, player.getPlayer().getUniqueId().toString());
                statement.execute();
            } else {
                statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "stats VALUES (?, ?, 0, 1, ?, ?, ?, ?)");
                statement.setString(1, player.getPlayer().getUniqueId().toString());
                statement.setInt(2, parkour.getId());
                statement.setLong(6, time);
                statement.setInt(4, player.getParkourRun().getCheckpointsHit());
                statement.setInt(3, player.getParkourRun().getJumps());
                statement.setDouble(5, player.getParkourRun().getTotalDistanceTravelled());
                statement.execute();
            }
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public Statistics getParkourStatistics(Player player, Parkour parkour) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "stats WHERE parkour_id = ? AND uuid = ?");
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
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
            return null;
        }
    }

    public Statistics getGeneralStats(Player player) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "stats WHERE uuid = ?");
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
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
            return null;
        }
    }


    public Connection getConnection() throws SQLException {
        if (isMysql) {
            return dbMySql.getConnection();
        } else {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (Exception e) {
                HubParkour.getInstance().getLogger().log(Level.SEVERE, "Unable to initialise MySQL Connection Libraries. Stack Trace:", e);
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
                    HubParkour.getInstance().getLogger().info("Database file " + dbLocation + " successfully created!");
                } catch (IOException e) {
                    HubParkour.getInstance().getLogger().log(Level.SEVERE, "Unable to create database. Stack Trace:", e);
                }
            }
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "Unable to initialise SQLite connection. Stack Trace:", e);
        }
    }

    public void updateTimestamp(UUID uuid, int id, int checkpoint, long timestamp) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + tablePrefix + "rewardtimestamps WHERE parkour_id = ? AND uuid = ? AND checkpoint = ?");
            statement.setInt(1, id);
            statement.setString(2, uuid.toString());
            statement.setInt(3, checkpoint);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                statement = connection.prepareStatement("UPDATE " + tablePrefix + "rewardtimestamps SET timestamp = ? WHERE uuid = ? AND parkour_id = ? AND checkpoint = ?");
                statement.setTimestamp(1, new Timestamp(timestamp));
                statement.setString(2, uuid.toString());
                statement.setInt(3, id);
                statement.setInt(4, checkpoint);
                statement.execute();
            } else {
                statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "rewardtimestamps VALUES (?, ?, ?, ?)");
                statement.setString(1, uuid.toString());
                statement.setInt(2, id);
                statement.setInt(3, checkpoint);
                statement.setTimestamp(4, new Timestamp(timestamp));
                statement.execute();
            }
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public long getTimestamp(UUID uuid, int id, int checkpoint) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `timestamp` FROM " + tablePrefix + "rewardtimestamps WHERE parkour_id = ? AND uuid = ? AND checkpoint = ?");
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
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
            return -1;
        }
    }

    public void addSign(ClickableSign sign) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tablePrefix + "signs(parkour_id, x, y, z, world, type, facing, wall) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setInt(1, sign.getParkour().getId());
            statement.setInt(2, sign.getSignState().getLocation().getBlockX());
            statement.setInt(3, sign.getSignState().getLocation().getBlockY());
            statement.setInt(4, sign.getSignState().getLocation().getBlockZ());
            statement.setString(5, sign.getSignState().getLocation().getWorld().getName());
            statement.setInt(6, sign.getType());
            statement.setString(7, ((org.bukkit.material.Sign)sign.getSignState().getData()).getFacing().name());
            statement.setBoolean(8, sign.getSignState().getBlock().getType() == Material.WALL_SIGN);

            boolean success = statement.execute();

            statement = connection.prepareStatement("SELECT sign_id FROM " + tablePrefix + "signs WHERE parkour_id = ? AND x = ? AND y = ? AND z = ? AND world = ? AND type = ?");
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
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void updateFacingWall(int sign, BlockFace face, boolean wall) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE " + tablePrefix + "signs set facing = ?, wall = ? WHERE sign_id = ?");
            statement.setString(1, face.name());
            statement.setBoolean(2, wall);
            statement.setInt(3, sign);

            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public void removeSign(ClickableSign sign) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tablePrefix + "signs WHERE sign_id = ?");
            statement.setInt(1, sign.getId());
            statement.execute();
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
        }
    }

    public boolean hasTables() {
        try (Connection connection = getConnection()) {
            String sql;
            if (isMysql) {
                sql = "DESCRIBE " + ConfigUtil.getString("Settings.Database.Details.MySQL.Database", "HubParkour") + "." + tablePrefix + "parkours";
            } else {
                sql = "SELECT name FROM sqlite_master " +
                        "WHERE type='table'" +
                        "ORDER BY name;";
            }
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet set = statement.executeQuery();
            return set.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean hasData() {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT SUM(TABLE_ROWS) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + ConfigUtil.getString("Settings.Database.Details.MySQL.Database", "HubParkour") + "'");
            ResultSet set = statement.executeQuery();
            set.next();
            return set.getInt(1) > 0;
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
            return false;
        }
    }

    public boolean importData() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "Unable to initialise MySQL Connection Libraries. Stack Trace:", e);
        }
        File dataFolder = Bukkit.getPluginManager().getPlugin("HubParkour").getDataFolder();
        String dbLocation = ConfigUtil.getString("Settings.Database.Details.SQLite.File-Name", "hp-storage.db");
        try (Connection connection = getConnection();Connection dbSqlite = DriverManager
                .getConnection("jdbc:sqlite:"
                        + dataFolder + "/"
                        + dbLocation);) {


            copy(tablePrefix + "parkours", dbSqlite, connection);
            copy(tablePrefix + "locations", dbSqlite, connection);
            copy(tablePrefix + "playertimes", dbSqlite, connection);
            copy(tablePrefix + "lastruncompleted", dbSqlite, connection);
            copy(tablePrefix + "reachedcheckpoints", dbSqlite, connection);
            copy(tablePrefix + "rewardtimestamps", dbSqlite, connection);
            copy(tablePrefix + "splittimes", dbSqlite, connection);
            copy(tablePrefix + "stats", dbSqlite, connection);
            copy(tablePrefix + "holograms", dbSqlite, connection);
            return true;
        } catch (SQLException e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
            error = true;
            return false;
        }
    }

    public void copy(String table, Connection from, Connection to) throws SQLException {
        try (PreparedStatement s1 = from.prepareStatement("SELECT * FROM " + table);
             ResultSet rs = s1.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();

            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                columns.add(meta.getColumnName(i));
            }


            try (PreparedStatement s2 = to.prepareStatement(
                    "INSERT INTO " + table + " ("
                            + columns.stream().collect(Collectors.joining(", "))
                            + ") VALUES ("
                            + columns.stream().map(c -> "?").collect(Collectors.joining(", "))
                            + ")"
            )) {
                while (rs.next()) {
                    for (int i = 1; i <= meta.getColumnCount(); i++)
                        s2.setObject(i, rs.getObject(i));

                    s2.addBatch();
                }

                s2.executeBatch();
            }
        }
    }

    public static boolean isMysql() {
        return isMysql;
    }

    public static String getTablePrefix() {
        return tablePrefix;
    }
}
