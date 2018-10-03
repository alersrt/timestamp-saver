package org.student;

public class Application {

  public static void main(String[] args) {
    Thread threadMain = new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName());
      }
    });

    threadMain.setName("#MAIN");
    threadMain.start();
  }
}
