package me.block2block.hubparkour.commands;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.entities.HubParkourPlayer;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.entities.plates.PressurePlate;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ItemUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommandParkour implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length > 0) {
                switch (args[0]) {
                    case "reset":
                        if (CacheManager.isParkour(p)) {
                            Location l = CacheManager.getPlayer(p).getParkour().getRestartPoint().getLocation().clone();
                            l.setX(l.getX() + 0.5);
                            l.setY(l.getY() + 0.5);
                            l.setZ(l.getZ() + 0.5);
                            p.teleport(l);
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Reset.Successful")));
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Reset.Not-In-Parkour")));
                        }
                        break;
                    case "checkpoint":
                        if (CacheManager.isParkour(p)) {
                            HubParkourPlayer player = CacheManager.getPlayer(p);
                            Location l = player.getParkour().getRestartPoint().getLocation().clone();
                            if (player.getLastReached() != 0) {
                                l = player.getParkour().getCheckpoint(player.getLastReached()).getLocation();
                            }
                            l.setX(l.getX() + 0.5);
                            l.setY(l.getY() + 0.5);
                            l.setZ(l.getZ() + 0.5);
                            p.teleport(l);
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Checkpoint.Successful")));
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Checkpoint.Not-In-Parkour")));
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
                                        StringBuilder sb = new StringBuilder(Main.getInstance().getConfig().getString("Messages.Leaderboard.Message.Header") + "\n");
                                        Map<Integer, List<String>> leaderboard = Main.getInstance().getDbManager().getLeaderboard(parkour, Main.getInstance().getConfig().getInt("Settings.Leaderboard.Limit"));

                                        for (int place : leaderboard.keySet()) {
                                            List<String> record = leaderboard.get(place);
                                            sb.append(Main.getInstance().getConfig().getString("Messages.Leaderboard.Message.Line").replace("{player-name}", record.get(0)).replace("{player-time}", "" + Float.parseFloat(record.get(1)) / 1000f).replace("{place}", "" + place)).append("\n");
                                        }

                                        sb.append(Main.getInstance().getConfig().getString("Messages.Leaderboard.Message.Footer"));
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
                            player.getParkour().playerEnd(player);
                            CacheManager.removePlayer(p);
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
                            StringBuilder sb = new StringBuilder(Main.getInstance().getConfig().getString("Messages.Admin.List.Header") + "\n");
                            for (Parkour parkour : CacheManager.getParkours()) {
                                sb.append(Main.getInstance().getConfig().getString("Messages.Admin.List.Line").replace("{parkour-name}", parkour.getName()).replace("{parkour-players}", "" + parkour.getPlayers().size()).replace("{id}", "" + parkour.getId())).append("\n");
                            }

                            sb.append(Main.getInstance().getConfig().getString("Messages.Leaderboard.Message.Footer"));
                            p.sendMessage(Main.c(true, sb.toString().trim()));
                        } else {
                            p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.No-Permission")));
                        }
                        break;
                    case "delete":
                        if (p.hasPermission("hubparkour.admin")) {
                            if (args.length == 2) {
                                int id = -1;
                                try {
                                    id = Integer.parseInt(args[1]);
                                } catch (NumberFormatException e) {
                                    p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Delete.Not-Valid-Parkour")));
                                    return true;
                                }
                                Parkour parkour = CacheManager.getParkour(id);
                                if (parkour != null) {
                                    for (HubParkourPlayer player : parkour.getPlayers()) {
                                        player.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Delete.Player-Kicked-From-Parkour")));
                                        CacheManager.removePlayer(player.getPlayer());
                                    }

                                    parkour.removeHolograms();
                                    for (PressurePlate pp : parkour.getAllPoints()) {
                                        CacheManager.removePlate(pp);
                                    }
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
                    default:
                        p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Help") + ((p.hasPermission("hubparkour.admin")?Main.getInstance().getConfig().getString("Messages.Commands.Help"):""))));
                        break;
                }
            } else {
                p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Help")));
            }
        } else {
            sender.sendMessage("You cannot execute HubParkour commands from console.");
        }
        return false;
    }
}
