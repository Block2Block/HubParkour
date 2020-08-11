package me.block2block.hubparkour.api.items;

import me.block2block.hubparkour.api.IHubParkourPlayer;

public class ResetItem extends ParkourItem {

    public ResetItem(IHubParkourPlayer player, int slot) {
        super(player, slot);
    }

    @Override
    public int getType() {
        return 0;
    }
}
