package me.block2block.hubparkour.entities;

import me.block2block.hubparkour.HubParkour;
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

    private final Player player;
    private StartPoint startPoint;
    private EndPoint endPoint;
    private ExitPoint exitPoint;
    private RestartPoint restartPoint;
    private final List<Checkpoint> checkpoints = new ArrayList<>();
    private String endCommand, name, checkpointCommand;
    private int currentStage;
    private int cooldown;
    private final List<BorderPoint> borderPoints = new ArrayList<>();


    public SetupWizard(Player player) {
        this.player = player;
        currentStage = 0;
        player.getInventory().addItem(ItemUtil.ci(Material.STICK, "&2&lHubParkour Setup Stick", 1, "&rUse this item to;&rsetup your HubParkour;&rParkour."));
        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Given-Setup-Stick", "You have been given the setup stick.", true, Collections.emptyMap());
        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Start", "Welcome to the parkour setup wizard!\n\nBefore we start, please note that the setup wizard will use your player location when clicking to register locations, not where you look when clicking the stick. When players use commands, they will get teleported facing the direction you are facing when clicking the stick.\nIn order to start, please start off by clicking where you would like your start pressure plate with the stick. Do not worry about placing any pressure plates, the plugin will do that for you! If you wish to cancel at any time, enter 'cancel' or type /parkour cancel.", true, Collections.emptyMap());
    }

    public boolean onChat(String message) {
        switch (currentStage) {
            case 2:
                if (message.equalsIgnoreCase("none")) {
                    currentStage++;
                    ConfigUtil.sendMessageOrDefault(player, "Message.Commands.Admin.Setup.Exit-Skip.Please-Set-Respawn-Location", "Exit location skipped. Next, you need to set your respawn point. Click the stick while standing in your respawn point.", true, Collections.emptyMap());
                }

                return true;
            case 7:
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
            case 8:
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
            case 9:
                if (!message.equalsIgnoreCase("cancel")) {
                    String command = message;
                    if (message.equalsIgnoreCase("none")) {
                        command = null;
                    }
                    this.checkpointCommand = command;
                    currentStage++;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Reward-Cooldown", "Please specify a cooldown for you rewards (only applicable when you have repeat-rewards enabled). If you do not wish to have one, please type 'none'.", true, Collections.emptyMap());
                } else {
                    CacheManager.exitSetup();
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup-Cancelled", "Parkour setup was cancelled. Any points that were setup have been deleted.", true, Collections.emptyMap());
                }
                return true;
            case 10: {
                if (!message.equalsIgnoreCase("cancel")) {
                    String cooldown = message;
                    if (message.equalsIgnoreCase("none")) {
                        cooldown = "-1";
                    }
                    currentStage++;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Is-Parkour-Global", "Would you like this parkour to be global (appear on all of your servers)? Type 'y' to make this parkour global, or 'n' to make it server-specific. This cannot be changed once the parkour has been created.", true, Collections.emptyMap());
                    try {
                        this.cooldown = Integer.parseInt(cooldown);
                    } catch (NumberFormatException e) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-Cooldown", "That cooldown is not valid. Please try again. If you do not wish to have one, please type 'none'.", true, Collections.emptyMap());
                        return true;
                    }


                } else {
                    CacheManager.exitSetup();
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup-Cancelled", "Parkour setup was cancelled. Any points that were setup have been deleted.", true, Collections.emptyMap());
                }
            }
            case 11:
                if (!message.equalsIgnoreCase("cancel")) {
                    boolean global = false;
                    if (message.equalsIgnoreCase("y")) {
                        global = true;
                    } else if (!message.equalsIgnoreCase("n")) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Is-Parkour-Global", "Would you like this parkour to be global (appear on all of your servers)? Type 'y' to make this parkour global, or 'n' to make it server-specific. This cannot be changed once the parkour has been created.", true, Collections.emptyMap());
                        return true;
                    }

                    final Parkour parkour = new Parkour(-1, ((global?null:HubParkour.getServerUuid())), name, startPoint, endPoint, null, checkpoints, restartPoint, borderPoints, checkpointCommand, endCommand, cooldown);

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

                            if (HubParkour.isHolograms()) {
                                parkour.generateHolograms();
                            }

                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    Parkour newParkour = HubParkour.getInstance().getDbManager().addParkour(parkour);
                                    CacheManager.addParkour(newParkour);
                                    CacheManager.exitSetup();
                                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup-Complete", "Parkour setup complete!", true, Collections.emptyMap());
                                }
                            }.runTaskAsynchronously(HubParkour.getInstance());
                        }
                    }.runTask(HubParkour.getInstance());
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
                    if (currentStage == 4 || currentStage == 5) {
                        currentStage = 7;
                        borderPoints.clear();
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Name", "Now, you need to set a name for your parkour! Please enter a name for your parkour into chat or type it with /parkour input [name]. It must be one word and not a duplicate. Names are compatible with formatting codes.", true, Collections.emptyMap());
                        return true;
                    } else if (currentStage == 3) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Border", "Next, you need to set your border points. In order to do so, please click the stick while in the location you would like to set your border points. If you do not wish to use border points, then enter 'done' or execute /parkour done.", true, Collections.emptyMap());
                        currentStage++;
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
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Exit", "End point set! Next, you need to set your exit point. Click the stick while standing in your exit point. You can say 'none' for no exit location.", true, Collections.emptyMap());
                break;
            case 2:
                exitPoint = new ExitPoint(location);
                currentStage++;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Respawn", "Exit point set! Next, you need to set your respawn point. Click the stick while standing in your respawn point.", true, Collections.emptyMap());
                break;
            case 3:
                restartPoint = new RestartPoint(location);
                currentStage++;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup.Please-Set-Checkpoints", "Respawn point set! Now, you need to select any checkpoints you want. Click on each checkpoint pressure plate, in order you want them completed, then enter 'done'.", true, Collections.emptyMap());
                break;
            case 4:
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
                break;
            case 5: {
                borderPoints.add(new BorderPoint(location));
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Second-Border", "Great, now set your second border point! If you do not wish to use border points, then enter 'done' or execute /parkour done.", true, Collections.emptyMap());
                currentStage++;
                break;
            }
            case 6: {
                Location other = borderPoints.get(0).getLocation();

                double highX = 0, lowX = 0, highY = 0, lowY = 0, highZ = 0, lowZ = 0;
                if (location.getX() > other.getX()) {
                    highX = location.getX();
                    lowX = other.getX();
                } else {
                    highX = other.getX();
                    lowX = location.getX();
                }

                if (location.getY() > other.getY()) {
                    highY = location.getY();
                    lowY = other.getY();
                } else {
                    highY = other.getY();
                    lowY = location.getY();
                }

                if (location.getZ() > other.getZ()) {
                    highZ = location.getZ();
                    lowZ = other.getZ();
                } else {
                    highZ = other.getZ();
                    lowZ = location.getZ();
                }
                if ((highX < restartPoint.getLocation().getX() || lowX > restartPoint.getLocation().getX()) || (highY < restartPoint.getLocation().getY() || lowY > restartPoint.getLocation().getY()) || (highZ < restartPoint.getLocation().getZ() || lowZ > restartPoint.getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Plates-Outside-Border.Restart", "Your restart point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentStage = 4;
                    return;
                }
                if ((highX < startPoint.getLocation().getX() || lowX > startPoint.getLocation().getX()) || (highY < startPoint.getLocation().getY() || lowY > startPoint.getLocation().getY()) || (highZ < startPoint.getLocation().getZ() || lowZ > startPoint.getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Plates-Outside-Border.Start", "Your start point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentStage = 4;
                    return;
                }
                if ((highX < endPoint.getLocation().getX() || lowX > endPoint.getLocation().getX()) || (highY < endPoint.getLocation().getY() || lowY > endPoint.getLocation().getY()) || (highZ < endPoint.getLocation().getZ() || lowZ > endPoint.getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Plates-Outside-Border.End", "Your end point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentStage = 4;
                    return;
                }
                for (Checkpoint checkpoint : checkpoints) {
                    if ((highX < checkpoint.getLocation().getX() || lowX > checkpoint.getLocation().getX()) || (highY < checkpoint.getLocation().getY() || lowY > checkpoint.getLocation().getY()) || (highZ < checkpoint.getLocation().getZ() || lowZ > checkpoint.getLocation().getZ())) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Plates-Outside-Border.Checkpoint", "One of your checkpoints is currently outside your border! Please try again!", true, Collections.emptyMap());
                        currentStage = 4;
                        return;
                    }
                }
                currentStage++;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Name", "Now, you need to set a name for your parkour! Please enter a name for your parkour into chat or type it with /parkour input [name]. It must be one word and not a duplicate. Names are compatible with formatting codes.", true, Collections.emptyMap());
                break;
            }
        }
    }


    public Player getPlayer() {
        return player;
    }
}
