package me.block2block.hubparkour.api.items;

import me.block2block.hubparkour.api.IHubParkourPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ShowItem extends ParkourItem {

    List<Player> hiddenPlayers;

    public ShowItem(IHubParkourPlayer player, int slot) {
        super(player, slot);
        hiddenPlayers = new ArrayList<>();
    }

    public ShowItem(HideItem item) {
        super(item.getPlayer(), item.getSlot());
        this.originalItem = item.getOriginalItem();
        hiddenPlayers = new ArrayList<>();
    }

    @Override
    public int getType() {
        return 4;
    }

    public List<Player> getHiddenPlayers() {
        return hiddenPlayers;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeItem() {
        if (slot >= 0 && slot < 9 && originalItem != null) {
            player.getPlayer().getInventory().setItem(slot, originalItem);
            originalItem = null;
        } else if (slot >= 0 && slot < 9) {
            player.getPlayer().getInventory().setItem(slot, new ItemStack(Material.AIR));
        }
        for (Player player1 : hiddenPlayers) {
            if (player1.isOnline()) {
                player.getPlayer().showPlayer(player1);
            }
        }
    }
}
