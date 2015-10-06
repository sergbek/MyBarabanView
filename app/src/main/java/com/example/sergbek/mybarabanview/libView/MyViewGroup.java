package com.example.sergbek.mybarabanview.libView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.example.sergbek.mybarabanview.R;


public class MyViewGroup extends FrameLayout {
    private int mRadius;


    public MyViewGroup(Context context) {
        super(context);
        init();
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);

    }

    public MyViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init() {
        MainCircle mainCircle = new MainCircle(getContext());
        addView(mainCircle);

        ArcsView arcsView = new ArcsView(getContext());
        addView(arcsView);

        CentralCircle centralCircle = new CentralCircle(getContext());
        addView(centralCircle);

    }

    private void init(AttributeSet attrs) {

        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MyViewGroup,
                0, 0);
        try {
            this.mRadius = typedArray.getInt(R.styleable.MyViewGroup_radius, 200);
        } finally {
            typedArray.recycle();
        }

        MainCircle mainCircle = new MainCircle(getContext());
        mainCircle.setRadius(mRadius);
        addView(mainCircle);

        ArcsView arcsView = new ArcsView(getContext());
        arcsView.setRadius(mRadius);
        addView(arcsView);

        CentralCircle centralCircle = new CentralCircle(getContext());
        centralCircle.setRadius(mRadius);
        addView(centralCircle);
    }
}
