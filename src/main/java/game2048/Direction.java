package game2048;

import javafx.scene.input.KeyCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * @author bruno.borges@oracle.com
 */
@Getter
@ToString
@RequiredArgsConstructor
public enum Direction {

    UP(0, -1), RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0);

    private final int x;
    private final int y;

    public static Direction valueFor(KeyCode keyCode) {
        return valueOf(keyCode.name());
    }
}
