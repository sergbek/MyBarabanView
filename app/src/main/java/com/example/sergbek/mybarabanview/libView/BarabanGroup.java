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
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.example.sergbek.mybarabanview.R;

import java.util.ArrayList;
import java.util.List;

public class BarabanGroup extends ViewGroup {

    private int mCenterX;
    private int mCenterY;
    private int mRadius;

    private List<Item> mData;

    private int mBarabanRotation;
    private Scroller mScroller;
    private ValueAnimator mScrollAnimator;
    private GestureDetector mDetector;
    private RectF mBarabanBounds;

    private BarabanView mBarabanView;
    private int mStrokeWidth;
    private int mCurrentTargetAngle;
    private int mColorArc;
    private int mColorLine;

    private ObjectAnimator mAutoCenterAnimator;

    private int mCurrentItem;

    public static final int DEG_CIRCLE = 360;
    public static final int FLING_VELOCITY_DOWNSCALE = 4;
    public static final int AUTOCENTER_ANIM_DURATION = 350;

    public BarabanGroup(Context context) {
        super(context);
        init();
    }

    public BarabanGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MyViewGroup,
                0, 0);
        try {
            this.mRadius = typedArray.getInt(R.styleable.MyViewGroup_radius, 0);
            this.mStrokeWidth = typedArray.getInt(R.styleable.MyViewGroup_strokeWidth, 5);
            this.mColorArc = typedArray.getColor(R.styleable.MyViewGroup_colorArc, 0xFF574153);
            this.mColorLine = typedArray.getColor(R.styleable.MyViewGroup_colorArc, 0xFFED4702);
        } finally {
            typedArray.recycle();
        }
        init();
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

        mAutoCenterAnimator = ObjectAnimator.ofInt(BarabanGroup.this, "BarabanRotation", 0);
        mDetector = new GestureDetector(BarabanGroup.this.getContext(), new GestureListener());
        mDetector.setIsLongpressEnabled(false);
    }

    public void setData() {

        int[] icons = {R.drawable.home_mbank_1_normal, R.drawable.home_mbank_2_normal,
                R.drawable.home_mbank_3_normal, R.drawable.home_mbank_4_normal
                , R.drawable.home_mbank_5_normal
                , R.drawable.home_mbank_6_normal};

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
            item.setColor(mColorArc);
            mData.add(item);
        }
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
        mBarabanBounds = new RectF(
                0, 0,
                diameter,
                diameter);
        mBarabanBounds.offsetTo(getPaddingLeft(), getPaddingTop());
    }


    private void tickScrollAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            setBarabanRotation(mScroller.getCurrY());
        } else {
            mScrollAnimator.cancel();
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


    private void moveItemUp(int startAngle, int endAngle) {
        int itemCenterAngle = ((startAngle + endAngle) / 2) +
                getBarabanRotation();

        itemCenterAngle %= 360;

        if (itemCenterAngle != 270) {
            int rotateAngle;
            if (itemCenterAngle > 270 || itemCenterAngle < 90) {
                rotateAngle = (270 - 360 - itemCenterAngle) % 360;
            } else {
                rotateAngle = (270 + 360 - itemCenterAngle) % 360;
            }

            mAutoCenterAnimator.setIntValues(getBarabanRotation(), rotateAngle +
                    getBarabanRotation());
            mAutoCenterAnimator.setDuration(AUTOCENTER_ANIM_DURATION).start();
        }

        invalidate();
    }

    public int getCurrentItem() {
        return mCurrentItem;
    }

    public int getBarabanRotation() {
        return mBarabanRotation;
    }

    public void setBarabanRotation(int rotation) {
        rotation = (rotation % DEG_CIRCLE + DEG_CIRCLE) % DEG_CIRCLE;
        mBarabanRotation = rotation;
        mBarabanView.rotateTo(rotation);

    }


    private class BarabanView extends View {

        private Paint mPMainCircle;
        private Paint mPCentralCircle;
        private Paint mPArc;

        public BarabanView(Context context) {
            super(context);
            init();
        }

        private void init() {
            mPMainCircle = new Paint();
            mPMainCircle.setShader(new LinearGradient(0, 100, 100, 0, Color.parseColor("#FFF04C08"),
                    Color.parseColor("#FFE18D68"), Shader.TileMode.MIRROR));

            mPCentralCircle = new Paint();
            mPArc = new Paint();

            mAutoCenterAnimator = ObjectAnimator.ofInt(BarabanGroup.this, "BarabanRotation", 0);
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
            mPMainCircle.setColor(0xFF574153);
            mPMainCircle.setAntiAlias(true);
            canvas.drawCircle(mCenterX, mCenterY, mRadius + mStrokeWidth, mPMainCircle);
        }

        private void drawArc(Canvas canvas) {

            mBarabanBounds.set(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);

            float sweepAngle = (float) DEG_CIRCLE / mData.size();
            float startAngle = 270 - sweepAngle / 2;

            for (int i = 0; i < mData.size(); i++) {
                Item item = mData.get(i);

                mPArc.setColor(item.getColor());
                mPArc.setAntiAlias(true);
                canvas.drawArc(mBarabanBounds, startAngle, sweepAngle, true, mPArc);

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), item.getPhoto());
                canvas.drawBitmap(bitmap, mCenterX - bitmap.getWidth() / 2, mCenterY -
                        (mRadius * 2 / 3) - bitmap.getHeight() / 2, mPArc);
                canvas.rotate(sweepAngle, mCenterX, mCenterY);

            }

            double y = mCenterY + (Math.sin(Math.toRadians(270 - sweepAngle / 2)) * mRadius);
            double x = mCenterX + (Math.cos(Math.toRadians(270 - sweepAngle / 2)) * mRadius);

            for (int i = 0; i < mData.size(); i++) {
                mPArc.setColor(mColorLine);
                mPArc.setStrokeWidth(4f);
                canvas.drawLine(mCenterX, mCenterY, (int) x, (int) y, mPArc);

                canvas.rotate(sweepAngle, mCenterX, mCenterY);
            }
        }


        private void drawCentralCircle(Canvas canvas) {
            mPCentralCircle.setColor(0xFF01E98C);
            mPCentralCircle.setAntiAlias(true);
            canvas.drawCircle(mCenterX, mCenterY, mRadius / 7, mPCentralCircle);

            mPCentralCircle.setColor(0xFFED4702);
            mPCentralCircle.setAntiAlias(true);
            canvas.drawCircle(mCenterX, mCenterY, mRadius / 11, mPCentralCircle);
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
                    e2.getX() - mBarabanBounds.centerX(),
                    e2.getY() - mBarabanBounds.centerY());
            setBarabanRotation(getBarabanRotation() - (int) scrollTheta / FLING_VELOCITY_DOWNSCALE);

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            float scrollTheta = Utils.vectorToScalarScroll(
                    velocityX,
                    velocityY,
                    e2.getX() - mBarabanBounds.centerX(),
                    e2.getY() - mBarabanBounds.centerY());
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
                item.setColor(mColorArc);
//                mColorLine = 0xDD77DD77;
                if (angle > item.getStartAngle() && angle < item.getEndAngle()) {
                    item.setColor(0xDD77DD77);
//                    setCurrentItem(item.getID());
                    moveItemUp(item.getStartAngle(), item.getEndAngle());

                } else if ((angle > item.getStartAngle() || angle < item.getEndAngle())
                        && item.getStartAngle() + DEG_CIRCLE / mData.size() >= DEG_CIRCLE) {

                    item.setColor(0xDD77DD77);
//                    setCurrentItem(item.getID());
                    moveItemUp(item.getStartAngle(), item.getEndAngle());
                }
            }
            mBarabanView.invalidate();

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mAutoCenterAnimator.cancel();

            return true;
        }
    }
}
