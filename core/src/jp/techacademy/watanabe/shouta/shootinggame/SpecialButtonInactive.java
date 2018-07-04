package jp.techacademy.watanabe.shouta.shootinggame;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class SpecialButtonInactive extends Sprite{

    public static final float Inactive_WIDTH = 1.5f;
    public static final float Inactive_HEIGHT = 1.5f;

    // 状態
    public static final int NORMAL= 0;
    public static final int NONE = 1;

    int mState;

    public SpecialButtonInactive(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
        super(texture, srcX, srcY, srcWidth, srcHeight);
        setSize(Inactive_WIDTH,Inactive_HEIGHT);
        mState = NORMAL;
    }

    public void Activate() {
        mState = NONE;
        setAlpha(0);
    }
}
