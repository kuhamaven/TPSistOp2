package message;

import resources.DrawGraph;

import javax.swing.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ProducerConsumerMessage {
    private static final int PORT = 1978;
    private static final String HOST = "localhost";
    private static final SharedArea shared = new SharedArea(10);

    public static void main(String[] args) throws InterruptedException {
        Thread server = new Thread(ProducerConsumerMessage::startServer);
        Thread producer = new Thread(ProducerConsumerMessage::startProducer);
        Thread consumer = new Thread(ProducerConsumerMessage::startConsumer);
        Thread graph = new Thread(ProducerConsumerMessage::startGraph);

        server.start();
        producer.start();
        consumer.start();
        graph.start();

        server.join();
        producer.join();
        consumer.join();
        graph.join();
    }

    public enum SocketType {
        PRODUCER, CONSUMER
    }

    public static void startServer() {
        System.out.println("[Server] - Initializing...");

        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                if (serverSocket != null) {
                    socket = serverSocket.accept();
                }
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            new SocketThread(socket, shared).start();
        }
    }

    public static void startProducer() {
        System.out.println("[Producer] - Initializing...");

        try {
            Producer producer = new Producer(HOST, PORT, shared);
            producer.startProduce();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startConsumer() {
        System.out.println("[Consumer] - Initializing...");

        try {
            Consumer consumer = new Consumer(HOST, PORT, shared);
            consumer.startConsume();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startGraph() {
        DrawGraph mainPanel = new DrawGraph(shared.getTimeline());
        JFrame frame = new JFrame("Cantidad del buffer");
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

    public static class SharedArea {
        final int SIZE;
        private final LinkedList<Integer> buffer = new LinkedList<>();
        private final LinkedList<Integer> bufferQuantityTimeline = new LinkedList<>();
        private int mutex = 1;
        private int empty;
        private int full = 0;

        public SharedArea(int size) {
            SIZE = size;
            this.empty = size;
        }

        public LinkedList<Integer> getBuffer() {
            return buffer;
        }

        public LinkedList<Integer> getTimeline() {
            return bufferQuantityTimeline;
        }

        public void addToTimeline() {
            bufferQuantityTimeline.add(buffer.size());
        }

        public void downEmpty() throws InterruptedException {
            // System.out.println("downEmpty:" + this.empty);
            synchronized (this) {
                while (this.empty <= 0)
                    wait();
                this.empty--;
            }
        }

        public void upEmpty() {
            // System.out.println("upEmpty:" + this.empty);
            synchronized (this) {
                this.empty++;
                notify();
            }
        }

        public void downMutex() throws InterruptedException {
            // System.out.println("downMutex:" + this.mutex);
            synchronized (this) {
                while (this.mutex <= 0)
                    wait();
                this.mutex--;
            }
        }

        public void upMutex() {
            // System.out.println("upMutex:" + this.mutex);
            synchronized (this) {
                this.mutex++;
                notify();
            }
        }

        public void downFull() throws InterruptedException {
            // System.out.println("downFull:" + this.full);
            synchronized (this) {
                while (this.full <= 0)
                    wait();
                this.full--;
            }
        }

        public void upFull() {
            // System.out.println("upFull:" + this.full);
            synchronized (this) {
                this.full++;
                notify();
            }
        }
    }
}
