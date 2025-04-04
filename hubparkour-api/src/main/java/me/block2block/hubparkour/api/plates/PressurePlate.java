package me.block2block.hubparkour.api.plates;

import me.block2block.hubparkour.api.HubParkourAPI;
import me.block2block.hubparkour.api.IParkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;

/**
 * Represents a generic pressure plate.
 * <p>
 * <strong>WARNING:</strong> never create PressurePlate objects. Always extend this class.
 */
public abstract class PressurePlate {

    protected final Location location;
    protected IParkour parkour;
    protected Material material;
    protected List<String> rewards;

    @SuppressWarnings("unused")
    public PressurePlate(Location location, List<String> rewards) {
        this.location =  location;
        this.material = HubParkourAPI.getPressurePlateType(this.getType());
        this.rewards = rewards;
    }

    /**
     * Set the parkour the pressure plate belongs to.
     * <p>
     * <strong>WARNING:</strong> It is not recommended that you use this once the parkour has been setup.
     * @param parkour the parkour it belongs to.
     */
    public void setParkour(IParkour parkour) {
        this.parkour = parkour;
    }

    /**
     * The location the pressure plate it located.
     * @return the location of the pressure plate.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * The type of pressure plate. This is just an arbitrary value.
     * <p>
     * Default ID's:
     * 0 - Start Point
     * 1 - End Point
     * 2 - Restart Point
     * 3 - Checkpoint
     * 4 - Border Point
     * 5 - Exit Point
     * @return the pressure plate type
     */
    public abstract int getType();

    /**
     * Get the type of material the pressure plate has.
     * @return Material type.
     */
    @SuppressWarnings("unused")
    public Material getMaterial() {
        return material;
    }

    /**
     * Places the pressure plate material on the block the pressure plate is located at.
     */
    public void placeMaterial() {
        if (material != Material.AIR) {
            if (location == null) {
                Bukkit.getLogger().warning("A location that one of your parkour points is in does not exist. Please delete the " + parkour.getName() + " parkour and set it up again.");
                return;
            }
            if (location.getWorld() == null) {
                Bukkit.getLogger().warning("A location that one of your parkour points is in does not exist. Please delete the " + parkour.getName() + " parkour and set it up again.");
                return;
            }
            location.getBlock().setType(material);
        }
    }

    /**
     * Remove the material by setting it to air.
     */
    public void removeMaterial() {
        if (location == null || location.getWorld() == null) return;
        location.getBlock().setType(Material.AIR);
    }

    /**
     * Get the parkour the pressure plate belongs to.
     * @return the parkour it belongs to.
     */
    public IParkour getParkour() {
        return parkour;
    }


    /**
     * Retrieves the commands associated with the pressure plate.
     * @return a list of commands, represented as strings, or null if this is not a checkpoint.
     */
    public List<String> getRewards() {
        return rewards;
    }

    /**
     * Sets the commands associated with the pressure plate. Does nothing if this is not a checkpoint.
     * @param rewards a list of commands, represented as strings, to be associated with this pressure plate.
     */
    public void setRewards(List<String> rewards) {
        if (getType() != 3) return;
        this.rewards = rewards;
    }
}
