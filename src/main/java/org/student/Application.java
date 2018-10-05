package org.student;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Application {

  /**
   * Standard logger.
   */
  private final static Logger LOGGER = Logger.getLogger(Application.class.getName());

  /**
   * This queue uses as buffer. At first timestamp is placed in this queue and after it takes from here and writes to
   * database. Note: it is non blocking concurrency safe queue, I use it because we write once per second, no more.
   */
  private final static Queue<LocalDateTime> queue = new ConcurrentLinkedQueue<>();

  /**
   * Url of database.
   */
  private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/testdb?user=root&password=root";

  /**
   * Main method, entry point.
   *
   * @param args array of arguments.
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      // There is Runnable under lambda's capote.
      var producer = new Thread(Application::produceTimestamp, "#producer");
      var consumer = new Thread(Application::consumeTimestamp, "#consumer");
      producer.start();
      consumer.start();
    } else if (args.length == 1 && args[0].equals("-p")) {
      var printer = new Thread(Application::printTimestamps, "#printer");
      printer.start();
    }
  }

  private static void produceTimestamp() {
    while (true) {
      try {
        TimeUnit.SECONDS.sleep(1);
        queue.offer(LocalDateTime.now());
      } catch (Exception e) {
        LOGGER.severe(e.toString());
      }
    }
  }

  private static void consumeTimestamp() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      LOGGER.severe(e.toString());
    }

    Connection connection = null;
    while (true) {
      // Use "try" inside "while" that the loop will not be broken.
      try {
        if (!queue.isEmpty()) {
          // Create connection and check if communication error.
          if (connection == null || !connection.isValid(0)) {
            try {
              connection = DriverManager.getConnection(DATABASE_URL);
              // Disable autocommit because we will use transactions.
              connection.setAutoCommit(false);
            } catch (SQLException e) {
              LOGGER.severe(String.format("SQLState: %s | Error code: %d", e.getSQLState(), e.getErrorCode()));
              TimeUnit.SECONDS.sleep(5);
              continue;
            }
          }

          // So, we will just reuse existed connection to write
          // into database, if it is possible.
          var iterator = queue.iterator();
          while (!queue.isEmpty()) {
            // Get but not remove head's element from queue.
            var timestamp = queue.peek();
            // Create and execute statement.
            try (var ps = connection.prepareStatement("insert into timestamp (time) values (?)")) {
              ps.setTimestamp(1, Timestamp.valueOf(timestamp));
              ps.execute();
              connection.commit();
              // Remove head's element from queue (if exception was thrown before this element stays in queue).
              queue.poll();
            } catch (SQLException e) {
              // Why need this try/catch? Connection can be lost when we try
              // to write whole buffer to database. In the next iteration of
              // the main loop we just start from latest place.
              LOGGER.severe(String.format("SQLState: %s | Error code: %d", e.getSQLState(), e.getErrorCode()));
              try {
                connection.rollback();
              } catch (SQLException rbEx) {
                LOGGER.warning(String.format("SQLState: %s | Error code: %d", rbEx.getSQLState(), rbEx.getErrorCode()));
              }
              // Release connection.
              connection.close();
              // Exit from cycle when exception happened.
              break;
            }
          }
        }
      } catch (Exception e) {
        LOGGER.severe(e.toString());
      }
    }
  }

  private static void printTimestamps() {
    try (
        var connection = DriverManager.getConnection(DATABASE_URL);
        var preparedStatement = connection.prepareStatement("SELECT * from timestamp")
    ) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      // Just for me, that I don't forget it. Sometimes select result so big and this
      // hint saves my memory.
      preparedStatement.setFetchSize(1024);
      var resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        System.out.println(resultSet.getTimestamp("time"));
      }
    } catch (ClassNotFoundException | SQLException e) {
      LOGGER.severe(e.toString());
    }
  }
}
