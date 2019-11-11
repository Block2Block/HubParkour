package me.Block2Block.HubParkour.Commands;

import me.Block2Block.HubParkour.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class CommandParkour implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String string, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length > 0) {
                switch (args[0]) {
                    case "reset":

                        break;
                    case "checkpoint":

                        break;
                    case "top3":

                        break;
                    case "leave":

                        break;
                    default:
                        p.sendMessage(Main.c(true, Main.getInstance().getConfig().getString("Messages.Commands.Help")));
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
