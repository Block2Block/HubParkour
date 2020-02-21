package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.entities.plates.*;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SetupListener implements Listener {

    private List<PressurePlate> data = new ArrayList<>();
    private List<String> commandData = new ArrayList<>();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (CacheManager.isSetup(e.getPlayer())) {
            switch (CacheManager.getSetupStage()) {
                case 4:
                    e.setCancelled(true);
                    if (e.getMessage().split(" ").length == 1 && !e.getMessage().equalsIgnoreCase("cancel")) {
                        if (CacheManager.getParkour(e.getMessage()) == null) {
                            commandData.add(e.getMessage());
                            CacheManager.nextStage();
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-End-Command")));
                        } else {
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Name-Taken")));
                        }
                    } else if (!e.getMessage().equalsIgnoreCase("cancel")) {
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Name-Too-Long")));
                    } else {
                        e.setCancelled(true);
                        data = new ArrayList<>();
                        commandData = new ArrayList<>();
                        CacheManager.exitSetup();
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Setup-Cancelled")));
                    }
                    break;
                case 5:
                    if (!e.getMessage().equalsIgnoreCase("cancel")) {
                        String command = e.getMessage();
                        if (e.getMessage().equalsIgnoreCase("none")) {
                            command = null;
                        }

                        commandData.add(command);
                        CacheManager.nextStage();
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-Checkpoint-Command")));
                        e.setCancelled(true);
                    } else {
                        e.setCancelled(true);
                        data = new ArrayList<>();
                        commandData = new ArrayList<>();
                        CacheManager.exitSetup();
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Setup-Cancelled")));
                    }
                    break;
                case 6:
                    if (!e.getMessage().equalsIgnoreCase("cancel")) {
                        StartPoint startPoint = (StartPoint) data.remove(0);
                        EndPoint endPoint = (EndPoint) data.remove(0);
                        RestartPoint restartPoint = (RestartPoint) data.remove(0);
                        List<Checkpoint> checkpoints = new ArrayList<>();
                        for (PressurePlate pp : data) {
                            checkpoints.add((Checkpoint) pp);
                        }
                        e.setCancelled(true);

                        String command = e.getMessage();
                        if (e.getMessage().equalsIgnoreCase("none")) {
                            command = null;
                        }
                        final Parkour parkour = new Parkour(-1, commandData.get(0), startPoint, endPoint, checkpoints, restartPoint, command, commandData.get(1));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (PressurePlate pp : parkour.getAllPoints()) {
                                    pp.placeMaterial();
                                    if (pp.getType() != 2) {
                                        CacheManager.addPoint(pp);
                                    }
                                }

                                if (Main.getInstance().getConfig().getBoolean("Settings.Holograms") && Main.isHolograms()) {
                                    parkour.generateHolograms();
                                }

                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        Parkour newParkour = Main.getInstance().getDbManager().addParkour(parkour);
                                        CacheManager.addParkour(newParkour);
                                        CacheManager.exitSetup();
                                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Setup-Complete")));
                                        commandData = new ArrayList<>();
                                        data = new ArrayList<>();
                                    }
                                }.runTaskAsynchronously(Main.getInstance());
                            }
                        }.runTask(Main.getInstance());
                        break;
                    } else {
                        e.setCancelled(true);
                        data = new ArrayList<>();
                        commandData = new ArrayList<>();
                        CacheManager.exitSetup();
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Setup-Cancelled")));
                    }
                default:
                    if (e.getMessage().toLowerCase().equalsIgnoreCase("cancel")) {
                        e.setCancelled(true);
                        data = new ArrayList<>();
                        commandData = new ArrayList<>();
                        CacheManager.exitSetup();
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Setup-Cancelled")));
                    } else if (e.getMessage().equalsIgnoreCase("done")) {
                        if (CacheManager.getSetupStage() == 3) {
                            e.setCancelled(true);
                            CacheManager.nextStage();
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-Name")));
                        }
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (CacheManager.isSetup(e.getPlayer())) {
            if (e.hasItem()) {
                if (e.getItem().getType() == Material.STICK && ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName()).equals("HubParkour Setup Stick")) {
                    switch (CacheManager.getSetupStage()) {
                        case 0:
                            data.add(new StartPoint(e.getPlayer().getLocation().getBlock().getLocation()));
                            CacheManager.nextStage();
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-End")));
                            e.setCancelled(true);
                            break;
                        case 1:
                            if (data.get(0).getLocation().equals(e.getPlayer().getLocation().getBlock().getLocation())) {
                                e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Invalid-Placement")));
                                e.setCancelled(true);
                                return;
                            }
                            data.add(new EndPoint(e.getPlayer().getLocation().getBlock().getLocation()));
                            CacheManager.nextStage();
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-Respawn")));
                            e.setCancelled(true);
                            break;
                        case 2:
                            data.add(new RestartPoint(e.getPlayer().getLocation().getBlock().getLocation()));
                            CacheManager.nextStage();
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-Checkpoints")));
                            e.setCancelled(true);
                            break;
                        case 3:
                            for (PressurePlate p : data) {
                                if (p.getLocation().equals(e.getPlayer().getLocation().getBlock().getLocation()) && p.getType() != 2) {
                                    e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Invalid-Placement")));
                                    e.setCancelled(true);
                                    return;
                                }
                            }
                            data.add(new Checkpoint(e.getPlayer().getLocation().getBlock().getLocation(), data.size() - 2));
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Checkpoint-Added")));
                            e.setCancelled(true);
                    }
                }
            }
        }
    }

}
