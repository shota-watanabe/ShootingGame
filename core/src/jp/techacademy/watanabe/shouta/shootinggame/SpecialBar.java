package jp.techacademy.watanabe.shouta.shootinggame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class SpecialBar extends ProgressBar {

    public SpecialBar(int width, int height) {
        super(0f, 1f, 0.001f, false, new ProgressBarStyle(getColoredDrawable(width, height, Color.WHITE), getColoredDrawable(0, height, Color.SKY)));
        getStyle().knobBefore = getColoredDrawable(width, height, Color.SKY);

        setWidth(width);
        setHeight(height);

        setAnimateDuration(0.0f);
        setValue(0.0f);

        setAnimateDuration(0.2f);
    }

    private static Drawable getColoredDrawable(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();

        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));

        pixmap.dispose();

        return drawable;
    }
}