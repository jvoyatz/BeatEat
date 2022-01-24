package com.jvoyatz.beatit.ui.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import androidx.core.content.ContextCompat;

import com.jvoyatz.beatit.R;


/**
 * custom view
 * Found here:
 * https://stackoverflow.com/questions/37741872/how-to-make-custom-dotted-progress-bar-in-android
 */
public class HorizontalDottedProgress extends View {

    private final Paint paint = new Paint();

    //Bounced Dot Radius
    private final int mBounceDotRadius = 8;

    //to get identified in which position dot has to bounce
    private int mDotPosition;

    //specify how many dots you need in a progressbar
    private final int mDotAmount = 10;

    public HorizontalDottedProgress(Context context) {
        super(context);
    }

    public HorizontalDottedProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalDottedProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //Method to draw your customized dot on the canvas
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //set the color for the dot that you want to draw
        paint.setColor(ContextCompat.getColor(getContext(), R.color.purple_500));
        //function to create dot
        createDot(canvas,paint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //Animation called when attaching to the window, i.e to your screen
        startAnimation();
    }

    private void createDot(Canvas canvas, Paint paint) {
        //here i have setted progress bar with 10 dots , so repeat and wnen i = mDotPosition  then increase the radius of dot i.e mBounceDotRadius
        for(int i = 0; i < mDotAmount; i++ ){
            if(i == mDotPosition){
                canvas.drawCircle(10+(i*20), mBounceDotRadius, mBounceDotRadius, paint);
            }else {
                //actual dot radius
                int mDotRadius = 5;
                canvas.drawCircle(10+(i*20), mBounceDotRadius, mDotRadius, paint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width;
        int height;

        //calculate the view width
        width = (20*9);
        height = (mBounceDotRadius*2);

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    private void startAnimation() {
        BounceAnimation bounceAnimation = new BounceAnimation();
        bounceAnimation.setDuration(100);
        bounceAnimation.setRepeatCount(Animation.INFINITE);
        bounceAnimation.setInterpolator(new LinearInterpolator());
        bounceAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) { }

            @Override
            public void onAnimationRepeat(Animation animation) {
                mDotPosition++;
                //when mDotPosition == mDotAmount , then startTasks again applying animation from 0th positon , i.e  mDotPosition = 0;
                if (mDotPosition == mDotAmount) {
                    mDotPosition = 0;
                }
            }
        });
        startAnimation(bounceAnimation);
    }

    private class BounceAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            //call invalidate to redraw your view again.
            invalidate();
        }
    }
}
