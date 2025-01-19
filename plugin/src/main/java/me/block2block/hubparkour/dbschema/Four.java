package me.block2block.hubparkour.dbschema;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.db.DatabaseSchemaUpdate;
import me.block2block.hubparkour.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

public class Four extends DatabaseSchemaUpdate {

    public Four() {
        super(4);
    }

    @Override
    public void execute() {
        String prefix = DatabaseManager.getTablePrefix();
        try (Connection connection = HubParkour.getInstance().getDbManager().getConnection()) {
            if (DatabaseManager.isMysql()) {
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE `" + prefix + "locations` ADD `rewards` TEXT AFTER `world`;");
                statement.execute();
                statement = connection.prepareStatement("ALTER TABLE `" + prefix + "parkours` ADD `item_material` TEXT NOT NULL DEFAULT 'SLIME_BALL' AFTER `server`, ADD `item_data` SMALLINT NOT NULL DEFAULT '0' AFTER `server`;");
                statement.execute();
            } else {
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE `" + prefix + "locations` ADD `rewards` TEXT;");
                statement.execute();
                statement = connection.prepareStatement("ALTER TABLE `" + prefix + "parkours` ADD `item_material` TEXT NOT NULL DEFAULT 'SLIME_BALL';");
                statement.execute();
                statement = connection.prepareStatement("ALTER TABLE `" + prefix + "parkours` ADD `item_data` INTEGER NOT NULL DEFAULT '0';");
                statement.execute();
            }
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }

    }

}
