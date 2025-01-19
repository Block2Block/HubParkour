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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EditWizard {

    private final Player player;
    private final Parkour parkour;
    private WizardStep currentModification;

    private int after;
    private Checkpoint checkpoint;

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
            case MENU: {
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
                        currentModification = WizardStep.NAME;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Enter-New-Name", "Please enter a new name for your parkour into chat or type it with /parkour input [name]. It must not be a duplicate. Names are compatible with formatting codes.", true, Collections.emptyMap());
                        return true;
                    case 2:
                        currentModification = WizardStep.GUI_ITEM;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Item.Enter-Item", "Please specify how you wish for the GUI to display in-game. Format it MATERIAL:DATA, where MATERIAL is the item type from the Spigot API, and DATA is a number. To cancel, type 'cancel'.", true, Collections.emptyMap());
                        return true;
                    case 3:
                        //End Command
                        displayEndCommandMenu();
                        return true;
                    case 4:
                        //Checkpoint Command
                        displayGlobalCheckpointMenu();
                        return true;
                    case 5:
                        //Start Point
                        currentModification = WizardStep.START_POINT;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-New-Start-Point", "Please use the setup stick and select a new end point. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                        return true;
                    case 6:
                        //End Point
                        currentModification = WizardStep.END_POINT;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-New-End-Point", "Please use the setup stick and select a new end point. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                        return true;
                    case 7:
                        //Restart Point
                        currentModification = WizardStep.RESTART_POINT;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-New-Restart-Point", "Please use the setup stick and select a new restart point. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                        return true;
                    case 8:
                        //Checkpoints
                        returnToCheckpointMenu();
                        return true;
                    case 9:
                        //Border points
                        currentModification = WizardStep.BORDER_POINT_A;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-First-Border-Point", "Please use the setup stick and select your first new border point. This will take the location of where you are standing, and does not take into account where you are looking. If you do not wish to use border points, then enter 'done' or execute /parkour done.", true, Collections.emptyMap());
                        return true;
                    case 10:
                        //Cooldown
                        currentModification = WizardStep.REWARD_COOLDOWN;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Please-Set-Reward-Cooldown", "Please specify a new cooldown for you rewards (only applicable when you have repeat-rewards enabled). If you do not wish to have one, please type 'none'.", true, Collections.emptyMap());
                        return true;
                    case 11:
                        // Exit Location
                        currentModification = WizardStep.EXIT_POINT;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Please-Set-Exit-Location", "Please use the setup stick and select the new Exit Location. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                        return true;
                    case 12:
                        //Exit
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Exited-Edit-Mode", "You have left edit mode.", true, Collections.emptyMap());
                        CacheManager.leaveEditMode();
                        return true;
                    default:
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                        return true;
                }
            }
            case NAME: {
                if (CacheManager.getParkour(message) == null || CacheManager.getParkour(message) == parkour) {
                    parkour.setName(message);
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Name-Set", "Your new name has been set!", true, Collections.emptyMap());
                    returnToMainMenu();
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Name-Taken", "That name is already taken! Try again!", true, Collections.emptyMap());
                }
                return true;
            }
            case GUI_ITEM: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToMainMenu();
                    return true;
                }
                String[] parts = message.split(":");

                short data;
                Material material;
                try {
                    data = Short.parseShort(parts[1]);
                } catch (NumberFormatException e) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Item.Invalid-GUI-Item", "That GUI item is not valid. Please try again. Format it MATERIAL:DATA, where MATERIAL is the item type from the Spigot API, and DATA is a number.", true, Collections.emptyMap());
                    return true;
                }
                try {
                    material = Material.valueOf(parts[0].toUpperCase());
                } catch (IllegalArgumentException e) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Item.Invalid-GUI-Item", "That GUI item is not valid. Please try again. Format it MATERIAL:DATA, where MATERIAL is the item type from the Spigot API, and DATA is a number.", true, Collections.emptyMap());
                    return true;
                }

                parkour.setItem(material, data);
                returnToMainMenu();
                return true;
            }
            case END_COMMAND: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToMainMenu();
                    return true;
                }
                int edit;
                try {
                    edit = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                    return true;
                }

                if (edit == 1) {
                    if (parkour.getEndCommands() == null || parkour.getEndCommands().isEmpty()) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Delete-No-Commands", "You cannot delete any commands as you have none set up. Either add a command or type 'cancel'.", true, Collections.emptyMap());
                        return true;
                    }
                    currentModification = WizardStep.END_COMMAND_DELETE;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Select-Command", "Which command number would you like to delete? Type 'cancel' to cancel.", true, Collections.emptyMap());
                } else if (edit == 2) {
                    currentModification = WizardStep.END_COMMAND_ADD;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Type-Command", "Please type your new commands in chat or type it with /parkour input [command]. Type 'done' once you're finished.", true, Collections.emptyMap());
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                }
                return true;
            }
            case END_COMMAND_DELETE: {
                if (message.equalsIgnoreCase("cancel")) {
                    displayEndCommandMenu();
                    return true;
                }
                int command;
                try {
                    command = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Not-Valid-Command", "That is not a valid command. Type the number of the command in chat.", true, Collections.emptyMap());
                    return true;
                }

                if (command < 1 || command > parkour.getEndCommands().size()) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Not-Valid-Command", "That is not a valid command. Type the number of the command in chat.", true, Collections.emptyMap());
                    return true;
                }

                List<String> commands = parkour.getEndCommands();
                commands.remove(command - 1);
                if (commands.isEmpty()) {
                    commands = null;
                }
                parkour.setEndCommands(commands);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Command-Deleted", "Command successfully deleted!", true, Collections.emptyMap());
                displayEndCommandMenu();
                return true;
            }
            case END_COMMAND_ADD: {
                if (message.equalsIgnoreCase("done")) {
                    displayEndCommandMenu();
                    return true;
                }
                List<String> commands = parkour.getEndCommands();
                if (commands == null) {
                    commands = new ArrayList<>();
                }
                commands.add(message);
                parkour.setEndCommands(commands);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Command-Added", "Command successfully added! Type 'done' once you're finished.", true, Collections.emptyMap());
                return true;
            }
            case CHECKPOINT_COMMAND: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToMainMenu();
                    return true;
                }
                int edit;
                try {
                    edit = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                    return true;
                }

                if (edit == 1) {
                    if (parkour.getGlobalCheckpointCommands() == null || parkour.getGlobalCheckpointCommands().isEmpty()) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Delete-No-Commands", "You cannot delete any commands as you have none set up. Either add a command or type 'cancel'.", true, Collections.emptyMap());
                        return true;
                    }
                    currentModification = WizardStep.CHECKPOINT_COMMAND_DELETE;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Select-Command", "Which command number would you like to delete? Type 'cancel' to cancel.", true, Collections.emptyMap());
                } else if (edit == 2) {
                    currentModification = WizardStep.CHECKPOINT_COMMAND_ADD;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Type-Command", "Please type your new commands in chat or type it with /parkour input [command]. Type 'done' once you're finished.", true, Collections.emptyMap());
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                }
                return true;
            }
            case CHECKPOINT_COMMAND_DELETE: {
                if (message.equalsIgnoreCase("cancel")) {
                    displayGlobalCheckpointMenu();
                    return true;
                }
                int command;
                try {
                    command = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Not-Valid-Command", "That is not a valid command. Type the number of the command in chat.", true, Collections.emptyMap());
                    return true;
                }

                if (command < 1 || command > parkour.getEndCommands().size()) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Not-Valid-Command", "That is not a valid command. Type the number of the command in chat.", true, Collections.emptyMap());
                    return true;
                }

                List<String> commands = parkour.getGlobalCheckpointCommands();
                commands.remove(command - 1);
                if (commands.isEmpty()) {
                    commands = null;
                }
                parkour.setGlobalCheckpointCommands(commands);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Command-Deleted", "Command successfully deleted!", true, Collections.emptyMap());
                displayEndCommandMenu();
                return true;
            }
            case CHECKPOINT_COMMAND_ADD: {
                if (message.equalsIgnoreCase("done")) {
                    displayGlobalCheckpointMenu();
                    return true;
                }
                List<String> commands = parkour.getGlobalCheckpointCommands();
                if (commands == null) {
                    commands = new ArrayList<>();
                }
                commands.add(message);
                parkour.setEndCommands(commands);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Command-Added", "Command successfully added! Type 'done' once you're finished.", true, Collections.emptyMap());
                return true;
            }
            case CHECKPOINTS: {
                int edit;
                try {
                    edit = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                    return true;
                }
                switch (edit) {
                    case 1:
                        if (parkour.getCheckpoints().isEmpty()) {
                            ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Delete.No-Checkpoints", "There are currently no checkpoints to delete! Try adding one instead!", true, Collections.emptyMap());
                            return true;
                        }
                        currentModification = WizardStep.CHECKPOINTS_DELETE;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Delete.Please-Enter-Checkpoint", "Which checkpoint number would you like to delete? Type the checkpoint number in chat.", true, Collections.emptyMap());
                        return true;
                    case 2:
                        currentModification = WizardStep.CHECKPOINTS_ADD;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Add.After-Which", "What checkpoint should this checkpoint come &a&lafter&r? Type the checkpoint number in chat. If you want this to become the first checkpoint, just type 0.", true, Collections.emptyMap());
                        return true;
                    case 3:
                        currentModification = WizardStep.CHECKPOINTS_REWARDS;
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Rewards.Select-Checkpoint", "Which checkpoint would you like to edit? You have {checkpoints} checkpoints. Type the checkpoint number into chat or type it with /parkour input [number]. Type 'cancel' to cancel.", true, Collections.singletonMap("checkpoints", parkour.getNoCheckpoints() + ""));
                        return true;
                    case 4:
                        returnToMainMenu();
                        return true;
                    default:
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                        return true;
                }
            }
            case CHECKPOINTS_REWARDS: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToCheckpointMenu();
                    return true;
                }
                int checkpointNo;
                try {
                    checkpointNo = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Rewards.Not-Valid", "That checkpoint is not valid.", true, Collections.emptyMap());
                    return true;
                }

                if (checkpointNo < 1 || checkpointNo > parkour.getNoCheckpoints()) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Rewards.Not-Valid", "That checkpoint is not valid.", true, Collections.emptyMap());
                    return true;
                }

                Checkpoint checkpoint = parkour.getCheckpoint(checkpointNo);
                if (checkpoint != null) {
                    this.checkpoint = checkpoint;
                    displayCheckpointRewardsMenu();
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Rewards.Not-Valid", "That checkpoint is not valid.", true, Collections.emptyMap());
                }
                return true;
            }
            case CHECKPOINTS_REWARDS_SELECTED: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToCheckpointMenu();
                    return true;
                }
                int edit;
                try {
                    edit = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                    return true;
                }

                if (edit == 1) {
                    if (checkpoint.getRewards() == null || checkpoint.getRewards().isEmpty()) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Delete-No-Commands", "You cannot delete any commands as you have none set up. Either add a command or type 'cancel'.", true, Collections.emptyMap());
                        return true;
                    }
                    currentModification = WizardStep.CHECKPOINTS_REWARDS_DELETE;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Select-Command", "Which command number would you like to delete? Type 'cancel' to cancel.", true, Collections.emptyMap());
                } else if (edit == 2) {
                    currentModification = WizardStep.CHECKPOINTS_REWARDS_ADD;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Type-Command", "Please type your new commands in chat or type it with /parkour input [command]. Type 'done' once you're finished.", true, Collections.emptyMap());
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Not-Valid-Edit", "That is not a valid edit. Please type the digit in chat.", true, Collections.emptyMap());
                }
                return true;
            }
            case CHECKPOINTS_REWARDS_DELETE: {
                if (message.equalsIgnoreCase("cancel")) {
                    displayCheckpointRewardsMenu();
                    return true;
                }
                int command;
                try {
                    command = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Not-Valid-Command", "That is not a valid command. Type the number of the command in chat.", true, Collections.emptyMap());
                    return true;
                }

                if (command < 1 || command > checkpoint.getRewards().size()) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Not-Valid-Command", "That is not a valid command. Type the number of the command in chat.", true, Collections.emptyMap());
                    return true;
                }

                List<String> commands = checkpoint.getRewards();
                commands.remove(command - 1);
                if (commands.isEmpty()) {
                    commands = null;
                }
                checkpoint.setRewards(commands);
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        HubParkour.getInstance().getDbManager().updateCheckpointRewards(
                                parkour.getId(),
                                checkpoint
                        );
                    }
                }.runTaskAsynchronously(HubParkour.getInstance());
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Command-Deleted", "Command successfully deleted!", true, Collections.emptyMap());
                displayCheckpointRewardsMenu();
                return true;
            }
            case CHECKPOINTS_REWARDS_ADD: {
                if (message.equalsIgnoreCase("done")) {
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            HubParkour.getInstance().getDbManager().updateCheckpointRewards(
                                    parkour.getId(),
                                    checkpoint
                            );
                        }
                    }.runTaskAsynchronously(HubParkour.getInstance());
                    displayCheckpointRewardsMenu();
                    return true;
                }
                List<String> commands = checkpoint.getRewards();
                if (commands == null) {
                    commands = new ArrayList<>();
                }
                commands.add(message);
                checkpoint.setRewards(commands);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Commands.Command-Added", "Command successfully added! Type 'done' once you're finished.", true, Collections.emptyMap());
                return true;
            }
            case CHECKPOINTS_DELETE: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToCheckpointMenu();
                    return true;
                }
                int checkpointNo;
                try {
                    checkpointNo = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Delete.Not-Valid-Checkpoint", "That is not a valid checkpoint, please try again. If you would like to cancel, type 'cancel'.", true, Collections.emptyMap());
                    return true;
                }

                Checkpoint checkpoint = parkour.getCheckpoint(checkpointNo);
                if (checkpoint != null) {
                    parkour.deleteCheckpoint(checkpoint);
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Delete.Success", "Checkpoint successfully deleted!", true, Collections.emptyMap());
                    returnToCheckpointMenu();
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Delete.Not-Valid-Checkpoint", "That checkpoint is not valid. If you want this to become the first checkpoint, just type 0.", true, Collections.emptyMap());
                    return true;
                }
                return true;
            }
            case CHECKPOINTS_ADD: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToCheckpointMenu();
                    return true;
                }
                int checkpointNo;
                try {
                    checkpointNo = Integer.parseInt(message);
                } catch (NumberFormatException exception) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Add.Not-Valid", "That checkpoint is not valid. If you want this to become the first checkpoint, just type 0.", true, Collections.emptyMap());
                    return true;
                }
                if (checkpointNo >= 0 && checkpointNo <= parkour.getNoCheckpoints()) {
                    this.after = checkpointNo;
                    currentModification = WizardStep.CHECKPOINTS_ADD_SELECTED;
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Add.Select-Checkpoint", "Please use the setup stick and select a new checkpoint to add. This will take the location of where you are standing, and does take into account where you are looking.", true, Collections.emptyMap());
                } else {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Add.Not-Valid", "The checkpoint has been successfully added!", true, Collections.emptyMap());
                }
                return true;
            }
            case CHECKPOINTS_ADD_SELECTED: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToCheckpointMenu();
                    return true;
                }
            }
            case CHECKPOINTS_ADD_REWARDS: {
                if (message.equalsIgnoreCase("cancel")) {
                    returnToCheckpointMenu();
                    return true;
                }
                if (message.equalsIgnoreCase("done")) {
                    parkour.addCheckpoint(checkpoint, after + 1);
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Add.Success", "The checkpoint has been successfully added!", true, Collections.emptyMap());
                    returnToCheckpointMenu();
                    return true;
                }

                List<String> rewards = checkpoint.getRewards();
                if (rewards == null) {
                    rewards = new ArrayList<>();
                }
                rewards.add(message);
                checkpoint.setRewards(rewards);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Add.Command-Added", "Command successfully added! If you're finished, type 'done'.", true, Collections.emptyMap());
                returnToCheckpointMenu();
                return true;
            }
            case BORDER_POINT_B:
            case BORDER_POINT_A: {
                if (message.equalsIgnoreCase("done")) {
                    parkour.setBorders(Collections.emptyList());
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Border-Updated", "Border successfully updated!", true, Collections.emptyMap());
                    returnToMainMenu();
                }
            }
            case REWARD_COOLDOWN: {
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
            case EXIT_POINT:
                if (message.equalsIgnoreCase("cancel")) {
                    returnToMainMenu();
                    return true;
                }
                if (message.equalsIgnoreCase("none")) {
                    if (parkour.getExitPoint() == null) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Exit-Point-Already-Nothing", "Exit point is already set to nothing.", true, Collections.emptyMap());
                        returnToMainMenu();
                        return true;
                    }
                    parkour.deleteExitPoint();
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Exit-Point-Updated-None", "Exit point successfully wiped.", true, Collections.emptyMap());
                    returnToMainMenu();
                    return true;
                }
        }
        return false;
    }

    public void onStick(Location location) {
        switch (currentModification) {
            case START_POINT: {
                StartPoint startPoint = new StartPoint(location);
                if (borderCheck(location)) return;
                parkour.setStartPoint(startPoint);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Start-Point-Set", "Your new start point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }
            case END_POINT: {
                EndPoint endPoint = new EndPoint(location);
                if (borderCheck(location)) return;
                parkour.setEndPoint(endPoint);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.End-Point-Set", "Your new end point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }
            case RESTART_POINT: {
                RestartPoint restartPoint = new RestartPoint(location);
                if (borderCheck(location)) return;
                parkour.setRestartPoint(restartPoint);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Restart-Point-Set", "Your new restart point has been set!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            }
            case CHECKPOINTS_ADD_SELECTED: {
                if (borderCheck(location)) return;
                checkpoint = new Checkpoint(location, after + 1, null);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Checkpoints.Add.Type-Rewards", "If you wish to add rewards for this particular checkpoint, type them in chat or type it with /parkour input [command]. You can specify more than one by submitting commands several times. Once you're finished, type 'done'.", true, Collections.emptyMap());
                break;
            }
            case BORDER_POINT_A: {
                point = new BorderPoint(location);
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Select-Second-Border-Point", "Please use the setup stick and select your second new border point.", true, Collections.emptyMap());
                currentModification = WizardStep.BORDER_POINT_B;
                break;
            }
            case BORDER_POINT_B:
                double highX, lowX, highY, lowY, highZ, lowZ;
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
                    currentModification = WizardStep.BORDER_POINT_A;
                    return;
                }
                if ((highX < parkour.getStart().getLocation().getX() || lowX > parkour.getStart().getLocation().getX()) || (highY < parkour.getStart().getLocation().getY() || lowY > parkour.getStart().getLocation().getY()) || (highZ < parkour.getStart().getLocation().getZ() || lowZ > parkour.getStart().getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Plates-Outside-Border.Start", "Your start point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentModification = WizardStep.BORDER_POINT_A;
                    return;
                }
                if ((highX < parkour.getEndPoint().getLocation().getX() || lowX > parkour.getEndPoint().getLocation().getX()) || (highY < parkour.getEndPoint().getLocation().getY() || lowY > parkour.getEndPoint().getLocation().getY()) || (highZ < parkour.getEndPoint().getLocation().getZ() || lowZ > parkour.getEndPoint().getLocation().getZ())) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Plates-Outside-Border.End", "Your end point is currently plates outside your border! Please try again!", true, Collections.emptyMap());
                    currentModification = WizardStep.BORDER_POINT_A;
                    return;
                }
                for (Checkpoint checkpoint : parkour.getCheckpoints()) {
                    if ((highX < checkpoint.getLocation().getX() || lowX > checkpoint.getLocation().getX()) || (highY < checkpoint.getLocation().getY() || lowY > checkpoint.getLocation().getY()) || (highZ < checkpoint.getLocation().getZ() || lowZ > checkpoint.getLocation().getZ())) {
                        ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Plates-Outside-Border.Checkpoint", "One of your checkpoints is currently outside your border! Please try again!", true, Collections.emptyMap());
                        currentModification = WizardStep.BORDER_POINT_A;
                        return;
                    }
                }

                parkour.setBorders(Arrays.asList(point, new BorderPoint(location)));
                point = null;
                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Admin.Edit.Border-Updated", "Border successfully updated!", true, Collections.emptyMap());
                returnToMainMenu();
                break;
            case EXIT_POINT: {
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


            double highX, lowX, highY, lowY, highZ, lowZ;
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

    private void displayEndCommandMenu() {
        currentModification = WizardStep.END_COMMAND;
        StringBuilder msg = new StringBuilder(ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-Header", "Your current {type} commands:").replace("{type}", "parkour end") + "\n");

        if (parkour.getEndCommands() != null && !parkour.getEndCommands().isEmpty()) {
            String entry = ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-Entry", "&a{number}&r - {command}");
            int i = 1;
            for (String command : parkour.getEndCommands()) {
                msg.append(entry.replace("{number}", String.valueOf(i++)).replace("{command}", command)).append("\n");
            }
        } else {
            msg.append(ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-No-Commands", "You currently have no commands set up.")).append("\n");
        }
        msg.append(ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-Footer", "If you wish to delete a command, type 1. If you wish to add new commands, type 2. To cancel, type 'cancel'."));
        String s = msg.toString();

        if (HubParkour.isPlaceholders()) {
            s = PlaceholderAPI.setPlaceholders(player, s);
        }
        player.sendMessage(HubParkour.c(true, s.trim()));
    }

    private void displayCheckpointRewardsMenu() {
        currentModification = WizardStep.CHECKPOINTS_REWARDS_SELECTED;
        StringBuilder msg = new StringBuilder(ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-Header", "Your current {type} commands:").replace("{type}", "checkpoint #" + checkpoint.getCheckpointNo()) + "\n");

        if (checkpoint.getRewards() != null && !checkpoint.getRewards().isEmpty()) {
            String entry = ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-Entry", "&a{number}&r - {command}");
            int i = 1;
            for (String command : checkpoint.getRewards()) {
                msg.append(entry.replace("{number}", String.valueOf(i++)).replace("{command}", command)).append("\n");
            }
        } else {
            msg.append(ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-No-Commands", "You currently have no commands set up.")).append("\n");
        }
        msg.append(ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-Footer", "If you wish to delete a command, type 1. If you wish to add new commands, type 2. To cancel, type 'cancel'."));
        String s = msg.toString();

        if (HubParkour.isPlaceholders()) {
            s = PlaceholderAPI.setPlaceholders(player, s);
        }
        player.sendMessage(HubParkour.c(true, s.trim()));
    }

    private void displayGlobalCheckpointMenu() {
        currentModification = WizardStep.END_COMMAND;
        StringBuilder msg = new StringBuilder(ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-Header", "Your current {type} commands:").replace("{type}", "global checkpoint") + "\n");

        if (parkour.getGlobalCheckpointCommands() != null && !parkour.getGlobalCheckpointCommands().isEmpty()) {
            String entry = ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-Entry", "&a{number}&r - {command}");
            int i = 1;
            for (String command : parkour.getEndCommands()) {
                msg.append(entry.replace("{number}", String.valueOf(i++)).replace("{command}", command)).append("\n");
            }
        } else {
            msg.append(ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-No-Commands", "You currently have no commands set up.")).append("\n");
        }
        msg.append(ConfigUtil.getString("Messages.Commands.Admin.Edit.Commands.List-Footer", "If you wish to delete a command, type 1. If you wish to add new commands, type 2. To cancel, type 'cancel'."));
        String s = msg.toString();

        if (HubParkour.isPlaceholders()) {
            s = PlaceholderAPI.setPlaceholders(player, s);
        }
        player.sendMessage(HubParkour.c(true, s.trim()));
    }



    private void returnToMainMenu() {
        checkpoint = null;
        after = -1;
        point = null;

        currentModification = WizardStep.MENU;
        StringBuilder sb = new StringBuilder();

        //Default value list
        List<String> defaultList = new ArrayList<>();
        defaultList.add("Please select what you would like to edit about parkour &a{parkour-name}&r. Type the digit into chat or type it with /parkour input [number].");
        defaultList.add("Please note that split times will be reset for this parkour upon modifications to any parkour points.");
        defaultList.add("&a1&r - Name");
        defaultList.add("&a2&r - GUI Item");
        defaultList.add("&a3&r - End Commands");
        defaultList.add("&a4&r - Global Checkpoint Commands");
        defaultList.add("&a5&r - Start Point");
        defaultList.add("&a6&r - End Point");
        defaultList.add("&a7&r - Restart Point");
        defaultList.add("&a8&r - Checkpoints");
        defaultList.add("&a9&r - Border Points");
        defaultList.add("&a10&r - Reward Cooldown");
        defaultList.add("&a11&r - Exit Point");
        defaultList.add("&a12&r - Exit Edit Mode");

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
        checkpoint = null;
        after = -1;
        currentModification = WizardStep.CHECKPOINTS;
        StringBuilder sb = new StringBuilder();

        //Default value list
        List<String> defaultList = new ArrayList<>();
        defaultList.add("What would you like to edit about checkpoints on parkour &a{parkour-name}&r? Type the digit into chat or type it with /parkour input [number].");
        defaultList.add("&a1&r - Delete checkpoints");
        defaultList.add("&a2&r - Add Checkpoints");
        defaultList.add("&a3&r - Edit Checkpoint Rewards");
        defaultList.add("&a4&r - Cancel");

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

    public enum WizardStep {
        MENU,
        NAME,

        END_COMMAND,
        END_COMMAND_DELETE,
        END_COMMAND_ADD,

        CHECKPOINT_COMMAND,
        CHECKPOINT_COMMAND_DELETE,
        CHECKPOINT_COMMAND_ADD,

        START_POINT,
        END_POINT,
        RESTART_POINT,
        CHECKPOINTS,
        BORDER_POINT_A,
        BORDER_POINT_B,
        REWARD_COOLDOWN,
        EXIT_POINT,
        GUI_ITEM,

        /*
        Checkpoint wizard.
         */
        CHECKPOINTS_DELETE,
        CHECKPOINTS_ADD,
        CHECKPOINTS_ADD_SELECTED,
        CHECKPOINTS_ADD_REWARDS,

        CHECKPOINTS_REWARDS,
        CHECKPOINTS_REWARDS_SELECTED,
        CHECKPOINTS_REWARDS_ADD,
        CHECKPOINTS_REWARDS_DELETE
    }
}
