package cn.truistic.enmicromsg.app;

import android.content.Context;

/**
 *
 */
public class PathConfig {

    /**
     * 微信配置文件保存路径
     *
     * @param context
     * @return
     */
    public static String getMMSharedPerfsPath(Context context) {
        return context.getFilesDir() + "/system_config_prefs.xml";
    }

}
