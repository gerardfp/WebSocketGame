package company.server;

import company.model.Message;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint(value = "/", decoders = Message.Decoder.class, encoders = Message.Encoder.class)
public class GameServerEndpoint {
    private static final Set<Session> clients = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
    }

    @OnMessage
    public void onMessage(Session sender, Message message) {
        broadcast(sender, message);
    }

    @OnClose
    public void onClose(Session sender) {
        clients.remove(sender);
        broadcast(sender, Message.exitGame());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
    }

    private static void broadcast(Session sender, Message message) {
        synchronized(clients){
            for(Session client : clients){
                if (!client.equals(sender)){
                    try {
                        client.getBasicRemote().sendObject(message.setSenderId(sender.getId()));
                    } catch (Exception e) {}
                }
            }
        }
    }
}
