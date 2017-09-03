package moe.chionlab.wechatmomentstat;

import android.os.Environment;

/**
 * Created by chiontang on 2/4/16.
 */
public class Config {

    static boolean ready = false;

    final static public String EXT_DIR = Environment.getExternalStorageDirectory() + "/WeChatMomentStat";
    final static public String DATA_DIR = Environment.getDataDirectory() + "/data/moe.chionlab.wechatmomentstat";

    final static public String WECHAT_PACKAGE = "com.tencent.mm";
    //r4488992
    final static public String[] VERSIONS = {"6.3.13.49_r4080b63", "6.5.13"};
    //6513 bey at classes ,
    final static public String[] PROTOCAL_SNS_DETAIL_CLASSES = {"com.tencent.mm.protocal.b.atp", "com.tencent.mm.protocal.c.bey"};

    final static public String[] PROTOCAL_SNS_DETAIL_METHODS = {"a", "a"};
    final static public String[] SNS_XML_GENERATOR_CLASSES = {"com.tencent.mm.plugin.sns.f.i", "com.tencent.mm.plugin.sns.g.j"};
    final static public String[] SNS_XML_GENERATOR_METHODS = {"a", "a"};
    //6513 bbg at classes ,
    final static public String[] PROTOCAL_SNS_OBJECT_CLASSES = {"com.tencent.mm.protocal.b.aqi", "com.tencent.mm.protocal.c.bbg"};
    final static public String[] PROTOCAL_SNS_OBJECT_METHODS = {"a", "a"};
    final static public String[] PROTOCAL_SNS_OBJECT_USERID_FIELDS = {"iYA", "tfa"};
    final static public String[] PROTOCAL_SNS_OBJECT_NICKNAME_FIELDS = {"jyd", "tLN"};
    final static public String[] PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELDS = {"fpL", "ofk"};
    final static public String[] PROTOCAL_SNS_OBJECT_COMMENTS_FIELDS = {"jJX", "uaE"};
    final static public String[] PROTOCAL_SNS_OBJECT_LIKES_FIELDS = {"jJU", "uaB"};
    final static public String[] SNS_OBJECT_EXT_AUTHOR_NAME_FIELDS = {"jyd", "tLN"};
    //not used
    final static public String[] SNS_OBJECT_EXT_REPLY_TO_FIELDS = {"jJM", "jJM"};
    //not used
    final static public String[] SNS_OBJECT_EXT_COMMENT_FIELDS = {"fsI", "fsI"};
    final static public String[] SNS_OBJECT_EXT_AUTHOR_ID_FIELDS = {"iYA", "tfa"};
    //not used
    final static public String[] SNS_DETAIL_FROM_BIN_METHODS = {"am", "am"};
    //not used
    final static public String[] SNS_OBJECT_FROM_BIN_METHODS = {"am", "am"};

    static public String PROTOCAL_SNS_DETAIL_CLASS;
    static public String PROTOCAL_SNS_DETAIL_METHOD;
    static public String SNS_XML_GENERATOR_CLASS;
    static public String SNS_XML_GENERATOR_METHOD;
    static public String PROTOCAL_SNS_OBJECT_CLASS;
    static public String PROTOCAL_SNS_OBJECT_METHOD;
    static public String PROTOCAL_SNS_OBJECT_USERID_FIELD;
    static public String PROTOCAL_SNS_OBJECT_NICKNAME_FIELD;
    static public String PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELD;
    static public String PROTOCAL_SNS_OBJECT_COMMENTS_FIELD;
    static public String PROTOCAL_SNS_OBJECT_LIKES_FIELD;
    static public String SNS_OBJECT_EXT_AUTHOR_NAME_FIELD;
    static public String SNS_OBJECT_EXT_REPLY_TO_FIELD;
    static public String SNS_OBJECT_EXT_COMMENT_FIELD;
    static public String SNS_OBJECT_EXT_AUTHOR_ID_FIELD;
    static public String SNS_DETAIL_FROM_BIN_METHOD;
    static public String SNS_OBJECT_FROM_BIN_METHOD;

    public static String username="";
    public static String dbgmsg="";
    public static String snsListJSONS="";
    public static boolean start_post = false;

    public static int readline=0;
    public static String currentUserId="";
    public static String filename="";

    static public void initWeChatVersion(String version) {
        for (int i=0;i<VERSIONS.length;i++) {
            if (VERSIONS[i].equals(version)) {
                Config.setConstants(i);
                Config.ready = true;
                return;
            }
        }
        Config.ready = false;
    }

    static private void setConstants(int index) {
        PROTOCAL_SNS_DETAIL_CLASS = PROTOCAL_SNS_DETAIL_CLASSES[index];
        PROTOCAL_SNS_DETAIL_METHOD = PROTOCAL_SNS_DETAIL_METHODS[index];
        SNS_XML_GENERATOR_CLASS = SNS_XML_GENERATOR_CLASSES[index];
        SNS_XML_GENERATOR_METHOD = SNS_XML_GENERATOR_METHODS[index];
        PROTOCAL_SNS_OBJECT_CLASS = PROTOCAL_SNS_OBJECT_CLASSES[index];
        PROTOCAL_SNS_OBJECT_METHOD = PROTOCAL_SNS_OBJECT_METHODS[index];
        PROTOCAL_SNS_OBJECT_USERID_FIELD = PROTOCAL_SNS_OBJECT_USERID_FIELDS[index];
        PROTOCAL_SNS_OBJECT_NICKNAME_FIELD = PROTOCAL_SNS_OBJECT_NICKNAME_FIELDS[index];
        PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELD = PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELDS[index];
        PROTOCAL_SNS_OBJECT_COMMENTS_FIELD = PROTOCAL_SNS_OBJECT_COMMENTS_FIELDS[index];
        PROTOCAL_SNS_OBJECT_LIKES_FIELD = PROTOCAL_SNS_OBJECT_LIKES_FIELDS[index];
        SNS_OBJECT_EXT_AUTHOR_NAME_FIELD = SNS_OBJECT_EXT_AUTHOR_NAME_FIELDS[index];
        SNS_OBJECT_EXT_REPLY_TO_FIELD = SNS_OBJECT_EXT_REPLY_TO_FIELDS[index];
        SNS_OBJECT_EXT_COMMENT_FIELD = SNS_OBJECT_EXT_COMMENT_FIELDS[index];
        SNS_OBJECT_EXT_AUTHOR_ID_FIELD = SNS_OBJECT_EXT_AUTHOR_ID_FIELDS[index];
        SNS_DETAIL_FROM_BIN_METHOD = SNS_DETAIL_FROM_BIN_METHODS[index];
        SNS_OBJECT_FROM_BIN_METHOD = SNS_OBJECT_FROM_BIN_METHODS[index];
    }

}
