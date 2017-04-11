package de.tum.androidcontroller.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import de.tum.androidcontroller.sensors.SensorDataSettings;
import de.tum.androidcontroller.utils.MathUtils;

/**
 * Created by konstantin on 08/04/17.
 */

public class SteeringWheelView extends android.support.v7.widget.AppCompatImageView {

    private static final String TAG = "SteeringWheelView";

    //used for preventing from multiple allocations and initializations
    private volatile Paint mPaint = new Paint();
    private volatile Bitmap mBitmap;
    private volatile Rect mRect;
    private volatile Canvas mCanvas;

    public SteeringWheelView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);

//        //get the parameters at the beginning
//        int[] attrsArray = new int[] {
//                android.R.attr.layout_width, // 0
//                android.R.attr.layout_height, // 1
//                android.R.attr.rotation // 2
//        };
//        TypedArray ta = ctx.obtainStyledAttributes(attrs, attrsArray);
//
//        layout_width = ta.getDimensionPixelSize(0, ViewGroup.LayoutParams.MATCH_PARENT);
//        layout_height = ta.getDimensionPixelSize(1, ViewGroup.LayoutParams.MATCH_PARENT);
//        rotation = Float.valueOf(ta.getString(2));
//        Log.e(TAG, "SteeringWheelView: rotation "+rotation);
    }



    //works!
    public void drawSomething1_2(){
        Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = getBasicPaint(Color.GREEN);
        Rect r = new Rect(0,getHeight()/2-50,getWidth(),getHeight()/2);
        canvas.drawRect(r,paint);
        this.setImageBitmap(bitmap);
    }

    public void drawAccelerationBrake(float accelerometerX){
        //call only if its initialized TODO NOT WORKING
        if(this.getRootView()!=null){
            mBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);

            //acceleration
            if(accelerometerX < SensorDataSettings.idleAccelerationBreakState){
                mPaint = getBasicPaint(SensorDataSettings.COLOR_ACCELERATION);
                int rectTop = getRectAccelerationValue(accelerometerX);
                mRect = new Rect(0,rectTop,getWidth(),getHeight()/2);
            }else{  //breaking
                mPaint = getBasicPaint(SensorDataSettings.COLOR_BREAKING);
                int rectBottom = getRectBrakingValue(accelerometerX);
                mRect = new Rect(0,getHeight()/2,getWidth(),rectBottom);
            }
            //draw it
            mCanvas.drawRect(mRect,mPaint);
            this.setImageBitmap(mBitmap);
        }
    }

    public void drawLeftRight(float accelerometerY){
        //call only if its initialized TODO NOT WORKING
        if(this.getRootView()!=null){
            mBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);

            //(acceleration == )left
            if(accelerometerY < SensorDataSettings.idleLeftRightState){
                mPaint = getBasicPaint(SensorDataSettings.COLOR_LEFT);
                int rectBottom = getRectTurningLeftValue(accelerometerY);
                mRect = new Rect(0,getHeight()/2,getWidth(),getHeight()/2+rectBottom);
            }else{  //(breaking == )right
                mPaint = getBasicPaint(SensorDataSettings.COLOR_RIGHT);
                int rectTop = getRectTurningRightValue(accelerometerY);
                mRect = new Rect(0,rectTop,getWidth(),getHeight()/2);
            }
            //draw it
            mCanvas.drawRect(mRect,mPaint);
            this.setImageBitmap(mBitmap);
        }
    }


    /**
     * This method is used for mapping the top component of the Rect
     * for drawing the canvas on the bitmap. 0 top component means full
     * acceleration and vice versa.
     * @param accelerometerX the X axis from the accelerometer
     * @return the mapped top rect value from the accelerometer
     */
    private int getRectAccelerationValue(float accelerometerX){
        if(accelerometerX <= SensorDataSettings.idleAccelerationBreakState - SensorDataSettings.maximumAccelerationBreakDeviation){
            return 0; //maximum acceleration
        }
        else if(accelerometerX >= SensorDataSettings.idleAccelerationBreakState)
            return getHeight(); //no acceleration
        else{ //somewhere in between
            //Log.e(TAG, "getRectAccelerationValue: getPercantege is "+getPercantage(accelerometerX,SensorDataSettings.maximumAccelerationBreakDeviation,SensorDataSettings.idleAccelerationBreakState));
            return (int) (getHeight()/2 * getPercentageAcceleratingSteeringLeft(accelerometerX,SensorDataSettings.maximumAccelerationBreakDeviation,SensorDataSettings.idleAccelerationBreakState)); //TODO
        }
    }

    /**
     * This method is used for mapping the top component of the Rect
     * for drawing the canvas on the bitmap.
     * @param accelerometerX the X axis from the rotation vector sensor
     * @return the mapped bottom rect value from the rotation vector sensor
     */
    private int getRectBrakingValue(float accelerometerX){
        if(accelerometerX > SensorDataSettings.idleAccelerationBreakState + SensorDataSettings.maximumAccelerationBreakDeviation){
            return getHeight(); //maximum breaking
        }
        else if(accelerometerX <= SensorDataSettings.idleAccelerationBreakState)
            return getHeight()/2; //no breaking
        else{ //somewhere in between
            //Log.e(TAG, "getRectAccelerationValue: getPercantege is "+getPercentageBreaking(accelerometerX,SensorDataSettings.maximumAccelerationBreakDeviation,SensorDataSettings.idleAccelerationBreakState));
            return getHeight()/2 + (int) (getHeight()/2 * (1.f - getPercentageBreakingSteeringRight(accelerometerX,SensorDataSettings.maximumAccelerationBreakDeviation,SensorDataSettings.idleAccelerationBreakState))); //TODO
        }
    }
    /**
     * This method is used for mapping the top component of the Rect
     * for drawing the canvas on the bitmap. getHeight top component means
     * max turning left and vice versa.
     * @param accelerometerY the Y axis from the accelerometer
     * @return the mapped bottom rect value from the accelerometer
     */
    private int getRectTurningLeftValue(float accelerometerY){
        if(accelerometerY <= SensorDataSettings.idleLeftRightState - SensorDataSettings.maximumLeftRightDeviation){
            return getHeight(); //maximum turning left
        }
        else if(accelerometerY >= SensorDataSettings.idleLeftRightState)
            return getHeight()/2; //no turning left
        else{ //somewhere in between
            //Log.e(TAG, "getRectAccelerationValue: getPercantege is "+getPercantage(accelerometerX,SensorDataSettings.maximumAccelerationBreakDeviation,SensorDataSettings.idleAccelerationBreakState));
            return (int) (getHeight()/2 * (1.f - getPercentageAcceleratingSteeringLeft(accelerometerY,
                    SensorDataSettings.maximumLeftRightDeviation,
                    SensorDataSettings.idleLeftRightState)));
        }
    }

    /**
     * This method is used for mapping the top component of the Rect
     * for drawing the canvas on the bitmap. 0 top component means
     * max turning left and vice versa.
     * @param accelerometerY the Y axis from the accelerometer
     * @return the mapped bottom rect value from the accelerometer
     */
    private int getRectTurningRightValue(float accelerometerY){
        if(accelerometerY >= SensorDataSettings.idleLeftRightState + SensorDataSettings.maximumLeftRightDeviation){
            return 0; //maximum turning right
        }
        else if(accelerometerY <= SensorDataSettings.idleLeftRightState)
            return getHeight()/2; //no turning right
        else{ //somewhere in between
            //Log.e(TAG, "getRectAccelerationValue: getPercantege is "+getPercantage(accelerometerX,SensorDataSettings.maximumAccelerationBreakDeviation,SensorDataSettings.idleAccelerationBreakState));
            return (int) (getHeight()/2 * (getPercentageBreakingSteeringRight(accelerometerY,
                    SensorDataSettings.maximumLeftRightDeviation,
                    SensorDataSettings.idleLeftRightState)));
        }
    }

    /**
     *
     */
    private float getPercentageAcceleratingSteeringLeft2(float sensorValue, float itsMaxDeviation, float itsIdleState){
        float absDiff = Math.abs(itsMaxDeviation - (sensorValue < 0 ? -sensorValue:sensorValue) );
        float maxDiff = Math.abs(itsIdleState - itsMaxDeviation);
        //Log.e(TAG, "getPercantage: sensorValue " + sensorValue + " absDiff "+ absDiff+" maxDiff " +maxDiff);
        return absDiff/maxDiff;

    }
    private float getPercentageAcceleratingSteeringLeft(float sensorValue, float itsMaxDeviation, float itsIdleState){
        float absDiff = MathUtils.getAbsDistance(sensorValue,itsMaxDeviation);
        float maxDiff = Math.abs(itsIdleState - itsMaxDeviation);
        //Log.e(TAG, "getPercantage: sensorValue " + sensorValue + " absDiff "+ absDiff+" maxDiff " +maxDiff);
        return absDiff/maxDiff;

    }

    private float getPercentageBreakingSteeringRight(float sensorValue, float itsMaxDeviation, float itsIdleState){
        float absDiff = Math.abs((sensorValue < 0 ? -sensorValue:sensorValue) - itsMaxDeviation);
        float maxDiff = Math.abs(itsIdleState - itsMaxDeviation);
        //Log.e(TAG, "getPercantage: sensorValue " + sensorValue + " absDiff "+ absDiff+" maxDiff " +maxDiff);
        return absDiff/maxDiff;

    }


    private Paint getBasicPaint(int color){

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(0);
        mPaint.setColor(color);
        return mPaint;
    }
}
