package jp.techacademy.watanabe.shouta.shootinggame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.delay;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveBy;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.removeActor;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class GameScreen extends ScreenAdapter implements InputProcessor {

    Music bgm = Gdx.audio.newMusic(Gdx.files.internal("stage01.mp3"));
    Sound explosion = Gdx.audio.newSound(Gdx.files.internal("explosion1.mp3"));
    Sound getshield = Gdx.audio.newSound(Gdx.files.internal("shield.mp3"));
    Sound slow = Gdx.audio.newSound(Gdx.files.internal("slow.mp3"));

    static final float CAMERA_WIDTH = 10;
    static final float CAMERA_HEIGHT = 15;
    static final float GUI_WIDTH = 320;
    static final float GUI_HEIGHT = 480;

    static final int GAME_STATE_PLAYING = 0;
    static final int GAME_STATE_GAMEOVER = 1;
    static final int GAME_STATE_GAMECLEAR = 2;

    private ShootingGame mGame;
    private AssetManager assets;
    private TextureAtlas atlas;
    private ProgressBar healthBar;
    private ProgressBar bossHealthBar;
    private ProgressBar specialBar;
    private GameSprite player;
    private GameSprite shield;
    private GameSprite pet;
    private long lastEnemySpawnedTime;
    private Stage stage;
    OrthographicCamera mCamera;
    OrthographicCamera mGuiCamera;
    Rectangle specialBounds;

    FitViewport mViewPort;
    FitViewport mGuiViewPort;
    BitmapFont mFont;
    SpecialButtonActive mSpecialButtonActive;
    SpecialButtonInactive mSpecialButtonInactive;

    Random mRandom;

    boolean specialButtonFlag = false;
    boolean spawnBossFlag = false;
    boolean spawnShieldFlag = false;
    boolean spawnPetFlag = false;
    int mGameState;
    Vector3 touchPos;
    Vector3 dragPos;
    int mScore;
    int mHighScore;
    Preferences mPrefs;
    ParallaxBackground rbg;

    private static final class GameSprite extends Image {

        String name = "";
        Rectangle bounds = new Rectangle();

        private GameSprite(Texture texture) {
            super(texture);
            bounds.setWidth(getWidth());
            bounds.setHeight(getHeight());
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            super.draw(batch, parentAlpha);
            bounds.setPosition(getX(), getY());
        }

        private boolean overlaps(GameSprite sprite) {
            return bounds.overlaps(sprite.bounds);
        }
    }

    public GameScreen(ShootingGame game) {
        mGame = game;
        stage = new Stage(new FitViewport(768,1200));

        assets = new AssetManager();
        assets.load("back_02.pack", TextureAtlas.class);
        assets.finishLoading();

        atlas = assets.get("back_02.pack");


        // カメラ、ViewPortを生成、設定する
        mCamera = new OrthographicCamera();
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT);
        mViewPort = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera);

        // GUI用のカメラを設定する
        mGuiCamera = new OrthographicCamera();
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT);
        mGuiViewPort = new FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera);

        mFont = new BitmapFont(Gdx.files.internal("font.fnt"), false);

        //メンバ変数の初期化
        mRandom = new Random();
        mGameState = GAME_STATE_PLAYING;
        touchPos = new Vector3();
        dragPos = new Vector3();
        mScore = 0;
        mHighScore = 0;
        specialBounds = new Rectangle((float) (CAMERA_WIDTH - 1.8), (float) (CAMERA_HEIGHT - 14.2), (float) 1.5, (float) 1.5);
        lastEnemySpawnedTime = TimeUtils.nanoTime();


        // ハイスコアをPreferencesから取得する
        mPrefs = Gdx.app.getPreferences("jp.techacademy.watanabe.shouta.shootinggame");
        mHighScore = mPrefs.getInteger("HIGHSCORE", 0);

        //背景をスクロールさせる
        rbg = new ParallaxBackground(new ParallaxLayer[]{
                new ParallaxLayer(atlas.findRegion("back_02"), new Vector2(0.0f, 0.9f), new Vector2(0, 0)),},
                576, 1024, new Vector2(0, 100));

        //プレイヤーのHPを表示
        healthBar = new HealthBar(200,30);
        healthBar.setPosition(10, 15);

        //スペシャルゲージの表示
        specialBar = new SpecialBar(200, 30);
        specialBar.setPosition(10, 50);

        if (mGame.mRequestHandler != null) {
            mGame.mRequestHandler.showAds(true);
        }

        createStage();

        stage.addActor(healthBar);
        stage.addActor(specialBar);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mCamera.update();
        mGame.batch.setProjectionMatrix(mCamera.combined);

        rbg.render(Gdx.graphics.getDeltaTime());

        mGame.batch.begin();

        if (specialBar.getValue() < 1f) {
            mSpecialButtonInactive.draw(mGame.batch);
        } else {
            mSpecialButtonActive.draw(mGame.batch);
        }

        // ランダムな間隔(3秒〜5秒)で敵を発生させる
        if (mScore < 100) {
            if (TimeUtils.nanoTime() - lastEnemySpawnedTime > (1000000000 * (long) MathUtils.random(3, 5)))
                spawnEnemy();
        }

        if (mScore >= 100 && spawnBossFlag == false) {
            //ボスのHPを表示
            bossHealthBar = new HealthBar(250, 40);
            bossHealthBar.setPosition(10, 1150);
            stage.addActor(bossHealthBar);
            spawnBoss();
            spawnBossFlag = true;
        }

        mGame.batch.end();

        if (Gdx.input.justTouched()) {
            mCamera.unproject(touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            //スペシャルボタンタップ時
            if (specialBounds.contains(touchPos.x, touchPos.y)) {
                if (specialBar.getValue() == 1f) {
                    specialButtonFlag = true;
                    if (specialButtonFlag == true) {
                        specialBar.addAction
                                (forever(
                                        sequence(
                                                delay(50 / 100.f),
                                                run(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (specialButtonFlag == true) {
                                                            specialBar.setValue(specialBar.getValue() - 0.1f);
                                                        }
                                                    }
                                                })

                                        )

                                ));
                    }
                    slow.play();
                }
                return;
            }

            if (mGame.mRequestHandler != null) {
                mGame.mRequestHandler.showAds(false);
            }
        }

        if (specialButtonFlag == true && specialBar.getValue() == 0f) {
            specialButtonFlag = false;
        }

        // スコア表示
        mGuiCamera.update();
        mGame.batch.setProjectionMatrix(mGuiCamera.combined);
        mGame.batch.begin();
        mFont.draw(mGame.batch, "HighScore: " + mHighScore, 140, 23);
        mFont.draw(mGame.batch, "Score: " + mScore, 140, 48);
        mGame.batch.end();

        stage.draw();
        stage.act(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        mViewPort.update(width, height);
        mGuiViewPort.update(width,height);
    }

    private void createStage() {

        //スペシャルボタンのテクスチャを用意
        Texture specialButtonActive = new Texture("buttonON.png");
        Texture specialButtonInctive = new Texture("buttonOFF.png");

        mSpecialButtonActive = new SpecialButtonActive(specialButtonActive, 0, 0, 128, 116);
        mSpecialButtonActive.setPosition((float) (CAMERA_WIDTH - 1.8), (float) (CAMERA_HEIGHT - 14.2));

        mSpecialButtonInactive = new SpecialButtonInactive(specialButtonInctive, 0, 0, 128, 116);
        mSpecialButtonInactive.setPosition((float) (CAMERA_WIDTH - 1.8), (float) (CAMERA_HEIGHT - 14.2));

        player = new GameSprite(new Texture(Gdx.files.internal("player.png")));
        player.name = "player";
        player.setPosition(stage.getWidth() * 0.5f - player.getWidth() * 0.5f, 120);

        //プレイヤーからビームを発射
        player.addAction
                (forever(
                        sequence(
                                delay(70 / 100.f),
                                run(new Runnable() {
                                    @Override
                                    public void run() {
                                        spawnPlayerBeam(player);
                                    }
                                })
                        )
                ));

        stage.addActor(player);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        bgm.dispose();
        explosion.dispose();
        getshield.dispose();
        slow.dispose();
    }

    private void update(float delta) {
        switch (mGameState) {
            case GAME_STATE_PLAYING:
                updatePlaying(delta, player);
                break;
            case GAME_STATE_GAMEOVER:
                updateGameOver();
                break;
            case GAME_STATE_GAMECLEAR:
                updateGameClear();
                break;
        }
    }

    private void checkGameOver() {
        if (healthBar.getValue() == 0) {
            mGameState = GAME_STATE_GAMEOVER;
        }
    }

    private void checkGameClear() {
        if (bossHealthBar.getValue() == 0) {
            mGameState = GAME_STATE_GAMECLEAR;
        }
    }

    private void updatePlaying(float delta, final GameSprite player) {

         Gdx.app.log("Gdx.input.getY", String.valueOf(Gdx.input.getY()));

        bgm.setLooping(true);
        bgm.setVolume(1.0f);
        bgm.play();

        checkCollision();
        checkItemCollision();
    }

    private void updateGameOver() {
        bgm.stop();
        mGame.setScreen(new GameOverScreen(mGame, mScore));
    }

    private void updateGameClear() {
        bgm.stop();
        mGame.setScreen(new GameClearScreen(mGame, mScore));
    }

    //敵の出現
    private void spawnEnemy() {
        final GameSprite enemy = new GameSprite(new Texture(Gdx.files.internal("enemy.png")));
        enemy.name = "enemy";
        enemy.setX(MathUtils.random(0, CAMERA_WIDTH * 76 - enemy.getWidth()));
        enemy.setY(CAMERA_HEIGHT * 70);
        // 画面を3秒〜6秒の時間で縦に移動するようにアクションを設定する
        // それと同時に、横方向に不規則に動くようにもアクションを設定する
        enemy.addAction(parallel(
                // 縦に移動するためのアクション
                sequence(
                        moveBy(0, -(CAMERA_HEIGHT + enemy.getHeight() * 2), MathUtils.random(3, 6)),
                        removeActor()),
                // 横方向に不規則に動くためのアクション
                forever(
                        sequence(
                                delay(MathUtils.random(50, 100) / 120.f),
                                // 毎回違う量だけ横に動かすためには、runアクション内でmoveByアクションを設定する必要がある
                                run(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (specialButtonFlag == true) {
                                            enemy.addAction(moveBy(MathUtils.random(-100, 100), 0, 3.5f));
                                        } else {
                                            enemy.addAction(moveBy(MathUtils.random(-100, 100), 0, .5f));
                                        }
                                    }
                                })
                        )
                )
        ));
        // 不規則な間隔(1.0秒〜1.5秒)でビームを撃ち続ける
        enemy.addAction(
                forever(
                        sequence(
                                delay(MathUtils.random(100, 150) / 100.f),
                                run(new Runnable() {
                                    @Override
                                    public void run() {
                                        spawnEnemyBeam(enemy);
                                    }
                                })
                        )
                ));

        stage.addActor(enemy);
        lastEnemySpawnedTime = TimeUtils.nanoTime();
    }

    //プレイヤーのビーム
    private void spawnPlayerBeam(GameSprite player) {
        GameSprite beam = new GameSprite(new Texture(Gdx.files.internal("playerbullet.png")));    // ビーム用のアクター(actor)を用意する
        beam.name = "playerbullet";

        // ビーム発射位置
        beam.setPosition(player.getX() + 350 + player.getWidth() * .5f * -beam.getWidth() * .5f, player.getY() + 50 + player.getHeight() * .5f);

        //発射位置から画面上まで1秒で移動
        beam.addAction(
                sequence(
                        moveTo(beam.getX(), beam.getY() + stage.getHeight(), 1.0f),
                        run(new Runnable() {
                            @Override
                            public void run() {
                            }
                        }),
                        removeActor()
                ));
        stage.addActor(beam);
    }

    //ペットのビーム
    private void spawnPetBeam(GameSprite pet) {
        GameSprite beam = new GameSprite(new Texture(Gdx.files.internal("playerbullet.png")));
        beam.name = "playerbullet";

        if (spawnPetFlag == true) {
            beam.setPosition(pet.getX() + 10, pet.getY() + 50);
            beam.addAction(
                    sequence(
                            moveTo(beam.getX(), beam.getY() + stage.getHeight(), .9f),
                            run(new Runnable() {
                                @Override
                                public void run() {
                                }
                            }),
                            removeActor()
                    ));
            stage.addActor(beam);
        }

    }

    //敵のビーム
    private void spawnEnemyBeam(Actor enemy) {
        GameSprite beam = new GameSprite(new Texture(Gdx.files.internal("enemybullet.png")));
        beam.name = "enemybullet";
        beam.setPosition(enemy.getX() + enemy.getWidth() * .5f - beam.getWidth() * 0.5f, enemy.getY());

        if (specialButtonFlag == true) {
            beam.addAction(sequence(
                    moveBy(0, -stage.getHeight(), 4.5f),
                    removeActor()
            ));
        } else {
            beam.addAction(sequence(
                    moveBy(0, -stage.getHeight(), 1.45f),
                    removeActor()
            ));
        }
        stage.addActor(beam);
    }

    //ボスの出現
    private void spawnBoss() {
        final GameSprite boss = new GameSprite(new Texture(Gdx.files.internal("boss.png")));
        boss.name = "boss";
        boss.setX(CAMERA_WIDTH * 40);
        boss.setY(CAMERA_HEIGHT * 70);
        // 画面を3秒〜6秒の時間で縦に移動するようにアクションを設定する
        // それと同時に、横方向に不規則に動くようにもアクションを設定する
        boss.addAction(parallel(
                // 縦に移動するためのアクション
                sequence(
                        moveBy(0, -(CAMERA_HEIGHT + boss.getHeight() * 2), MathUtils.random(3, 6)),
                        moveBy(0, +(CAMERA_HEIGHT + boss.getHeight() * 2), MathUtils.random(3, 6))
                ),
                // 横方向に不規則に動くためのアクション
                forever(
                        sequence(
                                delay(MathUtils.random(50, 100) / 120.f),
                                // 毎回違う量だけ横に動かすためには、runアクション内でmoveByアクションを設定する必要がある
                                run(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (specialButtonFlag == true) {
                                            boss.addAction(moveBy(MathUtils.random(-100, 100), 0, 3.5f));
                                        } else {
                                            boss.addAction(moveBy(MathUtils.random(-100, 100), 0, .5f));
                                        }
                                    }
                                })
                        )

                )

        ));
        // 不規則な間隔(1.0秒〜1.5秒)でビームを撃ち続ける
        boss.addAction(forever(
                sequence(
                        delay(MathUtils.random(100, 150) / 100.f),
                        run(new Runnable() {
                            @Override
                            public void run() {
                                spawnEnemyBeam(boss);
                            }
                        })
                )
        ));
        stage.addActor(boss);
        removeActor();
    }

    //シールドアイテム
    private void spawnShield(GameSprite enemy) {
        GameSprite shield = new GameSprite(new Texture(Gdx.files.internal("shielditem.png")));
        shield.name = "shielditem";
        shield.setX(enemy.getX());
        shield.setY(enemy.getY());
        shield.addAction(moveBy(0, -stage.getHeight(), 3.5f));
        stage.addActor(shield);
        removeActor();
    }

    //プレイヤーにシールド追加
    private void playerSetShield(GameSprite player) {
        shield = new GameSprite(new Texture(Gdx.files.internal("playershield.png")));
        shield.name = "playershield";
        shield.setPosition(player.getX(), player.getY() + 80 + shield.getHeight() * .5f);
        stage.addActor(shield);
        removeActor();
    }

    //ダブルビームアイテム
    private void spawnPetItem(GameSprite enemy) {
        GameSprite petItem = new GameSprite(new Texture(Gdx.files.internal("doublebeam.png")));
        petItem.name = "beamitem";
        petItem.setX(enemy.getX());
        petItem.setY(enemy.getY());
        petItem.addAction(moveBy(0, -stage.getHeight(), 3.5f));
        stage.addActor(petItem);
        removeActor();
    }

    //プレイヤーにペット(ビーム発射付き)追加
    private void playerSetPet(GameSprite player) {
        pet = new GameSprite(new Texture(Gdx.files.internal("pet.png")));
        pet.setPosition(player.getX() + 155, player.getY() + 45);

        if (spawnPetFlag == true) {
            pet.addAction(forever(
                    sequence(
                            delay(100 / 100.f),
                            run(new Runnable() {
                                @Override
                                public void run() {
                                    spawnPetBeam(pet);
                                }
                            })
                    )
            ));
        }

        stage.addActor(pet);
        removeActor();
    }

    private void checkCollision() {
        Array<Actor> actors = stage.getActors();
        for (int i = 0; i < actors.size; i++) {
            Actor actorA = actors.get(i);
            for (int j = i + 1; j < actors.size; j++) {
                Actor actorB = actors.get(j);
                // actorAもactorBもどちらもゲームキャラクター(GameSprite)の場合
                if (actorA instanceof GameSprite && actorB instanceof GameSprite) {
                    GameSprite spriteA = (GameSprite) actorA;
                    GameSprite spriteB = (GameSprite) actorB;
                    List<String> names = Arrays.asList(spriteA.name, spriteB.name);
                    if (spriteA.overlaps(spriteB)) {
                        // プレイヤーが敵または敵のビームに触れた場合
                        if (names.contains("player") && (names.contains("enemy") || names.contains("enemybullet") || names.contains("boss"))) {
                            if (spriteA.name.equals("player") && spriteB.name.equals("enemy")
                                    && spriteA.isVisible() == true && spriteB.isVisible() == true) {
                                healthBar.setValue(healthBar.getValue() - 0.25f);
                                spriteB.setVisible(false);
                                explodeEnemy(spriteB);
                                explosion.play();
                                if (healthBar.getValue() == 0) {
                                    explosion.play();
                                    explodePlayer(spriteA);
                                }
                            } else if (spriteA.name.equals("player") && spriteB.name.equals("enemybullet")
                                    && spriteA.isVisible() == true && spriteB.isVisible() == true) {
                                healthBar.setValue(healthBar.getValue() - 0.25f);
                                spriteB.setVisible(false);
                                if (healthBar.getValue() == 0) {
                                    explosion.play();
                                    explodePlayer(spriteA);
                                }
                            } else if (spriteA.name.equals("player") && spriteB.name.equals("boss") && spriteB.isVisible() == true) {
                                healthBar.setValue(healthBar.getValue() - 0.1f);
                                if (healthBar.getValue() == 0) {
                                    explosion.play();
                                    explodePlayer(spriteA);
                                }
                            }

                            // 敵がビームに触れた場合
                        } else if (names.contains("enemy") && names.contains("playerbullet")) {
                            explosion.play();
                            // spriteAとspriteBのどちらが敵か調べる
                            if (spriteA.name.equals("enemy") && spriteB.isVisible() == true) {
                                explodeEnemy(spriteA);
                                spriteB.setVisible(false);
                            } else {
                                explodeEnemy(spriteB);
                                spriteA.setVisible(false);
                            }

                            //ボスがビームに触れた場合
                        } else if (names.contains("boss") && names.contains("playerbullet")) {
                            if (spriteA.name.equals("boss") && spriteB.isVisible() == true) {
                                if (specialButtonFlag == false) {
                                    specialBar.setValue(specialBar.getValue() + 0.2f);
                                }
                                bossHealthBar.setValue(bossHealthBar.getValue() - 0.05f);
                                spriteB.setVisible(false);
                                if (bossHealthBar.getValue() == 0) {
                                    explosion.play();
                                    explodeBoss(spriteA);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkItemCollision() {
        Array<Actor> actors = stage.getActors();
        for (int i = 0; i < actors.size; i++) {
            Actor actorA = actors.get(i);
            for (int j = i + 1; j < actors.size; j++) {
                Actor actorB = actors.get(j);
                if (actorA instanceof GameSprite && actorB instanceof GameSprite) {
                    GameSprite spriteA = (GameSprite) actorA;
                    GameSprite spriteB = (GameSprite) actorB;
                    List<String> names = Arrays.asList(spriteA.name, spriteB.name);
                    if (spriteA.overlaps(spriteB)) {
                        // プレイヤーがシールドアイテムをゲットした場合
                        if (names.contains("player") && (names.contains("shielditem"))) {
                            if (spriteA.name.equals("player") && spriteB.isVisible() == true) {
                                getshield.play();
                                spawnShieldFlag = true;
                                spriteB.setVisible(false);
                                playerSetShield(player);
                            }
                            //シールドが敵またはビームに触れた場合
                        } else if (names.contains("playershield") && names.contains("enemy") || names.contains("boss") || names.contains("enemybullet")) {
                            if (spriteA.name.equals("playershield") && spriteB.name.equals("enemy") && spriteA.isVisible() == true) {
                                spriteA.setVisible(false);
                                explosion.play();
                                explodeEnemy(spriteB);
                            } else if (spriteA.name.equals("playershield") && spriteB.name.equals("boss") && spriteA.isVisible() == true) {
                                spriteA.setVisible(false);
                                bossHealthBar.setValue(bossHealthBar.getValue() - 0.005f);
                                if (bossHealthBar.getValue() == 0) {
                                    explosion.play();
                                    explodeBoss(spriteB);
                                    spriteB.setVisible(false);
                                }
                            } else if (spriteA.name.equals("playershield") && spriteB.name.equals("enemybullet")
                                    && spriteA.isVisible() == true && spriteB.isVisible() == true) {
                                spriteA.setVisible(false);
                                spriteB.setVisible(false);
                            }
                        }
                        if (names.contains("player") && (names.contains("beamitem"))) {
                            if (spriteA.name.equals("player") && spriteB.isVisible() == true) {
                                getshield.play();
                                spawnPetFlag = true;
                                spriteB.setVisible(false);
                                playerSetPet(player);
                            }
                        }
                    }
                }
            }
        }
    }

    // プレイヤーを爆破させる
    private void explodePlayer(final GameSprite player) {
        Image explosion = new Image(new Texture(Gdx.files.internal("explosion.png")));
        explosion.setPosition(player.getX(), player.getY());
        explosion.setOrigin(explosion.getWidth() * .5f, explosion.getHeight() * .5f);
        Color color = explosion.getColor();
        explosion.setScale(0, 0);
        explosion.setColor(color.r, color.g, color.b, 0.f);
        explosion.addAction(parallel(
                sequence(
                        fadeIn(.2f),
                        delay(.5f),
                        fadeOut(1.5f),
                        removeActor()
                ),
                scaleTo(2.f, 2.f, .2f)
        ));
        stage.addActor(explosion);
        player.remove();
        //爆発が終わった後(1秒後)にゲームオーバーの演出をする
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                checkGameOver();
            }
        }, 1.f);
    }

    // 敵を爆破させる
    private void explodeEnemy(final GameSprite enemy) {
        Image explosion = new Image(new Texture(Gdx.files.internal("explosion.png")));
        explosion.setPosition(enemy.getX(), enemy.getY());
        explosion.setOrigin(explosion.getWidth() * .5f, explosion.getHeight() * .5f);
        Color color = explosion.getColor();
        explosion.setScale(0, 0);
        explosion.setColor(color.r, color.g, color.b, 0.f);
        explosion.addAction(parallel(
                sequence(
                        fadeIn(.2f),
                        delay(.5f),
                        fadeOut(1.5f),
                        removeActor()
                ),
                scaleTo(2.f, 2.f, .2f)
        ));

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (mScore == 20) {
                    spawnShield(enemy);
                } else if (mScore == 40) {
                    spawnPetItem(enemy);
                }
            }
        }, .1f);

        mScore += 10;
        if (specialButtonFlag == false) {
            specialBar.setValue(specialBar.getValue() + 0.25f);
        }
        if (mScore > mHighScore) {
            mHighScore = mScore;
        }
        mPrefs.putInteger("HIGHSCORE", mHighScore); //ハイスコアの保存
        mPrefs.flush();
        stage.addActor(explosion);
        enemy.remove();
    }

    // ボスを爆破させる
    private void explodeBoss(GameSprite boss) {
        Image explosion = new Image(new Texture(Gdx.files.internal("explosion.png")));
        explosion.setPosition(boss.getX() + 50, boss.getY() + 50);
        explosion.setOrigin(explosion.getWidth() * .5f, explosion.getHeight() * .5f);
        Color color = explosion.getColor();
        explosion.setScale(0, 0);
        explosion.setColor(color.r, color.g, color.b, 0.f);
        explosion.addAction(parallel(
                sequence(
                        fadeIn(.2f),
                        delay(.5f),
                        fadeOut(1.5f),
                        removeActor()
                ),
                scaleTo(2.f, 2.f, .2f)
        ));
        stage.addActor(explosion);
        // 爆発が終わった後(1秒後)にゲームクリアの演出をする
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                checkGameClear();
            }
        }, 1.f);

        mScore += 50;
        if (mScore > mHighScore) {
            mHighScore = mScore;
        }
        mPrefs.putInteger("HIGHSCORE", mHighScore);
        mPrefs.flush();
        boss.remove();
    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }
    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float x = touchPos.x;
        float y = touchPos.y;
        touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        mCamera.unproject(touchPos);
        x = touchPos.x - x;
        y = touchPos.y - y;

        player.setPosition(player.getX() + x * 90f, player.getY() + y * 90f);
        if (spawnShieldFlag == true) {
            shield.setPosition(player.getX(), player.getY() + 80 + shield.getHeight() * .5f);
        }
        if (spawnPetFlag == true) {
            pet.setPosition(player.getX() + 150, player.getY() + 40);
        }

        if (player.getX() < 5) player.setX(5);
        if (player.getY() < 120) player.setY(120);
        if (player.getX() > 640) player.setX(640);
        if (player.getY() > 960) player.setY(960);

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
