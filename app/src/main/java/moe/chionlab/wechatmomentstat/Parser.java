package moe.chionlab.wechatmomentstat;

import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.graphics.Bitmap;

import moe.chionlab.wechatmomentstat.Model.SnsInfo;
/*

class mmjpeg {
    public static native int convertToProgressive(String str, String str2);

    public static native boolean decodeToBitmap(String str, Bitmap bitmap);

    public static native int isProgressiveFile(String str);

    public static native JpegParams queryParams(String str);

    public static native int queryQuality(String str);

    mmjpeg() {

    }
}

 class JpegParams {
    public int Depth;
    public int Height;
    public int Width;
    public int isProgressive;

    JpegParams() {

    }
}
*/

/**
 * Created by chiontang on 2/11/16.
 */
public class Parser {

    protected Class SnsDetailParser = null;
    protected Class SnsDetail = null;
    protected Class SnsObject = null;
    protected Class ModelClass = null;
    protected Class JpegObject = null;
    protected Object ModelClass2= null;
    protected Class sfsObject = null;
    protected boolean inited = false;
    public  long testlong=0;

    public Parser(Class SnsDetail, Class SnsDetailParser, Class SnsObject, Class ModelClass,
                  Class JpegObject,Class sfsObject) {
        this.SnsDetailParser = SnsDetailParser;
        this.SnsDetail = SnsDetail;
        this.SnsObject = SnsObject;
        this.ModelClass = ModelClass;
        this.JpegObject = JpegObject;
        this.sfsObject =  sfsObject;
        inited = false;
        //loadlib(new String().valueOf(testlong));
    }
    public static void loadlib(String str){
        //System.load(str);
    }
    public static void loadlib2(String str){
        System.loadLibrary(str);
    }
    /*
    public static Bitmap decodeAsBitmap(String str) {

        try {
            JpegParams queryParams = mmjpeg.queryParams(str);
            if (queryParams == null) {
                Log.d("wechatmomentstat", "can't query jpeg parames.");

                return null;
            }
            Bitmap createBitmap = Bitmap.createBitmap(queryParams.Width, queryParams.Height, Bitmap.Config.ARGB_8888);
            if (mmjpeg.decodeToBitmap(str, createBitmap)) {
                Log.d("wechatmomentstat", "decode bitmap successed.");

                return createBitmap;
            }
            Log.d("wechatmomentstat", "can't decode to bmp.");

            return null;
        } catch (Throwable e) {
            Log.d("wechatmomentstat", "decodeAsBitmap exception:%s");
            return null;
        }
    }
*/
    public SnsInfo parseSnsAllFromBin(byte[] snsDetailBin, byte[] snsObjectBin) throws Throwable {
        Object snsDetail = parseSnsDetailFromBin(snsDetailBin);
        Object snsObject = parseSnsObjectFromBin(snsObjectBin);

        SnsInfo snsInfo = parseSnsDetail(snsDetail);
        parseSnsObject(snsObject, snsInfo);

        return snsInfo;
    }

    public Object parseSnsDetailFromBin(byte[] bin) throws Throwable {
        Object snsDetail = SnsDetail.newInstance();
        //Method[] m = SnsDetail.getDeclaredMethods();
        //for(int i =0;i<m.length;i++){
            //Log.d("wechatmomentstat", "snsDetail Method "+i+" "+m[i].toString());
        //}

        Method fromBinMethod = SnsDetail.getMethod(Config.SNS_DETAIL_FROM_BIN_METHOD, byte[].class);
        //Log.d("wechatmomentstat", "snsDetail Method -- "+fromBinMethod.toString());
        fromBinMethod.invoke(snsDetail, bin);
        return snsDetail;
    }

    public SnsInfo parseSnsDetail(Object snsDetail) throws Throwable {
        Method snsDetailParserMethod = SnsDetailParser.getMethod(Config.SNS_XML_GENERATOR_METHOD, SnsDetail);
        String xmlResult = (String)snsDetailParserMethod.invoke(this, snsDetail);
        return parseTimelineXML(xmlResult);
    }

