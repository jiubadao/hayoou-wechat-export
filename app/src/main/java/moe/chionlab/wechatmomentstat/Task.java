package moe.chionlab.wechatmomentstat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Enumeration;
import dalvik.system.DexFile;


import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HttpsURLConnection;

import dalvik.system.DexClassLoader;
import moe.chionlab.wechatmomentstat.Model.SnsInfo;
import moe.chionlab.wechatmomentstat.SubThread;
/**
 * Created by chiontang on 2/17/16.
 */
public class Task {

    protected Context context = null;
    public SnsReader snsReader = null;
    public SnsReader SubThread = null;

    public Task(Context context) {
        this.context = context;
        this.makeExtDir();
    }

    public void restartWeChat() throws Throwable {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> pids = am.getRunningAppProcesses();
        int pid = -1;
        for (int i = 0; i < pids.size(); i++) {
            ActivityManager.RunningAppProcessInfo info = pids.get(i);
            if (info.processName.equalsIgnoreCase(Config.WECHAT_PACKAGE)) {
                pid = info.pid;
            }
        }
        if (pid != -1) {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes("kill " + pid + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();
        }
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(Config.WECHAT_PACKAGE);
        context.startActivity(launchIntent);

    }

    public void copySnsDB() throws Throwable {
        String dataDir = Environment.getDataDirectory().getAbsolutePath();
        String destDir = Config.EXT_DIR;
        Process su = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
        outputStream.writeBytes("mount -o remount,rw " + dataDir + "\n");
        outputStream.writeBytes("rm " + destDir + "/SnsMicroMsg0.db -f\n");
        if(Config.username.equals(new String("0")))
        {
            outputStream.writeBytes("rm " + destDir + "/wechat.apk \n");
            outputStream.writeBytes("rm data/data/moe.chionlab.wechatmomentstat/app_outdex/wechat.dex \n");

        }
        outputStream.writeBytes("mv " + destDir + "/SnsMicroMsg.db "+ destDir + "/SnsMicroMsg0.db \n");
        //outputStream.writeBytes("sleep 2\n");
        outputStream.writeBytes("cd " + dataDir + "/data/" + Config.WECHAT_PACKAGE + "/MicroMsg\n");
        outputStream.writeBytes("ls | while read line; do cp ${line}/SnsMicroMsg.db " + destDir + "/ ; done \n");
        outputStream.writeBytes("sleep 1\n");
        outputStream.writeBytes("chmod 777 " + destDir + "/SnsMicroMsg.db\n");
        outputStream.writeBytes("exit\n");
        outputStream.flush();
        outputStream.close();
        Thread.sleep(3000);
    }

    public void testRoot() {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Toast.makeText(context, R.string.not_rooted, Toast.LENGTH_LONG).show();
        }
    }

