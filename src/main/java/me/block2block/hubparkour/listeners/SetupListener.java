package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.events.admin.ParkourSetupEvent;
import me.block2block.hubparkour.api.plates.*;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class SetupListener implements Listener {

    private List<PressurePlate> data = new ArrayList<>();
    private List<String> commandData = new ArrayList<>();
    private int after = -1;

    @SuppressWarnings("unused")
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
                        ParkourSetupEvent setupEvent = new ParkourSetupEvent(parkour, e.getPlayer());
                        Bukkit.getPluginManager().callEvent(setupEvent);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (PressurePlate pp : parkour.getAllPoints()) {
                                    pp.placeMaterial();
                                    if (pp.getType() != 2) {
                                        CacheManager.addPoint(pp);
                                    } else {
                                        CacheManager.addRestartPoint(pp);
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
        } else if (CacheManager.isEdit(e.getPlayer())) {
            switch (CacheManager.getCurrentModification()) {
                case -1: {
                    int edit;
                    try {
                        edit = Integer.parseInt(e.getMessage());
                    } catch (NumberFormatException exception) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Not-Valid-Edit")));
                        return;
                    }

                    switch (edit) {
                        case 1:
                            //Name
                            CacheManager.setCurrentModification(1);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Enter-New-Name")));
                            break;
                        case 2:
                            //End Command
                            CacheManager.setCurrentModification(2);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Enter-New-End-Command")));
                            break;
                        case 3:
                            //Checkpoint Command
                            CacheManager.setCurrentModification(3);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Enter-New-Checkpoint-Command")));
                            break;
                        case 4:
                            //Start Point
                            CacheManager.setCurrentModification(4);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Select-New-Start-Point")));
                            break;
                        case 5:
                            //End Point
                            CacheManager.setCurrentModification(5);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Select-New-End-Point")));
                            break;
                        case 6:
                            //Restart Point
                            CacheManager.setCurrentModification(6);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Select-New-Restart-Point")));
                            break;
                        case 7:
                            //Checkpoints
                            CacheManager.setCurrentModification(7);
                            StringBuilder sb = new StringBuilder();
                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Checkpoint-Edit")) {
                                sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                            }
                            e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                            break;
                        case 8:
                            //Exit
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Exited-Edit-Mode")));
                            CacheManager.leaveEditMode();
                            break;
                        default:
                            e.setCancelled(true);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Not-Valid-Edit")));
                            return;
                    }
                    e.setCancelled(true);
                    break;
                }
                case 1: {
                    e.setCancelled(true);
                    if (e.getMessage().split(" ").length == 1) {
                        if (CacheManager.getParkour(e.getMessage()) == null || CacheManager.getParkour(e.getMessage()) == CacheManager.getEditParkour()) {
                            CacheManager.getEditParkour().setName(e.getMessage());
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Name-Set")));
                            CacheManager.setCurrentModification(-1);
                            StringBuilder sb = new StringBuilder();
                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Edit")) {
                                sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                            }
                            e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                        } else {
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Name-Taken")));
                        }
                    } else {
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Name-Too-Long")));
                    }
                    break;
                }
                case 2: {
                    e.setCancelled(true);
                    String command = e.getMessage();
                    if (e.getMessage().equalsIgnoreCase("none")) {
                        command = null;
                    }
                    CacheManager.getEditParkour().setEndCommand(command);
                    e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.End-Command-Set")));
                    CacheManager.setCurrentModification(-1);
                    StringBuilder sb = new StringBuilder();
                    for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Edit")) {
                        sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                    }
                    e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                    break;
                }
                case 3: {
                    e.setCancelled(true);
                    String command = e.getMessage();
                    if (e.getMessage().equalsIgnoreCase("none")) {
                        command = null;
                    }
                    CacheManager.getEditParkour().setCheckpointCommand(command);
                    e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Command-Set")));
                    CacheManager.setCurrentModification(-1);
                    StringBuilder sb = new StringBuilder();
                    for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Edit")) {
                        sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                    }
                    e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                    break;
                }
                case 7: {
                    int edit;
                    try {
                        edit = Integer.parseInt(e.getMessage());
                    } catch (NumberFormatException exception) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Not-Valid-Edit")));
                        return;
                    }
                    e.setCancelled(true);
                    switch (edit) {
                        case 1:
                            if (CacheManager.getEditParkour().getCheckpoints().size() == 0) {
                                e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Delete.No-Checkpoints")));
                                break;
                            }
                            CacheManager.setCurrentModification(8);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Delete.Please-Enter-Checkpoint")));
                            break;
                        case 2:
                            CacheManager.setCurrentModification(9);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Add.After-Which")));
                            break;
                        case 3:
                            CacheManager.setCurrentModification(-1);
                            StringBuilder sb = new StringBuilder();
                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Edit")) {
                                sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                            }
                            e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                            break;
                        default:
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Not-Valid-Edit")));
                            break;
                    }
                    break;
                }
                case 8: {
                    int checkpointNo;
                    try {
                        checkpointNo = Integer.parseInt(e.getMessage());
                    } catch (NumberFormatException exception) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Delete.Not-Valid-Checkpoint")));
                        return;
                    }
                    e.setCancelled(true);
                    Checkpoint checkpoint = CacheManager.getEditParkour().getCheckpoint(checkpointNo);
                    if (checkpoint != null) {
                        CacheManager.getEditParkour().deleteCheckpoint(checkpoint);
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Delete.Success")));
                        CacheManager.setCurrentModification(7);
                        StringBuilder sb = new StringBuilder();
                        for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Checkpoint-Edit")) {
                            sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                        }
                        e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                    } else {
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Delete.Not-Valid-Checkpoint")));
                        return;
                    }
                    break;
                }
                case 9: {
                    int checkpointNo;
                    try {
                        checkpointNo = Integer.parseInt(e.getMessage());
                    } catch (NumberFormatException exception) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Add.Not-Valid")));
                        return;
                    }
                    e.setCancelled(true);
                    if (checkpointNo >= 0 && checkpointNo <= CacheManager.getEditParkour().getNoCheckpoints()) {
                        this.after = checkpointNo;
                        CacheManager.setCurrentModification(10);
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Add.Select-Checkpoint")));
                    } else {
                        e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Add.Not-Valid")));
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (CacheManager.isSetup(e.getPlayer())) {
            if (e.hasItem()) {
                if (e.getItem().getType() == Material.STICK && ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName()).equals("HubParkour Setup Stick")) {
                    Location location = e.getPlayer().getLocation().getBlock().getLocation();
                    location.setPitch(e.getPlayer().getLocation().getPitch());
                    location.setYaw(e.getPlayer().getLocation().getYaw());
                    switch (CacheManager.getSetupStage()) {
                        case 0:
                            data.add(new StartPoint(location));
                            CacheManager.nextStage();
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-End")));
                            e.setCancelled(true);
                            break;
                        case 1:
                            if (data.get(0).getLocation().equals(location)) {
                                e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Invalid-Placement")));
                                e.setCancelled(true);
                                return;
                            }
                            data.add(new EndPoint(location));
                            CacheManager.nextStage();
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-Respawn")));
                            e.setCancelled(true);
                            break;
                        case 2:
                            data.add(new RestartPoint(location));
                            CacheManager.nextStage();
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Please-Set-Checkpoints")));
                            e.setCancelled(true);
                            break;
                        case 3:
                            for (PressurePlate p : data) {
                                if (p.getLocation().equals(location) && p.getType() != 2) {
                                    e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Invalid-Placement")));
                                    e.setCancelled(true);
                                    return;
                                }
                            }
                            data.add(new Checkpoint(location, data.size() - 2));
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Setup.Checkpoint-Added")));
                            e.setCancelled(true);
                    }
                }
            }
        } else if (CacheManager.isEdit(e.getPlayer())) {
            if (e.hasItem()) {
                if (e.getItem().getType() == Material.STICK && ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName()).equals("HubParkour Setup Stick")) {
                    Location location = e.getPlayer().getLocation().getBlock().getLocation();
                    location.setPitch(e.getPlayer().getLocation().getPitch());
                    location.setYaw(e.getPlayer().getLocation().getYaw());
                    switch (CacheManager.getCurrentModification()) {
                        case 4: {
                            StartPoint startPoint = new StartPoint(location);
                            CacheManager.getEditParkour().setStartPoint(startPoint);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Start-Point-Set")));
                            CacheManager.setCurrentModification(-1);
                            StringBuilder sb = new StringBuilder();
                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Edit")) {
                                sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                            }
                            e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                            break;
                        }
                        case 5: {
                            EndPoint endPoint = new EndPoint(location);
                            CacheManager.getEditParkour().setEndPoint(endPoint);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.End-Point-Set")));
                            CacheManager.setCurrentModification(-1);
                            StringBuilder sb = new StringBuilder();
                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Edit")) {
                                sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                            }
                            e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                            break;
                        }
                        case 6: {
                            RestartPoint restartPoint = new RestartPoint(location);
                            CacheManager.getEditParkour().setRestartPoint(restartPoint);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Restart-Point-Set")));
                            CacheManager.setCurrentModification(-1);
                            StringBuilder sb = new StringBuilder();
                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Edit")) {
                                sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                            }
                            e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                            break;
                        }
                        case 10: {
                            Checkpoint checkpoint = new Checkpoint(location, after + 1);
                            CacheManager.getEditParkour().addCheckpoint(checkpoint, after + 1);
                            e.getPlayer().sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Admin.Edit.Checkpoint-Add.Success")));
                            CacheManager.setCurrentModification(7);
                            StringBuilder sb = new StringBuilder();
                            for (String s : Main.getInstance().getConfig().getStringList("Messages.Commands.Admin.Edit.Choose-Checkpoint-Edit")) {
                                sb.append(s.replace("{parkour-name}", CacheManager.getEditParkour().getName())).append("\n");
                            }
                            e.getPlayer().sendMessage(Main.c(true, sb.toString()));
                            break;
                        }
                    }
                }
            }
        }
    }

}
