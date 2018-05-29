package jp.techacademy.watanabe.shouta.shootinggame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ShootingGame extends Game {
    // publicにして外からアクセスできるようにする
    public SpriteBatch batch;

    @Override
    public void create () {
        batch = new SpriteBatch();

        // TitleScreenを表示する
        setScreen(new TitleScreen(this));
    }
}
