package me.block2block.hubparkour.utils;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.plates.Checkpoint;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("ALL")
public class HubParkourExpansion extends PlaceholderExpansion {

    private final HubParkour plugin;

    @SuppressWarnings("unused")
    public HubParkourExpansion(HubParkour plugin) {
        this.plugin = plugin;
    }


    @Override
    public String getIdentifier() {
        return "hubparkour";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist(){
        return true;
    }

    public boolean canRegister(){
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){

        /*
        Not player-dependant variables. These can be returned when player is null.
         */

        if (identifier.matches("^checkpointcount_[0-9]{1,10}$")) {
            String[] args = identifier.split("_");
            Parkour parkour = CacheManager.getParkour(Integer.parseInt(args[1]));
            if (parkour == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Valid-Parkour", "Not a valid parkour");
            }
            return parkour.getNoCheckpoints() + "";
        }

        if (identifier.matches("^parkourname_[0-9]{1,10}$")) {
            String[] args = identifier.split("_");
            Parkour parkour = CacheManager.getParkour(Integer.parseInt(args[1]));
            if (parkour == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Valid-Parkour", "Not a valid parkour");
            }
            return parkour.getName() + "";
        }

        if (identifier.matches("^activeplayers_[0-9]{1,10}$")) {
            String[] args = identifier.split("_");
            Parkour parkour = CacheManager.getParkour(Integer.parseInt(args[1]));
            if (parkour == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Valid-Parkour", "Not a valid parkour");
            }
            return parkour.getPlayers().size() + "";
        }

        if (identifier.matches("^recordtime_[0-9]{1,10}$")) {
            String[] args = identifier.split("_");
            Parkour parkour = CacheManager.getParkour(Integer.parseInt(args[1]));
            if (parkour == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Valid-Parkour", "Not a valid parkour");
            }
            long ms = HubParkour.getInstance().getDbManager().getRecordTime(parkour);
            if (ms == -1) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Yet-Completed", "Not yet completed");
            }
            return ConfigUtil.formatTime(ms);
        }

        if (identifier.matches("^positiontime_[0-9]{1,10}_[0-9]{1,10}$")) {
            String[] args = identifier.split("_");
            Parkour parkour = CacheManager.getParkour(Integer.parseInt(args[1]));
            int position = Integer.parseInt(args[2]);
            if (parkour == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Valid-Parkour", "Not a valid parkour");
            }
            long ms = HubParkour.getInstance().getDbManager().getPositionTime(parkour, position);
            if (ms == -1) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Position-Not-Filled", "Position not filled");
            }
            return ConfigUtil.formatTime(ms);
        }

        if (identifier.matches("^positionname_[0-9]{1,10}_[0-9]{1,10}$")) {
            String[] args = identifier.split("_");
            Parkour parkour = CacheManager.getParkour(Integer.parseInt(args[1]));
            int position = Integer.parseInt(args[2]);
            if (parkour == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Valid-Parkour", "Not a valid parkour");
            }
            String name = HubParkour.getInstance().getDbManager().getPositionHolder(parkour, position);
            if (name == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Position-Not-Filled", "Position not filled");
            }
            return name;
        }

        if (identifier.matches("^recordholder_[0-9]{1,10}$")) {
            String[] args = identifier.split("_");
            Parkour parkour = CacheManager.getParkour(Integer.parseInt(args[1]));
            if (parkour == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Valid-Parkour", "Not a valid parkour");
            }
            String holder = HubParkour.getInstance().getDbManager().getRecordHolder(parkour);
            if (holder == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Yet-Completed", "Not yet completed");
            }
            return holder;
        }

        /*
        Player-dependant variables.
         */

        if(player == null){
            return "";
        }

        if(identifier.equals("currentparkour")){
            if (CacheManager.getPlayer(player) == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-In-Parkour", "Not in parkour");
            }
            return CacheManager.getPlayer(player).getParkour().getName() + "";
        }

        if(identifier.equals("currentparkourid")){
            if (CacheManager.getPlayer(player) == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-In-Parkour", "Not in parkour");
            }
            return CacheManager.getPlayer(player).getParkour().getId() + "";
        }

        if(identifier.equals("lastreachedcheckpoint")){
            if (CacheManager.getPlayer(player) == null) {
                return "N/A";
            }
            return CacheManager.getPlayer(player).getLastReached() + "";
        }

        if(identifier.equals("previoustime")){
            if (CacheManager.getPlayer(player) == null) {
                return "N/A";
            }
            return ((CacheManager.getPlayer(player).getPrevious() == -1)?ConfigUtil.getString("Messages.PlaceholderAPI.Not-Yet-Finished", "Not yet finished"):((CacheManager.getPlayer(player).getPrevious() == -2)?"Loading...":ConfigUtil.FormatTime(CacheManager.getPlayer(player).getPrevious()))) + "";
        }

