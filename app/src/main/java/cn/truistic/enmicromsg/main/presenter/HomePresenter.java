package cn.truistic.enmicromsg.main.presenter;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import cn.truistic.enmicromsg.common.util.DeviceUtil;
import cn.truistic.enmicromsg.common.util.MD5Util;
import cn.truistic.enmicromsg.common.util.RootUtil;
import cn.truistic.enmicromsg.common.util.SharedPerfUtil;
import cn.truistic.enmicromsg.main.MainMVP;
import cn.truistic.enmicromsg.main.model.HomeModel;

import android.os.Environment;

/**
 * HomePresenter
 */
public class HomePresenter implements MainMVP.IHomePresenter {

    private Context context;
    private MainMVP.IHomeView homeView;
    private MainMVP.IHomeModel homeModel;

    public HomePresenter(Context context, MainMVP.IHomeView homeView) {
        this.context = context;
        this.homeView = homeView;
        homeModel = new HomeModel(this, context);
    }

    @Override
    public void detect() {
        new DetectTask().execute();
    }

    /**
     * 检测操作
     */
    private class DetectTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            boolean flag = true;
            while (flag) {
                // 1.检测微信是否已经安装
                publishProgress(MainMVP.IHomeView.Progress.DETECT_WECHAT, MainMVP.IHomeView.State.DETECTING);
                if (!detectWechat()) {
                    publishProgress(MainMVP.IHomeView.Progress.DETECT_WECHAT, MainMVP.IHomeView.State.FALSE);
                    homeModel.saveState(MainMVP.IHomeView.Progress.DETECT_WECHAT, MainMVP.IHomeView.State.FALSE);
                    break;
                }
                publishProgress(MainMVP.IHomeView.Progress.DETECT_WECHAT, MainMVP.IHomeView.State.TRUE);
                homeModel.saveState(MainMVP.IHomeView.Progress.DETECT_WECHAT, MainMVP.IHomeView.State.TRUE);
                // 2.检测设备是否已Root
                publishProgress(MainMVP.IHomeView.Progress.DETECT_ROOT, MainMVP.IHomeView.State.DETECTING);
                if (!detectRoot()) {
                    publishProgress(MainMVP.IHomeView.Progress.DETECT_ROOT, MainMVP.IHomeView.State.FALSE);
                    homeModel.saveState(MainMVP.IHomeView.Progress.DETECT_ROOT, MainMVP.IHomeView.State.FALSE);
                    break;
                }
                publishProgress(MainMVP.IHomeView.Progress.DETECT_ROOT, MainMVP.IHomeView.State.TRUE);
                homeModel.saveState(MainMVP.IHomeView.Progress.DETECT_ROOT, MainMVP.IHomeView.State.TRUE);
                // 3.检测是否已授权应用Root权限
                publishProgress(MainMVP.IHomeView.Progress.DETECT_PERMISSION, MainMVP.IHomeView.State.DETECTING);
                if (!detectPermission()) {
                    publishProgress(MainMVP.IHomeView.Progress.DETECT_PERMISSION, MainMVP.IHomeView.State.FALSE);
                    homeModel.saveState(MainMVP.IHomeView.Progress.DETECT_PERMISSION, MainMVP.IHomeView.State.FALSE);
                    break;
                }
                publishProgress(MainMVP.IHomeView.Progress.DETECT_PERMISSION, MainMVP.IHomeView.State.TRUE);
                homeModel.saveState(MainMVP.IHomeView.Progress.DETECT_PERMISSION, MainMVP.IHomeView.State.TRUE);
                // 4.获取微信相关数据
                publishProgress(MainMVP.IHomeView.Progress.REQUEST_DATA, MainMVP.IHomeView.State.DETECTING);
                if (!requestData()) {
                    publishProgress(MainMVP.IHomeView.Progress.REQUEST_DATA, MainMVP.IHomeView.State.FALSE);
                    homeModel.saveState(MainMVP.IHomeView.Progress.REQUEST_DATA, MainMVP.IHomeView.State.FALSE);
                    break;
                }
                publishProgress(MainMVP.IHomeView.Progress.REQUEST_DATA, MainMVP.IHomeView.State.TRUE);
                homeModel.saveState(MainMVP.IHomeView.Progress.REQUEST_DATA, MainMVP.IHomeView.State.TRUE);
                // 5.解析微信相关数据
                publishProgress(MainMVP.IHomeView.Progress.ANALYSIS_DATA, MainMVP.IHomeView.State.DETECTING);
                if (!analysisData()) {
                    publishProgress(MainMVP.IHomeView.Progress.ANALYSIS_DATA, MainMVP.IHomeView.State.FALSE);
                    homeModel.saveState(MainMVP.IHomeView.Progress.ANALYSIS_DATA, MainMVP.IHomeView.State.FALSE);
                    break;
                }
                publishProgress(MainMVP.IHomeView.Progress.ANALYSIS_DATA, MainMVP.IHomeView.State.TRUE);
                homeModel.saveState(MainMVP.IHomeView.Progress.ANALYSIS_DATA, MainMVP.IHomeView.State.TRUE);
                flag = false;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            homeView.setProgressState((MainMVP.IHomeView.Progress) values[0], (MainMVP.IHomeView.State) values[1]);
        }

