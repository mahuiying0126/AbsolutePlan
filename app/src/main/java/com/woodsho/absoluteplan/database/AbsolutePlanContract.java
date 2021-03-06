package com.woodsho.absoluteplan.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by hewuzhao on 17/12/9.
 */

public class AbsolutePlanContract {
    public static final String AUTHORITY = "com.woodsho.absoluteplan";

    public static final class PlanTask implements BaseColumns {
        public static final String TASK_ID = "task_id";
        public static final String TASK_TITLE = "task_title";
        public static final String TASK_DESCRIBE = "task_describe";
        public static final String TASK_PRIORITY = "task_priority";
        public static final String TASK_STATE = "task_state";
        public static final String TASK_TIME = "task_time";

        public static final String CONTENT_PLANTASK_DIR_TYPE = "vnd.android.cursor.dir/" + AbsolutePlanDBHelper.DB_TABLE_PLANTASK;
        public static final String CONTENT_PLANTASK_ITEM_TYPE = "vnd.android.cursor.item/" + AbsolutePlanDBHelper.DB_TABLE_PLANTASK;
        public static final Uri CONTENT_URI_PLANTASK = Uri.parse("content://" + AUTHORITY + "/" + AbsolutePlanDBHelper.DB_TABLE_PLANTASK);
    }
}
