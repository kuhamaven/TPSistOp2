package exercises;

import resources.DrawGraph;

import javax.swing.*;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

class ProducerConsumerMonitor {

    public static void main(String[] args) throws InterruptedException {

        final Monitor producerConsumerMonitor = new Monitor(10);

        // Create producer thread
        Thread prod_thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    while (true){
                        Thread.sleep(ThreadLocalRandom.current().nextInt(0,1500 + 1));
                        producerConsumerMonitor.produce();
                    }

                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // Create consumer thread
        Thread cons_thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try {
                    while (true) {
                        Thread.sleep(1000);
                        producerConsumerMonitor.consume();
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread graph = new Thread(() -> startGraph(producerConsumerMonitor));

        prod_thread.start();
        cons_thread.start();
        graph.start();

        prod_thread.join();
        cons_thread.join();
        graph.join();
    }

    public static void startGraph(Monitor monitor) {
        DrawGraph mainPanel = new DrawGraph(monitor.getTimeline());
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

    public static class Monitor {
        Integer element = 0;
        Integer N;
        LinkedList<Integer> elements = new LinkedList<>();
        LinkedList<Integer> bufferQuantityTimeline = new LinkedList<>();

        public Monitor(Integer n) {
            N = n;
        }

        public synchronized LinkedList<Integer> getTimeline() {
            return bufferQuantityTimeline;
        }

        public synchronized void addToTimeline() {
            bufferQuantityTimeline.add(elements.size() + 1);
        }

        public synchronized void produce() throws InterruptedException {
            while (elements.size() >= N) {
                System.out.println("Produce Thread waiting, elements size: " + elements.size());
                wait();
            }

            System.out.println("Produce " + element);
            elements.add(element++);
            if (elements.size() == 1) {
                notify();
                System.out.println("Produce thread started");
            }
            addToTimeline();
        }

        public synchronized void consume() throws InterruptedException {
            while (elements.size() <= 0) {
                System.out.println("Consumer Thread waiting, elements size: " + elements.size());
                wait();
            }
            Integer elementConsumed = elements.removeFirst();
            if (elements.size() == N - 1) {
                notify();
                System.out.println("Consumer Thread started, elements size: " + elements.size());

            }
            System.out.println("            Consume " + elementConsumed);
            addToTimeline();
        }
    }
}
