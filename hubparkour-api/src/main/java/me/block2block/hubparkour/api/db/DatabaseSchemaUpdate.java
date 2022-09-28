package me.block2block.hubparkour.api.db;

/**
 * A database schema. The only function of schema classes are to execute SQL commands to update from one schema to another. Does not contain the whole schema.
 */
public abstract class DatabaseSchemaUpdate {

    private final int id;

    /**
     * Init the schema with the specified ID. Should go higher for newer versions.
     * @param id the schema id
     */
    public DatabaseSchemaUpdate(int id) {
        this.id = id;
    }

    /**
     * Update the database to this schema.
     */
    public abstract void execute();

    /**
     * Get the id of the schema.
     * @return the id of the schema.
     */
    public int getId() {
        return id;
    }

}
