package org.example;

import java.util.logging.Logger;

public class AlternatingOutputDemo implements Demo {
    private static final Logger LOGGER = Logger.getLogger(AlternatingOutputDemo.class.getName());
    private final Object monitor = new Object();
    private boolean turn = true; // true = ход потока 1, false = потока 2
    private volatile boolean running = true;

    @Override
    public void run() {
        Thread thread1 = new Thread(() -> {
            while (running) {
                synchronized (monitor) {
                    while (running && !turn) {
                        try {
                            monitor.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            running = false;
                            break;
                        }
                    }
                    if (!running) {
                        monitor.notifyAll();
                        break;
                    }
                    System.out.print("1 ");
                    turn = false;
                    monitor.notifyAll();
                }
            }
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            while (running) {
                synchronized (monitor) {
                    while (running && turn) {
                        try {
                            monitor.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            running = false;
                            break;
                        }
                    }
                    if (!running) {
                        monitor.notifyAll();
                        break;
                    }
                    System.out.print("2 ");
                    turn = true;
                    monitor.notifyAll();
                }
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();

        // Остановка через 3 секунды
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(3000);
                running = false;
                synchronized (monitor) {
                    monitor.notifyAll();
                }
                System.out.println(); // новая строка после серии вывода
                LOGGER.info("Stop ...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Stopper");
        stopper.setDaemon(true);
        stopper.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.info("AlternatingOutput demo is over");
    }
}