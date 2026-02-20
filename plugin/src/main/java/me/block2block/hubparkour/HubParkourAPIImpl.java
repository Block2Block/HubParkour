package me.block2block.hubparkour;

import me.block2block.hubparkour.api.BackendAPI;
import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.api.gui.GUI;
import me.block2block.hubparkour.api.hologram.IHologram;
import me.block2block.hubparkour.managers.CacheManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class HubParkourAPIImpl extends BackendAPI {

    @SuppressWarnings("unused")
    public boolean isInParkour(Player player) throws NullPointerException {
        if (player == null) {
            throw new NullPointerException("player cannot be null");
        }

        return CacheManager.isParkour(player);
    }


    @SuppressWarnings("unused")
    public IHubParkourPlayer getPlayer(Player player) throws NullPointerException {
        if (player == null) {
            throw new NullPointerException("Player cannot be null");
        }
        return CacheManager.getPlayer(player);
    }

    @SuppressWarnings("unused")
    public IParkour getParkour(int id) {
        return CacheManager.getParkour(id);
    }

    @SuppressWarnings("unused")
    public IParkour getParkour(String name) throws NullPointerException {
        if (name == null) {
            throw new NullPointerException("name cannot be null");
        }

        return CacheManager.getParkour(name);
    }

    @Override
    public Material getPressurePlateType(int type) {
        return CacheManager.getTypes().get(type);
    }

    @Override
    public ItemStack getItem(int type) {
        return CacheManager.getItems().get(type);
    }

    @Override
    public GUI getGUI(Player player) {
        if (player == null) {
            throw new NullPointerException("player cannot be null");
        }
        return CacheManager.getGUI(player);
    }

    @Override
    public void closeGUI(Player player) {
        if (player == null) {
            throw new NullPointerException("player cannot be null");
        }
        CacheManager.closeGUI(player);
    }

    @Override
    public void openGUI(Player player, GUI gui) {
        if (player == null) {
            throw new NullPointerException("player cannot be null");
        }
        if (gui == null) {
            throw new NullPointerException("gui cannot be null");
        }
        CacheManager.openGUI(player, gui);
    }

    @Override
    public boolean isPre1_13() {
        return HubParkour.isPre1_13();
    }

    @Override
    public boolean isPost1_14() {
        return HubParkour.isPost1_14();
    }

    @Override
    public IHologram createHologram(IParkour parkour, String name, Location location) {
        if (!HubParkour.isHolograms()) {
            return null;
        }
        return HubParkour.getHologramFactory().createHologram(parkour, name, location);
    }
}
