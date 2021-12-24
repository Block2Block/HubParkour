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
     */
    public MySQLConnectionPool(String hostname, String port, String username,
                               String password) throws ClassNotFoundException {
        this(hostname, port, null, username, password);
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
     */
    public MySQLConnectionPool(String hostname, String port, String database,
                               String username, String password) throws ClassNotFoundException {
        String connectionURL = "jdbc:mysql://"
                + hostname + ":" + port;
        if (database != null) {
            connectionURL = connectionURL + "/" + database + "?verifyServerCertificate=false&useSSL=false&requireSSL=false";
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
