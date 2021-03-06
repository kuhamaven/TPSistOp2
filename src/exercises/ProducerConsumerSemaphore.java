package exercises;

import resources.DrawGraph;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ProducerConsumerSemaphore {
    public static void main(String[] args) throws InterruptedException {
        int bufferSize = Scanner.getInt("Ingrese el tamaño del buffer: ");
        final SharedArea shared = new SharedArea(bufferSize);
        List<Consumer> consumers = new ArrayList<>();
        List<Producer> producers = new ArrayList<>();
        int consumersSize = Scanner.getInt("Ingrese la cantidad deseada de consumidores: ");
        int producersSize = Scanner.getInt("Ingrese la cantidad deseada de productores: ");
        Thread graph = new Thread(() -> startGraph(shared));

        // Create threads
        for (int i = 0; i < consumersSize; i++) consumers.add(new Consumer(shared, "Consumer " + i));
        for (int i = 0; i < producersSize; i++) producers.add(new Producer(shared, "Producer " + i));

        // Start threads
        for (Producer p : producers) p.start();
        for (Consumer c : consumers) c.start();
        graph.start();

        // Join threads
        for (Producer p : producers) p.join();
        for (Consumer c : consumers) c.join();
        graph.join();
    }

    public static void startGraph(SharedArea shared) {
        DrawGraph mainPanel = new DrawGraph(shared.getTimeline());
        JFrame frame = new JFrame("Cantidad en el buffer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);

        while (true) {
            frame.revalidate();
            frame.repaint();
        }
    }

    public static class Consumer {

        private final Thread cons_thread;

        public Consumer(SharedArea shared, String name) {
            cons_thread = new Thread(() -> {
                try {
                    shared.consume(name);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        public void start() {
            cons_thread.start();
        }

        public void join() throws InterruptedException {
            cons_thread.join();
        }
    }

    public static class Producer {

        private final Thread cons_thread;

        public Producer(SharedArea shared, String name) {
            cons_thread = new Thread(() -> {
                try {
                    shared.produce(name);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        public void start() {
            cons_thread.start();
        }

        public void join() throws InterruptedException {
            cons_thread.join();
        }
    }

    public static class SharedArea {

        final int SIZE;
        private int element;
        private final LinkedList<Integer> buffer = new LinkedList<>();
        private final LinkedList<Integer> bufferQuantityTimeline = new LinkedList<>();
        private final Semaphore mutex = new Semaphore(1, "mutex");
        private final Semaphore empty;
        private final Semaphore full = new Semaphore(0, "full");

        public SharedArea(int size) {
            SIZE = size;
            this.empty = new Semaphore(size, "empty");
        }

        // Avoid multiple elements with same number
        private synchronized int getElement() {
            return element++;
        }

        public LinkedList<Integer> getTimeline() {
            return bufferQuantityTimeline;
        }

        public void addToTimeline() {
            bufferQuantityTimeline.add(buffer.size() + 1);
        }

        public void produce(String name) throws InterruptedException {
            while (true) {
                int currentElement = getElement();
                int delay = ThreadLocalRandom.current().nextInt(0, 1500 + 1);
                Thread.sleep(delay);
                empty.down();
                mutex.down();
                System.out.println(name + " produced element: " + currentElement);
                synchronized (this) {
                    buffer.add(currentElement);
                }
                mutex.up();
                full.up();
                addToTimeline();
            }
        }

        public void consume(String name) throws InterruptedException {
            int element;
            while (true) {
                Thread.sleep(1000);
                full.down();
                mutex.down();
                synchronized (this) {
                    element = buffer.removeFirst();
                }
                System.out.println(name + " consumed element: " + element);
                mutex.up();
                empty.up();
                addToTimeline();
            }
        }

    }

    public static class Semaphore {

        private int value;
        private final String name;

        public Semaphore(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public void down() throws InterruptedException {
            //System.out.println("down " + this.name + ": " + this.value);
            synchronized (this) {
                while (this.value <= 0)
                    wait();
                this.value--;
            }
        }

        public void up() {
            //System.out.println("up " + this.name + ": " + this.value);
            synchronized (this) {
                this.value++;
                notify();
            }
        }

    }

    public static class Scanner {

        private static final java.util.Scanner scanner = new java.util.Scanner(System.in);

        private Scanner() {
        }

        public static String getString(String message) {
            System.out.print(message);
            final String result = scanner.nextLine().trim();
            if (result.isEmpty()) {
                System.out.println("Please enter a text.");
                return getString(message);
            }
            return result;
        }

        public static int getInt(String message) {
            System.out.print(message);
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter an integer.");
                return getInt(message);
            }
        }
    }
}
