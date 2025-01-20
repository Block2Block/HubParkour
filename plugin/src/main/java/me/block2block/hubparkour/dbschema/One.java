package me.block2block.hubparkour.dbschema;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.db.DatabaseSchemaUpdate;
import me.block2block.hubparkour.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

public class One extends DatabaseSchemaUpdate {

    public One() {
        super(1);
    }

    @Override
    public void execute() {
        try (Connection connection = HubParkour.getInstance().getDbManager().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("ALTER TABLE `hp_parkours` ADD `server` VARCHAR(36) NULL DEFAULT NULL" + ((DatabaseManager.isMysql())?" AFTER `checkpoint_reward`":"") + ";");
            statement.execute();
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }

    }
}
