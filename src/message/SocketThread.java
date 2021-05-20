package message;

import exercises.ProducerConsumerMessage;
import resources.SocketType;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class SocketThread extends Thread {
    protected Socket socket;
    protected ProducerConsumerMessage.SharedArea shared;

    public SocketThread(Socket socket, ProducerConsumerMessage.SharedArea shared) {
        this.socket = socket;
        this.shared = shared;
    }

    public void run() {
        BufferedReader input;
        DataOutputStream output;
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String line;
        while (true) {
            try {
                output.writeUTF("Petición recibida y aceptada");

                line = input.readLine();
                String[] words = line.split(" ");
                String type = words[0];
                String id = words[1];

                if (line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    return;
                } else {
                    type = type.replaceAll("[^a-zA-Z ]", "");
                    if (type.equals(SocketType.CONSUMER.name())) {
                        consume();
                    }
                    if (type.equals(SocketType.PRODUCER.name())) {
                        produce(Integer.parseInt(id));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private void produce(int element) throws InterruptedException {
        int delay = ThreadLocalRandom.current().nextInt(0, 1500 + 1);
        Thread.sleep(delay);
        shared.downEmpty();
        shared.downMutex();
        System.out.println("Produce: " + element);
        shared.getBuffer().add(element);
        shared.upMutex();
        shared.upFull();
        shared.addToTimeline();
    }

    private void consume() throws InterruptedException {
        Thread.sleep(1000);
        shared.downFull();
        shared.downMutex();
        int element = shared.getBuffer().removeFirst();
        System.out.println("                Consume: " + element);
        shared.upMutex();
        shared.upEmpty();
        shared.addToTimeline();
    }
}
