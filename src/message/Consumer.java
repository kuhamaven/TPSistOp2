package message;

import exercises.ProducerConsumerMessage.SharedArea;
import resources.SocketType;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Consumer {
    private final Socket SOCKET;
    private final SharedArea shared;

    public Consumer(String host, int port, SharedArea shared) throws IOException {
        SOCKET = new Socket(host, port);
        this.shared = shared;
    }

    public void startConsume() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(SOCKET.getOutputStream());
            int i = 0;
            while (true) {
                while (shared.getBuffer().size() <= 0) {
                    System.out.println("Consumidor esperando, buffer: " + shared.getBuffer().size());
                    Thread.sleep(1000);
                }
                Thread.sleep(1000);
                dataOutputStream.writeUTF(SocketType.CONSUMER + " " + (i++) + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
