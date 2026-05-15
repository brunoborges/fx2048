package io.github.brunoborges.fx2048.app;

import io.github.brunoborges.fx2048.app.*;
import io.github.brunoborges.fx2048.game.*;
import io.github.brunoborges.fx2048.persistence.*;
import io.github.brunoborges.fx2048.settings.*;
import io.github.brunoborges.fx2048.ui.*;

import javafx.application.Application;

/**
 * AppLauncher
 */
public class AppLauncher {

    public static void main(String[] args) {
        Application.launch(Game2048.class, args);
    }
}
