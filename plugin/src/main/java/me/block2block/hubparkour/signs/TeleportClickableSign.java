package me.block2block.hubparkour.signs;

import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.signs.ClickableSign;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeleportClickableSign extends ClickableSign {

    public TeleportClickableSign(Parkour parkour, Sign signState) {
        super(parkour, signState);
    }

    public void refresh() {
        List<String> defaultList = new ArrayList<>();
        defaultList.add("&2&l[PARKOUR]");
        defaultList.add("&a{parkour-name}");
        defaultList.add("{amount-of-players} players");
        defaultList.add("Click to teleport!");
        int counter = 0;
        for (String s : ConfigUtil.getStringList("Messages.Signs.Teleport", defaultList)) {
            signState.setLine(counter, ChatColor.translateAlternateColorCodes('&', s.replace("{parkour-name}", parkour.getName()).replace("{amount-of-players}", parkour.getPlayers().size() + "")));
            counter++;
            if (counter == 4) {
                break;
            }
        }
        signState.update(true);
    }

    public int getType() {
        return 0;
    }

    public void onClick(Player player) {
        player.teleport(parkour.getRestartPoint().getLocation());
    }
}
