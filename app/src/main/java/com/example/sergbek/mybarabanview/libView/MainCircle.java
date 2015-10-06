package com.example.sergbek.mybarabanview.libView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;


public class MainCircle extends View {

    private int mRadius;
    private Paint mPaint;

    public MainCircle(Context context) {
        super(context);
        init();
    }

    public MainCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public MainCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();


    }

    private void init() {
        mPaint = new Paint();
        mPaint.setShader(new LinearGradient(0, 100, 100, 0, Color.parseColor("#FFF04C08"),
                Color.parseColor("#FFE18D68"), Shader.TileMode.MIRROR));
    }


    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
        mRadius = radius;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int mCenterX = getWidth() / 2;
        int mCenterY = getHeight() / 2;

        mPaint.setColor(Color.parseColor("#FF574153"));
        mPaint.setAntiAlias(true);
        canvas.drawCircle(mCenterX, mCenterY, mRadius + 5, mPaint);
    }

}
