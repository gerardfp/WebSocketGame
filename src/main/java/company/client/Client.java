package company.client;

import company.model.Message;
import company.model.Player;

import javax.swing.*;
import javax.websocket.*;
import java.awt.*;
import java.net.URI;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


public class Client extends JPanel {
    private Player player;
    private Map<String, Player> enemies = new ConcurrentHashMap<>();

    private Random random = new Random();

    private GameClientEndpoint gameClientEndpoint;

    private JLabel connectionStatusLabel;

    public Client(int x, int y, int angle, Color color){
        player = new Player(new Player.Position(x, y, angle), color);

        gameClientEndpoint = new GameClientEndpoint(this);
    }

    public void init() {
        JFrame frame = new JFrame();

        JPanel buttonPane = new JPanel();

        Button enterButton = new Button("ENTER GAME");
        Button exitButton = new Button("EXIT GAME");
        Button connectButton = new Button("CONNECT TO SERVER");
        Button disconnectButton = new Button("DISCONNECT FROM SERVER");
        connectionStatusLabel = new JLabel("Status");

        connectButton.addActionListener(e -> gameClientEndpoint.connect());
        disconnectButton.addActionListener(e -> gameClientEndpoint.close());
        enterButton.addActionListener(e -> gameClientEndpoint.sendMessage(Message.enterGame(player)));
        exitButton.addActionListener(e -> gameClientEndpoint.sendMessage(Message.exitGame()));

        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.PAGE_AXIS));
        buttonPane.add(enterButton);
        buttonPane.add(exitButton);
        buttonPane.add(connectButton);
        buttonPane.add(disconnectButton);
        buttonPane.add(connectionStatusLabel);

        frame.getContentPane().add(this, BorderLayout.CENTER);
        frame.getContentPane().add(buttonPane, BorderLayout.PAGE_END);
        frame.setSize(600, 400);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation((int) player.position.x+200, (int) player.position.y+200);

        gameClientEndpoint.connect();
        gameClientEndpoint.sendMessage(Message.enterGame(player));

        while(true){
            repaint();
            try { Thread.sleep(10); } catch (Exception e) { }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        moveAndShoot();

        sendPositionToServer();

        paintPlayer(g);

        paintEnemies(g);
    }

    private void paintEnemies(Graphics g) {
        enemies.forEach((id, enemy) -> {
            g.setColor(enemy.color);
            g.fillOval((int) enemy.position.x, (int) enemy.position.y, 20, 20);
            enemy.shoots.forEach(shoot -> {
                g.fillRect((int) shoot.position.x, (int) shoot.position.y, 4, 4);
            });
        });
    }

    private void paintPlayer(Graphics g) {
        g.setColor(player.color);
        g.fillOval((int) player.position.x, (int) player.position.y, 20, 20);
        player.shoots.forEach(shoot -> {
            g.fillRect((int) shoot.position.x, (int) shoot.position.y, 4, 4);
        });
    }

    private void sendPositionToServer() {
        gameClientEndpoint.sendMessage(Message.position(player));
    }

    private void moveAndShoot() {
        player.position.x += player.vx;
        player.position.y += player.vy;

        player.position.x = player.position.x > this.getWidth() ? this.getHeight() : player.position.x < 0 ? 0 : player.position.x;
        player.position.y = player.position.y > this.getHeight() ? this.getHeight() : player.position.y < 0 ? 0 : player.position.y;

        if(random.nextInt()%20 == 0){
            player.vx = random.nextInt(4)-2;
            player.vy = random.nextInt(4)-2;
        }

        if(random.nextInt()%5 == 0){
            player.position.angle += random.nextFloat();
        }

        if(random.nextInt()%4==0) {
            player.shoots.add(new Player.Shoot(player.position.x, player.position.y, player.position.angle));
        }

        player.shoots.forEach(shoot -> {
            shoot.position.x += Math.cos(shoot.position.angle);
            shoot.position.y += Math.sin(shoot.position.angle);

            if(shoot.position.x < 0 || shoot.position.y > this.getWidth() || shoot.position.y < 0 || shoot.position.y > this.getHeight()){
                player.shoots.remove(shoot);
            }
        });
    }

    private void processMessageFromServer(Message message){
        switch (message.type){
            case ENTER_GAME:
                addEnemy(message.senderId, message.position, message.color);
                break;
            case POSITION:
                updateEnemy(message.senderId, message.position, message.shoots);
                break;
            case EXIT_GAME:
                removeEnemy(message.senderId);
                break;
        }
    }

    private void addEnemy(String enemyId, Player.Position position, Color color){
        enemies.put(enemyId, new Player(position, color));
    }

    private void updateEnemy(String enemyId, Player.Position position, List<Player.Shoot> shoots){
        enemies.get(enemyId).update(position, shoots);
    }

    private void removeEnemy(String enemyId){
        enemies.remove(enemyId);
    }

    private void onOpenConnectionToServer(Session session){
        connectionStatusLabel.setText("CONNECTED: " + session.getId());
        connectionStatusLabel.paintImmediately(getVisibleRect());
    }

    private void onCloseConnectionToServer(){
        connectionStatusLabel.setText("DISCONNECTED");
        connectionStatusLabel.paintImmediately(getVisibleRect());
    }


    @ClientEndpoint(decoders = {Message.Decoder.class}, encoders = {Message.Encoder.class})
    public static class GameClientEndpoint {
        private Client client;
        private Session session;

        private GameClientEndpoint(Client client){
            this.client = client;
        }

        private void connect(){
            try{
                ContainerProvider.getWebSocketContainer().connectToServer(this, new URI("ws://localhost:12345/"));
            }catch(Exception ex){}
        }

        private void close(){
            try {
                session.close();
            } catch (Exception e) {}
        }

        private void sendMessage(Message message){
            if(session == null) return;

            try {
                session.getBasicRemote().sendObject(message.setSenderId(session.getId()));
            } catch (Exception e) {}
        }

        @OnOpen
        public void onOpen(Session session) {
            this.session = session;
            client.onOpenConnectionToServer(session);
        }

        @OnMessage
        public void onMessage(Message message, Session session) {
            client.processMessageFromServer(message);
        }

        @OnClose
        public void onClose(Session session, CloseReason closeReason) {
            client.onCloseConnectionToServer();
        }
    }
}

