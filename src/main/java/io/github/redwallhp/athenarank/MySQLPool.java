package io.github.redwallhp.athenarank;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;


public class MySQLPool {


    private final HikariDataSource dataSource;


    public MySQLPool(FileConfiguration config) {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(config.getString("database.url"));
        dataSource.setUsername(config.getString("database.username"));
        dataSource.setPassword(config.getString("database.password"));
        dataSource.addDataSourceProperty("cachePrepStmts", "true");
    }


    public void close() {
        dataSource.close();
    }


    public Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            AthenaRank.instance.getLogger().warning("MySQL connection error: " + ex.getMessage());
            throw ex;
        }
    }


}
