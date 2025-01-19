package me.block2block.hubparkour.gui;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.gui.GUI;
import me.block2block.hubparkour.api.gui.GUIItem;
import me.block2block.hubparkour.entities.Parkour;
import me.block2block.hubparkour.managers.CacheManager;
import me.block2block.hubparkour.utils.ConfigUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ParkourListGUI extends GUI {

    private int currentPage;
    private List<Parkour> parkours;

    public ParkourListGUI(Player player) {
        super(player, ConfigUtil.getString("Settings.GUI.Title", "&a&lParkour List"), 5, true);
        border("&r", Collections.emptyList());
        currentPage = 1;
        refresh();
    }

    @Override
    public void onClick(int row, int column, ItemStack item, ClickType clickType) {
        if (item.getType() != Material.AIR) {
            //We can ignore this if it's not an arrow.
            if (row == 5 || row == 0 || column == 0 || column == 8) {
                //Arrows should only ever be in columns 1 and 7 when in in row 5.
                if (item.getType() == Material.ARROW && row == 5) {
                    if (column == 1) {
                        currentPage--;
                        refresh();
                    } else {
                        currentPage++;
                        refresh();
                    }
                } else {
                    if (HubParkour.isPre1_13()) {
                        this.player.playSound(player.getLocation(), Sound.valueOf("ITEM_BREAK"), 100, 0);
                    } else {
                        this.player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 100, 0);
                    }
                }
            } else {
                Parkour parkour = parkours.get(((currentPage - 1) * 28) + ((row - 1) * 7) + (column - 1));
                if (!CacheManager.isParkour(player)) {
                    Location l = parkour.getRestartPoint().getLocation().clone();
                    l.setX(l.getX() + 0.5);
                    l.setY(l.getY() + 0.5);
                    l.setZ(l.getZ() + 0.5);
                    player.teleport(l);
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Teleport.Teleported", "You have been teleported to the parkour restart point.", true, Collections.emptyMap());
                    player.closeInventory();
                } else {
                    if (HubParkour.isPre1_13()) {
                        this.player.playSound(player.getLocation(), Sound.valueOf("ITEM_BREAK"), 100, 0);
                    } else {
                        this.player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 100, 0);
                    }
                    ConfigUtil.sendMessageOrDefault(player, "Messages.Commands.Teleport.Currently-In-Parkour", "You cannot teleport to a parkour start point while in a parkour. Please leave your parkour and try again.", true, Collections.emptyMap());
                }
            }
        }
    }

    public void refresh() {
        //Make a copy in-case this changes between refreshes - will cause a NPE if that happens.
        parkours = new ArrayList<>(CacheManager.getParkours());

        GUIItem borderItem;

        if (HubParkour.isPre1_13()) {
            borderItem = new GUIItem(Material.getMaterial("STAINED_GLASS_PANE"), "&r", 1, Collections.emptyList(), (short) 7);
        } else {
            borderItem = new GUIItem(Material.GRAY_STAINED_GLASS_PANE, "&r", 1, Collections.emptyList());
        }

        if (currentPage == 1) {
            if (this.isOpen()) {
                this.updateItem(5, 1, borderItem);
            } else {
                this.setItem(5, 1, borderItem);
            }

            if (parkours.size() > 28) {
                if (this.isOpen()) {
                    this.updateItem(5, 7, new GUIItem(Material.ARROW, ConfigUtil.getString("Settings.GUI.Next-Page-Title", "&a&lNext Page")));
                } else {
                    this.setItem(5, 7, new GUIItem(Material.ARROW, ConfigUtil.getString("Settings.GUI.Next-Page-Title", "&a&lNext Page")));
                }
            } else {
                if (this.isOpen()) {
                    this.updateItem(5, 7, borderItem);
                } else {
                    this.setItem(5, 7, borderItem);
                }
            }
        } else {
            if (this.isOpen()) {
                this.updateItem(5, 1, new GUIItem(Material.ARROW, ConfigUtil.getString("Settings.GUI.Previous-Page-Title", "&a&lPrevious Page")));
            } else {
                this.setItem(5, 1, new GUIItem(Material.ARROW, ConfigUtil.getString("Settings.GUI.Previous-Page-Title", "&a&lPrevious Page")));
            }

            if (parkours.size() > 28 * currentPage) {
                if (this.isOpen()) {
                    this.updateItem(5, 7, new GUIItem(Material.ARROW, ConfigUtil.getString("Settings.GUI.Next-Page-Title", "&a&lNext Page")));
                } else {
                    this.setItem(5, 7, new GUIItem(Material.ARROW, ConfigUtil.getString("Settings.GUI.Next-Page-Title", "&a&lNext Page")));
                }
            } else {
                if (this.isOpen()) {
                    this.updateItem(5, 7, borderItem);
                } else {
                    this.setItem(5, 7, borderItem);
                }
            }
        }

        int column = 1;
        int row = 1;

        for (int i = 0;i < 28;i++) {
            int pi = (((currentPage - 1) * 28) + i);
            if (parkours.size() <= pi) {
                if (this.isOpen()) {
                    this.updateItem(row, column, null);
                } else {
                    this.setItem(row, column, null);
                }
                column++;
                if (column == 8) {
                    row++;
                    column = 1;
                    if (row == 5) {
                        break;
                    }
                }
                continue;
            }

            List<String> defaultValue = new ArrayList<>();
            defaultValue.add("&r");
            defaultValue.add("&r&fPlayers: &a{players}");
            defaultValue.add("&r&fCheckpoints: &a{checkpoints}");
            defaultValue.add("&r");
            defaultValue.add("Click to teleport!");

            List<String> lore = ConfigUtil.getStringList("Settings.GUI.Item-Lore", defaultValue);
            Parkour parkour = parkours.get(pi);

            GUIItem item = new GUIItem(
                    parkour.getItemMaterial(),
                    "&r" + parkour.getName(),
                    1,
                    lore.stream().map(line -> line.replace("{checkpoints}", parkour.getNoCheckpoints() + "").replace("{players}", parkour.getPlayers().size() + "")).collect(Collectors.toList())
            );

            if (this.isOpen()) {
                this.updateItem(row, column, item);
            } else {
                this.setItem(row, column, item);
            }

            column++;
            if (column == 8) {
                row++;
                column = 1;
                if (row == 5) {
                    break;
                }
            }
        }
    }
}
