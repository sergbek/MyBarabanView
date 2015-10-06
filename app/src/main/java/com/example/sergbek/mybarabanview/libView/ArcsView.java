package com.example.sergbek.mybarabanview.libView;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.example.sergbek.mybarabanview.R;

import java.util.ArrayList;
import java.util.List;

public class ArcsView extends View {

    public static final int FLING_VELOCITY_DOWNSCALE = 10;
    private int mCenterX;
    private int mCenterY;
    private int mRadius;
    private List<Item> mData = new ArrayList<>();
    private Paint mPiePaint;
    private int mPieRotation;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    private GestureDetector mDetector;
    private ObjectAnimator mAutoCenterAnimator;
    private RectF mPieBounds = new RectF();


    public ArcsView(Context context) {
        super(context);
        init();
    }

    public ArcsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArcsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
        mRadius = radius;
    }

    private void init() {
        mPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPiePaint.setStyle(Paint.Style.FILL);

        setRotation(mPieRotation);

        mAutoCenterAnimator = ObjectAnimator.ofInt(this, "PieRotation", 0);
        mScroller = new Scroller(getContext(), null, true);

        mScrollAnimator = ValueAnimator.ofFloat(0, 1);
        mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tickScrollAnimation();
            }
        });

        mDetector = new GestureDetector(getContext(), new GestureListener(mScroller, mScrollAnimator, mPieRotation));

        mDetector.setIsLongpressEnabled(false);

    }

    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            setPieRotation(mScroller.getCurrY());
        } else
            mScrollAnimator.cancel();
    }

    public void setPieRotation(int rotation) {
        rotation = (rotation % 360 + 360) % 360;
        mPieRotation = rotation;
        setRotation(rotation);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("sometag", "onTouch");
        Log.d("sometag", "x = " + event.getX() + "   y = " + event.getY());
        boolean result = mDetector.onTouchEvent(event);


        return Utils.inCircle(event.getX(), event.getY(), mCenterX, mCenterY, mRadius);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);


        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());


        float ww = (float) w - xpad;
        float hh = (float) h - ypad;

        float diameter = Math.min(ww, hh);
        mPieBounds = new RectF(
                0.0f,
                0.0f,
                diameter,
                diameter);
        mPieBounds.offsetTo(getPaddingLeft(), getPaddingTop());


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;

        mPiePaint.setColor(Color.parseColor("#FF574153"));

        mPieBounds.set(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);

        int[] icons = {R.drawable.home_mbank_1_normal, R.drawable.home_mbank_2_normal,
                R.drawable.home_mbank_3_normal, R.drawable.home_mbank_4_normal,
                R.drawable.home_mbank_5_normal, R.drawable.home_mbank_6_normal};

        int sweepAngle = 360 / icons.length;
        int startAngle = -90 - sweepAngle / 2;

        for (int i = 0; i < icons.length; i++) {
            canvas.drawArc(mPieBounds, startAngle, sweepAngle, true, mPiePaint);


            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), icons[i]);
            canvas.drawBitmap(bitmap, mCenterX - bitmap.getWidth() / 2, mCenterY - (mRadius * 2 / 3) - bitmap.getHeight() / 2, mPiePaint);
            canvas.rotate(sweepAngle, mCenterX, mCenterY);

        }

        double y = mCenterY + (Math.sin(Math.toRadians(270 - sweepAngle / 2)) * mRadius);
        double x = mCenterX + (Math.cos(Math.toRadians(270 - sweepAngle / 2)) * mRadius);

        for (int i = 0; i < icons.length; i++) {
            mPiePaint.setColor(Color.parseColor("#ffed4702"));
            mPiePaint.setStrokeWidth(4f);
            canvas.drawLine(mCenterX, mCenterY, (int) x, (int) y, mPiePaint);

            canvas.rotate(sweepAngle, mCenterX, mCenterY);
        }

    }
}
