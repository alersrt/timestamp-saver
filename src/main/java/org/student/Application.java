package org.student;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

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
   * Hibernate's session sessionFactory. It needs for working with database.
   */
  private static SessionFactory sessionFactory;

  static {
    try {
      var configuration = new Configuration();
      configuration.configure();
      sessionFactory = configuration.buildSessionFactory();
    } catch (Throwable ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  /**
   * Main method, entry point.
   *
   * @param args array of arguments.
   */
  public static void main(String[] args) {
    var timerThread = new Thread(() -> {
      while (true) {
        try {
          TimeUnit.SECONDS.sleep(1);
          queue.offer(LocalDateTime.now());
        } catch (Exception e) {
          LOGGER.severe(e.toString());
        }
      }
    }, "#timer");

    var databaseThread = new Thread(() -> {
      while (true) {
        try {
          var session = sessionFactory.openSession();
          var timestamp = queue.peek();
          if (timestamp != null) {
            Transaction tx = null;
            try {
              tx = session.beginTransaction();
              session.save(new TimestampEntity(timestamp));
              tx.commit();
              queue.poll();
            } catch (Exception e) {
              if (tx != null) {
                tx.rollback();
              }
              throw e;
            } finally {
              session.close();
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }, "#database");

    timerThread.start();
    databaseThread.start();
  }
}
