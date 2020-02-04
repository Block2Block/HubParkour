package me.block2block.hubparkour.utils;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.managers.CacheManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class HubParkourExpansion extends PlaceholderExpansion {

    private Main plugin;

    public HubParkourExpansion(Main plugin) {
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

        if(player == null){
            return "";
        }

        if(identifier.equals("currentparkour")){
            if (CacheManager.getPlayer(player) == null) {
                return "Not in parkour";
            }
            return CacheManager.getPlayer(player).getParkour().getName() + "";
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
            return ((CacheManager.getPlayer(player).getPrevious() == -1)?"Not yet finished":((CacheManager.getPlayer(player).getPrevious() == -2)?"Loading...":CacheManager.getPlayer(player).getPrevious())) + "";
        }



        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }



}
