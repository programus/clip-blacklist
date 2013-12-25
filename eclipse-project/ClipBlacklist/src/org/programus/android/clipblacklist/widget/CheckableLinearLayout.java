package org.programus.android.clipblacklist.widget;

import org.programus.android.clipblacklist.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private int mSpecifiedId = -1;
    private Checkable mCheckableCtrl;
    private boolean mChecked;

    public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.processAttrs(context, attrs);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.processAttrs(context, attrs);
    }

    public CheckableLinearLayout(Context context) {
        super(context);
    }
    
    private void processAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckableLinearLayout);
        this.mSpecifiedId = a.getResourceId(R.styleable.CheckableLinearLayout_checkable_id, -1);
    }

    @Override
    public boolean isChecked() {
        return this.mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        this.mChecked = checked;
        if (this.mCheckableCtrl != null) {
            this.mCheckableCtrl.setChecked(checked);
        }
    }

    @Override
    public void toggle() {
        this.setChecked(!this.isChecked());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mCheckableCtrl = this.getCheckableById(this.mSpecifiedId);
        if (this.mCheckableCtrl == null) {
            this.mCheckableCtrl = this.findCheckableChild(this);
        }
    }
    
    private Checkable getCheckableById(int id) {
        View v = this.findViewById(id);
        return v instanceof Checkable ? (Checkable) v : null;
    }
    
    private Checkable findCheckableChild(ViewGroup vg) {
        int count = vg.getChildCount();
        Checkable ret = null;
        for (int i = 0; i < count; i++) {
            View v = vg.getChildAt(i);
            if (v instanceof Checkable) {
                ret = (Checkable) v;
                break;
            } else if (v instanceof ViewGroup) {
                ret = this.findCheckableChild((ViewGroup)v);
                break;
            }
        }
        return ret;
    }
}
