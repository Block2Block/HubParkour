package me.block2block.hubparkour.api.signs;

import me.block2block.hubparkour.api.IParkour;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * A generic sign object.
 */
public abstract class ClickableSign {


    protected final IParkour parkour;
    protected final Sign signState;
    protected int id = -1;

    public ClickableSign(IParkour parkour, Sign signState) {
        this.parkour = parkour;
        this.signState = signState;
    }
    public ClickableSign(int id, IParkour parkour, Sign signState) {
        this.id = id;
        this.parkour = parkour;
        this.signState = signState;
    }

    /**
     * Called when a sign is needing to be refreshed.
     */
    public abstract void refresh();

    /**
     * Get the type of the sign.
     * @return the type of the sign.
     */
    public abstract int getType();

    /**
     * What happens when a sign is clicked.
     * @param player the player that clicked the sign.
     */
    public abstract void onClick(Player player);

    /**
     * Get the parkour this sign is associated with.
     * @return the parkour this sign belongs to.
     */
    public IParkour getParkour() {
        return parkour;
    }

    /**
     * Get the Bukkit Sign State of the sign.
     * @return a Bukkit Sign State which represents the actual sign.
     */
    public Sign getSignState() {
        return signState;
    }

    /**
     * Get the ID of this sign.
     * @return the ID of the sign.
     */
    public int getId() {
        return id;
    }

    /**
     * In the event that this is a new sign, set the ID.
     * @param id the id of this sign.
     * @throws IllegalStateException if the ID is already set.
     */
    public void setId(int id) {
        if (this.id != -1) {
            throw new IllegalStateException("This sign already has an ID.");
        }
        this.id = id;
    }
}
