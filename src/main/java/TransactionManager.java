import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyVetoException;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class TransactionManager {

    private static Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);

    private ComboPooledDataSource dataSource;

    private AtomicLong currentId = new AtomicLong(0);

    private Random binaryGenerator = new Random();

    public TransactionManager init() {
        dataSource = new ComboPooledDataSource();
        try {
            dataSource.setDriverClass(Configurations.DB_DRIVER);
            dataSource.setJdbcUrl(String.format("jdbc:%s://127.0.0.1:5432/%s?currentSchema=%s",
                    Configurations.DB_DRIVER_NAME,
                    Configurations.DATABASE_NAME,
                    Configurations.DB_SCHEME_NAME));

            dataSource.setUser(Configurations.DB_USER_NAME);
            dataSource.setPassword(Configurations.DB_USER_PASS);

            dataSource.setMaxPoolSize(Configurations.MAX_DB_CONNECTION_POOL_SIZE);
            dataSource.setInitialPoolSize(Configurations.START_DB_CONNECTION_POOL_SIZE);

            prepareSchema();

        } catch (PropertyVetoException e) {
            throw new IllegalStateException("Cannot create database connection!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    private void prepareSchema() throws SQLException {
        try (Connection connection = getConnection()) {
            if (checkIfSchemaExists()) {
                LOGGER.info("Scheme was created earlier. Dropping and creating new");
                connection.prepareStatement(String.format("DROP SCHEMA %s CASCADE", Configurations.DB_SCHEME_NAME)).execute();
            } else {
                LOGGER.info("Creating new schema for test");
            }

            connection.prepareStatement(String.format("CREATE SCHEMA %s", Configurations.DB_SCHEME_NAME)).execute();
            recreateTable();
        }
    }

    private boolean checkIfSchemaExists() throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(
                "SELECT TRUE FROM information_schema.schemata WHERE schema_name  = ?");
        statement.setString(1, Configurations.DB_SCHEME_NAME);

        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }

    public void recreateTable() throws SQLException {
        PreparedStatement statement = getConnection()
                .prepareStatement("DROP TABLE IF EXISTS data");
        statement.execute();

        statement = getConnection()
                .prepareStatement("CREATE TABLE data(id SERIAL NOT NULL PRIMARY KEY, binary_data bytea NOT NULL)");
        statement.execute();
        closeConnections(statement);

        LOGGER.info("Table for saving data was recreated");
    }

    public long executeAtomicTransaction() throws SQLException {
        byte[] binaryData = new byte[Configurations.BINARY_DATA_SIZE];
        binaryGenerator.nextBytes(binaryData);

//        long operationStartTime = System.currentTimeMillis();

        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO data(id, binary_data) VALUES (?,?)");
        statement.setLong(1, getNextId());
        statement.setBytes(2, binaryData);
        statement.executeUpdate();
        closeConnections(statement);

//        return System.currentTimeMillis() - operationStartTime;
        return 0;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void closeConnections(Statement statement) throws SQLException {
        if (!statement.getConnection().isClosed())
            statement.getConnection().close();
    }

    private long getNextId() {
        return currentId.incrementAndGet();
    }

}
