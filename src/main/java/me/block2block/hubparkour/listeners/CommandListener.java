package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CommandListener implements Listener {

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (CacheManager.isParkour(e.getPlayer())) {
            ArrayList<String> args = new ArrayList<>(Arrays.asList(e.getMessage().split(" ")));
            String commandLabel = args.remove(0).substring(1);
            if (!commandLabel.equalsIgnoreCase("parkour") && !commandLabel.equalsIgnoreCase("pk")) {
                if (ConfigUtil.getBoolean("Settings.Exploit-Prevention.Block-Commands", true)) {
                    e.setCancelled(true);
                    ConfigUtil.sendMessage(e.getPlayer(), "Messages.Parkour.Cannot-Execute-Commands", "You cannot execute commands from other plugins while doing a parkour. To leave your current parkour, do /parkour leave.", true, Collections.emptyMap());
                }
            }

        }
    }

}
