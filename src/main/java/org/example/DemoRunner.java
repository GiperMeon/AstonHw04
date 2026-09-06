package org.example;

// DemoRunner.java
public class DemoRunner {
    public static void main(String[] args) {
        Demo[] demos = {
                new DeadLockDemo(),
                new LiveLockDemo(),
                new AlternatingOutputDemo()
        };

        for (Demo demo : demos) {
            System.out.println("\n=== Start: " + demo.getClass().getSimpleName() + " ===");
            demo.run();
            System.out.println("=== Completed: " + demo.getClass().getSimpleName() + " ===\n");

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
