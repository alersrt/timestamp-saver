package org.student;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
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
      var timerThread = new Thread(Application::runTimer, "#timer");
      var databaseThread = new Thread(Application::runSaver, "#database");
      timerThread.start();
      databaseThread.start();
    } else if (args.length == 1 && args[0].equals("-p")) {
      var printerThread = new Thread(Application::runLister, "#list");
      printerThread.start();
    }
  }

  private static void runTimer() {
    while (true) {
      try {
        TimeUnit.SECONDS.sleep(1);
        queue.offer(LocalDateTime.now());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static void runSaver() {
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    while (true) {
      // Use "try" inside "while" that the loop will not be broken.
      try {
        // Get but not remove head's element from queue.
        var timestamp = queue.peek();
        if (timestamp != null) {

          // Create connection and check if communication error.
          Connection connection = null;
          try {
            connection = DriverManager.getConnection(DATABASE_URL);
          } catch (CommunicationsException ex) {
            LOGGER.warning("DB CONNECTION IS NOT VALID");
            TimeUnit.SECONDS.sleep(5);
            continue;
          } catch (SQLException e) {
            e.printStackTrace();
            continue;
          }

          // Create and execute statement.
          var ps = connection.prepareStatement("insert into timestamp (time) values (?)");
          ps.setTimestamp(1, Timestamp.valueOf(timestamp));
          ps.execute();

          // Remove head's element from queue (if exception was thrown before this element stays in queue).
          queue.poll();

          // Release connection.
          ps.close();
          connection.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static void runLister() {
    try (
        var connection = DriverManager.getConnection(DATABASE_URL);
        var preparedStatement = connection.prepareStatement("SELECT * from timestamp")
    ) {
      Class.forName("com.mysql.cj.jdbc.Driver");

      preparedStatement.setFetchSize(1024);
      var resultSet = preparedStatement.executeQuery();

      while (resultSet.next()) {
        System.out.println(resultSet.getTimestamp("time"));
      }
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
    }
  }
}
