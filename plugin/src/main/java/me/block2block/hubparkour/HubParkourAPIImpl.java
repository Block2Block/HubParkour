package me.block2block.hubparkour;

import me.block2block.hubparkour.api.BackendAPI;
import me.block2block.hubparkour.api.IHubParkourPlayer;
import me.block2block.hubparkour.api.IParkour;
import me.block2block.hubparkour.managers.CacheManager;
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

}
