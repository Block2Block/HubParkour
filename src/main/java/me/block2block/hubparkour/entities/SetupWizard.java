package me.block2block.hubparkour.entities;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.events.admin.ParkourSetupEvent;
import me.block2block.hubparkour.api.plates.*;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import me.block2block.hubparkour.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SetupWizard {

    private Player player;
    private StartPoint startPoint;
    private EndPoint endPoint;
    private RestartPoint restartPoint;
    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private String endCommand, name;
    private int after = -1;
    private int currentStage;


    public SetupWizard(Player player) {
        this.player = player;
        currentStage = 0;
        player.getInventory().addItem(ItemUtil.ci(Material.STICK, "&2&lHubParkour Setup Stick", 1, "&rUse this item to;&rsetup your HubParkour;&rParkour."));
        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Given-Setup-Stick", "You have been given the setup stick.", true, Collections.emptyMap());
        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Start", "Welcome to the parkour setup wizard!\n\nBefore we start, please note that the setup wizard will use your player location when clicking to register locations, not where you look when clicking the stick. When players use commands, they will get teleported facing the direction you are facing when clicking the stick.\nIn order to start, please start off by clicking where you would like your start pressure plate with the stick. Do not worry about placing any pressure plates, the plugin will do that for you! If you wish to cancel at any time, enter 'cancel' or type /parkour cancel.", true, Collections.emptyMap());
    }

    public boolean onChat(String message) {
        switch (currentStage) {
            case 4:
                if (!message.equalsIgnoreCase("cancel")) {
                    if (CacheManager.getParkour(message) == null) {
                        name = message;
                        currentStage++;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-End-Command", "The name has been set! Please now type out your reward command for the end of your parkour into chat or type it with /parkour input [name]. If you don't want one, just type 'none'. Available placeholders are {player-uuid} and {player-name}.", true, Collections.emptyMap());
                    } else {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Name-Taken", "That name is already taken. Please try again.", true, Collections.emptyMap());
                    }
                } else {
                    CacheManager.exitSetup();
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup-Cancelled", "Parkour setup was cancelled. Any points that were setup have been deleted.", true, Collections.emptyMap());
                }
                return true;
            case 5:
                if (!message.equalsIgnoreCase("cancel")) {
                    String command = message;
                    if (message.equalsIgnoreCase("none")) {
                        command = null;
                    }

                    endCommand = command;
                    currentStage++;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Checkpoint-Command", "The End Command has been set! Please now type out your reward command for checkpoints of your parkour into chat or type it with /parkour input [name]. This command will only be executed for each reached checkpoint after they finish the parkour to prevent exploitation. If you don't want one, just type 'none'. Available placeholders are {player-uuid} and {player-name}.", true, Collections.emptyMap());
                } else {
                    CacheManager.exitSetup();
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup-Cancelled", "Parkour setup was cancelled. Any points that were setup have been deleted.", true, Collections.emptyMap());
                }
                return true;
            case 6:
                if (!message.equalsIgnoreCase("cancel")) {

                    String command = message;
                    if (message.equalsIgnoreCase("none")) {
                        command = null;
                    }
                    final Parkour parkour = new Parkour(-1, name, startPoint, endPoint, checkpoints, restartPoint, command, endCommand);
                    ParkourSetupEvent setupEvent = new ParkourSetupEvent(parkour, player);
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

                            if (Main.isHolograms()) {
                                parkour.generateHolograms();
                            }

                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    Parkour newParkour = Main.getInstance().getDbManager().addParkour(parkour);
                                    CacheManager.addParkour(newParkour);
                                    CacheManager.exitSetup();
                                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup-Complete", "Parkour setup complete!", true, Collections.emptyMap());
                                }
                            }.runTaskAsynchronously(Main.getInstance());
                        }
                    }.runTask(Main.getInstance());
                } else {
                    CacheManager.exitSetup();
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup-Cancelled", "Parkour setup was cancelled. Any points that were setup have been deleted.", true, Collections.emptyMap());
                }
                return true;
            default:
                if (message.equalsIgnoreCase("cancel")) {
                    CacheManager.exitSetup();
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup-Cancelled", "Parkour setup was cancelled. Any points that were setup have been deleted.", true, Collections.emptyMap());
                    return true;
                } else if (message.equalsIgnoreCase("done")) {
                    if (currentStage == 3) {
                        currentStage++;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Name", "Now, you need to set a name for your parkour! Please enter a name for your parkour into chat or type it with /parkour input [name]. It must be one word and not a duplicate. Names are compatible with formatting codes.", true, Collections.emptyMap());
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    public void stickInteract(Location location) {
        switch (currentStage) {
            case 0:
                startPoint = new StartPoint(location);
                currentStage++;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-End", "Start point set! Now, click where you would like your end pressure plate with your stick.", true, Collections.emptyMap());
                break;
            case 1:
                if (startPoint.getLocation().equals(location)) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-Placement", "The place you are trying to setup that point is currently set for a different type of point. If this is a mistake, please type 'cancel' and re-setup your parkour.", true, Collections.emptyMap());
                    return;
                }
                endPoint = new EndPoint(location);
                currentStage++;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Respawn", "End point set! Next, you need to set your respawn point. Click the stick while standing in your respawn point.", true, Collections.emptyMap());
                break;
            case 2:
                restartPoint = new RestartPoint(location);
                currentStage++;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Checkpoints", "Respawn point set! Now, you need to select any checkpoints you want. Click on each checkpoint pressure plate, in order you want them completed, then enter 'done'.", true, Collections.emptyMap());
                break;
            case 3:
                for (PressurePlate p : checkpoints) {
                    if (p.getLocation().equals(location) && p.getType() != 2) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-Placement", "The place you are trying to setup that point is currently set for a different type of point. If this is a mistake, please type 'cancel' and re-setup your parkour.", true, Collections.emptyMap());
                        return;
                    }
                }
                if (startPoint.getLocation().equals(location)) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-Placement", "The place you are trying to setup that point is currently set for a different type of point. If this is a mistake, please type 'cancel' and re-setup your parkour.", true, Collections.emptyMap());
                    return;
                }
                if (endPoint.getLocation().equals(location)) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-Placement", "The place you are trying to setup that point is currently set for a different type of point. If this is a mistake, please type 'cancel' and re-setup your parkour.", true, Collections.emptyMap());
                    return;
                }

                checkpoints.add(new Checkpoint(location, checkpoints.size() + 1));
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Checkpoint-Added", "Checkpoint successfully added.", true, Collections.emptyMap());
        }
    }


    public Player getPlayer() {
        return player;
    }
}
