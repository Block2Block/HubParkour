package me.block2block.hubparkour.commands;


import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class ParkourTabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (command.getName().equals("parkour")) {
            if (args.length == 1) {
                List<String> subcommands = Arrays.asList("checkpoint", "reset", "leaderboard", "leave", "setup", "delete", "list", "hologram", "reload", "done", "cancel", "input", "teleport", "edit", "removetime", "cleartimes");
                return getSubcommands(args, subcommands);
            } else {
                if (args[0].toLowerCase().equals("hologram")) {
                    List<String> subcommands = Arrays.asList("list", "delete", "create");
                    return getSubcommands(args, subcommands);
                }
            }
        }
        return null;
    }

    private List<String> getSubcommands(String[] args, List<String> subcommands) {
        List<String> list = new ArrayList<>();

        for (String subcommand : subcommands) {
            if (subcommand.startsWith(args[0].toLowerCase())) {
                list.add(subcommand);
            }
        }

        Collections.sort(list);
        return list;
    }
}
