package org.example;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class DeadLockDemo implements Demo {
    private static final Logger LOGGER = Logger.getLogger(DeadLockDemo.class.getName());
    private static final ReentrantLock lock1 = new ReentrantLock();
    private static final ReentrantLock lock2 = new ReentrantLock();
    private volatile boolean running = true;

    @Override
    public void run() {
        Thread thread1 = new Thread(() -> {
            while (running) {
                try {
                    lock1.lockInterruptibly();
                    try {
                        LOGGER.info("Thread 1: hold lock1");
                        Thread.sleep(100);
                        LOGGER.info("Thread 1: wait lock2");
                        lock2.lockInterruptibly();
                        try {
                            LOGGER.info("Thread 1: hold lock1 и lock2");
                        } finally {
                            lock2.unlock();
                        }
                    } finally {
                        lock1.unlock();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warning("Thread 1 interrupted, exiting the loop");
                    break;
                }
            }
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            while (running) {
                try {
                    lock2.lockInterruptibly();
                    try {
                        LOGGER.info("Thread 2: hold lock2");
                        Thread.sleep(100);
                        LOGGER.info("Thread 2: wait lock1");
                        lock1.lockInterruptibly();
                        try {
                            LOGGER.info("Thread 2: hold lock2 и lock1");
                        } finally {
                            lock1.unlock();
                        }
                    } finally {
                        lock2.unlock();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.warning("Thread 2 interrupted, exiting the loop");
                    break;
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
                thread1.interrupt();
                thread2.interrupt();
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
        LOGGER.info("DeadLock demo is over");
    }
}