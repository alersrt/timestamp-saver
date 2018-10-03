package org.student;

import java.util.logging.Logger;

public class Application {

  private final static Logger LOGGER = Logger.getLogger(Application.class.getName());

  public static void main(String[] args) {
    LOGGER.info("======> Application started");
    Thread threadMain = new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        LOGGER.info(Thread.currentThread().getName());
      }
    });

    threadMain.setName("#MAIN");
    threadMain.start();
  }
}
