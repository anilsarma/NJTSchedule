package com.smartdeviceny.njts.lib;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.TypedValue;

import java.util.ArrayList;

public class ExpandableFabButtons {
    final FloatingActionButton master;
    ArrayList<FloatingActionButton> slave = new ArrayList<>();
    ArrayList<FloatingActionButton> hidden = new ArrayList<>();
    boolean expanded;
    Context context;
    int resid_expanded;
    int resid_closed;

    public ExpandableFabButtons(Context context, FloatingActionButton master, int resid_closed, int resid_expanded) {
        this.master = master;
        expanded = true;
        this.context = context;

        this.resid_closed = resid_closed;
        this.resid_expanded = resid_expanded;
    }

    public void addFloatingActionButton(FloatingActionButton button) {
        slave.add(button);
    }

    public void addHiddenFloatingActionButton(FloatingActionButton button) {
        hidden.add(button);
    }

    public void toggle() {
        show(!expanded);
    }

    public void show(boolean state) {
//        if( expanded ==state ) {
//            return;
//        }
        expanded = state;

        if(expanded) {
            master.setImageDrawable(context.getResources().getDrawable(resid_closed));

            float y = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, context.getResources().getDisplayMetrics());
            for(FloatingActionButton fb:slave) {
                fb.animate().translationZ(0);
                fb.animate().translationY(-y).setInterpolator(new FastOutSlowInInterpolator());
            }

        } else {
            master.setImageDrawable(context.getResources().getDrawable(resid_expanded));

            float z = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, context.getResources().getDisplayMetrics());
            float y = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 55, context.getResources().getDisplayMetrics());
            for(FloatingActionButton fb:slave) {
                fb.animate().translationZ(-z);
                fb.animate().translationY(-y).setInterpolator(new FastOutSlowInInterpolator());
            }
            for(FloatingActionButton fb:hidden) {
                fb.animate().translationZ(-z);
                fb.animate().translationY(-y).setInterpolator(new FastOutSlowInInterpolator());
            }
        }
    }


}
