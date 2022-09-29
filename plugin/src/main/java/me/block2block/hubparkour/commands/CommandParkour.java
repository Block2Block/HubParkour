package me.block2block.hubparkour.commands;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.ILeaderboardHologram;
import me.block2block.hubparkour.api.events.admin.ParkourDeleteEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerFailEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerLeaveEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerTeleportEvent;
import me.block2block.hubparkour.api.plates.PressurePlate;
import me.block2block.hubparkour.api.signs.ClickableSign;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.LeaderboardHologram;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.entities.Statistics;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.managers.DatabaseManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

@SuppressWarnings("unused")
public class CommandParkour implements CommandExecutor {


    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length > 0) {
                switch (args[0].toLowerCase()) {
                    case "reset":
                        if (p.hasPermission("hubparkour.command.reset")) {
                            if (CacheManager.isParkour(p)) {
                                ParkourPlayerTeleportEvent event = new ParkourPlayerTeleportEvent(CacheManager.getPlayer(p).getParkour(), CacheManager.getPlayer(p), CacheManager.getPlayer(p).getParkour().getRestartPoint());
                                Bukkit.getPluginManager().callEvent(event);
                                if (event.isCancelled()) {
                                    return true;
                                }
                                Location l = CacheManager.getPlayer(p).getParkour().getRestartPoint().getLocation().clone();
                                l.setX(l.getX() + 0.5);
                                l.setY(l.getY() + 0.5);
                                l.setZ(l.getZ() + 0.5);
                                p.setVelocity(new Vector(0, 0, 0));
                                p.teleport(l);
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Reset.Successful", "You have been teleported to the start.", true, Collections.emptyMap());
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Reset.Not-Started-Parkour", "You must start a parkour in order to reset!", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "checkpoint":
                        if (p.hasPermission("hubparkour.command.checkpoint")) {
                            if (CacheManager.isParkour(p)) {
                                HubParkourPlayer player = CacheManager.getPlayer(p);
                                ParkourPlayerTeleportEvent event = new ParkourPlayerTeleportEvent(player.getParkour(), player, (player.getLastReached() != 0)?player.getParkour().getCheckpoint(player.getLastReached()):player.getParkour().getRestartPoint());
                                Bukkit.getPluginManager().callEvent(event);
                                if (event.isCancelled()) {
                                    return true;
                                }
                                Location l = player.getParkour().getRestartPoint().getLocation().clone();
                                if (player.getLastReached() != 0) {
                                    l = player.getParkour().getCheckpoint(player.getLastReached()).getLocation().clone();
                                }
                                l.setX(l.getX() + 0.5);
                                l.setY(l.getY() + 0.5);
                                l.setZ(l.getZ() + 0.5);
                                p.setVelocity(new Vector(0, 0, 0));
                                p.teleport(l);
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Checkpoint.Successful", "You have been teleported to your last checkpoint.", true, Collections.emptyMap());
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Checkpoint.Not-Started-Parkour", "You must start a parkour in order to teleport to a checkpoint!", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }

                        break;
                    case "top10":
                    case "leaderboard":
                        if (p.hasPermission("hubparkour.command.leaderboard")) {
                            if (args.length >= 2) {
                                List<String> argsList = new ArrayList<>(Arrays.asList(args));
                                argsList.remove(0);
                                Parkour parkour = CacheManager.getParkour(String.join(" ", argsList));
                                if (parkour != null) {
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            StringBuilder sb = new StringBuilder(ConfigUtil.getString("Messages.Commands.Leaderboard.Message.Header", "The top times are:") + "\n");
                                            Map<Integer, List<String>> leaderboard = HubParkour.getInstance().getDbManager().getLeaderboard(parkour, ConfigUtil.getInt("Settings.Leaderboard.Limit", 10));

                                            for (int place : leaderboard.keySet()) {
                                                List<String> record = leaderboard.get(place);
                                                sb.append(ConfigUtil.getString("Messages.Commands.Leaderboard.Message.Line", "&a#{place} &r- &a{player-name} &r- &a{player-time} &rseconds.").replace("{player-name}", record.get(0)).replace("{player-time}", ConfigUtil.formatTime(Long.parseLong(record.get(1)))).replace("{place}", "" + place)).append("\n");
                                            }

                                            sb.append(ConfigUtil.getString("Messages.Commands.Leaderboard.Message.Footer", ""));
                                            String s = sb.toString();

                                            if (HubParkour.isPlaceholders()) {
                                                s = PlaceholderAPI.setPlaceholders(p, s);
                                            }

                                            p.sendMessage(HubParkour.c(true, s.trim()));
                                        }
                                    }.runTaskAsynchronously(HubParkour.getInstance());
                                } else {
                                    ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Leaderboard.Not-Valid-Parkour", "That is not a valid parkour.", true, Collections.emptyMap());
                                }
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Leaderboard.Not-Valid-Parkour", "That is not a valid parkour.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }

                        break;
                    case "leave":
                        if (p.hasPermission("hubparkour.command.leave")) {
                            if (CacheManager.isParkour(p)) {
                                HubParkourPlayer player = CacheManager.getPlayer(p);
                                ParkourPlayerLeaveEvent leaveEvent = new ParkourPlayerLeaveEvent(player.getParkour(), player);
                                Bukkit.getPluginManager().callEvent(leaveEvent);
                                if (leaveEvent.isCancelled()) {
                                    return true;
                                }
                                player.end(ParkourPlayerFailEvent.FailCause.LEAVE);
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Leave.Left", "You have left the parkour and your progress has been reset.", true, Collections.emptyMap());
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Leave.Not-In-Parkour", "You must have started a parkour in order to leave it.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }

                        break;
                    case "setup":
                        if (p.hasPermission("hubparkour.admin.setup")) {
                            if (!CacheManager.isParkour(p)) {
                                if (!CacheManager.alreadySetup()) {
                                    CacheManager.startSetup(p);
                                } else {
                                    ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Setup.Someone-Already-In-Setup", "Someone is already setting up a parkour. Please wait for them to finish in order to setup another parkour.", true, Collections.emptyMap());
                                }
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Setup.Must-Not-Be-In-Parkour", "You must not be in a parkour in order to set up a parkour.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "list":
                        if (p.hasPermission("hubparkour.admin.list")) {
                            StringBuilder sb = new StringBuilder(ConfigUtil.getString("Messages.Commands.Admin.List.Header", "All active parkours:") + "\n");
                            for (Parkour parkour : CacheManager.getParkours()) {
                                sb.append(ConfigUtil.getString("Messages.Commands.Admin.List.Line", "&aID: {id} &r- &a{parkour-name} &r- &a{parkour-players} &ractive players.").replace("{parkour-name}", parkour.getName()).replace("{parkour-players}", "" + parkour.getPlayers().size()).replace("{id}", "" + parkour.getId())).append("\n");
                            }

                            sb.append(ConfigUtil.getString("Messages.Commands.Admin.List.Footer", ""));
                            String s = sb.toString();

                            if (HubParkour.isPlaceholders()) {
                                s = PlaceholderAPI.setPlaceholders(p, s);
                            }

                            p.sendMessage(HubParkour.c(true, s.trim()));
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "delete":
                        if (p.hasPermission("hubparkour.admin.delete")) {
                            if (args.length >= 2) {
                                int id = -1;
                                try {
                                    id = Integer.parseInt(args[1]);
                                } catch (NumberFormatException ignored) {
                                }
                                Parkour parkour;
                                if (id != -1) {
                                    parkour = CacheManager.getParkour(id);
                                } else {
                                    List<String> argsList = new ArrayList<>(Arrays.asList(args));
                                    argsList.remove(0);
                                    parkour = CacheManager.getParkour(String.join(" ", argsList));
                                }

                                if (parkour != null) {
                                    ParkourDeleteEvent deleteEvent = new ParkourDeleteEvent(parkour, p);
                                    Bukkit.getPluginManager().callEvent(deleteEvent);
                                    for (IHubParkourPlayer player : parkour.getPlayers()) {
                                        ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Delete.Player-Kicked-From-Parkour", "The parkour you were doing was deleted. You have left the parkour.", true, Collections.emptyMap());
                                        CacheManager.playerEnd((HubParkourPlayer) player);
                                    }

                                    parkour.removeHolograms();
                                    for (PressurePlate pp : parkour.getAllPoints()) {
                                        CacheManager.removePlate(pp);
                                    }

                                    for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                        hologram.remove();
                                    }

                                    new BukkitRunnable(){
                                        @Override
                                        public void run() {
                                            for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                                CacheManager.removeHologram((LeaderboardHologram) hologram);
                                                HubParkour.getInstance().getDbManager().removeHologram((LeaderboardHologram) hologram);
                                            }
                                            HubParkour.getInstance().getDbManager().deleteParkour(parkour);
                                            for (ClickableSign sign : new ArrayList<>(CacheManager.getSigns().values())) {
                                                if (sign.getParkour().equals(parkour)) {
                                                    CacheManager.getSigns().remove(sign.getSignState().getLocation());
                                                    HubParkour.getInstance().getDbManager().removeSign(sign);
                                                }
                                            }
                                            CacheManager.getParkours().remove(parkour);
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Delete.Success", "Parkour deleted successfully.", true, Collections.emptyMap());
                                        }
                                    }.runTaskAsynchronously(HubParkour.getInstance());
                                } else {
                                    ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Delete.Not-Valid-Parkour", "That is not a valid parkour ID. If you wish to see a list of all parkours and their IDs, do /parkour list.", true, Collections.emptyMap());
                                }
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Delete.Not-Valid-Parkour", "That is not a valid parkour ID. If you wish to see a list of all parkours and their IDs, do /parkour list.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "hologram":
                        if (p.hasPermission("hubparkour.admin.hologram")) {
                            if (HubParkour.isHolograms()) {
                                if (args.length > 1) {
                                    switch (args[1]) {
                                        case "list":
                                            StringBuilder list = new StringBuilder(ConfigUtil.getString("Messages.Commands.Admin.Hologram.List.Header", "All Active Holograms:") + "\n");
                                            for (LeaderboardHologram hologram : CacheManager.getLeaderboards()) {
                                                list.append(ConfigUtil.getString("Messages.Commands.Admin.Hologram.List.Line", "&aID: {id} &r- &a{parkour-name}").replace("{parkour-name}", ((hologram.getParkour() != null)?hologram.getParkour().getName():"Global")).replace("{id}", "" + hologram.getId())).append("\n");
                                            }

                                            list.append(ConfigUtil.getString("Messages.Commands.Admin.Hologram.List.Footer", ""));
                                            String s = list.toString();

                                            if (HubParkour.isPlaceholders()) {
                                                s = PlaceholderAPI.setPlaceholders(p, s);
                                            }

                                            p.sendMessage(HubParkour.c(true, s.trim()));
                                            break;
                                        case "delete":
                                            if (args.length == 3) {
                                                int id;
                                                try {
                                                    id = Integer.parseInt(args[2]);
                                                } catch (NumberFormatException e) {
                                                    ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Hologram.Delete.Not-Valid-Hologram", "That is not a valid hologram ID. If you wish to see a list of all parkours and their IDs, do /parkour hologram list.", true, Collections.emptyMap());
                                                    return true;
                                                }

                                                LeaderboardHologram hologram = CacheManager.getHologram(id);
                                                if (hologram == null) {
                                                    ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Hologram.Delete.Not-Valid-Hologram", "That is not a valid hologram ID. If you wish to see a list of all parkours and their IDs, do /parkour hologram list.", true, Collections.emptyMap());
                                                    return true;
                                                }

                                                hologram.remove();
                                                if (hologram.getParkour() != null) {
                                                    hologram.getParkour().removeHologram(hologram);
                                                }
                                                CacheManager.removeHologram(hologram);
                                                new BukkitRunnable(){
                                                    @Override
                                                    public void run() {
                                                        HubParkour.getInstance().getDbManager().removeHologram(hologram);
                                                        ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Hologram.Delete.Successful", "Hologram successfully deleted.", true, Collections.emptyMap());
                                                    }
                                                }.runTaskAsynchronously(HubParkour.getInstance());
                                            } else {
                                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Hologram.Delete.Not-Enough-Arguments", "Invalid Arguments. Correct Arguments: &a/parkour hologram delete [hologram id]", true, Collections.emptyMap());
                                            }
                                            break;
                                        case "create":
                                            if (args.length == 2) {
                                                Location location = new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
                                                LeaderboardHologram hologram = new LeaderboardHologram(location, null);
                                                CacheManager.addHologram(hologram);
                                                hologram.generate();
                                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Hologram.Create.Successful", "Hologram successfully created.", true, Collections.emptyMap());
                                                return true;
                                            }
                                            int id = -1;
                                            try {
                                                id = Integer.parseInt(args[2]);
                                            } catch (NumberFormatException ignored) {
                                            }
                                            Parkour parkour;
                                            if (id != -1) {
                                                parkour = CacheManager.getParkour(id);
                                            } else {
                                                List<String> argsList = new ArrayList<>(Arrays.asList(args));
                                                argsList.remove(0);
                                                argsList.remove(0);
                                                parkour = CacheManager.getParkour(String.join(" ", argsList));
                                            }
                                            if (parkour == null) {
                                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Hologram.Create.Not-Valid-Parkour", "That is not a valid parkour ID. If you wish to see a list of all parkours and their IDs, do /parkour list.", true, Collections.emptyMap());
                                                return true;
                                            }
                                            Location location = new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
                                            LeaderboardHologram hologram = new LeaderboardHologram(location, parkour);
                                            parkour.addHologram(hologram);
                                            CacheManager.addHologram(hologram);

                                            hologram.generate();
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Hologram.Create.Successful", "Hologram successfully created.", true, Collections.emptyMap());
                                            break;
                                        default:
                                            StringBuilder sb = new StringBuilder();

                                            //Default value list
                                            List<String> defaultList = new ArrayList<>();
                                            defaultList.add("Available sub-commands:");
                                            defaultList.add("&a/parkour hologram list&r - List all active holograms and their ID's.");
                                            defaultList.add("&a/parkour hologram create [parkour id]&r - Place a Leaderboard hologram for the specified parkour ID, or overall if none is specified.");
                                            defaultList.add("&a/parkour hologram delete [hologram id]&r - Delete the hologram with the specified ID.");

                                            for (String s2 : ConfigUtil.getStringList("Messages.Commands.Admin.Hologram.Help", defaultList)) {
                                                sb.append(s2).append("\n");
                                            }
                                            String s2 = sb.toString();

                                            if (HubParkour.isPlaceholders()) {
                                                s2 = PlaceholderAPI.setPlaceholders(p, s2);
                                            }

                                            p.sendMessage(HubParkour.c(true, s2.trim()));
                                            break;
                                    }
                                } else {
                                    StringBuilder sb = new StringBuilder();

                                    //Default value list
                                    List<String> defaultList = new ArrayList<>();
                                    defaultList.add("Available sub-commands:");
                                    defaultList.add("&a/parkour hologram list&r - List all active holograms and their ID's.");
                                    defaultList.add("&a/parkour hologram create [parkour id]&r - Place a Leaderboard hologram for the specified parkour ID, or overall if none is specified.");
                                    defaultList.add("&a/parkour hologram delete [hologram id]&r - Delete the hologram with the specified ID.");

                                    for (String s : ConfigUtil.getStringList("Messages.Commands.Admin.Hologram.Help", defaultList)) {
                                        sb.append(s).append("\n");
                                    }
                                    String s = sb.toString();

                                    if (HubParkour.isPlaceholders()) {
                                        s = PlaceholderAPI.setPlaceholders(p, s);
                                    }

                                    p.sendMessage(HubParkour.c(true, s.trim()));
                                }
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Hologram.Must-Have-Holographic-Displays", "You must have Holographic Displays installed in order to use this command.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "removetime":
                        if (p.hasPermission("hubparkour.admin.removetime")) {
                            if (args.length >= 3) {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        int id = -1;
                                        try {
                                            id = Integer.parseInt(args[1]);
                                        } catch (NumberFormatException ignored) {
                                        }
                                        Parkour parkour;
                                        String name;
                                        if (id != -1) {
                                            parkour = CacheManager.getParkour(id);
                                            name = args[2];
                                        } else {
                                            List<String> argsList = new ArrayList<>(Arrays.asList(args));
                                            argsList.remove(0);
                                            name = argsList.remove(argsList.size() - 1);
                                            parkour = CacheManager.getParkour(String.join(" ", argsList));
                                        }
                                        if (parkour == null) {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.RemoveTime.Not-Valid-Parkour", "That is not a valid parkour ID. If you wish to see a list of all parkours and their IDs, do /parkour list.", true, Collections.emptyMap());
                                            return;
                                        }
                                        long time = HubParkour.getInstance().getDbManager().getTime(name, parkour);
                                        if (time == -1) {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.RemoveTime.Not-Valid-Player", "That player has never attempted this parkour.", true, Collections.emptyMap());
                                            return;
                                        }

                                        HubParkour.getInstance().getDbManager().resetTime(name, parkour.getId());
                                        ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.RemoveTime.Success", "The players time has been reset!", true, Collections.emptyMap());

                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                                    hologram.refresh();
                                                }
                                            }
                                        }.runTask(HubParkour.getInstance());
                                    }
                                }.runTaskAsynchronously(HubParkour.getInstance());
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.RemoveTime.Not-Valid-Parkour", "That is not a valid parkour ID. If you wish to see a list of all parkours and their IDs, do /parkour list.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "cleartimes":
                        if (p.hasPermission("hubparkour.admin.cleartimes")) {
                            if (args.length >= 2) {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        int id = -1;
                                        try {
                                            id = Integer.parseInt(args[1]);
                                        } catch (NumberFormatException ignored) {
                                        }
                                        Parkour parkour;
                                        if (id != -1) {
                                            parkour = CacheManager.getParkour(id);
                                        } else {
                                            List<String> argsList = new ArrayList<>(Arrays.asList(args));
                                            argsList.remove(0);
                                            parkour = CacheManager.getParkour(String.join(" ", argsList));
                                        }
                                        if (parkour == null) {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.ClearTimes.Not-Valid-Parkour", "That is not a valid parkour ID. If you wish to see a list of all parkours and their IDs, do /parkour list.", true, Collections.emptyMap());
                                            return;
                                        }

                                        HubParkour.getInstance().getDbManager().resetTimes(parkour.getId());
                                        ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.ClearTimes.Success", "All player times have been reset for parkour {parkour-name}!", true, Collections.singletonMap("parkour-name", parkour.getName()));

                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                                    hologram.refresh();
                                                }
                                            }
                                        }.runTask(HubParkour.getInstance());
                                    }
                                }.runTaskAsynchronously(HubParkour.getInstance());
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.ClearTimes.Not-Valid-Parkour", "That is not a valid parkour ID. If you wish to see a list of all parkours and their IDs, do /parkour list.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "edit":
                        if (p.hasPermission("hubparkour.admin.edit")) {
                            if (args.length >= 2) {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        int id = -1;
                                        try {
                                            id = Integer.parseInt(args[1]);
                                        } catch (NumberFormatException ignored) {
                                        }
                                        Parkour parkour;
                                        if (id != -1) {
                                            parkour = CacheManager.getParkour(id);
                                        } else {
                                            List<String> argsList = new ArrayList<>(Arrays.asList(args));
                                            argsList.remove(0);
                                            parkour = CacheManager.getParkour(String.join(" ", argsList));
                                        }
                                        if (parkour == null) {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Edit.Not-Valid-Parkour", "That is not a valid parkour. To see a list of valid parkours, do &a/parkour list&r.", true, Collections.emptyMap());
                                            return;
                                        }

                                        if (CacheManager.isSomeoneEdit()) {
                                            if (CacheManager.isEdit(p)) {
                                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Edit.Already-Editing", "You are already editing a parkour. In order to edit another parkour, use 8 in the main edit menu to finish setting up.", true, Collections.emptyMap());
                                            } else{
                                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Edit.Someone-Already-Editing", "Someone is already editing a parkour. Wait for them to finish before editing another.", true, Collections.emptyMap());
                                            }
                                            return;
                                        } else {
                                            if (parkour.getPlayers().size() > 0) {
                                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Edit.Must-Be-Empty", "The parkour must be empty before you can edit it.", true, Collections.emptyMap());
                                                return;
                                            }
                                            CacheManager.enterEditMode(p, parkour);
                                        }

                                        return;

                                    }
                                }.runTaskAsynchronously(HubParkour.getInstance());
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Edit.Not-Valid-Parkour", "That is not a valid parkour. To see a list of valid parkours, do &a/parkour list&r.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "reload":
                        if (p.hasPermission("hubparkour.admin.reload")) {
                            ConfigUtil.reload();
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Reload-Success", "The plugin configuration has been reloaded. This reload does not affect any holograms, please restart your server for those changes to take effect.", true, Collections.emptyMap());
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "teleport":
                        if (p.hasPermission("hubparkour.command.teleport")) {
                            if (args.length >= 2) {
                                List<String> argsList = new ArrayList<>(Arrays.asList(args));
                                argsList.remove(0);
                                Parkour parkour = CacheManager.getParkour(String.join(" ", argsList));
                                if (parkour != null) {
                                    if (CacheManager.isParkour(p)) {
                                        HubParkourPlayer player = CacheManager.getPlayer(p);
                                        if (player.getParkour().getId() == parkour.getId()) {
                                            Location l = parkour.getRestartPoint().getLocation().clone();
                                            l.setX(l.getX() + 0.5);
                                            l.setY(l.getY() + 0.5);
                                            l.setZ(l.getZ() + 0.5);
                                            p.teleport(l);
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Teleport.Teleported", "You have been teleported to the parkour restart point.", true, Collections.emptyMap());
                                        } else {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Teleport.Currently-In-Another-Parkour", "You cannot teleport to a parkour start point while in a different parkour. Please leave your parkour and try again.", true, Collections.emptyMap());
                                        }
                                    } else {
                                        Location l = parkour.getRestartPoint().getLocation().clone();
                                        l.setX(l.getX() + 0.5);
                                        l.setY(l.getY() + 0.5);
                                        l.setZ(l.getZ() + 0.5);
                                        p.teleport(l);
                                        ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Teleport.Teleported", "You have been teleported to the parkour restart point.", true, Collections.emptyMap());
                                    }
                                } else {
                                    ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Teleport.Not-Valid-Parkour", "That is not a valid parkour.", true, Collections.emptyMap());
                                }
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Teleport.Not-Valid-Parkour", "That is not a valid parkour.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }

                        break;
                    case "done":
                        if (p.hasPermission("hubparkour.admin.setup")) {
                            if (CacheManager.isSetup(p)) {
                                CacheManager.getSetupWizard().onChat("done");
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Setup.Not-In-Setup", "You are not currently in setup. If you want to setup a parkour, use /parkour setup.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }

                        break;
                    case "cancel":
                        if (p.hasPermission("hubparkour.admin.setup")) {
                            if (CacheManager.isSetup(p)) {
                                CacheManager.getSetupWizard().onChat("cancel");
                            } else if (CacheManager.isEdit(p)) {
                                CacheManager.getEditWizard().onChat("cancel");
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Setup.Not-In-Setup", "You are not currently in setup. If you want to setup a parkour, use /parkour setup.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }

                        break;
                    case "input":
                        if (p.hasPermission("hubparkour.admin.setup")) {
                            if (CacheManager.isSetup(p)) {
                                List<String> message = new ArrayList<>(Arrays.asList(args));
                                message.remove(0);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        CacheManager.getSetupWizard().onChat(String.join(" ", message));
                                    }
                                }.runTaskAsynchronously(HubParkour.getInstance());
                            } else if (CacheManager.isEdit(p)) {
                                List<String> message = new ArrayList<>(Arrays.asList(args));
                                message.remove(0);
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        CacheManager.getEditWizard().onChat(String.join(" ", message));
                                    }
                                }.runTaskAsynchronously(HubParkour.getInstance());
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Setup.Not-In-Setup", "You are not currently in setup. If you want to setup a parkour, use /parkour setup.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "resettimes":
                        if (p.hasPermission("hubparkour.admin.resettimes")) {
                            if (args.length == 2) {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        HubParkour.getInstance().getDbManager().resetTimes(args[1]);
                                        ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.ResetTimes.Success", "Times for all parkours reset for player {player-name}!", true,  Collections.singletonMap("player-name", args[1]));

                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                for (Parkour parkour : CacheManager.getParkours()) {
                                                    for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                                        hologram.refresh();
                                                    }
                                                }
                                            }
                                        }.runTask(HubParkour.getInstance());
                                    }
                                }.runTaskAsynchronously(HubParkour.getInstance());
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.ClearTimes.Not-Valid-Parkour", "That is not a valid parkour ID. If you wish to see a list of all parkours and their IDs, do /parkour list.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "resetalltimes":
                        if (p.hasPermission("hubparkour.admin.resetalltimes")) {
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    HubParkour.getInstance().getDbManager().resetTimes();
                                    ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.ResetAllTimes.Success", "All player times for all parkours have been reset!", true, Collections.emptyMap());

                                    new BukkitRunnable(){
                                        @Override
                                        public void run() {
                                            for (Parkour parkour : CacheManager.getParkours()) {
                                                for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                                    hologram.refresh();
                                                }
                                            }
                                        }
                                    }.runTask(HubParkour.getInstance());
                                }
                            }.runTaskAsynchronously(HubParkour.getInstance());
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    case "import": {
                        if (p.hasPermission("hubparkour.admin.resetalltimes")) {
                            if (DatabaseManager.isMysql()) {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        if (HubParkour.getInstance().getDbManager().hasData()) {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Import.Must-Be-Empty", "You must have no data in MySQL in order to use the import command.", true, Collections.emptyMap());
                                            return;
                                        }

                                        File file = new File(HubParkour.getInstance().getDataFolder(), ConfigUtil.getString("Settings.Database.Details.SQLite.File-Name", "hp-storage.db"));
                                        if (!file.exists()) {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Import.No-SQLite-File", "There is no SQLite file to import.", true, Collections.emptyMap());
                                            return;
                                        }

                                        int currentSchema = ConfigUtil.getInternal().getInt("dbschema.sqlite");
                                        if (currentSchema < HubParkour.getCurrentSchema()) {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Import.Updating-SQLite-Schema", "The SQLite Database Schema is out of date, updating...", true, Collections.emptyMap());
                                            for (int i = currentSchema + 1;i <= HubParkour.getCurrentSchema();i++) {
                                                HubParkour.getSchemaUpdates().get(i).execute();
                                            }
                                        }

                                        ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Import.Importing", "Importing data from SQLite into MySQL, please wait...", true, Collections.emptyMap());
                                        if (HubParkour.getInstance().getDbManager().importData()) {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Import.Import-Complete", "Import complete! Please restart your server in order for the data to be loaded!", true, Collections.emptyMap());
                                        } else {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Import.Import-Failed", "Import failed! Please try again!", true, Collections.emptyMap());
                                        }
                                    }
                                }.runTaskAsynchronously(HubParkour.getInstance());
                            } else {
                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.Import.Must-Be-MySQL", "You must have MySQL database storage active in order to use the import command.", true, Collections.emptyMap());
                            }
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    }
                    case "stats":
                        if (p.hasPermission("hubparkour.command.stats")) {
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    if (args.length >= 2) {
                                        List<String> argsList = new ArrayList<>(Arrays.asList(args));
                                        argsList.remove(0);
                                        Parkour parkour = CacheManager.getParkour(String.join(" ", argsList));

                                        if (parkour != null) {
                                            Statistics statistics = HubParkour.getInstance().getDbManager().getParkourStatistics(p.getPlayer(), parkour);

                                            if (statistics.getAttempts().size() == 0) {
                                                ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Stats.No-Parkour-Stats", "No stats have been tracked for you in this parkour yet. Attempt this parkour to earn stats!", true, Collections.emptyMap());
                                                return;
                                            }

                                            StringBuilder sb = new StringBuilder();

                                            //default help list
                                            List<String> defaultList = new ArrayList<>();
                                            defaultList.add("Your stats for parkour &a{parkour-name}&r:");
                                            defaultList.add("&aParkour attempts:&r {attempts}");
                                            defaultList.add("&aParkour completions:&r {completions}");
                                            defaultList.add("&aTotal jumps:&r {jumps}");
                                            defaultList.add("&aTotal checkpoints hit:&r {checkpoints}");
                                            defaultList.add("&aTotal distance travelled:&r {distance} blocks");
                                            defaultList.add("&aTotal time in parkour:&r {time}");

                                            for (String s : ConfigUtil.getStringList("Messages.Commands.Stats.Parkour-Stats", defaultList)) {
                                                sb.append(s).append("\n");
                                            }

                                            Map<String, String> bindings = new HashMap<>();
                                            bindings.put("parkour-name", parkour.getName());
                                            bindings.put("attempts", statistics.getAttempts().get(parkour.getId()) + "");
                                            bindings.put("completions", statistics.getCompletions().get(parkour.getId()) + "");
                                            bindings.put("jumps", statistics.getJumps().get(parkour.getId()) + "");
                                            bindings.put("distance", String.format("%.2f", statistics.getTotalDistanceTravelled().get(parkour.getId())));
                                            bindings.put("time", ConfigUtil.formatTime(statistics.getTotalTime().get(parkour.getId())));
                                            bindings.put("checkpoints", statistics.getCheckpointsHit().get(parkour.getId()) + "");

                                            String s = sb.toString();

                                            if (HubParkour.isPlaceholders()) {
                                                s = PlaceholderAPI.setPlaceholders(p, s);
                                            }

                                            for (Map.Entry<String, String> entry : bindings.entrySet()) {
                                                s = s.replace("{" + entry.getKey() + "}", entry.getValue());
                                            }
                                            p.sendMessage(HubParkour.c(true, s.trim()));
                                        } else {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Stats.Not-Valid-Parkour", "That is not a valid parkour.", true, Collections.emptyMap());
                                        }
                                    } else {
                                        Statistics statistics = HubParkour.getInstance().getDbManager().getGeneralStats(p.getPlayer());

                                        if (statistics.getAttempts().size() == 0) {
                                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Stats.No-Parkour-Stats", "No stats have been tracked for you in this parkour yet. Attempt this parkour to earn stats!", true, Collections.emptyMap());
                                            return;
                                        }

                                        StringBuilder sb = new StringBuilder();

                                        //default help list
                                        List<String> defaultList = new ArrayList<>();
                                        defaultList.add("Your general stats:");
                                        defaultList.add("&aParkour attempts:&r {attempts}");
                                        defaultList.add("&aParkour completions:&r {completions}");
                                        defaultList.add("&aTotal jumps:&r {jumps}");
                                        defaultList.add("&aTotal checkpoints hit:&r {checkpoints}");
                                        defaultList.add("&aTotal distance travelled:&r {distance} blocks");
                                        defaultList.add("&aTotal time in parkour:&r {time}");

                                        for (String s : ConfigUtil.getStringList("Messages.Commands.Stats.General-Stats", defaultList)) {
                                            sb.append(s).append("\n");
                                        }

                                        Map<String, String> bindings = new HashMap<>();
                                        int attempts = 0;
                                        int completions = 0;
                                        int jumps = 0;
                                        int checkpoints = 0;
                                        long time = 0L;
                                        double distance = 0.0;

                                        for (Map.Entry<Integer, Integer> entry : statistics.getAttempts().entrySet()) {
                                            attempts += entry.getValue();
                                        }
                                        for (Map.Entry<Integer, Integer> entry : statistics.getCompletions().entrySet()) {
                                            completions += entry.getValue();
                                        }
                                        for (Map.Entry<Integer, Integer> entry : statistics.getCheckpointsHit().entrySet()) {
                                            checkpoints += entry.getValue();
                                        }
                                        for (Map.Entry<Integer, Integer> entry : statistics.getJumps().entrySet()) {
                                            jumps += entry.getValue();
                                        }
                                        for (Map.Entry<Integer, Long> entry : statistics.getTotalTime().entrySet()) {
                                            time += entry.getValue();
                                        }
                                        for (Map.Entry<Integer, Double> entry : statistics.getTotalDistanceTravelled().entrySet()) {
                                            distance += entry.getValue();
                                        }

                                        bindings.put("attempts", attempts + "");
                                        bindings.put("completions", completions + "");
                                        bindings.put("jumps", jumps + "");
                                        bindings.put("distance", String.format("%.2f", distance));
                                        bindings.put("time", ConfigUtil.formatTime(time));
                                        bindings.put("checkpoints", checkpoints + "");

                                        String s = sb.toString();

                                        if (HubParkour.isPlaceholders()) {
                                            s = PlaceholderAPI.setPlaceholders(p, s);
                                        }

                                        for (Map.Entry<String, String> entry : bindings.entrySet()) {
                                            s = s.replace("{" + entry.getKey() + "}", entry.getValue());
                                        }
                                        p.sendMessage(HubParkour.c(true, s.trim()));
                                    }
                                }
                            }.runTaskAsynchronously(HubParkour.getInstance());
                        } else {
                            ConfigUtil.sendMessageOrDefault(p, "Messages.Commands.Admin.No-Permission", "You do not have permission to perform this command.", true, Collections.emptyMap());
                        }
                        break;
                    default:
                        StringBuilder sb = new StringBuilder();

                        //default help list
                        List<String> defaultList = new ArrayList<>();
                        defaultList.add("Parkour Help:");
                        defaultList.add("&a/parkour reset &r- Sends you back to the start.");
                        defaultList.add("&a/parkour checkpoint &r- Teleports you to the last checkpoint you reached.");
                        defaultList.add("&a/parkour leave &r- Makes you leave the parkour.");
                        defaultList.add("&a/parkour leaderboard [parkour] &r- View the leaderboard for specific Parkour.");
                        defaultList.add("&a/parkour teleport [parkour] &r- Teleport to the beginning of a parkour.");
                        defaultList.add("&a/parkour stats <parkour> &r- View your general stats or stats for a specific parkour.");

                        for (String s : ConfigUtil.getStringList("Messages.Commands.Help", defaultList)) {
                            sb.append(s).append("\n");
                        }
                        if (p.hasPermission("hubparkour.admin")) {

                            //default admin help list
                            defaultList.clear();
                            defaultList.add("&a/parkour setup&r - Enter setup mode and begin parkour setup.");
                            defaultList.add("&a/parkour done&r - Continues with the setup wizard when setting checkpoints.");
                            defaultList.add("&a/parkour input [text]&r - Gives the setup wizard or edit mode text input when you are asked for input.");
                            defaultList.add("&a/parkour cancel&r - Cancels the current operation in the setup/edit wizard.");
                            defaultList.add("&a/parkour delete [parkour id or name] &r- Delete the parkour with the specific ID.");
                            defaultList.add("&a/parkour list&r - Lists all active parkours.");
                            defaultList.add("&a/parkour hologram list&r - List all active holograms and their ID's.");
                            defaultList.add("&a/parkour hologram create [parkour id or name]&r - Place a Leaderboard hologram for the specified parkour ID, or overall if none is specified.");
                            defaultList.add("&a/parkour hologram delete [hologram id]&r - Delete the hologram with the specified ID.");
                            defaultList.add("&a/parkour removetime [parkour id or name] [player name]&r - Reset a players leaderboard time.");
                            defaultList.add("&a/parkour cleartimes [parkour id or name]&r - Completely clear all times for a specific parkour.");
                            defaultList.add("&a/parkour resettimes [player name]&r - Completely reset all times for a specific player.");
                            defaultList.add("&a/parkour resetalltimes&r - Completely reset all times for all players.");
                            defaultList.add("&a/parkour edit [parkour id or name]&r - Enables edit mode to modify information about a parkour.");
                            defaultList.add("&a/parkour reload &r- Reload HubParkour's configuration.");
                            defaultList.add("&a/parkour import &r- Import SQLite database configuration into MySQL. Only works if MySQL is empty and has no data.");

                            for (String s : ConfigUtil.getStringList("Messages.Commands.Help-Admin", defaultList)) {
                                sb.append(s).append("\n");
                            }
                        }

                        String s = sb.toString();

                        if (HubParkour.isPlaceholders()) {
                            s = PlaceholderAPI.setPlaceholders(p, s);
                        }

                        p.sendMessage(HubParkour.c(true, s.trim()));
                        break;
                }
            } else {
                StringBuilder sb = new StringBuilder();

                //default help list
                List<String> defaultList = new ArrayList<>();
                defaultList.add("Parkour Help:");
                defaultList.add("&a/parkour reset &r- Sends you back to the start.");
                defaultList.add("&a/parkour checkpoint &r- Teleports you to the last checkpoint you reached.");
                defaultList.add("&a/parkour leave &r- Makes you leave the parkour.");
                defaultList.add("&a/parkour leaderboard [parkour] &r- View the leaderboard for specific Parkour.");
                defaultList.add("&a/parkour teleport [parkour] &r- Teleport to the beginning of a parkour.");

                for (String s : ConfigUtil.getStringList("Messages.Commands.Help", defaultList)) {
                    sb.append(s).append("\n");
                }
                if (p.hasPermission("hubparkour.admin")) {

                    //default admin help list
                    defaultList.clear();
                    defaultList.add("&a/parkour setup&r - Enter setup mode and begin parkour setup.");
                    defaultList.add("&a/parkour done&r - Continues with the setup wizard when setting checkpoints.");
                    defaultList.add("&a/parkour input [text]&r - Gives the setup wizard or edit mode text input when you are asked for input.");
                    defaultList.add("&a/parkour cancel&r - Cancels the current operation in the setup/edit wizard.");
                    defaultList.add("&a/parkour delete [parkour id or name] &r- Delete the parkour with the specific ID.");
                    defaultList.add("&a/parkour list&r - Lists all active parkours.");
                    defaultList.add("&a/parkour hologram list&r - List all active holograms and their ID's.");
                    defaultList.add("&a/parkour hologram create [parkour id or name]&r - Place a Leaderboard hologram for the specified parkour ID, or overall if none is specified.");
                    defaultList.add("&a/parkour hologram delete [hologram id]&r - Delete the hologram with the specified ID.");
                    defaultList.add("&a/parkour removetime [parkour id or name] [player name]&r - Reset a players leaderboard time.");
                    defaultList.add("&a/parkour cleartimes [parkour id or name]&r - Completely clear all times for a specific parkour.");
                    defaultList.add("&a/parkour resettimes [player name]&r - Completely reset all times for a specific player.");
                    defaultList.add("&a/parkour resetalltimes&r - Completely reset all times for all players.");
                    defaultList.add("&a/parkour edit [parkour id or name]&r - Enables edit mode to modify information about a parkour.");
                    defaultList.add("&a/parkour reload &r- Reload HubParkour's configuration.");
                    defaultList.add("&a/parkour import &r- Import SQLite database configuration into MySQL. Only works if MySQL is empty and has no data.");

                    for (String s : ConfigUtil.getStringList("Messages.Commands.Help-Admin", defaultList)) {
                        sb.append(s).append("\n");
                    }
                }

                String s = sb.toString();

                if (HubParkour.isPlaceholders()) {
                    s = PlaceholderAPI.setPlaceholders(p, s);
                }

                p.sendMessage(HubParkour.c(true, s.trim()));
            }
        } else {
            sender.sendMessage("You cannot execute HubParkour commands from console.");
        }
        return false;
    }
}
