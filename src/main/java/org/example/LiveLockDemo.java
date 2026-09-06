package org.example;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class LiveLockDemo implements Demo {
    private static final Logger LOGGER = Logger.getLogger(LiveLockDemo.class.getName());
    private final AtomicBoolean resourceBusy = new AtomicBoolean(false);
    private final AtomicBoolean firstWantsToEnter = new AtomicBoolean(false);
    private final AtomicBoolean secondWantsToEnter = new AtomicBoolean(false);
    private volatile boolean running = true;

    @Override
    public void run() {
        Thread first = new Thread(() -> {
            while (running) {
                firstWantsToEnter.set(true);
                while (secondWantsToEnter.get() && running) {
                    LOGGER.info("Thread-First: give in Thread-Second");
                    firstWantsToEnter.set(false);
                    sleep(100);
                    firstWantsToEnter.set(true);
                }
                if (!running) break;
                if (resourceBusy.compareAndSet(false, true)) {
                    try {
                        LOGGER.info("Thread-First: Working");
                        sleep(500);
                    } finally {
                        resourceBusy.set(false);
                    }
                }
                firstWantsToEnter.set(false);
                sleep(100);
            }
        }, "Thread-First");

        Thread second = new Thread(() -> {
            while (running) {
                secondWantsToEnter.set(true);
                while (firstWantsToEnter.get() && running) {
                    LOGGER.info("Thread-Second: give in Thread-First");
                    secondWantsToEnter.set(false);
                    sleep(100);
                    secondWantsToEnter.set(true);
                }
                if (!running) break;
                if (resourceBusy.compareAndSet(false, true)) {
                    try {
                        LOGGER.info("Thread-Second: Working");
                        sleep(500);
                    } finally {
                        resourceBusy.set(false);
                    }
                }
                secondWantsToEnter.set(false);
                sleep(100);
            }
        }, "Thread-Second");

        first.start();
        second.start();

        // Остановка через 5 секунд
        Thread stopper = new Thread(() -> {
            try {
                Thread.sleep(5000);
                running = false;
                LOGGER.info("Stop ...");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Stopper");
        stopper.setDaemon(true);
        stopper.start();

        try {
            first.join();
            second.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LOGGER.info("LiveLock demo is over");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running = false;
        }
    }
}