        if (identifier.equals("currenttime")){
            if (CacheManager.getPlayer(player) == null) {
                return "N/A";
            }
            return ConfigUtil.formatTime(System.currentTimeMillis() - CacheManager.getPlayer(player).getStartTime());
        }

        if (identifier.equals("currentsplittime")) {
            if (CacheManager.getPlayer(player) == null) {
                return "N/A";
            }
            return ConfigUtil.formatTime(System.currentTimeMillis() - CacheManager.getPlayer(player).getCurrentSplit());
        }

        if (identifier.matches("^besttime_[0-9]{1,10}$")) {
            String[] args = identifier.split("_");
            Parkour parkour = CacheManager.getParkour(Integer.parseInt(args[1]));
            if (parkour == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Valid-Parkour", "Not a valid parkour");
            }
            long ms = HubParkour.getInstance().getDbManager().getTime(player, parkour);
            if (ms == -1) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Yet-Completed", "Not yet completed");
            }
            return ConfigUtil.formatTime(ms);
        }

        if (identifier.matches("^highestreachedcheckpoint_[0-9]{1,10}$")) {
            String[] args = identifier.split("_");
            Parkour parkour = CacheManager.getParkour(Integer.parseInt(args[1]));
            if (parkour == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Valid-Parkour", "Not a valid parkour");
            }
            List<Checkpoint> checkpointList = HubParkour.getInstance().getDbManager().getReachedCheckpoints(player, parkour);
            Checkpoint highest = null;
            for (Checkpoint checkpoint : checkpointList) {
                if (highest == null) {
                    highest = checkpoint;
                } else if (highest.getCheckpointNo() < checkpoint.getCheckpointNo()) {
                    highest = checkpoint;
                }

            }
            if (highest == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-Reached-Checkpoint", "Not reached a checkpoint");
            } else {
                return highest.getCheckpointNo() + "";
            }
        }

        /*
            Current-Parkour Helper
         */
        if (identifier.equals("current_besttime")) {
            if (CacheManager.getPlayer(player) == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-In-Parkour", "Not in parkour");
            }
            Parkour parkour = CacheManager.getPlayer(player).getParkour();

            return PlaceholderAPI.setPlaceholders(player, "%hubparkour_besttime_" + parkour.getId() + "%");
        }

        if (identifier.matches("current_highestreachedcheckpoint")) {
            if (CacheManager.getPlayer(player) == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-In-Parkour", "Not in parkour");
            }
            Parkour parkour = CacheManager.getPlayer(player).getParkour();

            return PlaceholderAPI.setPlaceholders(player, "%hubparkour_highestreachedcheckpoint_" + parkour.getId() + "%");
        }

        if (identifier.matches("current_checkpointcount")) {
            if (CacheManager.getPlayer(player) == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-In-Parkour", "Not in parkour");
            }
            Parkour parkour = CacheManager.getPlayer(player).getParkour();

            return PlaceholderAPI.setPlaceholders(player, "%hubparkour_checkpointcount_" + parkour.getId() + "%");
        }

        if (identifier.matches("current_activeplayers")) {
            if (CacheManager.getPlayer(player) == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-In-Parkour", "Not in parkour");
            }
            Parkour parkour = CacheManager.getPlayer(player).getParkour();

            return PlaceholderAPI.setPlaceholders(player, "%hubparkour_activeplayers_" + parkour.getId() + "%");
        }

        if (identifier.matches(("^current_positiontime_[0-9]{1,10}"))) {
            if (CacheManager.getPlayer(player) == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-In-Parkour", "Not in parkour");
            }
            Parkour parkour = CacheManager.getPlayer(player).getParkour();

            String[] args = identifier.split("_");
            int position = Integer.parseInt(args[2]);

            return PlaceholderAPI.setPlaceholders(player, "%hubparkour_positiontime_" + parkour.getId() + "_" + position + "%");
        }

        if (identifier.matches(("^current_positionname_[0-9]{1,10}"))) {
            if (CacheManager.getPlayer(player) == null) {
                return ConfigUtil.getString("Messages.PlaceholderAPI.Not-In-Parkour", "Not in parkour");
            }
            Parkour parkour = CacheManager.getPlayer(player).getParkour();

            String[] args = identifier.split("_");
            int position = Integer.parseInt(args[2]);

            return PlaceholderAPI.setPlaceholders(player, "%hubparkour_positionname_" + parkour.getId() + "_" + position + "%");
        }


        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }



}
