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
    private String name;
    private final List<String> endCommands;
    private final List<String> globalCheckpointRewards;
    private SetupStage currentStage;
    private int cooldown;
    private final List<BorderPoint> borderPoints = new ArrayList<>();

    private Material material;
    private short data;
    private int customModelData;


    public SetupWizard(Player player) {
        this.player = player;
        currentStage = SetupStage.START_POINT;
        player.getInventory().addItem(ItemUtil.ci(Material.STICK, "&2&lHubParkour Setup Stick", 1, "&rUse this item to;&rsetup your HubParkour;&rParkour."));
        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Given-Setup-Stick", "You have been given the setup stick.", true, Collections.emptyMap());
        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Start", "Welcome to the parkour setup wizard!\n\nBefore we start, please note that the setup wizard will use your player location when clicking to register locations, not where you look when clicking the stick. When players use commands, they will get teleported facing the direction you are facing when clicking the stick.\nIn order to start, please start off by clicking where you would like your start pressure plate with the stick. Do not worry about placing any pressure plates, the plugin will do that for you! If you wish to cancel at any time, enter 'cancel' or type /parkour cancel.", true, Collections.emptyMap());

        endCommands = new ArrayList<>();
        globalCheckpointRewards = new ArrayList<>();
    }

    public boolean onChat(String message) {
        if (message.equalsIgnoreCase("cancel")) {
            CacheManager.exitSetup();
            ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup-Cancelled", "Parkour setup was cancelled. Any points that were setup have been deleted.", true, Collections.emptyMap());
            return true;
        }
        switch (currentStage) {
            case EXIT_POINT:
                if (message.equalsIgnoreCase("none")) {
                    currentStage = SetupStage.RESTART_POINT;
                    ConfigUtil.sendMessageOrDefault(player, "Message.Commands.Admin.Setup.Exit-Skip.Please-Set-Respawn-Location", "Exit location skipped. Next, you need to set your respawn point. Click the stick while standing in your respawn point.", true, Collections.emptyMap());
                }

                return true;
            case NAME:
                if (CacheManager.getParkour(message) == null) {
                    name = message;
                    currentStage = SetupStage.END_COMMAND;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-End-Command", "The name has been set! Please now type out your reward commands for the end of your parkour into chat or type it with /parkour input [command]. You can specify more than one by submitting commands several times. When you're done or if you don't want any, type 'done'. Available placeholders are {player-uuid} and {player-name}.", true, Collections.emptyMap());
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Name-Taken", "That name is already taken. Please try again.", true, Collections.emptyMap());
                }
                return true;
            case END_COMMAND:
                if (message.equalsIgnoreCase("done")) {
                    currentStage = SetupStage.CHECKPOINT_COMMAND;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Checkpoint-Command", "The End Commands have been set! Please now type out your reward commands for all checkpoints of your parkour into chat or type it with /parkour input [command]. You can specify more than one by submitting commands several times. This command will only be executed for each reached checkpoint after they finish the parkour to prevent exploitation. When you're finished or if you don't want one, just type 'done'. Available placeholders are {player-uuid} and {player-name}.", true, Collections.emptyMap());
                    return true;
                }

                endCommands.add(message);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Command-Added", "The command has been added! When you're finished, type 'done'.", true, Collections.emptyMap());
                return true;
            case CHECKPOINT_COMMAND:
                if (message.equalsIgnoreCase("done")) {
                    currentStage = SetupStage.COOLDOWN;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Reward-Cooldown", "Please specify a cooldown for you rewards (only applicable when you have repeat-rewards enabled). If you do not wish to have one, please type 'none'.", true, Collections.emptyMap());
                    return true;
                }
                this.globalCheckpointRewards.add(message);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Command-Added", "The command has been added! When you're finished, type 'done'.", true, Collections.emptyMap());
                return true;
            case COOLDOWN: {
                String cooldown = message;
                if (message.equalsIgnoreCase("none")) {
                    cooldown = "-1";
                }
                try {
                    this.cooldown = Integer.parseInt(cooldown);
                } catch (NumberFormatException e) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-Cooldown", "That cooldown is not valid. Please try again. If you do not wish to have one, please type 'none'.", true, Collections.emptyMap());
                    return true;
                }
                currentStage = SetupStage.GUI_ITEM;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Select-GUI-Item", "Cooldown has been set! Please specify how you wish for the GUI to display in-game. Format it MATERIAL:DATA:MODEL, where MATERIAL is the item type from the Spigot API, DATA is a number, and MODEL is the custom model data number (1.14+ only, -1 to disable).", true, Collections.emptyMap());
                return true;
            }
            case GUI_ITEM: {
                String[] parts = message.split(":");
                if (parts.length != 3) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-GUI-Item", "That GUI item is not valid. Please try again. Format it MATERIAL:DATA:MODEL, where MATERIAL is the item type from the Spigot API, DATA is a number, and MODEL is the custom model data number (1.14+ only, -1 to disable).", true, Collections.emptyMap());
                    return true;
                }

                try {
                    data = Short.parseShort(parts[1]);
                } catch (NumberFormatException e) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-GUI-Item", "That GUI item is not valid. Please try again. Format it MATERIAL:DATA:MODEL, where MATERIAL is the item type from the Spigot API, DATA is a number, and MODEL is the custom model data number (1.14+ only, -1 to disable).", true, Collections.emptyMap());
                    return true;
                }
                try {
                    material = Material.valueOf(parts[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-GUI-Item", "That GUI item is not valid. Please try again. Format it MATERIAL:DATA:MODEL, where MATERIAL is the item type from the Spigot API, DATA is a number, and MODEL is the custom model data number (1.14+ only, -1 to disable).", true, Collections.emptyMap());
                    return true;
                }

                try {
                    customModelData = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-GUI-Item", "That GUI item is not valid. Please try again. Format it MATERIAL:DATA:MODEL, where MATERIAL is the item type from the Spigot API, DATA is a number, and MODEL is the custom model data number (1.14+ only, -1 to disable).", true, Collections.emptyMap());
                    return true;
                }

                currentStage = SetupStage.GLOBAL;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Is-Parkour-Global", "Item successfully set! Would you like this parkour to be global (appear on all of your servers)? Type 'y' to make this parkour global, or 'n' to make it server-specific. This cannot be changed once the parkour has been created.", true, Collections.emptyMap());
                return true;
            }
            case GLOBAL:
                boolean global = false;
                if (message.equalsIgnoreCase("y")) {
                    global = true;
                } else if (!message.equalsIgnoreCase("n")) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Is-Parkour-Global", "Item successfully set! Would you like this parkour to be global (appear on all of your servers)? Type 'y' to make this parkour global, or 'n' to make it server-specific. This cannot be changed once the parkour has been created.", true, Collections.emptyMap());
                    return true;
                }

                final Parkour parkour = new Parkour(-1, ((global ? null : HubParkour.getServerUuid())), name, startPoint, endPoint, exitPoint, checkpoints, restartPoint, borderPoints, globalCheckpointRewards, endCommands, cooldown, material, data, customModelData);

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

                        new BukkitRunnable() {
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
                return true;
            case CHECKPOINTS:
                if (message.equalsIgnoreCase("done")) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Border", "Next, you need to set your border points. In order to do so, please click the stick while in the location you would like to set your border points. If you do not wish to use border points, then enter 'done' or execute /parkour done.", true, Collections.emptyMap());
                    currentStage = SetupStage.BORDER_POINT_A;
                    return true;
                }
                Checkpoint checkpoint = checkpoints.get(checkpoints.size() - 1);
                List<String> rewards = checkpoint.getRewards();
                if (rewards == null) {
                    rewards = new ArrayList<>();
                }
                rewards.add(message);
                checkpoint.setRewards(rewards);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Command-Added", "The command has been added! When you're finished, type 'done'.", true, Collections.emptyMap());
                return true;
            default:
                if (message.equalsIgnoreCase("done")) {
                    if (currentStage == SetupStage.BORDER_POINT_A || currentStage == SetupStage.BORDER_POINT_B) {
                        currentStage = SetupStage.NAME;
                        borderPoints.clear();
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
            case START_POINT:
                startPoint = new StartPoint(location);
                currentStage = SetupStage.END_POINT;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-End", "Start point set! Now, click where you would like your end pressure plate with your stick.", true, Collections.emptyMap());
                break;
            case END_POINT:
                if (startPoint.getLocation().equals(location)) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Invalid-Placement", "The place you are trying to setup that point is currently set for a different type of point. If this is a mistake, please type 'cancel' and re-setup your parkour.", true, Collections.emptyMap());
                    return;
                }
                endPoint = new EndPoint(location);
                currentStage = SetupStage.EXIT_POINT;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Exit", "End point set! Next, you need to set your exit point. Click the stick while standing in your exit point. You can say 'none' for no exit location.", true, Collections.emptyMap());
                break;
            case EXIT_POINT:
                exitPoint = new ExitPoint(location);
                currentStage = SetupStage.RESTART_POINT;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Respawn", "Exit point set! Next, you need to set your respawn point. Click the stick while standing in your respawn point.", true, Collections.emptyMap());
                break;
            case RESTART_POINT:
                restartPoint = new RestartPoint(location);
                currentStage = SetupStage.CHECKPOINTS;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Setup.Please-Set-Checkpoints", "Respawn point set! Now, you need to select any checkpoints you want. Click on each checkpoint pressure plate, in order you want them completed, then enter 'done'.", true, Collections.emptyMap());
                break;
            case CHECKPOINTS:
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

                checkpoints.add(new Checkpoint(location, checkpoints.size() + 1, null));
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Checkpoint-Added", "Checkpoint successfully added. If you wish to add rewards for this particular checkpoint, type them in chat or type it with /parkour input [command]. You can specify more than one by submitting commands several times. To continue, either add another checkpoint or type 'done' to finish adding checkpoints.", true, Collections.emptyMap());
                break;
            case BORDER_POINT_A: {
                borderPoints.add(new BorderPoint(location));
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Second-Border", "Great, now set your second border point! If you do not wish to use border points, then enter 'done' or execute /parkour done.", true, Collections.emptyMap());
                currentStage = SetupStage.BORDER_POINT_B;
                break;
            }
            case BORDER_POINT_B: {
                Location other = borderPoints.get(0).getLocation();

                double highX, lowX, highY, lowY, highZ, lowZ;
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
                    currentStage = SetupStage.BORDER_POINT_A;
                    return;
                }
                if ((highX < startPoint.getLocation().getX() || lowX > startPoint.getLocation().getX()) || (highY < startPoint.getLocation().getY() || lowY > startPoint.getLocation().getY()) || (highZ < startPoint.getLocation().getZ() || lowZ > startPoint.getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Plates-Outside-Border.Start", "Your start point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentStage = SetupStage.BORDER_POINT_A;
                    return;
                }
                if ((highX < endPoint.getLocation().getX() || lowX > endPoint.getLocation().getX()) || (highY < endPoint.getLocation().getY() || lowY > endPoint.getLocation().getY()) || (highZ < endPoint.getLocation().getZ() || lowZ > endPoint.getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Plates-Outside-Border.End", "Your end point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentStage = SetupStage.BORDER_POINT_A;
                    return;
                }
                for (Checkpoint checkpoint : checkpoints) {
                    if ((highX < checkpoint.getLocation().getX() || lowX > checkpoint.getLocation().getX()) || (highY < checkpoint.getLocation().getY() || lowY > checkpoint.getLocation().getY()) || (highZ < checkpoint.getLocation().getZ() || lowZ > checkpoint.getLocation().getZ())) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Plates-Outside-Border.Checkpoint", "One of your checkpoints is currently outside your border! Please try again!", true, Collections.emptyMap());
                        currentStage = SetupStage.BORDER_POINT_A;
                        return;
                    }
                }
                currentStage = SetupStage.NAME;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-Name", "Now, you need to set a name for your parkour! Please enter a name for your parkour into chat or type it with /parkour input [name]. It must be one word and not a duplicate. Names are compatible with formatting codes.", true, Collections.emptyMap());
                break;
            }
        }
    }


    public Player getPlayer() {
        return player;
    }

    public enum SetupStage {
        START_POINT,
        END_POINT,
        EXIT_POINT,
        RESTART_POINT,
        CHECKPOINTS,
        BORDER_POINT_A,
        BORDER_POINT_B,
        NAME,
        END_COMMAND,
        CHECKPOINT_COMMAND,
        COOLDOWN,
        GUI_ITEM,
        GLOBAL
    }


}
