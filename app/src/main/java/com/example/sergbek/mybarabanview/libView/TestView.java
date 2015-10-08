package com.example.sergbek.mybarabanview.libView;


import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.example.sergbek.mybarabanview.R;

import java.util.ArrayList;
import java.util.List;

public class TestView extends ViewGroup {

    private int mCenterX;
    private int mCenterY;
    private int mRadius;

    private List<Item> mData;

    private int mBarabanRotation;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    private GestureDetector mDetector;
    private RectF mPieBounds;

    private BarabanView mBarabanView;
    private int mStrokeWidth;
    private int mCurrentTargetAngle;
    private int mColor;

    private ObjectAnimator mAutoCenterAnimator;

    private int mCurrentItem;

    public static final int DEG_CIRCLE = 360;

    public static final int FLING_VELOCITY_DOWNSCALE = 4;

    public static final int AUTOCENTER_ANIM_DURATION = 550;

    public TestView(Context context) {
        super(context);
        init();
    }

    public TestView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MyViewGroup,
                0, 0);
        try {
            this.mRadius = typedArray.getInt(R.styleable.MyViewGroup_radius, 0);
            this.mStrokeWidth = typedArray.getInt(R.styleable.MyViewGroup_strokeWidth, 5);
            this.mColor = typedArray.getColor(R.styleable.MyViewGroup_colorArc, 0xFF574153);
        } finally {
            typedArray.recycle();
        }

        init();
    }


    public int getBarabanRotation() {
        return mBarabanRotation;
    }

    public void setBarabanRotation(int rotation) {
        rotation = (rotation % DEG_CIRCLE + DEG_CIRCLE) % DEG_CIRCLE;
        mBarabanRotation = rotation;
        mBarabanView.rotateTo(rotation);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);

        return Utils.inCircle(event.getX(), event.getY(), mCenterX, mCenterY, mRadius);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mBarabanView.layout(l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int measuredWidth = measureWidth(widthMeasureSpec);
        if (mRadius == 0) {
            mRadius = measuredWidth / 2;
        }
        int measuredHeight = measureHeight(heightMeasureSpec);
        if (measuredHeight < measuredWidth)
            mRadius = measuredHeight / 2;
        mRadius -= mStrokeWidth;
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 0;
        if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 0;
        if (specMode == MeasureSpec.AT_MOST) {
            result = mRadius * 2;
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
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
                0, 0,
                diameter,
                diameter);
        mPieBounds.offsetTo(getPaddingLeft(), getPaddingTop());
    }

    private void init() {

        mData = new ArrayList<>();

        setData();

        mBarabanView = new BarabanView(getContext());
        addView(mBarabanView);
        mBarabanView.rotateTo(mBarabanRotation);

        mScroller = new Scroller(getContext(), null, true);

        mScrollAnimator = ValueAnimator.ofFloat(0, 1);
        mScrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tickScrollAnimation();
            }
        });

        mAutoCenterAnimator = ObjectAnimator.ofInt(TestView.this, "BarabanRotation", 0);

        mDetector = new GestureDetector(TestView.this.getContext(), new GestureListener());

        mDetector.setIsLongpressEnabled(false);
    }

    private void setData() {
        int[] icons = {R.drawable.home_mbank_1_normal, R.drawable.home_mbank_2_normal,
                R.drawable.home_mbank_3_normal};

        int sweepAngle = DEG_CIRCLE / icons.length;
        int startAngle = 270 - sweepAngle / 2;

        for (int i = 0; i < icons.length; i++) {
            Item item = new Item();
            item.setID(i);
            item.setPhoto(icons[i]);

            item.setStartAngle(startAngle);

            if (startAngle + sweepAngle >= DEG_CIRCLE) {
                startAngle = (startAngle + sweepAngle) - DEG_CIRCLE;
                item.setEndAngle(startAngle);
            } else {
                item.setEndAngle(startAngle + sweepAngle);
                startAngle += sweepAngle;
            }

            item.setColor(mColor);


            mData.add(item);

        }
    }

    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            setBarabanRotation(mScroller.getCurrY());
        } else {
            mScrollAnimator.cancel();
        }
    }


    private class BarabanView extends View {

        private Paint mMainCircle;
        private Paint mCentralCircle;
        private Paint mArc;

        public BarabanView(Context context) {
            super(context);
            init();
        }

        private void init() {
            mMainCircle = new Paint();

            mMainCircle.setShader(new LinearGradient(0, 100, 100, 0, Color.parseColor("#FFF04C08"),
                    Color.parseColor("#FFE18D68"), Shader.TileMode.MIRROR));

            mCentralCircle = new Paint();
            mArc = new Paint();

            mAutoCenterAnimator = ObjectAnimator.ofInt(TestView.this, "PieRotation", 0);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            mCenterX = getWidth() / 2;
            mCenterY = getHeight() / 2;

            drawMainCircle(canvas);

            drawArc(canvas);

            drawCentralCircle(canvas);

        }

        private void drawMainCircle(Canvas canvas) {
            mMainCircle.setColor(Color.parseColor("#FF574153"));
            mMainCircle.setAntiAlias(true);
            canvas.drawCircle(mCenterX, mCenterY, mRadius + mStrokeWidth, mMainCircle);
        }

        private void drawArc(Canvas canvas) {

            mPieBounds.set(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);

            int sweepAngle = DEG_CIRCLE / mData.size();
            int startAngle = 270 - sweepAngle / 2;

            for (int i = 0; i < mData.size(); i++) {
                Item item = mData.get(i);

                mArc.setColor(item.getColor());
                mArc.setAntiAlias(true);
                canvas.drawArc(mPieBounds, startAngle, sweepAngle, true, mArc);

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), item.getPhoto());
                canvas.drawBitmap(bitmap, mCenterX - bitmap.getWidth() / 2, mCenterY -
                        (mRadius * 2 / 3) - bitmap.getHeight() / 2, mArc);
                canvas.rotate(sweepAngle, mCenterX, mCenterY);

            }

            double y = mCenterY + (Math.sin(Math.toRadians(270 - sweepAngle / 2)) * mRadius);
            double x = mCenterX + (Math.cos(Math.toRadians(270 - sweepAngle / 2)) * mRadius);

            for (int i = 0; i < mData.size(); i++) {
                mArc.setColor(Color.parseColor("#ffed4702"));
                mArc.setStrokeWidth(4f);
                canvas.drawLine(mCenterX, mCenterY, (int) x, (int) y, mArc);

                canvas.rotate(sweepAngle, mCenterX, mCenterY);
            }
        }

        private void drawCentralCircle(Canvas canvas) {
            mCentralCircle.setColor(Color.parseColor("#FF01E98C"));
            mCentralCircle.setAntiAlias(true);
            canvas.drawCircle(mCenterX, mCenterY, mRadius / 7, mCentralCircle);

            mCentralCircle.setColor(Color.parseColor("#FFED4702"));
            mCentralCircle.setAntiAlias(true);
            canvas.drawCircle(mCenterX, mCenterY, mRadius / 11, mCentralCircle);
        }

        public void rotateTo(float pieRotation) {
            setRotation(pieRotation);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            float scrollTheta = Utils.vectorToScalarScroll(
                    distanceX,
                    distanceY,
                    e2.getX() - mPieBounds.centerX(),
                    e2.getY() - mPieBounds.centerY());
            setBarabanRotation(getBarabanRotation() - (int) scrollTheta / FLING_VELOCITY_DOWNSCALE);

            return true;
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
                    getBarabanRotation(),
                    0,
                    (int) scrollTheta / FLING_VELOCITY_DOWNSCALE,
                    0,
                    0,
                    Integer.MIN_VALUE,
                    Integer.MAX_VALUE);

            mScrollAnimator.setDuration(mScroller.getDuration());
            mScrollAnimator.start();

            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            float xPosition = event.getX();
            float yPosition = event.getY();

            float y = mCenterY - yPosition;
            float x = mCenterX - xPosition;

            double angle = Math.toDegrees(Math.atan2(y, x)) - 180;
            if (angle < 0) {
                angle += DEG_CIRCLE;
            }

            angle = (angle + DEG_CIRCLE - getBarabanRotation()) % DEG_CIRCLE;

            for (int i = 0; i < mData.size(); i++) {
                Item item = mData.get(i);
                item.setColor(mColor);
                if (angle > item.getStartAngle() && angle < item.getEndAngle()) {
                    item.setColor(0xDD77DD77);
                    setCurrentItem(item.getID());

                } else if ((angle > item.getStartAngle() || angle < item.getEndAngle())
                        && item.getStartAngle() + DEG_CIRCLE / mData.size() >= DEG_CIRCLE) {

                    item.setColor(0xDD77DD77);
                    setCurrentItem(item.getID());
                }
            }
            mBarabanView.invalidate();

            return true;
        }
    }

    public void setCurrentItem(int currentItem) {
        setCurrentItem(currentItem, true);
    }

    private void setCurrentItem(int currentItem, boolean scrollIntoView) {
        mCurrentItem = currentItem;
        if (scrollIntoView) {
            centerOnCurrentItem();
        }
        invalidate();
    }

    private void centerOnCurrentItem() {
        Item item = mData.get(getCurrentItem());
        int targetAngle = ((DEG_CIRCLE / mData.size()) / 2 + DEG_CIRCLE / mData.size()) - item.getEndAngle();
        targetAngle -= DEG_CIRCLE / mData.size();
        if (targetAngle != mCurrentTargetAngle || targetAngle + mCurrentTargetAngle == 0) {
            mAutoCenterAnimator.setIntValues(targetAngle - 90);
            mAutoCenterAnimator.setDuration(AUTOCENTER_ANIM_DURATION).start();
            mCurrentTargetAngle = targetAngle;
        }
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }
}
