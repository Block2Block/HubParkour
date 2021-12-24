package me.block2block.hubparkour.api.items;

import me.block2block.hubparkour.api.IHubParkourPlayer;

public class HideItem extends ParkourItem {

    public HideItem(IHubParkourPlayer player, int slot) {
        super(player, slot);
    }

    public HideItem(ShowItem item) {
        super(item.getPlayer(), item.getSlot());
        this.originalItem = item.getOriginalItem();
    }

    @Override
    public int getType() {
        return 3;
    }
}
