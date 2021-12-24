package me.block2block.hubparkour.signs;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.signs.ClickableSign;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.entities.Statistics;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class StatsClickableSign extends ClickableSign {

    public StatsClickableSign(Parkour parkour, Sign signState) {
        super(parkour, signState);
    }

    public void refresh() {
        List<String> defaultList = new ArrayList<>();
        defaultList.add("&2&l[PARKOUR]");
        defaultList.add("&a{parkour-name}");
        defaultList.add("");
        defaultList.add("Click to view stats!");
        int counter = 0;
        for (String s : ConfigUtil.getStringList("Messages.Signs.Stats", defaultList)) {
            signState.setLine(counter, ChatColor.translateAlternateColorCodes('&', s.replace("{parkour-name}", parkour.getName())));
            counter++;
            if (counter == 4) {
                break;
            }
        }
        signState.update(true);
    }

    public int getType() {
        return 1;
    }

    public void onClick(Player player) {
        new BukkitRunnable(){
            @Override
            public void run() {
                Statistics statistics = HubParkour.getInstance().getDbManager().getParkourStatistics(player, (Parkour) parkour);

                if (statistics.getAttempts().size() == 0) {
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Stats.No-Parkour-Stats", "No stats have been tracked for you in this parkour yet. Attempt this parkour to earn stats!", true, Collections.emptyMap());
                    return;
                }

                StringBuilder sb = new StringBuilder();

                //default help list
                List<String> defaultList = new ArrayList<>();
                defaultList.add("Your stats for parkour &a{parkour-name}&r:");
                defaultList.add("&aParkour attempts:&r {attempts}");
                defaultList.add("&aParkour completions:&r {completions}");
                defaultList.add("&aTotal jumps:&r {jumps}");
                defaultList.add("&aTotal checkpoints hit:&r {checkpoints}");
                defaultList.add("&aTotal distance travelled:&r {distance} blocks");
                defaultList.add("&aTotal time in parkour:&r {time}");

                for (String s : ConfigUtil.getStringList("Messages.Commands.Stats.Parkour-Stats", defaultList)) {
                    sb.append(s).append("\n");
                }

                Map<String, String> bindings = new HashMap<>();
                bindings.put("parkour-name", parkour.getName());
                bindings.put("attempts", statistics.getAttempts().get(parkour.getId()) + "");
                bindings.put("completions", statistics.getCompletions().get(parkour.getId()) + "");
                bindings.put("jumps", statistics.getJumps().get(parkour.getId()) + "");
                bindings.put("distance", String.format("%.2f", statistics.getTotalDistanceTravelled().get(parkour.getId())));
                bindings.put("time", ConfigUtil.formatTime(statistics.getTotalTime().get(parkour.getId())));
                bindings.put("checkpoints", statistics.getCheckpointsHit().get(parkour.getId()) + "");

                ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Stats.Parkour-Stats", sb.toString(), true, bindings);
            }
        }.runTaskAsynchronously(HubParkour.getInstance());
    }
}
