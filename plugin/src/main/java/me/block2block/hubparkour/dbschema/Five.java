package me.block2block.hubparkour.dbschema;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.db.DatabaseSchemaUpdate;
import me.block2block.hubparkour.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

public class Five extends DatabaseSchemaUpdate {

    public Five() {
        super(5);
    }

    @Override
    public void execute() {
        String prefix = DatabaseManager.getTablePrefix();
        try (Connection connection = HubParkour.getInstance().getDbManager().getConnection()) {
            if (DatabaseManager.isMysql()) {
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE `" + prefix + "parkours` DROP COLUMN `item_material`, DROP COLUMN `item_data`;");
                statement.execute();
                statement = connection.prepareStatement("ALTER TABLE `" + prefix + "parkours` ADD `item_material` TEXT NOT NULL AFTER `rewards`, ADD `item_data` SMALLINT NOT NULL DEFAULT 0 AFTER `item_material`, ADD `item_model_data` SMALLINT NOT NULL DEFAULT -1 AFTER `item_data`;");
                statement.execute();
            } else {
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE `" + prefix + "parkours` ADD `item_model_data` INTEGER NOT NULL DEFAULT -1;");
                statement.execute();
            }
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }

    }

}