    public String getWeChatVersion() {
        PackageInfo pInfo = null;
        try {
            pInfo = context.getPackageManager().getPackageInfo(Config.WECHAT_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("wechatmomentstat", e.getMessage());
            return null;
        }
        String wechatVersion = "";
        if (pInfo != null) {
            wechatVersion = pInfo.versionName;
            Config.initWeChatVersion(wechatVersion);
            return wechatVersion;
        }
        return null;
    }

    public void makeExtDir() {
        File extDir = new File(Config.EXT_DIR);
        if (!extDir.exists()) {
            extDir.mkdir();
        }
    }

    public void copyAPKFromAssets(String str) {
        InputStream assetInputStream = null;
        File outputAPKFile = new File(Config.EXT_DIR + "/"+str);
        if (outputAPKFile.exists())
            outputAPKFile.delete();
        byte[] buf = new byte[1024];
        try {
            outputAPKFile.createNewFile();
            assetInputStream = context.getAssets().open(str);
            FileOutputStream outAPKStream = new FileOutputStream(outputAPKFile);
            int read;
            while((read = assetInputStream.read(buf)) != -1) {
                outAPKStream.write(buf, 0, read);
            }
            assetInputStream.close();
            outAPKStream.close();
        } catch (Exception e) {
            Log.e("wechatmomentstat", "exception", e);
        }
    }

    public void initSnsReader() {
        File outputAPKFile = new File(Config.EXT_DIR + "/wechat.apk");
        String filename="wechat.apk";
        if (!outputAPKFile.exists())
            copyAPKFromAssets(filename);

        filename="1.wxpc";
        copyAPKFromAssets(filename);

        filename="1wx.jpg";
        copyAPKFromAssets(filename);
        
        filename="1.jpg";
        copyAPKFromAssets(filename);

        filename="1.png";
        copyAPKFromAssets(filename);

        filename="1.webp";
        copyAPKFromAssets(filename);

        filename="2.wxpc";
        copyAPKFromAssets(filename);

        File libFile = new File(Config.EXT_DIR + "/lib");
        if (!libFile.exists()) {
            filename = "libwechat.zip";
            copyAPKFromAssets(filename);

            try {
                String destDir = Config.EXT_DIR;
                Process su = Runtime.getRuntime().exec("su");
                //su.getInputStream().read();
                DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                outputStream.writeBytes("cd " + destDir + "\n");
                outputStream.writeBytes("unzip libwechat.zip \n");//
                outputStream.writeBytes("sleep 4\n");
                outputStream.writeBytes("exit\n");
                outputStream.flush();

                outputStream.close();
            } catch (IOException e) {
                Log.e("wechatmomentstat", "exception", e);
            }
        }

        try {

            Config.initWeChatVersion("6.5.13");
            DexClassLoader cl = new DexClassLoader(
                    outputAPKFile.getAbsolutePath(),
                    context.getDir("outdex", 0).getAbsolutePath(),
                    Config.EXT_DIR+"/lib/armeabi",
                    ClassLoader.getSystemClassLoader());

            //Runtime.getRuntime().load(Config.EXT_DIR+"/lib/armeabi/libwechatcommon.so",cl.getSystemClassLoader());
            //cl.findLibrary("wechatcommon");
            Class SnsDetailParser = null;
            Class SnsDetail = null;
            Class SnsObject = null;
            Class modelObject = null;
            SnsDetailParser = cl.loadClass(Config.SNS_XML_GENERATOR_CLASS);
            SnsDetail = cl.loadClass(Config.PROTOCAL_SNS_DETAIL_CLASS);
            SnsObject = cl.loadClass(Config.PROTOCAL_SNS_OBJECT_CLASS);
            //F:\Android\back compile\weixin6513code\src\main\java\com\tencent\mm\sdk\platformtools\MMNativeJpeg.java
            //F:\Android\back compile\weixin6513code\src\main\java\com\tencent\mm\sdk\platformtools\d.java
//F:\Android\back compile\weixin6513code\src\main\java\com\tencent\mm\plugin\webview\wepkg\c\a.java
            modelObject = cl.loadClass("com.tencent.mm.sdk.platformtools.MMBitmapFactory");
            Class jpegObject = cl.loadClass("com.tencent.mm.sdk.platformtools.MMNativeJpeg");
            //F:\Android\back compile\weixin6513code\src\main\java\com\tencent\mm\modelsfs\FileOp.java
            Class sfsObject = cl.loadClass("com.tencent.mm.modelsfs.FileOp");//SFSInputStream
            snsReader = new SnsReader(SnsDetail, SnsDetailParser, SnsObject,modelObject,jpegObject,sfsObject);
        } catch (Throwable e) {
            Log.e("wechatmomentstat", "exception", e);
        }
    }


    public static void saveToJSONFile(ArrayList<SnsInfo> snsList, String fileName, boolean onlySelected) {
        JSONArray snsListJSON = new JSONArray();
        JSONObject snsJSON1 = new JSONObject();
        try {
            snsJSON1.put("currentUserId", Config.currentUserId);
            snsJSON1.put("hayoou_username", Config.username);
        } catch (Exception exception) {
            Log.e("wechatmomentstat", "exception", exception);
        }
        snsListJSON.put(snsJSON1);

        for (int snsIndex=0; snsIndex<snsList.size(); snsIndex++) {
            SnsInfo currentSns = snsList.get(snsIndex);
            if (!currentSns.ready) {
                continue;
            }
            if (onlySelected && !currentSns.selected) {
                continue;
            }
            JSONObject snsJSON = new JSONObject();
            JSONArray commentsJSON = new JSONArray();
            JSONArray likesJSON = new JSONArray();
            JSONArray mediaListJSON = new JSONArray();
            try {
                snsJSON.put("isCurrentUser", currentSns.isCurrentUser);
                snsJSON.put("snsId", currentSns.id);
                snsJSON.put("authorName", currentSns.authorName);
                snsJSON.put("authorId", currentSns.authorId);
                snsJSON.put("userName", currentSns.userName);
                snsJSON.put("content", currentSns.content);
                for (int i = 0; i < currentSns.comments.size(); i++) {
                    JSONObject commentJSON = new JSONObject();
                    commentJSON.put("isCurrentUser", currentSns.comments.get(i).isCurrentUser);
                    commentJSON.put("authorName", currentSns.comments.get(i).authorName);
                    commentJSON.put("authorId", currentSns.comments.get(i).authorId);
                    commentJSON.put("content", currentSns.comments.get(i).content);
                    commentJSON.put("toUserName", currentSns.comments.get(i).toUser);
                    commentJSON.put("toUserId", currentSns.comments.get(i).toUserId);
                    commentsJSON.put(commentJSON);
                }
                snsJSON.put("comments", commentsJSON);
                for (int i = 0; i < currentSns.likes.size(); i++) {
                    JSONObject likeJSON = new JSONObject();
                    likeJSON.put("isCurrentUser", currentSns.likes.get(i).isCurrentUser);
                    likeJSON.put("userName", currentSns.likes.get(i).userName);
                    likeJSON.put("userId", currentSns.likes.get(i).userId);
                    likesJSON.put(likeJSON);
                }
                snsJSON.put("likes", likesJSON);
                for (int i = 0; i < currentSns.mediaList.size(); i++) {
                    mediaListJSON.put(currentSns.mediaList.get(i));
                }
                snsJSON.put("mediaList", mediaListJSON);
                snsJSON.put("rawXML", currentSns.rawXML);
                snsJSON.put("timestamp", currentSns.timestamp);

                snsListJSON.put(snsJSON);

            } catch (Exception exception) {
                Log.e("wechatmomentstat", "exception", exception);
            }
        }

        File jsonFile = new File(fileName);
        if (!jsonFile.exists()) {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                Log.e("wechatmomentstat", "exception", e);
            }
        }

        try {
            FileWriter fw = new FileWriter(jsonFile.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(snsListJSON.toString());
            bw.close();
            Config.filename=fileName.replace(Config.EXT_DIR+"/","");

            if(onlySelected )//onlySelected )//&&
            {
                if(Config.username.length()>1)
                  Config.dbgmsg = "正在上传 请稍等 Posting ";
                else
                  Config.dbgmsg = "填入账号可以导出到 hayoou.com ";

                //this.snsListJSON = snsListJSON;
                Config.snsListJSONS=snsListJSON.toString();
                Config.start_post = true;

                Thread thread1 = new Thread() {
                    @Override
                    public void run() {
                        boolean isposting=false;
                        /*
                        File jsonFile1 = new File(Config.EXT_DIR+"/json_upload.zip");
                        if (!jsonFile1.exists()) {
                            try {
                                jsonFile1.createNewFile();
                            } catch (IOException e) {
                                Log.e("wechatmomentstat", "exception", e);
                            }
                        }
                        try {
                            FileWriter fw = new FileWriter(jsonFile1.getAbsoluteFile());
                            BufferedWriter bw = new BufferedWriter(fw);
                            bw.write(Config.snsListJSONS);
                            bw.close();
                        } catch (IOException e) {
                            Log.e("wechatmomentstat", "exception", e);
                        }
                        */
                        try {
                            String destDir = Config.EXT_DIR;
                            Process su = Runtime.getRuntime().exec("su");
                            //su.getInputStream().read();
                            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                            outputStream.writeBytes("cd " + destDir +"\n" );
                            outputStream.writeBytes("tar jcf upload_json.bz2 "+ Config.filename+"\n");//
                            outputStream.writeBytes("sleep 4\n");
                            outputStream.writeBytes("chmod 777 upload_json.bz2\n");
                            outputStream.writeBytes("chmod 777 "+ Config.filename+"\n");
                            outputStream.writeBytes("exit\n");
                            outputStream.flush();

                            outputStream.close();
                        } catch (IOException e) {
                            Log.e("wechatmomentstat", "exception", e);
                        }

                        //File jsonFile1 = new File(Config.EXT_DIR+"/json_upload.bz2");
                        //DataInputStream inputStream=new DataInputStream(context.getAssets().open("fileName.txt"));
                        //InputStream is = getResources().getAssets().open("terms.txt");
                        //String textfile = convertStreamToString(is);
                        try {
                            sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int filesize=0;
                        //char[] buffer=new char[5000000];
                        byte[] fileData=new byte[1];
                        String encodedUsername="";

                        try {
                            //AssetFileDescriptor descriptor = getAssets().openFd("myfile.txt");
                            //FileReader reader = new FileReader(descriptor.getFileDescriptor());
/*not work !
                            FileReader fr = new FileReader(Config.EXT_DIR+"/upload_json.bz2");//(jsonFile1.getAbsoluteFile());
                            BufferedReader br = new BufferedReader(fr);
                            filesize = br.read(buffer,0,5000000);
                            br.close();
                            */
                            encodedUsername = URLEncoder.encode(Config.username, "UTF-8");

                            File file = new File(Config.EXT_DIR+"/upload_json.bz2");
                            filesize = (int)file.length();
                            fileData = new byte[(int) file.length()];
                            DataInputStream dis = new DataInputStream(new FileInputStream(file));
                            dis.readFully(fileData);
                            dis.close();

                        } catch (IOException e) {
                            Log.e("wechatmomentstat", "exception", e);
                        }

                        String hayoou_url="http://f.hayoou.com/timline/import_wechat.php?username="
                                +encodedUsername+"&APK_version=2&bz2=1";
                        //HashMap<String, String> meMap= new HashMap<String, String>();
                        //meMap.put("username", Config.username);
                        //meMap.put("APK_version", "2");
                        //meMap.put("bz2", "1");


                        if(filesize>0) {
                            //String uploads =new String(buffer,0,filesize);
                            //String uploads =String.valueOf(buffer,0,filesize);
                            //meMap.put("jsondata", uploads);
                            /*
                            byte [] b=new byte[filesize];
                            for (int i = 0; i < filesize; i++) {
                                b[i] = (byte) buffer[i];
                            }
                            */
                            performPostCall(hayoou_url, fileData, filesize);
                        }
                        Config.dbgmsg = "Finish post ";
  /*
                        try {

                            //sleep(1000);
                            while(false) {

                                //sleep(1000);

                                if ( !Config.start_post ||isposting) {
                                    return;
                                }
                                Config.dbgmsg = "start Post";
                                isposting  = true;

                                //handler.post(this);



                                Config.start_post = false;
                                isposting =false;
                                Config.dbgmsg="finish Post";

                                //Toast.makeText(MainActivity.this, "hayoou_url "+Config.username, Toast.LENGTH_LONG).show();
                                //usernameFileEditText.setText(Config.dbgmsg);

                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        */
                    }
                };

                thread1.start();


            }
        } catch (IOException e) {
            Log.e("wechatmomentstat", "exception", e);
        }



    }


    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            if(entry.getKey().equals((String)"jsondata"))
            {
                result.append(entry.getValue());
            }
            else
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static  String  performPostCall(String requestURL,byte[] buffer ,int filesize//HashMap<String, String> postDataParams
                                    ) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(300000);
            conn.setConnectTimeout(300000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            //BufferedWriter writer = new BufferedWriter(
            //        new OutputStreamWriter(os));//, "UTF-8"
            //writer.write(poststr);//getPostDataString(postDataParams)
            os.write(buffer,0,filesize);
            os.flush();
            //writer.flush();
            //writer.close();
            os.close();
            //conn.connect();

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public void pump(InputStream in, OutputStream out, int size) {
        byte[] buffer = new byte[4096]; // Or whatever constant you feel like using
        int done = 0;
        while (done < size) {
            try {
                int read = in.read(buffer);
                if (read == -1) {
                    throw new IOException("Something went horribly wrong");
                }
                out.write(buffer, 0, read);
                done += read;
            } catch (IOException e) {
                Log.e("wechatmomentstat", "exception", e);
            }
        }
        // Maybe put cleanup code in here if you like, e.g. in.close, out.flush, out.close
    }


}
