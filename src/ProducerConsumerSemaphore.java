import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class ProducerConsumerSemaphore {
    public static void main(String[] args)
            throws InterruptedException {
        final SharedArea shared = new SharedArea(10);

        // Create producer thread
        Thread prod_thread = new Thread(() -> {
            try {
                shared.produce();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Create consumer thread
        Thread cons_thread = new Thread(() -> {
            try {
                shared.consume();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        prod_thread.start();
        cons_thread.start();

        prod_thread.join();
        cons_thread.join();
    }

    public static class SharedArea {

        final int SIZE;
        private final LinkedList<Integer> buffer = new LinkedList<>();
        private final Semaphore mutex = new Semaphore(1,"mutex");
        private final Semaphore empty;
        private final Semaphore full = new Semaphore(0, "full");

        public SharedArea(int size) {
            SIZE = size;
            this.empty = new Semaphore(size, "empty");
        }

        public void produce() throws InterruptedException {
            int element = 0;
            while (true) {
                int delay = ThreadLocalRandom.current().nextInt(0, 1500 + 1);
                Thread.sleep(delay);
                empty.down();
                mutex.down();
                System.out.println("Produce " + element);
                buffer.add(element++);
                mutex.up();
                full.up();
            }
        }

        public void consume() throws InterruptedException {
            int element;
            while (true) {
                Thread.sleep(1000);
                full.down();
                mutex.down();
                element = buffer.removeFirst();
                System.out.println("Consume " + element);
                mutex.up();
                empty.up();
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
            System.out.println("down "+ this.name +": " + this.value);
            synchronized (this) {
                while (this.value <= 0)
                    wait();
                this.value--;
            }
        }

        public void up() {
            System.out.println("up "+ this.name +": "+ this.value);
            synchronized (this) {
                this.value++;
                notify();
            }
        }

    }
}