    public Object parseSnsObjectFromBin(byte[] bin) throws Throwable {
        Object snsObject = SnsObject.newInstance();
        Method fromBinMethod = SnsObject.getMethod(Config.SNS_OBJECT_FROM_BIN_METHOD, byte[].class);
        fromBinMethod.invoke(snsObject, bin);
        return snsObject;
    }

    //static
    public SnsInfo parseTimelineXML(String xmlResult) throws Throwable {
        SnsInfo currentSns = new SnsInfo();
        Pattern userIdPattern = Pattern.compile("<username><!\\[CDATA\\[(.+?)\\]\\]></username>",Pattern.DOTALL);
        Pattern contentPattern = Pattern.compile("<contentDesc><!\\[CDATA\\[(.+?)\\]\\]></contentDesc>", Pattern.DOTALL);
        Pattern mediaPattern = Pattern.compile("<media>.*?<url.*?><!\\[CDATA\\[(.+?)\\]\\]></url>.*?</media>",Pattern.DOTALL);
        Pattern mediaTokenPattern = Pattern.compile("<media>.*?<url.*?><!\\[CDATA\\[(.+?)\\]\\]></url>.*?<urltoken.*?><!\\[CDATA\\[(.+?)\\]\\]></urltoken>.*?<urlidx.*?><!\\[CDATA\\[(.+?)\\]\\]></urlidx>.*?<urlenc.*?><!\\[CDATA\\[(.+?)\\]\\]></urlenc>.*?<urlenckey.*?><!\\[CDATA\\[(.+?)\\]\\]></urlenckey>.*?</media>",Pattern.DOTALL);
        Pattern mediaIdxPattern = Pattern.compile("<media>.*?<urlenckey.*?><!\\[CDATA\\[(.+?)\\]\\]></urlenckey>.*?</media>");
        Pattern timestampPattern = Pattern.compile("<createTime><!\\[CDATA\\[(.+?)\\]\\]></createTime>");

        Matcher userIdMatcher = userIdPattern.matcher(xmlResult);
        Matcher contentMatcher = contentPattern.matcher(xmlResult);
        Matcher mediaMatcher = mediaPattern.matcher(xmlResult);
        Matcher mediaTokenMatcher = mediaTokenPattern.matcher(xmlResult);
        Matcher mediaIdxMatcher = mediaIdxPattern.matcher(xmlResult);
        Matcher timestampMatcher = timestampPattern.matcher(xmlResult);

        currentSns.id = getTimelineId(xmlResult);

        currentSns.rawXML = xmlResult;
        Log.d("wechatmomentstat",xmlResult);

        if (timestampMatcher.find()) {
            currentSns.timestamp = Integer.parseInt(timestampMatcher.group(1));
        }

        if (userIdMatcher.find()) {
            currentSns.authorId = userIdMatcher.group(1);
        }

        if (contentMatcher.find()) {
            currentSns.content = contentMatcher.group(1);
        }

        while (mediaMatcher.find()) {
            boolean flag = true;
            for (int i=0;i<currentSns.mediaList.size();i++) {
                if (currentSns.mediaList.get(i).equals(mediaMatcher.group(1))) {
                    flag = false;
                    break;
                }
            }

            if (flag) {

                //Object snsObject = this.ModelClass.newInstance();
                //Method fromBinMethod = this.ModelClass.getMethod("FI", byte[].class);
                String url =mediaMatcher.group(1);
                //Log.d("wechatmomentstat", "url0="+url);
                //url = (String)fromBinMethod.invoke(snsObject, url);
                //Log.d("wechatmomentstat", "url1="+url);
                //url = url.replace("wxpc","webp");
                //currentSns.mediaList.add(url);
            }
        }


        while (mediaTokenMatcher.find()) {
            boolean flag = true;

            if (flag) {

                //Object snsObject = this.ModelClass.newInstance();
                //Method fromBinMethod = this.ModelClass.getMethod("FI", byte[].class);
                String url =mediaTokenMatcher.group(1);

                String urltoken =mediaTokenMatcher.group(2);
                String urlidx =mediaTokenMatcher.group(3);
                String urlenc =mediaTokenMatcher.group(4);
                String urlenckey =mediaTokenMatcher.group(3);
                //Log.d("wechatmomentstat", "url ="+url+" urltoken="+urltoken+" urlidx="+urlidx);
                //url = url.replace("wxpc","webp");

                Log.d("wechatmomentstat", "url webp="+url+"?tp=webp&token="+urltoken+"&idx=1");
                Log.d("wechatmomentstat", "url urlidx(thumbtoken)="+urlidx+" ,urlenc="+urlenc+" ,urlenckey="+urlenckey);
                url = url+"?tp=jpg&token="+urltoken+"&idx=1"+"&urlenc="+urlenc+"&urlenckey="+urlenckey;

/*
        try {
            System.loadLibrary("wcdb");
            System.loadLibrary("wechatcommon");
            System.loadLibrary("wechatmm");
            System.loadLibrary("FFmpeg");
            System.loadLibrary("libvoipCodec_v7a.so");
            //NativeBlurProcess.isLoadLibraryOk.set(true);
            //Log.d("wechatmomentstat", "loadLibrary success!");
        } catch (Throwable throwable) {
            //Log.d("wechatmomentstat", "loadLibrary error!" + throwable);
        }
        */

                if(ModelClass2 == null)
                    ModelClass2 = ModelClass.newInstance();
                Object JpegObject2 = JpegObject.newInstance();
                String path = Config.EXT_DIR +   "/2.wxpc";
                Method loadMethod = ModelClass.getMethod("loadlib2",String.class);
                //Method nativeInitMethod = ModelClass.getMethod("nativeInit",String.class);
                Method decodeAsBitmapMethod = JpegObject.getMethod("decodeAsBitmap",String.class);
                if(!inited) {
                    try {
                        loadMethod.invoke(ModelClass, "wcdb");
                        loadMethod.invoke(ModelClass, "wechatcommon");
                        loadMethod.invoke(ModelClass, "wechatmm");
                        loadMethod.invoke(ModelClass, "FFmpeg");
                        loadMethod.invoke(ModelClass, "wechatpack");
                        loadMethod.invoke(ModelClass, "voipCodec_v7a");

                    } catch (Exception e) {
                        Log.d("wechatmomentstat", "not load loadlib2 ");
                    }
                }

                Method isProgressive = JpegObject.getMethod("isProgressive",String.class);
                Method IsJpegFile = JpegObject.getMethod("IsJpegFile",String.class);

                if((boolean)IsJpegFile.invoke(ModelClass,Config.EXT_DIR +   "/2.wxpc")) {
                    Log.d("wechatmomentstat", "IsJpegFile ");
                    if ((boolean) isProgressive.invoke(ModelClass, path))
                        Log.d("wechatmomentstat", "isProgressive ");
                    else
                        Log.d("wechatmomentstat", "is not Progressive ");
                }else{
                    Log.d("wechatmomentstat", "Is not JpegFile ");
                }

                if((boolean)IsJpegFile.invoke(ModelClass,Config.EXT_DIR +   "/1.jpg")) {
                    Log.d("wechatmomentstat", "IsJpegFile ");
                    if ((boolean) isProgressive.invoke(ModelClass, path))
                        Log.d("wechatmomentstat", "isProgressive ");
                    else
                        Log.d("wechatmomentstat", "is not Progressive ");
                }else{
                    Log.d("wechatmomentstat", "Is not JpegFile ");
                }
                //Method initMethod = ModelClass.getMethod("switchDecoder",int.class,boolean.class);
                //initMethod.invoke(ModelClass,1,true);

                //if(inited)
                //    Log.d("wechatmomentstat", "MMBitmapFactory init");

                //decodeByteArrayWithMMDecoderIfPossible(bArr,0,bArr.length,null, null,new int[0])

                //Method decodeFileMethod = ModelClass.getMethod("decodeByteArrayWithMMDecoderIfPossible");//,
                //        byte[].class,int.class,int.class,BitmapFactory.Options.class,int.class,int.class);
                Method decodeFileMethod = null;
                Method decodeFileMethod2 = null;
                Method nativeCheckIsImageLegalMethod = null;
                BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inJustDecodeBounds = true;
                Method[] methods = ModelClass.getDeclaredMethods();
                //Log.d("wechatmomentstat", "MethodDeclaration in " + ModelClass.getName());
                String[] initlib = new String[]{Config.EXT_DIR+"/lib/armeabi/libvoipCodec_v7a.so"};

                for(Method method : methods){
                    method.setAccessible(true);
                    //Log.d("wechatmomentstat", method.getName());
                    //if(method.getName()=="decodeStreamWithMMDecoderIfPossible")// nativeDecodeStream decodeByteArrayWithMMDecoderIfPossible
                    if(method.getName()=="decodeByteArrayWithMMDecoderIfPossible")
                            decodeFileMethod = method;
                    if(method.getName()=="decodeStreamWithSystemDecoder")//decodeByteArrayWithSystemDecoder
                        decodeFileMethod2 = method;
                    if("nativeInit"==method.getName() && (!inited))
                        if((boolean)method.invoke(ModelClass, new Object[]{initlib})) {
                            Log.d("wechatmomentstat", "nativeInit ok");
                            inited = true;
                        }
                    if("nativeCheckIsImageLegal"==method.getName())
                        nativeCheckIsImageLegalMethod = method;

                }
                inited = true;
                //Object sfsObject1 = sfsObject.newInstance();
                methods = sfsObject.getDeclaredMethods();
                Method sfsread = null;
                for(Method method : methods){
                    method.setAccessible(true);
                    //Log.d("wechatmomentstat",  "sfsObject method :" +method.getName());
                    if(method.getName() == "transFor")
                        sfsread = method;
                }
                //Method sfssetkey = sfsObject.getMethod("SFSInputStream",long.class);
                //sfssetkey.invoke(this,(long)314159265);
             //Method sfsinit = sfsObject.getMethod("init",void.class);
                //sfsinit.invoke(this);

                File file1 = new File(path);
                int filesize = (int) file1.length();
                byte[] fileData = new byte[filesize];
                try {
                    InputStream fileIutputStream = new FileInputStream(file1);

                    int read = fileIutputStream.read(fileData,0,filesize);
                    if (read == -1) {

                    }
                    fileIutputStream.close();

                } catch (Throwable e) {
                    Log.d("wechatmomentstat", "read file error");
                }

                String msg ="data0 ";
                for(int i=0;i<30;i++){
                    msg += fileData[i] +" ";
                }

                Log.d("wechatmomentstat", "fileData "+msg);
/*
                Method sfsopen = sfsObject.getMethod("nativeDecodeByteArray",byte[].class, BitmapFactory.Options.class);
                String pass = String.valueOf(314159265);
                Bitmap bitmap0  = (Bitmap)sfsopen.invoke(this,fileData,options);
                if(bitmap0!=null)
                    Log.d("wechatmomentstat", "bitmap0 size "+bitmap0.getByteCount());
                */
                //MMIMAGEENCJNI.transFor(this.mNativePtr, bArr, this.hQH, (long) i);

                //Method sfsread = sfsObject.getMethod("nativeRead",
                //        long.class,byte[].class,int.class,int.class);
                //sfsread.invoke(ModelClass,ptr,fileData,0,(long)filesize);

                msg ="data2 :";
                for(int i=0;i<30;i++){
                    msg += fileData[i] +" ";
                }

                Log.d("wechatmomentstat", "fileData "+msg);

                File file4 = new File(path);
                InputStream fileIutputStream4 = new FileInputStream(file4);

                //nativeCheckIsImageLegalMethod
                File file2 = new File(path);
                InputStream fileIutputStream2 = new FileInputStream(file2);

                int re= (int)nativeCheckIsImageLegalMethod.invoke(ModelClass,
                        fileIutputStream2,new byte[8192],null);
                Log.d("wechatmomentstat", "CheckIsImageLegal "+re);

                InputStream inputStream5 = null;
                Method FileOp_openread = sfsObject.getMethod("openRead",String.class);
                inputStream5 = (InputStream)FileOp_openread.invoke(ModelClass,Config.EXT_DIR+
                        "/1.wxpc");
                Bitmap bitmap = (Bitmap)decodeFileMethod.invoke(ModelClass,
                        //inputStream5,new byte[8192],null,null,null,new int[0]);
                        //fileIutputStream4,null,null,null,new int[0]);
                        fileData,0,(int) file1.length(),null,null,new int[0]);
                if(bitmap!=null)
                    Log.d("wechatmomentstat", "bitmap size "+bitmap.getByteCount());
/*
                msg ="data3 :";
                for(int i=0;i<30;i++){
                    msg += fileData[i] +" ";
                }

                Log.d("wechatmomentstat", "fileData "+msg);

                bitmap = (Bitmap)decodeFileMethod2.invoke(ModelClass,
                        fileIutputStream4,null,null,null);
                        //fileData,0,(int) file1.length(),null,null);
                if(bitmap!=null)
                    Log.d("wechatmomentstat", "bitmap size1 "+bitmap.getByteCount());

                bitmap = (Bitmap)decodeAsBitmapMethod.invoke(ModelClass,path);

*/
                if(bitmap!=null) {
                    Log.d("wechatmomentstat", "bitmap size2 " + bitmap.getByteCount());

                    //Bitmap bitmap = decodeAsBitmap(path);
                    File file = new File(path + ".png");
                    if (file.exists()) {
                        //file.delete();
                    }
                    else {
                        try {
                            OutputStream fileOutputStream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                            fileOutputStream.close();

                        } catch (Throwable e) {
                            Log.d("wechatmomentstat", "save file error");
                        }
                    }
                }

                currentSns.mediaList.add(url);
            }
        }

        while (mediaIdxMatcher.find()) {
            boolean flag = true;
            /*for (int i=0;i<currentSns.mediaList.size();i++) {
                if (currentSns.mediaList.get(i).equals(mediaIdxMatcher.group(1))) {
                    flag = false;
                    break;
                }
            }*/
            if (flag) {

                //Object snsObject = this.ModelClass.newInstance();
                //Method fromBinMethod = this.ModelClass.getMethod("FI", byte[].class);
                String url =mediaIdxMatcher.group(1);
                Log.d("wechatmomentstat", "urlidx="+url);
                //url = (String)fromBinMethod.invoke(snsObject, url);
                //Log.d("wechatmomentstat", "url1="+url);
                //url = url.replace("wxpc","webp");
                //currentSns.mediaList.add(url);
            }
        }
        return currentSns;
    }