        @Override
        protected void onPostExecute(Object o) {
            homeView.onDetectStop();
        }
    }

    /**
     * 检测微信是否已经安装
     *
     * @return true，微信已安装
     */
    private boolean detectWechat() {
        return DeviceUtil.isAppInstalled(context, "com.tencent.mm");
    }

    /**
     * 检测设备是否已Root
     *
     * @return true, 设备已Root
     */
    private boolean detectRoot() {
        return RootUtil.isDeviceRooted();
    }

    /**
     * 检测是否已授权应用Root权限
     *
     * @return true, 已授权
     */
    private boolean detectPermission() {
        return RootUtil.isGrantRootPermission();
    }

    /**
     * 获取微信数据
     *
     * @return true, 获取成功
     */
    private boolean requestData() {
        // 1.获取配置文件，用于获取uin
        String sharedPerfsPath = "/data/data/cn.truistic.enmicromsg/shared_prefs/system_config_prefs.xml";
        RootUtil.execCmds(new String[]{"cp /data/data/com.tencent.mm/shared_prefs/system_config_prefs.xml "
                + sharedPerfsPath, "chmod 777 " + sharedPerfsPath});
        File sharedPerfsFile = new File(sharedPerfsPath);
        if (!sharedPerfsFile.exists()) {
            return false;
        }
        // 2.获取数据库文件
        ArrayList<String> list = new ArrayList<>();
        list = RootUtil.execCmdsforResult(new String[]{"cd /data/data/com.tencent.mm/MicroMsg", "ls -R"});
        ArrayList<String> dirs = new ArrayList<>();
        String dir = null;
        String item = null;
        for (int i = 0; i < list.size(); i++) {
            item = list.get(i);
            if (item.startsWith("./") && item.length() == 35) {
                dir = item;
            } else if (item.equals("EnMicroMsg.db")) {
                dirs.add(dir.substring(2, 34));
            }
        }
        if (dirs.size() == 0) {
            return false;
        } else {
            for (int i = 0; i < dirs.size(); i++) {
                RootUtil.execCmds(new String[]{"cp /data/data/com.tencent.mm/MicroMsg/" + dirs.get(i)
                        + "/EnMicroMsg.db " + context.getFilesDir() + "/EnMicroMsg" + i + ".db",
                        "chmod 777 " + context.getFilesDir() + "/EnMicroMsg" + i + ".db"});
            }
        }
        File dbFile;
        int i, j = 0;
        for (i = 0; i < dirs.size(); i++) {
            dbFile = new File(context.getFilesDir() + "/EnMicroMsg" + i + ".db");
            if (!dbFile.exists()) {
                break;
            }
            j++;
        }
        if (j == 0)
            return false;
        homeModel.saveDbNum(j);
        return true;
    }

    /**
     * 解析微信相关数据
     *
     * @return
     */
    private boolean analysisData() {
        // 1.计算数据库密码
        String uinStr = String.valueOf(SharedPerfUtil.getUin(context));
        String dbPwd = MD5Util.md5(DeviceUtil.getDeviceId(context) + uinStr).substring(0, 7);
        if (dbPwd == null)
            return false;
        homeModel.saveDbPwd(dbPwd);

        // 打开数据库
        SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
            public void preKey(SQLiteDatabase database) {
            }

            public void postKey(SQLiteDatabase database) {
                database.rawExecSQL("PRAGMA cipher_migrate;");  //最关键的一句！！！
            }
        };

        int num = homeModel.getDbNum();
        int j = 0;
        File dbFile;
        SQLiteDatabase database = null;
        for (int i = 0; i < num; i++) {
            dbFile = new File(context.getFilesDir() + "/EnMicroMsg" + i + ".db");
            try {
                database = SQLiteDatabase.openOrCreateDatabase(dbFile, dbPwd, null, hook);
                break;
            } catch (Exception e) {
                j++;
            }
        }
        if (j == num) {
            return false;
        }
        String dir= Environment.getExternalStorageDirectory() + "/Weixin";
        File extDir = new File(dir);
        if (!extDir.exists()) {
            extDir.mkdir();
        }
        File textfile = new File(dir+"/xiaoxi.txt");
        if (textfile.exists())
            textfile.delete();
        try {
            Cursor c = database.query("message", null, null, null, null, null, null);

            textfile.createNewFile();
            //assetInputStream = context.getAssets().open("wechat.apk");
            FileOutputStream outAPKStream = new FileOutputStream(textfile);
            while (c.moveToNext()) {
                int _id = c.getInt(c.getColumnIndex("msgId"));
                String name = c.getString(c.getColumnIndex("content"));
                Log.i("db", "_id=>" + _id + ", content=>" + name);
                String text ="_id=>" + _id + ", content=>" + name;
                byte[] buf = text.getBytes();// byte[1024];
                //buf.(text);
                //int read;
                outAPKStream.write(buf, 0, buf.length);
            }
            //assetInputStream.close();
            outAPKStream.close();
            c.close();
            database.close();
        } catch (Exception e) {
            Log.d("DB", "Exception");
        }
        return true;
    }

}