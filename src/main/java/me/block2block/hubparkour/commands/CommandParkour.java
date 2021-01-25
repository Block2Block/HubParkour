package me.block2block.hubparkour.commands;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.ILeaderboardHologram;
import me.block2block.hubparkour.api.events.admin.ParkourDeleteEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerLeaveEvent;
import me.block2block.hubparkour.api.events.player.ParkourPlayerTeleportEvent;
import me.block2block.hubparkour.api.plates.PressurePlate;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.LeaderboardHologram;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class CommandParkour implements CommandExecutor {


    @SuppressWarnings("DuplicatedCode")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length > 0) {
                switch (args[0]) {
                    case "reset":
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
                            p.teleport(l);
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Reset.Successful")));
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Reset.Not-Started-Parkour")));
                        }
                        break;
                    case "checkpoint":
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
                            p.teleport(l);
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Checkpoint.Successful")));
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Checkpoint.Not-Started-Parkour")));
                        }
                        break;
                    case "top10":
                    case "leaderboard":
                        if (args.length == 2) {
                            Parkour parkour = CacheManager.getParkour(args[1]);
                            if (parkour != null) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        StringBuilder sb = new StringBuilder(Main.getInstance().getConfig().getString("Messages.Commands.Leaderboard.Message.Header") + "\n");
                                        Map<Integer, List<String>> leaderboard = Main.getInstance().getDbManager().getLeaderboard(parkour, Main.getInstance().getConfig().getInt("Settings.Leaderboard.Limit"));

                                        for (int place : leaderboard.keySet()) {
                                            List<String> record = leaderboard.get(place);
                                            sb.append(Main.getInstance().getConfig().getString("Messages.Commands.Leaderboard.Message.Line").replace("{player-name}", record.get(0)).replace("{player-time}", "" + Float.parseFloat(record.get(1)) / 1000f).replace("{place}", "" + place)).append("\n");
                                        }

                                        sb.append(Main.getInstance().getConfig().getString("Messages.Commands.Leaderboard.Message.Footer"));
                                        p.sendMessage(Main.c(true, sb.toString().trim()));
                                    }
                                }.runTaskAsynchronously(Main.getInstance());
                            } else {
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Leaderboard.Not-Valid-Parkour")));
                            }
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Leaderboard.Not-Valid-Parkour")));
                        }
                        break;
                    case "leave":
                        if (CacheManager.isParkour(p)) {
                            HubParkourPlayer player = CacheManager.getPlayer(p);
                            ParkourPlayerLeaveEvent leaveEvent = new ParkourPlayerLeaveEvent(player.getParkour(), player);
                            Bukkit.getPluginManager().callEvent(leaveEvent);
                            if (leaveEvent.isCancelled()) {
                                return true;
                            }
                            player.removeItems();
                            player.getParkour().playerEnd(player);
                            CacheManager.playerEnd(player);
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Leave.Left")));
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Leave.Not-In-Parkour")));
                        }
                        break;
                    case "setup":
                        if (p.hasPermission("hubparkour.admin")) {
                            if (!CacheManager.isParkour(p)) {
                                if (CacheManager.getSetupStage() == -1) {
                                    CacheManager.nextStage();
                                    CacheManager.setSetupPlayer(p);
                                    p.getInventory().addItem(ItemUtil.ci(Material.STICK, "&2&lHubParkour Setup Stick", 1, "&rUse this item to;&rsetup your HubParkour;&rParkour."));
                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Given-Setup-Stick")));
                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-Start")));
                                } else {
                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Someone-Already-In-Setup")));
                                }
                            } else {
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Must-Not-Be-In-Parkour")));
                            }
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.No-Permission")));
                        }
                        break;
                    case "list":
                        if (p.hasPermission("hubparkour.admin")) {
                            StringBuilder sb = new StringBuilder(Main.getInstance().getConfig().getString("Messages.Commands.Admin.List.Header") + "\n");
                            for (Parkour parkour : CacheManager.getParkours()) {
                                sb.append(Main.getInstance().getConfig().getString("Messages.Commands.Admin.List.Line").replace("{parkour-name}", parkour.getName()).replace("{parkour-players}", "" + parkour.getPlayers().size()).replace("{id}", "" + parkour.getId())).append("\n");
                            }

                            sb.append(Main.getInstance().getConfig().getString("Messages.Commands.Admin.List.Footer"));
                            p.sendMessage(Main.c(true, sb.toString().trim()));
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.No-Permission")));
                        }
                        break;
                    case "delete":
                        if (p.hasPermission("hubparkour.admin")) {
                            if (args.length == 2) {
                                int id;
                                try {
                                    id = Integer.parseInt(args[1]);
                                } catch (NumberFormatException e) {
                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Delete.Not-Valid-Parkour")));
                                    return true;
                                }
                                Parkour parkour = CacheManager.getParkour(id);
                                if (parkour != null) {
                                    ParkourDeleteEvent deleteEvent = new ParkourDeleteEvent(parkour, p);
                                    Bukkit.getPluginManager().callEvent(deleteEvent);
                                    for (IHubParkourPlayer player : parkour.getPlayers()) {
                                        player.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Delete.Player-Kicked-From-Parkour")));
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
                                                Main.getInstance().getDbManager().removeHologram((LeaderboardHologram) hologram);
                                            }
                                            Main.getInstance().getDbManager().deleteParkour(parkour);
                                            CacheManager.getParkours().remove(parkour);
                                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Delete.Success")));
                                        }
                                    }.runTaskAsynchronously(Main.getInstance());
                                } else {
                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Delete.Not-Valid-Parkour")));
                                }
                            } else {
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Delete.Not-Valid-Parkour")));
                            }
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.No-Permission")));
                        }
                        break;
                    case "hologram":
                        if (p.hasPermission("hubparkour.admin")) {
                            if (Main.isHolograms() && Main.getInstance().getConfig().getBoolean("Settings.Holograms")) {
                                if (args.length > 1) {
                                    switch (args[1]) {
                                        case "list":
                                            StringBuilder list = new StringBuilder(Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.List.Header") + "\n");
                                            for (LeaderboardHologram hologram : CacheManager.getLeaderboards()) {
                                                list.append(Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.List.Line").replace("{parkour-name}", hologram.getParkour().getName()).replace("{id}", "" + hologram.getId())).append("\n");
                                            }

                                            list.append(Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.List.Footer"));
                                            p.sendMessage(Main.c(true, list.toString().trim()));
                                            break;
                                        case "delete":
                                            if (args.length == 3) {
                                                int id;
                                                try {
                                                    id = Integer.parseInt(args[2]);
                                                } catch (NumberFormatException e) {
                                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.Delete.Not-Valid-Hologram")));
                                                    return true;
                                                }

                                                LeaderboardHologram hologram = CacheManager.getHologram(id);
                                                if (hologram == null) {
                                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.Delete.Not-Valid-Hologram")));
                                                    return true;
                                                }

                                                hologram.remove();
                                                hologram.getParkour().removeHologram(hologram);
                                                CacheManager.removeHologram(hologram);
                                                new BukkitRunnable(){
                                                    @Override
                                                    public void run() {
                                                        Main.getInstance().getDbManager().removeHologram(hologram);
                                                        p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.Delete.Successful")));
                                                    }
                                                }.runTaskAsynchronously(Main.getInstance());
                                            } else {
                                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.Delete.Not-Enough-Arguments")));
                                            }
                                            break;
                                        case "create":
                                            if (args.length == 3) {
                                                int id;
                                                try {
                                                    id = Integer.parseInt(args[2]);
                                                } catch (NumberFormatException e) {
                                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.Create.Not-Valid-Parkour")));
                                                    return true;
                                                }

                                                Parkour parkour = CacheManager.getParkour(id);
                                                if (parkour == null) {
                                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.Create.Not-Valid-Parkour")));
                                                    return true;
                                                }
                                                Location location = new Location(p.getWorld(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
                                                LeaderboardHologram hologram = new LeaderboardHologram(location, parkour);
                                                parkour.addHologram(hologram);
                                                CacheManager.addHologram(hologram);

                                                hologram.generate();
                                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.Create.Successful")));
                                            } else {
                                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.Create.Not-Enough-Arguments")));
                                            }
                                            break;
                                        default:
                                            StringBuilder sb = new StringBuilder();
                                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Hologram.Help")) {
                                                sb.append(s).append("\n");
                                            }
                                            p.sendMessage(Main.c(true, sb.toString().trim()));
                                            break;
                                    }
                                } else {
                                    StringBuilder sb = new StringBuilder();
                                    for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Hologram.Help")) {
                                        sb.append(s).append("\n");
                                    }
                                    p.sendMessage(Main.c(true, sb.toString().trim()));
                                }
                            } else {
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Hologram.Must-Have-Holographic-Displays")));
                            }
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.No-Permission")));
                        }
                        break;
                    case "removetime":
                        if (p.hasPermission("hubparkour.admin")) {
                            if (args.length == 3) {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        int parkourID;
                                        try {
                                            parkourID = Integer.parseInt(args[1]);
                                        } catch (NumberFormatException e) {
                                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.RemoveTime.Not-Valid-Parkour")));
                                            return;
                                        }

                                        Parkour parkour = CacheManager.getParkour(parkourID);
                                        if (parkour == null) {
                                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.RemoveTime.Not-Valid-Parkour")));
                                            return;
                                        }
                                        long time = Main.getInstance().getDbManager().getTime(args[2], parkour);
                                        if (time == -1) {
                                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.RemoveTime.Not-Valid-Player")));
                                            return;
                                        }

                                        Main.getInstance().getDbManager().resetTime(args[2], parkour.getId());
                                        p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.RemoveTime.Success")));

                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                                    hologram.refresh();
                                                }
                                            }
                                        }.runTask(Main.getInstance());
                                    }
                                }.runTaskAsynchronously(Main.getInstance());
                            } else {
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.RemoveTime.Not-Valid-Parkour")));
                            }
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.No-Permission")));
                        }
                        break;
                    case "cleartimes":
                        if (p.hasPermission("hubparkour.admin")) {
                            if (args.length == 2) {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        int parkourID;
                                        try {
                                            parkourID = Integer.parseInt(args[1]);
                                        } catch (NumberFormatException e) {
                                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.ClearTimes.Not-Valid-Parkour")));
                                            return;
                                        }

                                        Parkour parkour = CacheManager.getParkour(parkourID);
                                        if (parkour == null) {
                                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.ClearTimes.Not-Valid-Parkour")));
                                            return;
                                        }

                                        Main.getInstance().getDbManager().resetTimes(parkour.getId());
                                        p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.ClearTimes.Success")));

                                        new BukkitRunnable(){
                                            @Override
                                            public void run() {
                                                for (ILeaderboardHologram hologram : parkour.getLeaderboards()) {
                                                    hologram.refresh();
                                                }
                                            }
                                        }.runTask(Main.getInstance());
                                    }
                                }.runTaskAsynchronously(Main.getInstance());
                            } else {
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.ClearTimes.Not-Valid-Parkour")));
                            }
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.No-Permission")));
                        }
                        break;
                    case "edit":
                        if (p.hasPermission("hubparkour.admin")) {
                            if (args.length == 2) {
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        int parkourID;
                                        try {
                                            parkourID = Integer.parseInt(args[1]);
                                        } catch (NumberFormatException e) {
                                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Not-Valid-Parkour")));
                                            return;
                                        }

                                        Parkour parkour = CacheManager.getParkour(parkourID);
                                        if (parkour == null) {
                                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Not-Valid-Parkour")));
                                            return;
                                        }

                                        if (CacheManager.isSomeoneEdit()) {
                                            if (CacheManager.isEdit(p)) {
                                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Already-Editing")));
                                            } else{
                                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Someone-Already-Editing")));
                                            }
                                            return;
                                        } else {
                                            if (parkour.getPlayers().size() > 0) {
                                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Must-Be-Empty")));
                                                return;
                                            }
                                            CacheManager.enterEditMode(p, parkour);
                                            p.getInventory().addItem(ItemUtil.ci(Material.STICK, "&2&lHubParkour Setup Stick", 1, "&rUse this item to;&rsetup your HubParkour;&rParkour."));
                                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Entered-Edit-Mode")));
                                            StringBuilder sb = new StringBuilder();
                                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Edit")) {
                                                sb.append(s.replace("{parkour-name}", parkour.getName())).append("\n");
                                            }
                                            p.sendMessage(Main.c(true, sb.toString()));
                                        }

                                        return;

                                    }
                                }.runTaskAsynchronously(Main.getInstance());
                            } else {
                                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Not-Valid-Parkour")));
                            }
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.No-Permission")));
                        }
                        break;
                    default:
                        StringBuilder sb = new StringBuilder();
                        for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Help")) {
                            sb.append(s).append("\n");
                        }
                        if (p.hasPermission("hubparkour.admin")) {
                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Help-Admin")) {
                                sb.append(s).append("\n");
                            }
                        }

                        p.sendMessage(Main.c(true, sb.toString().trim()));
                        break;
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Help")) {
                    sb.append(s).append("\n");
                }
                if (p.hasPermission("hubparkour.admin")) {
                    for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Help-Admin")) {
                        sb.append(s).append("\n");
                    }
                }

                p.sendMessage(Main.c(true, sb.toString().trim()));
            }
        } else {
            sender.sendMessage("You cannot execute HubParkour commands from console.");
        }
        return false;
    }
}
