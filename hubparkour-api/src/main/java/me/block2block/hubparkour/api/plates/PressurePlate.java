package me.block2block.hubparkour.api.plates;

import me.block2block.hubparkour.api.HubParkourAPI;
import me.block2block.hubparkour.api.IParkour;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Represents a generic pressure plate.
 *
 * <strong>WARNING:</strong> never create PressurePlate objects. Always extend this class.
 */
public abstract class PressurePlate {

    protected final Location location;
    protected IParkour parkour;
    protected Material material;

    @SuppressWarnings("unused")
    public PressurePlate(Location location) {
        this.location =  location;
        this.material = HubParkourAPI.getPressurePlateType(this.getType());
    }

    /**
     * Set the parkour the pressure plate belongs to.
     *
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
     *
     * Default ID's:
     * 0 - Start Point
     * 1 - End Point
     * 2 - Restart Point
     * 3 - Checkpoint
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
        location.getBlock().setType(Material.AIR);
    }

    /**
     * Get the parkour the pressure plate belongs to.
     * @return the parkour it belongs to.
     */
    public IParkour getParkour() {
        return parkour;
    }
}
