package me.block2block.hubparkour.utils.database;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConnectionPool {

    private final BasicDataSource dataSource;

    /**
     * Creates a new MySQL instance
     *
     * @param hostname
     *            Name of the host
     * @param port
     *            Port number
     * @param username
     *            Username
     * @param password
     *            Password
     * @param jdbcOptions
     *            options for the JDBC connection string
     * @throws ClassNotFoundException
     *            if the required libraries are not present
     */
    public MySQLConnectionPool(String hostname, String port, String username,
                               String password, String jdbcOptions) throws ClassNotFoundException {
        this(hostname, port, null, username, password, jdbcOptions);
    }

    /**
     * Creates a new MySQL instance for a specific database
     *
     * @param hostname
     *            Name of the host
     * @param port
     *            Port number
     * @param database
     *            Database name
     * @param username
     *            Username
     * @param password
     *            Password
     * @param jdbcOptions
     *            options for the JDBC connection string
     * @throws ClassNotFoundException
     *            if the required libraries are not present
     */
    public MySQLConnectionPool(String hostname, String port, String database,
                               String username, String password, String jdbcOptions) throws ClassNotFoundException {
        String connectionURL = "jdbc:mysql://"
                + hostname + ":" + port;
        if (database != null) {
            connectionURL = connectionURL + "/" + database + "?" + jdbcOptions;
        }

        Class.forName("com.mysql.jdbc.Driver");
        dataSource = new BasicDataSource();
        dataSource.setUrl(connectionURL);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

}
