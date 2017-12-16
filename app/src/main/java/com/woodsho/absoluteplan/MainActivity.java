package com.woodsho.absoluteplan;

import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.hubert.library.Controller;
import com.app.hubert.library.HighLight;
import com.app.hubert.library.NewbieGuide;
import com.app.hubert.library.OnGuideChangedListener;
import com.woodsho.absoluteplan.adapter.SideAdapter;
import com.woodsho.absoluteplan.bean.PlanTask;
import com.woodsho.absoluteplan.bean.SideItem;
import com.woodsho.absoluteplan.common.AbsPSharedPreference;
import com.woodsho.absoluteplan.data.CachePlanTaskStore;
import com.woodsho.absoluteplan.ui.AllFragment;
import com.woodsho.absoluteplan.ui.CalendarFragment;
import com.woodsho.absoluteplan.ui.FinishedFragment;
import com.woodsho.absoluteplan.ui.PlanTaskDetailsActivity;
import com.woodsho.absoluteplan.ui.SettingsActivity;
import com.woodsho.absoluteplan.ui.TodayFragment;
import com.woodsho.absoluteplan.ui.TomorrowFragment;
import com.woodsho.absoluteplan.utils.CommonUtil;
import com.woodsho.absoluteplan.utils.StatusBarUtil;
import com.woodsho.absoluteplan.widget.CenteredImageSpan;
import com.woodsho.absoluteplan.widget.SideNavigationView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SideAdapter.OnSideItemClickListener, CachePlanTaskStore.OnPlanTaskChangedListener {
    public static final String TAG = "MainActivity";

    public DrawerLayout mDrawerLayout;
    public FrameLayout mContentLayout;
    public FrameLayout mSideLayout;
    public TextView mToolbarTitle;
    public TextView mToolbarSubTitleYear;
    public TextView mToolbarSubTitleDay;
    public TextView mToolbarToToday;

    public RecyclerView mSideRecyclerView;
    public TextView mSettingBt;
    public TextView mSearchBt;
    public SideNavigationView mSideNavigationView;

    public TodayFragment mTodayFragment;
    public AllFragment mAllFragment;
    public TomorrowFragment mTomorrowFragment;
    public FinishedFragment mFinishedFragment;
    public CalendarFragment mCalendarFragment;
    public SideAdapter mSideAdapter;

    public static final int ID_TODAY = 0;
    public static final int ID_TOMORROW = 1;
    public static final int ID_CALENDAR = 2;
    public static final int ID_ALL = 3;
    public static final int ID_FINISHED = 4;

    public static final String TAG_CALENDAR_FRAGMENT = "tag_calendar_fragment";
    public static final String TAG_TODAY_FRAGMENT = "tag_today_fragment";
    public static final String TAG_ALL_FRAGMENT = "tag_all_fragment";
    public static final String TAG_TOMORROW_FRAGMENT = "tag_tomorrow_fragment";
    public static final String TAG_FINISHED_FRAGMENT = "tag_finished_fragment";

    public static final String KEY_GUIDE_BUILD = "guide_build";
    public static final String KEY_GUIDE_SIDE = "guide_side";

    public static final int MSG_CLOSE_DRAWER = 0;

    public int mLastSelectedSideId;
    public List<SideItem> mSideItemList;

    private int mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay;

    public UIHandler mUIHandler;
    public FloatingActionButton mFloatActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CachePlanTaskStore.getInstance().addOnPlanTaskChangedListener(this);
        StatusBarUtil statusbar = new StatusBarUtil(this);
        statusbar.setColorBarForDrawer(ContextCompat.getColor(this, R.color.colorPrimary));

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                showGuideBuild();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        mContentLayout = (FrameLayout) findViewById(R.id.content_frame_layout);
        mSideLayout = (FrameLayout) findViewById(R.id.side_frame_layout);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbarSubTitleYear = (TextView) findViewById(R.id.toolbar_sub_title_year);
        mToolbarSubTitleDay = (TextView) findViewById(R.id.toolbar_sub_title_day);
        mToolbarToToday = (TextView) findViewById(R.id.toolbar_to_today);
        mToolbarToToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastSelectedSideId == ID_CALENDAR) {
                    if (mCalendarFragment != null && mCalendarFragment.isAdded() && mCalendarFragment.isVisible()) {
                        mCalendarFragment.JumpToToday();
                    }
                }
            }
        });
        mFloatActionButton = (FloatingActionButton) findViewById(R.id.main_float_action_button);
        mFloatActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlanTaskDetailsActivity.class);
                intent.putExtra(PlanTaskDetailsActivity.KEY_SHOW_TYPE, PlanTaskDetailsActivity.TYPE_NEW_BUILD);
                startActivity(intent);
            }
        });
        mSideNavigationView = (SideNavigationView) findViewById(R.id.toolbar_slide_navigation_view);
        mSideNavigationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(mSideLayout);
            }
        });

        mUIHandler = new UIHandler(this);
        getLastSelectedSideId();
        initSideView();
        changeMainView(mLastSelectedSideId);
        showGuideSide();
    }

    public void getLastSelectedSideId() {
        mLastSelectedSideId = AbsPSharedPreference.getInstanc().getLastSelectedSideId(ID_TODAY);
    }

    public void initSideView() {
        View view = View.inflate(this, R.layout.side_layout, null);
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AbsolutePlanApplication.sAppContext, "开发中，敬请期待！", Toast.LENGTH_SHORT).show();
            }
        });
        final RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.side_layout_relativelayout);
        Drawable wallpaperDrawable = CommonUtil.getWallpaperDrawable();
        if (wallpaperDrawable != null) {
            relativeLayout.setBackground(wallpaperDrawable);
        } else {
            relativeLayout.setBackgroundResource(R.drawable.common_bg);
        }
        final LinearLayout bottomLayout = (LinearLayout) view.findViewById(R.id.bottom_side_layout);
        mSideRecyclerView = (RecyclerView) view.findViewById(R.id.side_layout_recyclerview);
        mSideItemList = getAllSideItems();
        mSideAdapter = new SideAdapter(AbsolutePlanApplication.sAppContext, mSideItemList, mLastSelectedSideId);
        mSideAdapter.setOnSideItemClickListener(this);
        mSideRecyclerView.setAdapter(mSideAdapter);
        mSideRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mSideRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int pos = parent.getChildAdapterPosition(view);
                //if (pos == 3) {
                outRect.bottom = 20;
                //}
            }
        });

        ViewTreeObserver vto = relativeLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 保证只调用一次
                relativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // 组件生成cache（组件显示内容）
                relativeLayout.buildDrawingCache();
                // 得到组件显示内容
                Bitmap bitmap = CommonUtil.drawableToBitmap(relativeLayout.getBackground());
                // 局部模糊处理
                CommonUtil.blur(AbsolutePlanApplication.sAppContext, bitmap, bottomLayout, 18);
            }
        });

        mSettingBt = (TextView) view.findViewById(R.id.setting_side_layout);
        mSettingBt.setText(createStringWithLeftPicture(R.drawable.ic_side_setting, "  设置"));
        mSettingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                mUIHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.closeDrawer(mSideLayout);
                    }
                }, 200);
            }
        });
        mSearchBt = (TextView) view.findViewById(R.id.search_side_layout);
        mSearchBt.setText(createStringWithLeftPicture(R.drawable.ic_side_search, "  搜索"));
        mSearchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AbsolutePlanApplication.sAppContext, "开发中，敬请期待！", Toast.LENGTH_SHORT).show();
            }
        });
        mSideLayout.addView(view);
    }

    public SpannableString createStringWithLeftPicture(int drawableId, String str) {
        Resources res = getResources();
        String replacedStr = "image";
        final SpannableString spannableString = new SpannableString(replacedStr + str);
        Drawable drawable = res.getDrawable(drawableId);
        drawable.setBounds(0, 0, 50, 50);
        CenteredImageSpan span = new CenteredImageSpan(drawable);
        spannableString.setSpan(span, 0, replacedStr.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private List<SideItem> getAllSideItems() {
        List<SideItem> sideItemList = new ArrayList<>();
        int allCount = 0;
        int tomorrowCount = 0;
        int todayCount = 0;
        int finishedCount = 0;
        CachePlanTaskStore planTaskStore = CachePlanTaskStore.getInstance();
        if (planTaskStore.isPlanTaskInitializedFinished()) {
            allCount = planTaskStore.getCachePlanTaskList().size();
            if (allCount > 0) {
                todayCount = CommonUtil.getTodayPlanTaskList().size();
                tomorrowCount = CommonUtil.getTomorrowPlanTaskList().size();
                finishedCount = CommonUtil.getFinishedPlanTaskList().size();
            }
        }

        sideItemList.add(new SideItem(ID_TODAY, R.drawable.ic_side_today, "今天", todayCount));
        sideItemList.add(new SideItem(ID_TOMORROW, R.drawable.ic_side_tomorrow, "明天", tomorrowCount));
        sideItemList.add(new SideItem(ID_CALENDAR, R.drawable.ic_side_calendar, "日历", 0));
        sideItemList.add(new SideItem(ID_ALL, R.drawable.ic_side_all, "所有", allCount));
        sideItemList.add(new SideItem(ID_FINISHED, R.drawable.ic_side_finished, "已完成", finishedCount));

        return sideItemList;
    }

    public void changeMainView(int id) {
        SideItem sideItem = null;
        for (int i = 0; i < mSideItemList.size(); i++) {
            if (mSideItemList.get(i).id == id) {
                sideItem = mSideItemList.get(i);
                break;
            }
        }
        if (sideItem == null) {
            Log.e(TAG, "error, id: " + id + ", can not find");
            return;
        }

        mLastSelectedSideId = id;
        if (id == ID_CALENDAR) {
            mToolbarTitle.setText(mCurrentSelectMonth + "月");
            mToolbarSubTitleDay.setText(mCurrentSelectDay + "日");
            mToolbarSubTitleYear.setVisibility(View.VISIBLE);
            mToolbarSubTitleDay.setVisibility(View.VISIBLE);
            if (CommonUtil.isToday(mCurrentSelectYear, mCurrentSelectMonth, mCurrentSelectDay)) {
                mToolbarToToday.setVisibility(View.GONE);
            } else {
                mToolbarToToday.setVisibility(View.VISIBLE);
            }
        } else {
            mToolbarTitle.setText(sideItem.title);
            mToolbarSubTitleYear.setVisibility(View.GONE);
            mToolbarSubTitleDay.setVisibility(View.GONE);
            mToolbarToToday.setVisibility(View.GONE);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentById(R.id.content_frame_layout);
        if (fragment != null && !fragment.isVisible()) {
            List<Fragment> fragmentList = fragmentManager.getFragments();
            if (fragmentList != null && fragmentList.size() > 0) {
                for (Fragment fg : fragmentList) {
                    if (fg != null && fg.isVisible()) {
                        fragment = fg;
                        break;
                    }
                }
            }
        }

        switch (id) {
            case ID_ALL:
                if (!(fragment instanceof AllFragment)) {
                    if (fragment != null) {
                        fragmentTransaction.hide(fragment);
                    }
                    if (mAllFragment == null) {
                        mAllFragment = new AllFragment();
                    }
                    Fragment allFragment = fragmentManager.findFragmentByTag(TAG_ALL_FRAGMENT);
                    if (allFragment != mAllFragment) {
                        if (allFragment != null) {
                            fragmentTransaction.remove(allFragment);
                        }
                        fragmentTransaction.add(R.id.content_frame_layout, mAllFragment, TAG_ALL_FRAGMENT);
                    }
                    fragmentTransaction.show(mAllFragment);
                    fragmentTransaction.commitAllowingStateLoss();
                }
                break;
            case ID_TODAY:
                if (!(fragment instanceof TodayFragment)) {
                    if (fragment != null) {
                        fragmentTransaction.hide(fragment);
                    }
                    if (mTodayFragment == null) {
                        mTodayFragment = new TodayFragment();
                    }
                    Fragment todayFragment = fragmentManager.findFragmentByTag(TAG_TODAY_FRAGMENT);
                    if (todayFragment != mTodayFragment) {
                        if (todayFragment != null) {
                            fragmentTransaction.remove(todayFragment);
                        }
                        fragmentTransaction.add(R.id.content_frame_layout, mTodayFragment, TAG_TODAY_FRAGMENT);
                    }
                    fragmentTransaction.show(mTodayFragment);
                    fragmentTransaction.commitAllowingStateLoss();
                }
                break;
            case ID_TOMORROW:
                if (!(fragment instanceof TomorrowFragment)) {
                    if (fragment != null) {
                        fragmentTransaction.hide(fragment);
                    }
                    if (mTomorrowFragment == null) {
                        mTomorrowFragment = new TomorrowFragment();
                    }
                    Fragment tomorrowFragment = fragmentManager.findFragmentByTag(TAG_TOMORROW_FRAGMENT);
                    if (tomorrowFragment != mTomorrowFragment) {
                        if (tomorrowFragment != null) {
                            fragmentTransaction.remove(tomorrowFragment);
                        }
                        fragmentTransaction.add(R.id.content_frame_layout, mTomorrowFragment, TAG_TOMORROW_FRAGMENT);
                    }
                    fragmentTransaction.show(mTomorrowFragment);
                    fragmentTransaction.commitAllowingStateLoss();
                }
                break;
            case ID_CALENDAR:
                if (!(fragment instanceof CalendarFragment)) {
                    if (fragment != null) {
                        fragmentTransaction.hide(fragment);
                    }
                    if (mCalendarFragment == null) {
                        mCalendarFragment = new CalendarFragment();
                    }
                    Fragment calendarFragment = fragmentManager.findFragmentByTag(TAG_CALENDAR_FRAGMENT);
                    if (calendarFragment != mCalendarFragment) {
                        if (calendarFragment != null) {
                            fragmentTransaction.remove(calendarFragment);
                        }
                        fragmentTransaction.add(R.id.content_frame_layout, mCalendarFragment, TAG_CALENDAR_FRAGMENT);
                    }
                    fragmentTransaction.show(mCalendarFragment);
                    fragmentTransaction.commitAllowingStateLoss();
                }
                break;
            case ID_FINISHED:
                if (!(fragment instanceof FinishedFragment)) {
                    if (fragment != null) {
                        fragmentTransaction.hide(fragment);
                    }
                    if (mFinishedFragment == null) {
                        mFinishedFragment = new FinishedFragment();
                    }

                    Fragment finishedFragment = fragmentManager.findFragmentByTag(TAG_FINISHED_FRAGMENT);
                    if (finishedFragment != mFinishedFragment) {
                        if (finishedFragment != null) {
                            fragmentTransaction.remove(finishedFragment);
                        }
                        fragmentTransaction.add(R.id.content_frame_layout, mFinishedFragment, TAG_FINISHED_FRAGMENT);
                    }
                    fragmentTransaction.show(mFinishedFragment);
                    fragmentTransaction.commitAllowingStateLoss();
                }
                break;
        }
        mUIHandler.removeMessages(MSG_CLOSE_DRAWER);
        mUIHandler.sendEmptyMessageDelayed(MSG_CLOSE_DRAWER, 50);
    }

    protected Uri getLocalUri(int resId) {
        StringBuilder strBuilder = new StringBuilder("res://");
        strBuilder.append(AbsolutePlanApplication.sAppContext.getPackageName());
        strBuilder.append("/");
        strBuilder.append(resId);
        return Uri.parse(strBuilder.toString());
    }

    private static class UIHandler extends Handler {
        private WeakReference<MainActivity> mWRef;

        public UIHandler(MainActivity activity) {
            mWRef = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity mainActivity = mWRef.get();
            if (mainActivity == null) {
                return;
            }
            switch (msg.arg1) {
                case MSG_CLOSE_DRAWER:
                    if (mainActivity.mDrawerLayout != null) {
                        mainActivity.mDrawerLayout.closeDrawer(mainActivity.mSideLayout);
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AbsPSharedPreference.getInstanc().saveLastSelectedSideId(mLastSelectedSideId);
    }

    @Override
    public void onSideItemClick(SideItem sideItem) {
        changeMainView(sideItem.id);
    }

    @Override
    public void onPlanTaskChanged() {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                CachePlanTaskStore planTaskStore = CachePlanTaskStore.getInstance();
                List<PlanTask> allTask = planTaskStore.getCachePlanTaskList();
                updateSideItemOfAll(allTask.size());

                updateSideItemOfToday(CommonUtil.getTodayPlanTaskList().size());
                updateSideItemOfTomorrow(CommonUtil.getTomorrowPlanTaskList().size());
                updateSideItemOfFinished(CommonUtil.getFinishedPlanTaskList().size());
            }
        });
    }

    public void updateSideItemOfAll(int count) {
        if (mSideItemList == null || mSideAdapter == null)
            return;

        SideItem item = mSideItemList.get(ID_ALL);
        item.count = count;
        mSideItemList.set(ID_ALL, item);
        mSideAdapter.notifyItemChanged(ID_ALL, "pos: " + ID_ALL);
    }

    public void updateSideItemOfToday(int count) {
        if (mSideItemList == null || mSideAdapter == null)
            return;

        SideItem item = mSideItemList.get(ID_TODAY);
        item.count = count;
        mSideItemList.set(ID_TODAY, item);
        mSideAdapter.notifyItemChanged(ID_TODAY, "pos: " + ID_TODAY);
    }

    public void updateSideItemOfTomorrow(int count) {
        if (mSideItemList == null || mSideAdapter == null)
            return;

        SideItem item = mSideItemList.get(ID_TOMORROW);
        item.count = count;
        mSideItemList.set(ID_TOMORROW, item);
        mSideAdapter.notifyItemChanged(ID_TOMORROW, "pos: " + ID_TOMORROW);
    }

    public void updateSideItemOfFinished(int count) {
        if (mSideItemList == null || mSideAdapter == null)
            return;

        SideItem item = mSideItemList.get(ID_FINISHED);
        item.count = count;
        mSideItemList.set(ID_FINISHED, item);
        mSideAdapter.notifyItemChanged(ID_FINISHED, "pos: " + ID_FINISHED);
    }

    public void updateToolbarDate(int year, int month, int day) {
        mToolbarTitle.setVisibility(View.VISIBLE);
        mToolbarToToday.setText(String.valueOf(CommonUtil.getToday()));
        if (CommonUtil.isToday(year, month, day)) {
            mToolbarToToday.setVisibility(View.GONE);
        } else {
            mToolbarToToday.setVisibility(View.VISIBLE);
        }
        mToolbarSubTitleYear.setVisibility(View.VISIBLE);
        mToolbarSubTitleDay.setVisibility(View.VISIBLE);
        mToolbarSubTitleDay.setText(day + "日");
        mToolbarTitle.setText(month + "月");
        mToolbarSubTitleYear.setText(year + "年");
        setCurrentSelectDate(year, month, day);
    }

    private void setCurrentSelectDate(int year, int month, int day) {
        mCurrentSelectYear = year;
        mCurrentSelectMonth = month;
        mCurrentSelectDay = day;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSideAdapter != null) {
            mSideAdapter.removeOnSideItemClickListener();
        }
        CachePlanTaskStore.getInstance().removePlanTaskChangedListener(this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mSideLayout)) {
            mDrawerLayout.closeDrawer(mSideLayout);
            return;
        }

        //只显示一次启动页（ App 没被 kill 的情况下）
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    private void showGuideBuild() {
        Controller controller = NewbieGuide.with(this)
                .setOnGuideChangedListener(new OnGuideChangedListener() {
                    @Override
                    public void onShowed(Controller controller) {
                        //when guide layer display
                    }

                    @Override
                    public void onRemoved(Controller controller) {
                        startActivity(new Intent(MainActivity.this, PlanTaskDetailsActivity.class));
                    }
                })
                .setBackgroundColor(getResources().getColor(R.color.guide_bg_color))
                .setEveryWhereCancelable(true)
                .setLayoutRes(R.layout.guide_build_view_layout)
                .alwaysShow(false)
                .addHighLight(mFloatActionButton, HighLight.Type.CIRCLE)
                .setLabel(KEY_GUIDE_BUILD)
                .build();
        controller.show();
    }

    private void showGuideSide() {
        Controller controller = NewbieGuide.with(this)
                .setOnGuideChangedListener(new OnGuideChangedListener() {
                    @Override
                    public void onShowed(Controller controller) {
                        //when guide layer display
                    }

                    @Override
                    public void onRemoved(Controller controller) {
                        mDrawerLayout.openDrawer(mSideLayout);
                    }
                })
                .setBackgroundColor(getResources().getColor(R.color.guide_bg_color))
                .setEveryWhereCancelable(true)
                .setLayoutRes(R.layout.guide_side_view_layout)
                .alwaysShow(false)
                .addHighLight(mSideNavigationView, HighLight.Type.CIRCLE)
                .setLabel(KEY_GUIDE_SIDE)
                .build();
        controller.show();
    }
}
