package company.model;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player {

    public Position position;
    public float vx, vy;
    public Color color;
    public List<Shoot> shoots = new CopyOnWriteArrayList<>();

    public Player(Position position, Color color) {
        this.position = position;
        this.color = color;
    }

    public void update(Position position, List<Shoot> shoots){
        this.position.update(position);
        this.shoots.clear();
        this.shoots.addAll(shoots);
    }

    public static class Position {
        public float x, y;
        public double angle;

        public Position(float x, float y, double angle) {
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        private void update(Position position){
            this.x = position.x;
            this.y = position.y;
            this.angle = position.angle;
        }
    }

    public static class Shoot {
        public Position position;

        public Shoot(float x, float y, double angle) {
            this.position = new Position(x, y, angle);
        }
    }
}