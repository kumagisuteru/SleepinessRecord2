package com.websarva.wings.android.sleepinessrecord2;

import android.app.Activity;
import android.content.Context;

/*******************
 *
 * MainActivityのcontextとactivityを保持するクラス
 * Activityをextendsしていないクラス内メソッドの引数等で
 * アクティビティのcontextやactivityを求められたときに使う
 * staticなのでどこからでも利用可能
 *
 */

public class ContextAct {
     private static Activity mActivity;
     private static Context mContext;

    public ContextAct(Activity activity, Context context){
        mActivity = activity;
        mContext = context;
    }
    public static Activity getmActivity(){
        return mActivity;
    }
    public static Context getmContext(){
        return mContext;
    }
}
