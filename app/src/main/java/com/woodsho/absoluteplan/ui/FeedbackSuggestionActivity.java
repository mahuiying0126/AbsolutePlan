package com.woodsho.absoluteplan.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.woodsho.absoluteplan.R;
import com.woodsho.absoluteplan.utils.CommonUtil;
import com.woodsho.absoluteplan.utils.StatusBarUtil;

public class FeedbackSuggestionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_suggestion);
        SlidrConfig mConfig = new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .velocityThreshold(2400)
                .distanceThreshold(.25f)
                .edge(true)
                .touchSize(CommonUtil.dp2px(this, 32))
                .build();
        Slidr.attach(this, mConfig);
        setupActionBar();
        StatusBarUtil statusBarUtil = new StatusBarUtil(this);
        statusBarUtil.setColorBarForDrawer(ContextCompat.getColor(this, R.color.colorPrimary));
        init();
    }

    private void setupActionBar() {
        ViewGroup rootView = (ViewGroup) findViewById(R.id.action_bar_root); //id from appcompat

        if (rootView != null) {
            View view = getLayoutInflater().inflate(R.layout.feedback_suggestion_toolbar_layout, rootView, false);
            rootView.addView(view, 0);

            Toolbar toolbar = (Toolbar) findViewById(R.id.feedback_suggestion_toolbar);
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    //通过反射获取状态栏高度，默认25dp
    private int getStatusBarHeight() {
        int statusBarHeight = CommonUtil.dp2px(this, 25);
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusBarHeight = getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    private void init() {
        Resources res = getResources();
        CardView qqCadView = (CardView) findViewById(R.id.qq_cardview);
        qqCadView.setCardBackgroundColor(res.getColor(R.color.white));
        CardView emailCardView = (CardView) findViewById(R.id.email_cardview);
        emailCardView.setCardBackgroundColor(res.getColor(R.color.white));
    }
}