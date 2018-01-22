import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.stream.IntStream.rangeClosed;

public class TransactionExecutor {

    private static Logger LOGGER = LoggerFactory.getLogger(TransactionManager.class);

    private TransactionManager transactionManager = new TransactionManager().init();

    private LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>(100);

    public void makeTest() {
        prepareConsumerThreads();

//        blockingQueueObserver.scheduleAtFixedRate(blockingQueueObser, 1, 1, SECONDS);

        int from = Configurations.INITIAL_INTENSITY_PEAR_SECOND;
        int to = Configurations.FINAL_INTENSITY_PEAR_SECOND;

        rangeClosed(from, to)
                .forEach(Unchecked.intConsumer(this::processEachIntensity));
    }

    private void processEachIntensity(int intensity) throws SQLException {
        transactionManager.recreateTable();
        blockingQueue.clear();

        LOGGER.info("Use intensity i - {}", intensity);
        LOGGER.info("Wait for {} seconds", Configurations.SECOND_FOR_PHASE);

        int messageCount = intensity * Configurations.SECOND_FOR_PHASE;
        long delay = (long) (1000 * (double)1/intensity);

        rangeClosed(0, messageCount)
                .peek(messageIndex -> blockingQueue.add("test"))
                .forEach(messageIndex -> {
                    try {
                        processEachMessage(intensity, delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

        LOGGER.info("Intensity i - {} is successful!!! Try next", intensity);
    }

    private void processEachMessage(int i, long delay) throws InterruptedException {
        Thread.sleep(delay);

        if (blockingQueue.size() > Configurations.BLOCKING_QUEUE_DELIMITER) {
            LOGGER.info("Max intensity is - {} inserts in 1 sec", (i-1));
            LOGGER.info("Try to increase thread consumer count");
            System.exit(0);
        }
    }

    private void prepareConsumerThreads() {
        rangeClosed(0, Configurations.THREAD_CONSUMER_COUNT)
                .mapToObj(i -> consumerRunnable)
                .map(Thread::new)
                .forEach(Thread::start);
    }

    private Runnable consumerRunnable = () -> {
        while (true) {
            try {
                blockingQueue.take();
                transactionManager.executeAtomicTransaction();
            } catch (InterruptedException | SQLException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    };

    private Runnable messageProducer = () -> blockingQueue.add("test");

    private Runnable blockingQueueObser = () -> System.out.println(blockingQueue.size());
}
