package org.student;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
   * Hibernate's session factory. It needs for working with database.
   */
  private static SessionFactory factory;

  static {
    try {
      Configuration configuration = new Configuration();
      configuration.configure();

      factory = configuration.buildSessionFactory();
    } catch (Throwable ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }

  public static Session getSession() throws HibernateException {
    return factory.openSession();
  }

  /**
   * Main method, entry point.
   *
   * @param args array of arguments.
   */
  public static void main(String[] args) {
    final Session session = getSession();

    var timerThread = new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(1000);
          queue.offer(LocalDateTime.now());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    var databaseThread = new Thread(() -> {
      while (true) {
        var timestamp = queue.poll();
        if (timestamp != null) {
          LOGGER.info(timestamp.toString());
        }
      }
    });

    timerThread.setName("#timer");
    databaseThread.setName("#database");
    timerThread.start();
    databaseThread.start();
  }
}
