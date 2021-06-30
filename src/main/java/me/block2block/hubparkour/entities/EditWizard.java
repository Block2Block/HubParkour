package me.block2block.hubparkour.entities;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.plates.Checkpoint;
import me.block2block.hubparkour.api.plates.EndPoint;
import me.block2block.hubparkour.api.plates.RestartPoint;
import me.block2block.hubparkour.api.plates.StartPoint;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import me.block2block.hubparkour.utils.ItemUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EditWizard {

    private final Player player;
    private final Parkour parkour;
    private int currentModification;
    private int after;

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
        }
        return false;
    }

    public void onStick(Location location) {
        switch (currentModification) {
            case 4: {
                StartPoint startPoint = new StartPoint(location);
                parkour.setStartPoint(startPoint);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Start-Point-Set", "Your new start point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }
            case 5: {
                EndPoint endPoint = new EndPoint(location);
                parkour.setEndPoint(endPoint);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.End-Point-Set", "Your new end point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }
            case 6: {
                RestartPoint restartPoint = new RestartPoint(location);
                parkour.setRestartPoint(restartPoint);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Restart-Point-Set", "Your new restart point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }
            case 10: {
                Checkpoint checkpoint = new Checkpoint(location, after + 1);
                parkour.addCheckpoint(checkpoint, after + 1);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoint-Add.Success", "The checkpoint has been successfully added!", true, Collections.emptyMap());
                returnToCheckpointMenu();
                break;
            }
        }
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
        defaultList.add("&a8&r - Exit Edit Mode");

        for (String s : ConfigUtil.getStringList("Messages.Commands.Admin.Edit.Choose-Edit", defaultList)) {
            sb.append(s.replace("{parkour-name}", parkour.getName())).append("\n");
        }
        String s = sb.toString();

        if (Main.isPlaceholders()) {
            s = PlaceholderAPI.setPlaceholders(player, s);
        }

        player.sendMessage(Main.c(true, s.trim()));
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

        if (Main.isPlaceholders()) {
            s = PlaceholderAPI.setPlaceholders(player, s);
        }

        player.sendMessage(Main.c(true, s.trim()));
    }

    public Player getPlayer() {
        return player;
    }

    public Parkour getParkour() {
        return parkour;
    }
}
