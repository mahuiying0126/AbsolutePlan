package com.woodsho.absoluteplan.data;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.woodsho.absoluteplan.AbsolutePlanApplication;
import com.woodsho.absoluteplan.bean.PlanTask;
import com.woodsho.absoluteplan.common.PlanTaskState;
import com.woodsho.absoluteplan.service.UserActionService;
import com.woodsho.absoluteplan.ui.AbsPlanWidgetProvider;
import com.woodsho.absoluteplan.ui.AbsPlanWidgetProvider4x3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hewuzhao on 17/12/9.
 */

public class CachePlanTaskStore {
    public static final String TAG = "CachePlanTaskStore";

    private volatile static CachePlanTaskStore sCachePlanTaskStore;
    private List<PlanTask> mCachePlanTaskList;
    private boolean mIsPlanTaskInitFinished;
    private final Object mCachePlanTaskStoreLock = new Object();
    private HashSet<OnPlanTaskChangedListener> mOnPlanTaskChangedListeners;

    public interface OnPlanTaskChangedListener {
        void onPlanTaskChanged();
    }

    public static CachePlanTaskStore getInstance() {
        if (sCachePlanTaskStore == null) {
            synchronized (CachePlanTaskStore.class) {
                if (sCachePlanTaskStore == null) {
                    sCachePlanTaskStore = new CachePlanTaskStore();
                }
            }
        }
        return sCachePlanTaskStore;
    }

    public static void initialize(Context context) {
        Log.d(TAG, "initialize , start intent service: UserActionService");
        Intent intent = new Intent(context, UserActionService.class);
        intent.setAction(UserActionService.INTENT_ACTION_CACHEPLANTASK);
        context.startService(intent);
    }

    public CachePlanTaskStore() {
        mCachePlanTaskList = new ArrayList<>();
        mOnPlanTaskChangedListeners = new HashSet<>();
    }

    public void setCachePlanTaskList(List<PlanTask> list, boolean needNotify) {
        synchronized (mCachePlanTaskStoreLock) {
            reset();
            mCachePlanTaskList = list;
            Log.d(TAG, "plantask init finished");
            mIsPlanTaskInitFinished = true;
            if (needNotify) {
                notifyPlanTaskChanged();
            }
        }
    }

    private void notifyAppWidgetWhenDataChanged() {
        if (AbsolutePlanApplication.isAppInitialized()) {
            Log.w(TAG, "app is not initialized");
            AbsolutePlanApplication.checkAppInitializedBlock();
        }
        Context context = AbsolutePlanApplication.sAppContext;
        updateWidget(context);
    }

    //发送广播，使appWidget更新
    private void updateWidget(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName provider = new ComponentName(context, AbsPlanWidgetProvider.class);
        ComponentName provideer_4x3 = new ComponentName(context, AbsPlanWidgetProvider4x3.class);
        int[] ids_4x3 = manager.getAppWidgetIds(provideer_4x3);
        int length_4x3 = ids_4x3.length;
        int[] ids = manager.getAppWidgetIds(provider);
        int length = ids.length;
        int[] allIds;
        if (length_4x3 <= 0) {
            allIds = ids;
        } else if (length <= 0) {
            allIds = ids_4x3;
        } else {
            allIds = concat(ids, ids_4x3);
        }
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allIds);
        context.sendBroadcast(intent);
    }

    private int[] concat(int[] first, int[] second) {
        int firstLen = first.length;
        int secondLen = second.length;
        int[] result = Arrays.copyOf(first, firstLen + secondLen);
        System.arraycopy(second, 0, result, firstLen, secondLen);
        return result;
    }

    public List<PlanTask> getCachePlanTaskList() {
        synchronized (mCachePlanTaskStoreLock) {
            List<PlanTask> planTaskList = new ArrayList<>();
            if (mCachePlanTaskList == null || mCachePlanTaskList.size() <= 0)
                return planTaskList;

            return mCachePlanTaskList;
        }
    }

    public List<PlanTask> getCacheNormalPlanTaskList() {
        synchronized (mCachePlanTaskStoreLock) {
            List<PlanTask> planTaskList = new ArrayList<>();
            if (mCachePlanTaskList == null || mCachePlanTaskList.size() <= 0)
                return planTaskList;

            for (PlanTask task : mCachePlanTaskList) {
                if (task.state == PlanTaskState.STATE_NORMAL) {
                    planTaskList.add(task);
                }
            }
            return planTaskList;
        }
    }

    public void addPlanTask(PlanTask task, boolean needNotify) {
        synchronized (mCachePlanTaskStoreLock) {
            if (mCachePlanTaskList == null)
                return;

            if (mCachePlanTaskList.size() > 0) {
                Iterator iterator = mCachePlanTaskList.iterator();
                while (iterator.hasNext()) {
                    PlanTask planTask = (PlanTask) iterator.next();
                    if (planTask.id.equals(task.id)) {
                        mCachePlanTaskList.remove(planTask);
                        break;
                    }
                }
            }
            mCachePlanTaskList.add(task);
            if (needNotify) {
                notifyPlanTaskChanged();
            }
        }
    }

    public void removePlanTask(PlanTask task, boolean needNotify) {
        synchronized (mCachePlanTaskStoreLock) {
            if (mCachePlanTaskList == null || mCachePlanTaskList.size() <= 0)
                return;

            Iterator<PlanTask> iterator = mCachePlanTaskList.iterator();
            while (iterator.hasNext()) {
                PlanTask planTask = iterator.next();
                if (planTask.id.equals(task.id)) {
                    iterator.remove();
                    break;
                }
            }

            if (needNotify) {
                notifyPlanTaskChanged();
            }
        }
    }

    public void updatePlanTaskState(PlanTask task, boolean needNotify) {
        synchronized (mCachePlanTaskStoreLock) {
            if (mCachePlanTaskList == null || mCachePlanTaskList.size() <= 0)
                return;

            for (PlanTask planTask : mCachePlanTaskList) {
                if (planTask.id.equals(task.id)) {
                    planTask.state = task.state;
                    break;
                }
            }
            if (needNotify) {
                notifyPlanTaskChanged();
            }
        }
    }

    public boolean isPlanTaskInitializedFinished() {
        return mIsPlanTaskInitFinished;
    }

    public void reset() {
        synchronized (mCachePlanTaskStoreLock) {
            mCachePlanTaskList.clear();
        }
    }

    public void notifyPlanTaskChanged() {
        notifyAppWidgetWhenDataChanged();
        if (mOnPlanTaskChangedListeners == null || mOnPlanTaskChangedListeners.size() <= 0)
            return;

        synchronized (mCachePlanTaskStoreLock) {
            for (OnPlanTaskChangedListener listener : mOnPlanTaskChangedListeners) {
                listener.onPlanTaskChanged();
            }
        }
    }

    public void addOnPlanTaskChangedListener(OnPlanTaskChangedListener listener) {
        if (listener == null)
            return;

        synchronized (mCachePlanTaskStoreLock) {
            if (!mOnPlanTaskChangedListeners.contains(listener)) {
                mOnPlanTaskChangedListeners.add(listener);
            }
        }
    }

    public void removePlanTaskChangedListener(OnPlanTaskChangedListener listener) {
        if (listener == null)
            return;

        synchronized (mCachePlanTaskStoreLock) {
            mOnPlanTaskChangedListeners.remove(listener);
        }
    }
}