    static public void parseSnsObject(Object aqiObject, SnsInfo matchSns) throws Throwable{
        Field field = null;
        Object userId=null, nickname=null,ales=null;

        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_USERID_FIELD);
        userId = field.get(aqiObject);


        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_NICKNAME_FIELD);
        nickname = field.get(aqiObject);

        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_TIMESTAMP_FIELD);
        long snsTimestamp = ((Integer) field.get(aqiObject)).longValue();

        if (userId == null || nickname == null) {
            return;
        }

        matchSns.ready = true;
        matchSns.authorName = (String)nickname;
        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_COMMENTS_FIELD);
        LinkedList list = (LinkedList)field.get(aqiObject);
        for (int i=0;i<list.size();i++) {
            Object childObject = list.get(i);
            parseSnsObjectExt(childObject, true, matchSns);
        }

        field = aqiObject.getClass().getField(Config.PROTOCAL_SNS_OBJECT_LIKES_FIELD);
        LinkedList likeList = (LinkedList)field.get(aqiObject);
        for (int i=0;i<likeList.size();i++) {
            Object likeObject = likeList.get(i);
            parseSnsObjectExt(likeObject, false, matchSns);
        }

    }

    static public void parseSnsObjectExt(Object apzObject, boolean isComment, SnsInfo matchSns) throws Throwable {
        if (isComment) {
            Field field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_AUTHOR_NAME_FIELD);
            Object authorName = field.get(apzObject);

            field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_REPLY_TO_FIELD);
            Object replyToUserId = field.get(apzObject);

            field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_COMMENT_FIELD);
            Object commentContent = field.get(apzObject);

            field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_AUTHOR_ID_FIELD);
            Object authorId = field.get(apzObject);

            if (authorId == null || commentContent == null || authorName == null) {
                return;
            }

            for (int i = 0; i < matchSns.comments.size(); i++) {
                SnsInfo.Comment loadedComment = matchSns.comments.get(i);
                if (loadedComment.authorId.equals((String) authorId) && loadedComment.content.equals((String) commentContent)) {
                    return;
                }
            }

            SnsInfo.Comment newComment = new SnsInfo.Comment();
            newComment.authorName = (String) authorName;
            newComment.content = (String) commentContent;
            newComment.authorId = (String) authorId;
            newComment.toUserId = (String) replyToUserId;

            SnsInfo.Like newLike = new SnsInfo.Like();
            newLike.userId = (String)authorId;
            //if(authorName!=null)
                newLike.userName = (String)authorName;
            //else
                //newLike.userName = (String)authorId;
            if(Config.currentUserId.equals((String)matchSns.authorId)) {
                boolean skip =false;
                //Log.d("wechatmomentstat", "like me isCurrentUser ");
                for (int i = 0; i < matchSns.likeme.size(); i++) {
                    if (matchSns.likeme.get(i).userId.equals((String)authorId)) {
                        skip=true;
                    }
                }
                if(!skip)
                    matchSns.likeme.add(newLike);
            }


            for (int i = 0; i < matchSns.comments.size(); i++) {
                SnsInfo.Comment loadedComment = matchSns.comments.get(i);
                if (replyToUserId != null && loadedComment.authorId.equals((String) replyToUserId)) {
                    newComment.toUser = loadedComment.authorName;
                    break;
                }
            }

            matchSns.comments.add(newComment);
        } else {
            Field field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_AUTHOR_NAME_FIELD);
            Object nickname = field.get(apzObject);
            field = apzObject.getClass().getField(Config.SNS_OBJECT_EXT_AUTHOR_ID_FIELD);
            Object userId = field.get(apzObject);
            if (nickname == null || userId == null) {
                return;
            }

            if (((String)userId).equals("")) {
                return;
            }

            SnsInfo.Like newLike = new SnsInfo.Like();
            newLike.userId = (String)userId;
            //if(nickname!=null)
                newLike.userName = (String)nickname;
            //else
                //newLike.userName = (String)userId;
            //Log.d("wechatmomentstat", "like me isCurrentUser check");
            //Log.d("wechatmomentstat", "currentuserid "+Config.currentUserId);
            //Log.d("wechatmomentstat", "authorId"+matchSns.authorId);
            if(Config.currentUserId.equals((String)matchSns.authorId)) {
                boolean skip =false;
                //Log.d("wechatmomentstat", "like me isCurrentUser ");
                for (int i = 0; i < matchSns.likeme.size(); i++) {
                    if (matchSns.likeme.get(i).userId.equals((String)userId)) {
                        skip=true;
                    }
                }
                if(!skip)
                    matchSns.likeme.add(newLike);
            }

            for (int i = 0; i < matchSns.likes.size(); i++) {
                if (matchSns.likes.get(i).userId.equals((String)userId)) {
                    return;
                }
            }
            matchSns.likes.add(newLike);

        }
    }

    static public String getTimelineId(String xmlResult) {
        Pattern idPattern = Pattern.compile("<id><!\\[CDATA\\[(.+?)\\]\\]></id>");
        Matcher idMatcher = idPattern.matcher(xmlResult);
        if (idMatcher.find()) {
            return idMatcher.group(1);
        } else {
            return "";
        }
    }
}
