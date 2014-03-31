package game2048;

/**
 * @author bruno.borges@oracle.com
 */
public enum Direction {

    UP(0, -1), RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0);

    private final int y;
    private final int x;

    Direction(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Direction{" + "y=" + y + ", x=" + x + '}' + name();
    }

    public Direction goBack() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
        }
        return null;
    }

}
