package com.example.sergbek.mybarabanview.libView;


import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Scroller;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private RectF mPieBounds = new RectF();

    private Scroller mScroller;
    private ValueAnimator mValueAnimator;
    private int mPieRotation;

    public GestureListener(Scroller scroller, ValueAnimator valueAnimator, int pieRotation) {
        mScroller = scroller;
        mValueAnimator = valueAnimator;
        mPieRotation = pieRotation;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float scrollTheta = Utils.vectorToScalarScroll(
                velocityX,
                velocityY,
                e2.getX() - mPieBounds.centerX(),
                e2.getY() - mPieBounds.centerY());
        mScroller.fling(
                0,
                mPieRotation,
                0,
                (int) scrollTheta / ArcsView.FLING_VELOCITY_DOWNSCALE,
                0,
                0,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE);

        mValueAnimator.setDuration(mScroller.getDuration());
        mValueAnimator.start();
        return true;
    }
}
