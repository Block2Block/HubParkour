package me.block2block.hubparkour.api.items;

import me.block2block.hubparkour.api.IHubParkourPlayer;

public class CancelItem extends ParkourItem {

    public CancelItem(IHubParkourPlayer player, int slot) {
        super(player, slot);
    }

    @Override
    public int getType() {
        return 2;
    }
}
