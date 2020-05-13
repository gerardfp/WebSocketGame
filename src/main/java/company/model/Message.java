package company.model;

import com.google.gson.Gson;

import javax.websocket.EndpointConfig;
import java.awt.*;
import java.util.List;


public class Message {
    public enum Type {
        ENTER_GAME, EXIT_GAME, POSITION
    }

    private static Gson gson = new Gson();

    public String senderId;
    public String name;
    public Color color;
    public Type type;
    public Player.Position position;
    public List<Player.Shoot> shoots;

    public Message(){}

    public static Message position(Player player){
        Message message = new Message();
        message.type = Type.POSITION;
        message.position = player.position;
        message.shoots = player.shoots;
        return message;
    }

    public static Message enterGame(Player player){
        Message message = new Message();
        message.type = Type.ENTER_GAME;
        message.color = player.color;
        message.position = player.position;
        return message;
    }

    public static Message exitGame(){
        Message message = new Message();
        message.type = Type.EXIT_GAME;
        return message;
    }

    public Message setSenderId(String senderId){
        this.senderId = senderId;
        return this;
    }

    public static class Encoder implements javax.websocket.Encoder.Text<Message> {
        @Override
        public String encode(Message message) {
            return gson.toJson(message);
        }

        @Override
        public void init(EndpointConfig config) {}

        @Override
        public void destroy() {}
    }

    public static class Decoder implements javax.websocket.Decoder.Text<Message> {
        @Override
        public Message decode(String s) {
            return gson.fromJson(s, Message.class);
        }

        @Override
        public boolean willDecode(String s) {
            return (s != null);
        }

        @Override
        public void init(EndpointConfig config) {}

        @Override
        public void destroy() {}
    }
}