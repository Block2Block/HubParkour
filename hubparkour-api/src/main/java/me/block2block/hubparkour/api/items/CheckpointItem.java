package me.block2block.hubparkour.api.items;

import me.block2block.hubparkour.api.IHubParkourPlayer;

public class CheckpointItem extends ParkourItem {

    public CheckpointItem(IHubParkourPlayer player, int slot) {
        super(player, slot);
    }

    @Override
    public int getType() {
        return 1;
    }
}
