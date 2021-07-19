package io.fxgame.game2048.fxgl;

import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.EffectComponent;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.effects.WobbleEffect;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.util.Duration;

/**
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
public class EasterEggApp extends GameApplication {

    private final Bounds gameBounds;

    public EasterEggApp(Bounds gameBounds) {
        this.gameBounds = gameBounds;
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setScaleAffectedOnResize(false);
        settings.setApplicationMode(ApplicationMode.RELEASE);
    }

    public void playGlitchAnimation(Image screenshot) {
        var effectComp = new EffectComponent();

        FXGL.entityBuilder()
                .with(effectComp)
                .with(new ExpireCleanComponent(Duration.seconds(3)))
                .buildAndAttach();

        effectComp.startEffect(new WobbleEffect(new Texture(screenshot), Duration.seconds(3)));

        FXGL.getNotificationService().pushNotification("FXGL mode!");
    }
}
