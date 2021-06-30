package me.block2block.hubparkour.listeners;

import me.block2block.hubparkour.Main;
import me.block2block.hubparkour.api.events.admin.ParkourSetupEvent;
import me.block2block.hubparkour.api.plates.*;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("ALL")
public class SetupListener implements Listener {

    private List<PressurePlate> data = new ArrayList<>();
    private List<String> commandData = new ArrayList<>();
    private int after = -1;

    @SuppressWarnings("unused")
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (CacheManager.isSetup(e.getPlayer())) {
            e.setCancelled(CacheManager.getSetupWizard().onChat(e.getMessage()));
        } else if (CacheManager.isEdit(e.getPlayer())) {
            e.setCancelled(CacheManager.getEditWizard().onChat(e.getMessage()));
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (CacheManager.isSetup(e.getPlayer())) {
            if (e.hasItem()) {
                if (e.getItem().getType() == Material.STICK && ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName()).equals("HubParkour Setup Stick")) {
                    Location location = e.getPlayer().getLocation().getBlock().getLocation();
                    location.setPitch(e.getPlayer().getLocation().getPitch());
                    location.setYaw(e.getPlayer().getLocation().getYaw());
                    CacheManager.getSetupWizard().stickInteract(location);
                }
            }
        } else if (CacheManager.isEdit(e.getPlayer())) {
            if (e.hasItem()) {
                if (e.getItem().getType() == Material.STICK && ChatColor.stripColor(e.getItem().getItemMeta().getDisplayName()).equals("HubParkour Setup Stick")) {
                    Location location = e.getPlayer().getLocation().getBlock().getLocation();
                    location.setPitch(e.getPlayer().getLocation().getPitch());
                    location.setYaw(e.getPlayer().getLocation().getYaw());
                    CacheManager.getEditWizard().onStick(location);
                }
            }
        }
    }

}
