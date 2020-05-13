package company;

import company.client.Cliente;
import company.server.ServidorEndpoint;
import org.glassfish.tyrus.server.Server;

import javax.websocket.DeploymentException;
import java.awt.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws DeploymentException {

        new Server("localhost", 12345, "/", ServidorEndpoint.class).start();

        Cliente[] clientes = {
                new Cliente(10, 10, 0, Color.BLUE),
                new Cliente(590, 10, -1, Color.RED),
                new Cliente(10, 390, -1, Color.GREEN),
                new Cliente(590, 390, -1, Color.YELLOW),
        };

        Executor executor = Executors.newFixedThreadPool(clientes.length);
        for(Cliente cliente : clientes) executor.execute(cliente::iniciar);
    }
}
