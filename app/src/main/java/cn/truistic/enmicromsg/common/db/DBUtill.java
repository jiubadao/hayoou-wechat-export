package cn.truistic.enmicromsg.common.db;

import android.content.Context;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;

/**
 * 数据库工具类
 */
public class DBUtill {

    public static void init(Context context) {
        SQLiteDatabase.loadLibs(context);
        File file = new File(context.getFilesDir().getPath() + "EnMicroMsg0.db");

    }

    public static void test() {

    }

}