public interface Configurations {
//----------TransactionExecutor configuration
    int INITIAL_INTENSITY_PEAR_SECOND = 100;
    int FINAL_INTENSITY_PEAR_SECOND = 200;

    int THREAD_CONSUMER_COUNT = 10;

    int SECOND_FOR_PHASE = 10;

    int BLOCKING_QUEUE_DELIMITER = 20;

//------------TransactionManager connection configuration--------------------
    int MAX_DB_CONNECTION_POOL_SIZE = 10;
    int START_DB_CONNECTION_POOL_SIZE = 5;

    String DATABASE_NAME = "postgres";
    String DB_SCHEME_NAME = "database";

    String DB_DRIVER = "org.postgresql.Driver";
    String DB_DRIVER_NAME = "postgresql";

    String DB_USER_NAME = "postgres";
    String DB_USER_PASS = "postgres";

    int BINARY_DATA_SIZE = 50;


}
