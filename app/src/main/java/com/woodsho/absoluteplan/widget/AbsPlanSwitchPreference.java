package com.woodsho.absoluteplan.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.SwitchPreference;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.woodsho.absoluteplan.R;

/**
 * Created by hewuzhao on 17/12/21.
 */

public class AbsPlanSwitchPreference extends SwitchPreference {
    public static final String TAG = "AbsPlanSwitchPreference";

    private int mIcon;
    private String mTitle;
    private String mSummary;
    private SwitchCompat mSwitchCompat;

    public AbsPlanSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public AbsPlanSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public AbsPlanSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.settings_preference);
        mIcon = array.getResourceId(R.styleable.settings_preference_image, 0);
        mTitle = array.getString(R.styleable.settings_preference_title);
        mSummary = array.getString(R.styleable.settings_preference_summary);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);
        return LayoutInflater.from(getContext()).inflate(R.layout.item_settings_switchpreference_layout, parent, false);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newValue = !mSwitchCompat.isChecked();
                mSwitchCompat.setChecked(newValue);
            }
        });
        TextView title = (TextView) view.findViewById(R.id.settings_switchpreference_title);
        mSwitchCompat = (SwitchCompat) view.findViewById(R.id.settings_switchpreference_switch);
        mSwitchCompat.setChecked(true);
        title.setText(mTitle);
        TextView summary = (TextView) view.findViewById(R.id.settings_switchpreference_summary);
        summary.setText(mSummary);
    }
}
