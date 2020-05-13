package company;

import company.client.Client;
import company.server.GameServerEndpoint;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import java.awt.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws DeploymentException {

        new Server("localhost", 12345, "/", GameServerEndpoint.class).start();

        Client[] clients = {
                new Client(10, 10, 0, Color.BLUE),
                new Client(590, 10, -1, Color.RED),
                new Client(10, 390, -1, Color.GREEN),
                new Client(590, 390, -1, Color.YELLOW),
        };

        Executor executor = Executors.newFixedThreadPool(clients.length);
        for(Client client : clients) executor.execute(client::init);
    }
}
