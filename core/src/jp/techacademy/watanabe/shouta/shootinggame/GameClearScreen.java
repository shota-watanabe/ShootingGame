package jp.techacademy.watanabe.shouta.shootinggame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameClearScreen extends ScreenAdapter {

    Sound gameclear = Gdx.audio.newSound(Gdx.files.internal("gameclear.mp3"));

    static final float GUI_WIDTH = 320;
    static final float GUI_HEIGHT = 480;

    private ShootingGame mGame;
    Sprite mBg;
    OrthographicCamera mGuiCamera;
    FitViewport mGuiViewPort;
    BitmapFont mFont;

    int mScore;

    public GameClearScreen(ShootingGame game, int score) {

        mGame = game;

        if (mGame.mRequestHandler != null) {
            mGame.mRequestHandler.showAds(true);
        }

        mScore = score;

        Texture bgTexture = new Texture("gameclearback.png");
        mBg = new Sprite(new TextureRegion(bgTexture, 0, 0, 576, 1024));
        mBg.setSize(GUI_WIDTH, GUI_HEIGHT);
        mBg.setPosition(0, 0);

        mGuiCamera = new OrthographicCamera();
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT);
        mGuiViewPort = new FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera);

        mFont = new BitmapFont(Gdx.files.internal("font.fnt"), false);
    }

    @Override
    public void render(float delta) {
        gameclear.play(5f);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mGuiCamera.update();
        mGame.batch.setProjectionMatrix(mGuiCamera.combined);

        mGame.batch.begin();
        mBg.draw(mGame.batch);
        mFont.draw(mGame.batch, "Game Clear", 0, GUI_HEIGHT / 2 + 90, GUI_WIDTH, Align.center, false);
        mFont.draw(mGame.batch, "Score: " + mScore, 0, GUI_HEIGHT / 2 + 40, GUI_WIDTH, Align.center, false);
        mFont.draw(mGame.batch, "Touch The NextStage", 0, GUI_HEIGHT / 2 - 40, GUI_WIDTH, Align.center, false);
        mFont.draw(mGame.batch, "（開発中）", 0, GUI_HEIGHT / 2 - 65, GUI_WIDTH, Align.center, false);
        mGame.batch.end();

        if (Gdx.input.justTouched()) {
            if (mGame.mRequestHandler != null) {
                mGame.mRequestHandler.showAds(false);
            }
            mGame.setScreen(new TitleScreen(mGame));
        }
        gameclear.dispose();
    }
}
