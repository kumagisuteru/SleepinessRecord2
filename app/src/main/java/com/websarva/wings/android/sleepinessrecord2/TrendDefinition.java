package com.websarva.wings.android.sleepinessrecord2;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.websarva.wings.android.sleepinessrecord2.R;
public class TrendDefinition {
    protected static final int INCREASE_TREND =1;
    protected static final int STAGNATION_TREND =0;
    protected static final int DECREMENT_TREND =-1;
    protected static final int NO_DATA = 100;
    protected static final double BORDER_OF_TREND = 0.2;


    protected static int showTrend(int trend, ImageView img){
        int drawableId;
        if(trend == NO_DATA){
            img.setVisibility(View.INVISIBLE);
            drawableId= 0;
        }else if(trend == INCREASE_TREND){
            Log.d("increse", String.valueOf(trend));
            img.setVisibility(View.VISIBLE);
            img.setImageDrawable(null);
            img.setImageResource(R.drawable.increse);
            drawableId= R.drawable.increse;
        }else if(trend == STAGNATION_TREND){
            Log.d("stagnant", String.valueOf(trend));
            img.setVisibility(View.VISIBLE);
            img.setImageDrawable(null);
            img.setImageResource(R.drawable.stagnation);
            drawableId= R.drawable.stagnation;
        }else{
            Log.d("decrement", String.valueOf(trend));
            img.setVisibility(View.VISIBLE);
            img.setImageDrawable(null);
            img.setImageResource(R.drawable.decrese);
            drawableId= R.drawable.decrese;
        }
        return drawableId;
    }

    protected static double calculateRateOfChange(Integer[] array, int length){
        double rateofchange=0;
        Log.d("rateofchange", String.valueOf(rateofchange));
        if(length == 1){
            rateofchange = 0;
        }else if(length < 10) {
            for (int i = length - 1; i > 0; i--) {
                Log.d("rateofchange", String.valueOf(rateofchange));
                rateofchange += (double) (array[i] - array[0]) / array[0];
            }
        } else{
            double base = (array[0]+array[1]+array[2])/3.0;
            for(int i = length-1; i>length - 5; i--){
                rateofchange += (double)(array[i]-base)/base;
                Log.d("rateofchange", String.valueOf(rateofchange));
            }
        }
        return rateofchange;
    }

    protected static int judgeTrend(double roc, boolean flag){
        int trend;
        if(!flag){
            trend = NO_DATA;
        }else if(roc>BORDER_OF_TREND){
            trend = INCREASE_TREND;
        }else if(roc< BORDER_OF_TREND*(-1)){
            trend = DECREMENT_TREND;
        }else{
            trend = STAGNATION_TREND;
        }
        return trend;
    }

}

