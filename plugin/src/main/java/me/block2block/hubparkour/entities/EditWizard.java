package me.block2block.hubparkour.entities;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.plates.*;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import me.block2block.hubparkour.utils.ItemUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EditWizard {

    private final Player player;
    private final Parkour parkour;
    private int currentModification;
    private int after;
    private BorderPoint point;

    public EditWizard(Player player, Parkour parkour) {
        this.player = player;
        this.parkour = parkour;
        player.getInventory().addItem(ItemUtil.ci(Material.STICK, "&2&lHubParkour Setup Stick", 1, "&rUse this item to;&rsetup your HubParkour;&rParkour."));
        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Entered-Edit-Mode", "You have entered edit mode. You have been given the setup stick.", true, Collections.emptyMap());
        returnToMainMenu();
    }


    public boolean onChat(String message) {
        switch (currentModification) {
            case -1: {
                int edit;
                try {
                    edit = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                    return true;
                }

                switch (edit) {
                    case 1:
                        //Name
                        currentModification = 1;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Enter-New-Name", "Please enter a new name for your parkour into chat or type it with /parkour input [name]. It must not be a duplicate. Names are compatible with formatting codes.", true, Collections.emptyMap());
                        return true;
                    case 2:
                        //End Command
                        currentModification = 2;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Enter-New-End-Command", "Please enter a new end command for your parkour into chat or type it with /parkour input [name]. If you don't want one, just type 'none'.", true, Collections.emptyMap());
                        return true;
                    case 3:
                        //Checkpoint Command
                        currentModification = 3;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Enter-New-Checkpoint-Command", "Please enter a new checkpoint command for your parkour into chat or type it with /parkour input [name]. If you don't want one, just type 'none'.", true, Collections.emptyMap());
                        return true;
                    case 4:
                        //Start Point
                        currentModification = 4;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-New-Start-Point", "Please use the setup stick and select a new end point. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                        return true;
                    case 5:
                        //End Point
                        currentModification = 5;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-New-End-Point", "Please use the setup stick and select a new end point. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                        return true;
                    case 6:
                        //Restart Point
                        currentModification = 6;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-New-Restart-Point", "Please use the setup stick and select a new restart point. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                        return true;
                    case 7:
                        //Checkpoints
                        returnToCheckpointMenu();
                        return true;
                    case 8:
                        //Border points
                        currentModification = 11;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-First-Border-Point", "Please use the setup stick and select your first new border point. This will take the location of where you are standing, and does not take into account where you are looking. If you do not wish to use border points, then enter 'done' or execute /parkour done.", true, Collections.emptyMap());
                        return true;
                    case 9:
                        //Cooldown
                        currentModification = 13;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Please-Set-Reward-Cooldown", "Please specify a new cooldown for you rewards (only applicable when you have repeat-rewards enabled). If you do not wish to have one, please type 'none'.", true, Collections.emptyMap());
                        return true;
                    case 10:
                        // Exit Location
                        currentModification = 14;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Please-Set-Exit-Location", "Please use the setup stick and select the new Exit Location. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                        return true;
                    case 11:
                        //Exit
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Exited-Edit-Mode", "You have left edit mode.", true, Collections.emptyMap());
                        CacheManager.leaveEditMode();
                        return true;
                    default:
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Setup.Please-Set-End-Command", "You have entered edit mode. You have been given the setup stick.", true, Collections.emptyMap());
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                        return true;
                }
            }
            case 1: {
                if (CacheManager.getParkour(message) == null || CacheManager.getParkour(message) == parkour) {
                    parkour.setName(message);
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Name-Set", "Your new name has been set!", true, Collections.emptyMap());
                    returnToMainMenu();
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Name-Taken", "That name is already taken! Try again!", true, Collections.emptyMap());
                }
                return true;
            }
            case 2: {
                String command = message;
                if (message.equalsIgnoreCase("none")) {
                    command = null;
                }
                parkour.setEndCommand(command);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.End-Command-Set", "Your new end command has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                return true;
            }
            case 3: {
                String command = message;
                if (message.equalsIgnoreCase("none")) {
                    command = null;
                }
                parkour.setCheckpointCommand(command);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Command-Set", "Your new checkpoint command has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                return true;
            }
            case 7: {
                int edit;
                try {
                    edit = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                    return true;
                }
                switch (edit) {
                    case 1:
                        if (parkour.getCheckpoints().size() == 0) {
                            ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Delete.No-Checkpoints", "There are currently no checkpoints to delete! Try adding one instead!", true, Collections.emptyMap());
                            return true;
                        }
                        currentModification = 8;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Delete.Please-Enter-Checkpoint", "Which checkpoint number would you like to delete? Type the checkpoint number in chat.", true, Collections.emptyMap());
                        return true;
                    case 2:
                        currentModification = 9;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Add.After-Which", "What checkpoint should this checkpoint come &a&lafter&r? Type the checkpoint number in chat. If you want this to become the first checkpoint, just type 0.", true, Collections.emptyMap());
                        return true;
                    case 3:
                        returnToMainMenu();
                        return true;
                    default:
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                        return true;
                }
            }
            case 8: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToCheckpointMenu();
                    return true;
                }
                int checkpointNo;
                try {
                    checkpointNo = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Delete.Not-Valid-Checkpoint", "That is not a valid checkpoint, please try again. If you would like to cancel, type 'cancel'.", true, Collections.emptyMap());
                    return true;
                }

                Checkpoint checkpoint = parkour.getCheckpoint(checkpointNo);
                if (checkpoint != null) {
                    parkour.deleteCheckpoint(checkpoint);
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Delete.Success", "Checkpoint successfully deleted!", true, Collections.emptyMap());
                    returnToCheckpointMenu();
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Delete.Not-Valid-Checkpoint", "That checkpoint is not valid. If you want this to become the first checkpoint, just type 0.", true, Collections.emptyMap());
                    return true;
                }
                return true;
            }
            case 9: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToCheckpointMenu();
                    return true;
                }
                int checkpointNo;
                try {
                    checkpointNo = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Add.Not-Valid", "That checkpoint is not valid. If you want this to become the first checkpoint, just type 0.", true, Collections.emptyMap());
                    return true;
                }
                if (checkpointNo >= 0 && checkpointNo <= parkour.getNoCheckpoints()) {
                    this.after = checkpointNo;
                    currentModification = (10);
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Add.Select-Checkpoint", "Please use the setup stick and select a new checkpoint to add. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Add.Not-Valid", "The checkpoint has been successfully added!", true, Collections.emptyMap());
                }
                return true;
            }
            case 11: {
                if (message.equalsIgnoreCase("done")) {
                    parkour.setBorders(Collections.emptyList());
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Border-Updated", "Border successfully updated!", true, Collections.emptyMap());
                    returnToMainMenu();
                }
            }
            case 13: {
                String cooldowns = message;
                int cooldown;
                if (message.equalsIgnoreCase("none")) {
                    cooldowns = "-1";
                }

                try {
                    cooldown = Integer.parseInt(cooldowns);
                } catch (NumberFormatException e) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Invalid-Cooldown", "That cooldown is not valid. Please try again. If you do not wish to have one, please type 'none'.", true, Collections.emptyMap());
                    return true;
                }
                parkour.setRewardCooldown(cooldown);

                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Reward-Cooldown-Updated", "Reward cooldown successfully updated!", true, Collections.emptyMap());
                returnToMainMenu();
                return true;
            }
            case 14:
                if (message.equalsIgnoreCase("cancel")) {
                    returnToMainMenu();
                    return true;
                }
                if (message.equalsIgnoreCase("none")) {
                    if (parkour.getExitPoint() == null) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Exit-Point-Already-Null", "Exit point is already set to nothing.", true, Collections.emptyMap());
                        returnToMainMenu();
                        return true;
                    }
                    parkour.deleteExitPoint();
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Exit-Point-Updated-None", "Exit point successfully wiped.", true, Collections.emptyMap());
                    return true;
                }
        }
        return false;
    }

    public void onStick(Location location) {
        switch (currentModification) {
            case 4: {
                StartPoint startPoint = new StartPoint(location);

                if (borderCheck(location)) return;

                parkour.setStartPoint(startPoint);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Start-Point-Set", "Your new start point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }
            case 5: {
                EndPoint endPoint = new EndPoint(location);

                if (borderCheck(location)) return;
                parkour.setEndPoint(endPoint);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.End-Point-Set", "Your new end point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }
            case 6: {
                RestartPoint restartPoint = new RestartPoint(location);
                if (borderCheck(location)) return;
                parkour.setRestartPoint(restartPoint);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Restart-Point-Set", "Your new restart point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }
            case 10: {
                Checkpoint checkpoint = new Checkpoint(location, after + 1);
                if (borderCheck(location)) return;
                parkour.addCheckpoint(checkpoint, after + 1);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Add.Success", "The checkpoint has been successfully added!", true, Collections.emptyMap());
                returnToCheckpointMenu();
                break;
            }
            case 11: {
                point = new BorderPoint(location);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-Second-Border-Point", "Please use the setup stick and select your second new border point.", true, Collections.emptyMap());
                currentModification = 12;
                break;
            }
            case 12:
                double highX = 0, lowX = 0, highY = 0, lowY = 0, highZ = 0, lowZ = 0;
                if (location.getX() > point.getLocation().getX()) {
                    highX = location.getX();
                    lowX = point.getLocation().getX();
                } else {
                    highX = point.getLocation().getX();
                    lowX = location.getX();
                }

                if (location.getY() > point.getLocation().getY()) {
                    highY = location.getY();
                    lowY = point.getLocation().getY();
                } else {
                    highY = point.getLocation().getY();
                    lowY = location.getY();
                }

                if (location.getZ() > point.getLocation().getZ()) {
                    highZ = location.getZ();
                    lowZ = point.getLocation().getZ();
                } else {
                    highZ = point.getLocation().getZ();
                    lowZ = location.getZ();
                }
                if ((highX < parkour.getRestartPoint().getLocation().getX() || lowX > parkour.getRestartPoint().getLocation().getX()) || (highY < parkour.getRestartPoint().getLocation().getY() || lowY > parkour.getRestartPoint().getLocation().getY()) || (highZ < parkour.getRestartPoint().getLocation().getZ() || lowZ > parkour.getRestartPoint().getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Plates-Outside-Border.Restart", "Your restart point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentModification = 11;
                    return;
                }
                if ((highX < parkour.getStart().getLocation().getX() || lowX > parkour.getStart().getLocation().getX()) || (highY < parkour.getStart().getLocation().getY() || lowY > parkour.getStart().getLocation().getY()) || (highZ < parkour.getStart().getLocation().getZ() || lowZ > parkour.getStart().getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Plates-Outside-Border.Start", "Your start point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentModification = 11;
                    return;
                }
                if ((highX < parkour.getEndPoint().getLocation().getX() || lowX > parkour.getEndPoint().getLocation().getX()) || (highY < parkour.getEndPoint().getLocation().getY() || lowY > parkour.getEndPoint().getLocation().getY()) || (highZ < parkour.getEndPoint().getLocation().getZ() || lowZ > parkour.getEndPoint().getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Plates-Outside-Border.End", "Your end point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentModification = 11;
                    return;
                }
                for (Checkpoint checkpoint : parkour.getCheckpoints()) {
                    if ((highX < checkpoint.getLocation().getX() || lowX > checkpoint.getLocation().getX()) || (highY < checkpoint.getLocation().getY() || lowY > checkpoint.getLocation().getY()) || (highZ < checkpoint.getLocation().getZ() || lowZ > checkpoint.getLocation().getZ())) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Plates-Outside-Border.Checkpoint", "One of your checkpoints is currently outside your border! Please try again!", true, Collections.emptyMap());
                        currentModification = 11;
                        return;
                    }
                }

                parkour.setBorders(Arrays.asList(point, new BorderPoint(location)));
                point = null;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Border-Updated", "Border successfully updated!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            case 14: {
                ExitPoint exitPoint = new ExitPoint(location);

                if (borderCheck(location)) return;
                parkour.setExitPoint(exitPoint, parkour.getExitPoint() != null);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Exit-Point-Set", "Your new exit point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }

        }
    }

    private boolean borderCheck(Location location) {
        if (!parkour.getBorders().isEmpty()) {
            Location borderA = parkour.getBorders().get(0).getLocation(), borderB = parkour.getBorders().get(1).getLocation();


            double highX = 0, lowX = 0, highY = 0, lowY = 0, highZ = 0, lowZ = 0;
            if (borderA.getX() > borderB.getX()) {
                highX = borderA.getX();
                lowX = borderB.getX();
            } else {
                highX = borderB.getX();
                lowX = borderA.getX();
            }

            if (borderA.getY() > borderB.getY()) {
                highY = borderA.getY();
                lowY = borderB.getY();
            } else {
                highY = borderB.getY();
                lowY = borderA.getY();
            }

            if (borderA.getZ() > borderB.getZ()) {
                highZ = borderA.getZ();
                lowZ = borderB.getZ();
            } else {
                highZ = borderB.getZ();
                lowZ = borderA.getZ();
            }
            if ((highX < location.getX() || lowX > location.getX()) || (highY < location.getY() || lowY > location.getY()) || (highZ < location.getZ() || lowZ > location.getZ())) {
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Outside-Border", "The new location of your plate is outside your border! Please try again or adjust your border points first.", true, Collections.emptyMap());
                return true;
            }
        }
        return false;
    }


    private void returnToMainMenu() {
        currentModification = -1;
        StringBuilder sb = new StringBuilder();

        //Default value list
        List<String> defaultList = new ArrayList<>();
        defaultList.add("Please select what you would like to edit about parkour &a{parkour-name}&r. Type the digit into chat or type it with /parkour input [name].");
        defaultList.add("Please note that split times will be reset for this parkour upon modifications to any parkour points.");
        defaultList.add("&a1&r - Name");
        defaultList.add("&a2&r - End Command");
        defaultList.add("&a3&r - Checkpoint Command");
        defaultList.add("&a4&r - Start Point");
        defaultList.add("&a5&r - End Point");
        defaultList.add("&a6&r - Restart Point");
        defaultList.add("&a7&r - Checkpoints");
        defaultList.add("&a8&r - Border Points");
        defaultList.add("&a9&r - Reward Cooldown");
        defaultList.add("&a10&r - Exit Point");
        defaultList.add("&a11&r - Exit Edit Mode");

        for (String s : ConfigUtil.getStringList("Messages.Commands.Admin.Edit.Choose-Edit", defaultList)) {
            sb.append(s.replace("{parkour-name}", parkour.getName())).append("\n");
        }
        String s = sb.toString();

        if (HubParkour.isPlaceholders()) {
            s = PlaceholderAPI.setPlaceholders(player, s);
        }

        player.sendMessage(HubParkour.c(true, s.trim()));
    }

    private void returnToCheckpointMenu() {
        currentModification = 7;
        StringBuilder sb = new StringBuilder();

        //Default value list
        List<String> defaultList = new ArrayList<>();
        defaultList.add("What would you like to edit about checkpoints on parkour &a{parkour-name}&r? Type the digit into chat or type it with /parkour input [name].");
        defaultList.add("&a1&r - Delete checkpoints");
        defaultList.add("&a2&r - Add Checkpoints");
        defaultList.add("&a3&r - Cancel");

        for (String s : ConfigUtil.getStringList("Messages.Commands.Admin.Edit.Choose-Checkpoint-Edit", defaultList)) {
            sb.append(s.replace("{parkour-name}", parkour.getName())).append("\n");
        }
        String s = sb.toString();

        if (HubParkour.isPlaceholders()) {
            s = PlaceholderAPI.setPlaceholders(player, s);
        }

        player.sendMessage(HubParkour.c(true, s.trim()));
    }

    public Player getPlayer() {
        return player;
    }

    public Parkour getParkour() {
        return parkour;
    }
}
