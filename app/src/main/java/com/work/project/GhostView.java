package com.work.project;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.StaticLayout;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class GhostView extends androidx.appcompat.widget.AppCompatImageView {
    static final float MAX_TEXT_WIDTH = 0.35f, TEXT_X = 0.756f, TEXT_Y = 0.239f;
    String text;
    float textLength;
    Paint paint;
    StaticLayout staticLayout;

    public GhostView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.GhostView);
        text = attributes.getString(R.styleable.GhostView_ghost_text);

        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        paint.setTextSize(100);
        textLength = paint.measureText(text) / 100;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setTextSize(getHeight() * MAX_TEXT_WIDTH / textLength);
        float x = TEXT_X * getWidth();
        float y = TEXT_Y * getHeight() - (paint.descent() + paint.ascent()) / 2;
        canvas.drawText(text, x, y, paint);
    }
}
