package jp.techacademy.watanabe.shouta.shootinggame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ShootingGame extends Game {
    public SpriteBatch batch;
    public jp.techacademy.watanabe.shouta.shootinggame.ActivityRequestHandler mRequestHandler;

    public ShootingGame(jp.techacademy.watanabe.shouta.shootinggame.ActivityRequestHandler requestHandler) {
        super();
        mRequestHandler = requestHandler;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new TitleScreen(this));
    }
}