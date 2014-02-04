package org.programus.android.clipblacklist.widget;

import org.programus.android.clipblacklist.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * A checkable layout to hold checkable widgets.
 * @author programus
 *
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    private final static int[] STATE_CHECKED_SET = {android.R.attr.state_checked};
    private int mSpecifiedId = -1;
    private Checkable mCheckableCtrl;
    private boolean mChecked;
    private boolean mAutoSearch;

    /**
     * Constructor.
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CheckableRelativeLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        this.addStatesFromChildren();
        this.processAttrs(context, attrs);
    }

    /**
     * Constructor.
     * @param context
     * @param attrs
     */
    public CheckableRelativeLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.addStatesFromChildren();
        this.processAttrs(context, attrs);
    }

    /**
     * Constructor.
     * @param context
     */
    public CheckableRelativeLayout(final Context context) {
        super(context);
        this.addStatesFromChildren();
    }
    
    private void processAttrs(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CheckableRelativeLayout);
        this.mSpecifiedId = a.getResourceId(R.styleable.CheckableRelativeLayout_checkable_id, -1);
        this.mChecked = a.getBoolean(R.styleable.CheckableRelativeLayout_checked, false);
        this.mAutoSearch = a.getBoolean(R.styleable.CheckableRelativeLayout_auto_search_checkable, false);
        a.recycle();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = null;
        if (this.isChecked()) {
            drawableState = super.onCreateDrawableState(extraSpace + 1);
            mergeDrawableStates(drawableState, STATE_CHECKED_SET);
        } else {
            drawableState = super.onCreateDrawableState(extraSpace);
        }
        
        return drawableState;
    }

    @Override
    public boolean isChecked() {
        return this.mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (this.mChecked != checked) {
            this.mChecked = checked;
            this.refreshDrawableState();
            if (this.mCheckableCtrl != null) {
                this.mCheckableCtrl.setChecked(checked);
            }
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
        if (this.mCheckableCtrl == null && this.mAutoSearch) {
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
