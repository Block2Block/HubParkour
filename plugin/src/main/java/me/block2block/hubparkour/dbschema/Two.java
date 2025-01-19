package me.block2block.hubparkour.dbschema;

import me.block2block.hubparkour.HubParkour;
import me.block2block.hubparkour.api.db.DatabaseSchemaUpdate;
import me.block2block.hubparkour.managers.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

public class Two extends DatabaseSchemaUpdate {

    public Two() {
        super(2);
    }

    @Override
    public void execute() {
        if (!DatabaseManager.isMysql()) {
            return;
        }
        try (Connection connection = HubParkour.getInstance().getDbManager().getConnection()) {
            PreparedStatement statement = connection.prepareStatement("ALTER TABLE `hp_parkours` DROP `server`;");
            statement.execute();

            statement = connection.prepareStatement("ALTER TABLE `hp_parkours` ADD `server` VARCHAR(36) NULL DEFAULT NULL" + ((DatabaseManager.isMysql())?" AFTER `reward_cooldown`":"") + ";");
            statement.execute();
        } catch (Exception e) {
            HubParkour.getInstance().getLogger().log(Level.SEVERE, "There has been an error accessing the database. Try checking your database is online. Stack trace:", e);
        }

    }

}
