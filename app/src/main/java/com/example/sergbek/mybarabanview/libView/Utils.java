package com.example.sergbek.mybarabanview.libView;


public class Utils {

    public static float vectorToScalarScroll(float dx, float dy, float x, float y) {
        float l = (float) Math.sqrt(dx * dx + dy * dy);


        float crossX = -y;
        float crossY = x;

        float dot = (crossX * dx + crossY * dy);
        float sign = Math.signum(dot);

        return l * sign;
    }

    public static boolean inCircle(float x, float y, float circleCenterX,
                                   float circleCenterY, float circleRadius) {
        double dx = Math.pow(x - circleCenterX, 2);
        double dy = Math.pow(y - circleCenterY, 2);


        return (dx + dy) < Math.pow(circleRadius, 2);
    }
}
