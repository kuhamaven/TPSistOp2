package message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class Producer {
    private final Socket SOCKET;
    private ProducerConsumerMessage.SharedArea shared;

    public Producer(String host, int port, ProducerConsumerMessage.SharedArea shared) throws IOException {
        SOCKET = new Socket(host, port);
        this.shared = shared;
    }

    public void startProduce() {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(SOCKET.getOutputStream());
            int i = 0;
            while (true) {
                while (shared.getBuffer().size() >= shared.SIZE) {
                    System.out.println("Productor esperando, buffer: " + shared.getBuffer().size());
                    Thread.sleep(1000);
                }
                int delay = ThreadLocalRandom.current().nextInt(0, 1500 + 1);
                Thread.sleep(delay);
                dataOutputStream.writeUTF(ProducerConsumerMessage.SocketType.PRODUCER + " " + (i++) + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
