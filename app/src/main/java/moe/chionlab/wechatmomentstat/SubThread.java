package moe.chionlab.wechatmomentstat;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import moe.chionlab.wechatmomentstat.Model.SnsInfo;
import moe.chionlab.wechatmomentstat.common.Share;


/**
 * Created by chiontang on 2/4/16.
 */
public class SubThread extends Thread {

    //ArrayList<Tweet> tweetList;
    ArrayList<SnsInfo> snsList;
    ArrayList<SnsInfo> snsList1;
    String oid;

    SubThread(ArrayList<SnsInfo> snsList,String oid) {
        this.snsList = snsList;
        this.oid = oid;
    }
    public int last_size=0;


    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if ( !Config.start_post) {
                return;
            }

            postTohost();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public String  performPostCall(String requestURL,
                                   HashMap<String, String> postDataParams) {

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(25000);
            conn.setConnectTimeout(25000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
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


    private void postTohost() {
        JSONArray snsListJSON = new JSONArray();
        snsList = Share.snsData.snsList;
        for (int snsIndex=0; snsIndex<snsList.size(); snsIndex++) {
            SnsInfo currentSns = snsList.get(snsIndex);
            if (!currentSns.ready) {
                continue;
            }
            if ( !currentSns.selected) {
                continue;
            }
            JSONObject snsJSON = new JSONObject();
            JSONArray commentsJSON = new JSONArray();
            JSONArray likesJSON = new JSONArray();
            JSONArray mediaListJSON = new JSONArray();
            /*
            try {
                snsJSON.put("isCurrentUser", currentSns.isCurrentUser);
                snsJSON.put("snsId", currentSns.id);
                snsJSON.put("authorName", currentSns.authorName);
                snsJSON.put("authorId", currentSns.authorId);
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
                */
                for (int i = 0; i < currentSns.mediaList.size(); i++) {
                    mediaListJSON.put(currentSns.mediaList.get(i));
                }
            try {
                snsJSON.put("mediaList", mediaListJSON);

                snsJSON.put("rawXML", currentSns.rawXML);
                snsJSON.put("timestamp", currentSns.timestamp);

                snsListJSON.put(snsJSON);

            } catch (Exception exception) {
                Log.e("wechatmomentstat", "exception", exception);
            }
        }


        try {

            if(Config.username.length()>3)//onlySelected )//&&
            {
                String hayoou_url="http://f.hayoou.com/timline/import_wechat.php?username1="+Config.username;
                HashMap<String, String> meMap= new HashMap<String, String>();
                meMap.put("username", Config.username);
                meMap.put("APK_version","2");
                meMap.put("jsondata", snsListJSON.toString());

                performPostCall(hayoou_url, meMap);
            }

        } catch (Exception e) {
            Log.e("wechatmomentstat", "exception", e);
        }

        Config.start_post =false;



    }



}
