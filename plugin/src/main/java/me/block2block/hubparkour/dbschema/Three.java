package me.block2block.hubparkour.dbschema;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.db.DatabaseSchemaUpdate;
import me.block2block.hubparkour.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

public class Three extends DatabaseSchemaUpdate {

    public Three() {
        super(3);
    }

    @Override
    public void execute() {
        try (Connection connection = HubParkour.getInstance().getDbManager().getConnection()) {
            if (DatabaseManager.isMysql()) {
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE `hp_signs` ADD `facing` TEXT NOT NULL AFTER `type`, ADD `wall` BOOLEAN NOT NULL AFTER `facing`;");
                statement.execute();
            } else {
                PreparedStatement statement = connection.prepareStatement("ALTER TABLE `hp_signs` ADD `facing` TEXT NOT NULL;");
                statement.execute();
                statement = connection.prepareStatement("ALTER TABLE `hp_signs` ADD `wall` BOOLEAN NOT NULL;");
                statement.execute();
            }
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:");
            e.printStackTrace();
        }

    }

}
