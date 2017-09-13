package moe.chionlab.wechatmomentstat.gui;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import moe.chionlab.wechatmomentstat.Config;
import moe.chionlab.wechatmomentstat.R;
import moe.chionlab.wechatmomentstat.SnsStat;
import moe.chionlab.wechatmomentstat.Task;
import moe.chionlab.wechatmomentstat.common.Share;
import moe.chionlab.wechatmomentstat.SubThread;

import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    Task task = null;
    SnsStat snsStat = null;
    EditText usernameFileEditText = null;
    TextView usernameText = null;
    SubThread SubThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        task = new Task(this.getApplicationContext());
        usernameFileEditText = (EditText)findViewById(R.id.username);

        if(Config.username.length()<3)
            Config.username = this.getApplicationContext().getSharedPreferences("shared_perf_app", Context.MODE_PRIVATE).getString("username","");

        if(Config.username.length()>3) {
            usernameFileEditText.setText(Config.username);
        }

        setContentView(R.layout.activity_main);

        task.testRoot();

        Config.Context = this.getApplicationContext();

        ((Button)findViewById(R.id.launch_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                usernameFileEditText = (EditText)findViewById(R.id.username);
                Config.username=usernameFileEditText.getText().toString();

                SharedPreferences.Editor editor = Config.Context.getSharedPreferences("shared_perf_app", Context.MODE_PRIVATE).edit();
                editor.putString("username", Config.username);
                editor.commit();

                ((Button) findViewById(R.id.launch_button)).setText(R.string.exporting_sns);
                ((Button) findViewById(R.id.launch_button)).setEnabled(false);
                new RunningTask().execute();
            }
        });

        TextView descriptionHtmlTextView = (TextView)findViewById(R.id.description_html_textview);
        descriptionHtmlTextView.setMovementMethod(LinkMovementMethod.getInstance());
        descriptionHtmlTextView.setText(Html.fromHtml(getResources().getString(R.string.description_html)));

    }

    class RunningTask extends AsyncTask<Void, Void, Void> {

        Throwable error = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {

                task.copySnsDB();
                task.initSnsReader();
                task.snsReader.run();

                snsStat = new SnsStat(task.snsReader.getSnsList());

                //Thread intervalSaveThread = null;

                //task.SubThread.run();

            } catch (Throwable e) {
                this.error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voidParam) {
            super.onPostExecute(voidParam);
            ((Button)findViewById(R.id.launch_button)).setText(R.string.launch);
            ((Button) findViewById(R.id.launch_button)).setEnabled(true);
            if (this.error != null) {
                Toast.makeText(MainActivity.this, R.string.not_rooted, Toast.LENGTH_LONG).show();
                Log.e("wechatmomentstat", "exception", this.error);
                try {
                       ((TextView)findViewById(R.id.description_textview_2)).setText("Error: " + this.error.getMessage());
                    } catch (Throwable e) {
                        Log.e("wechatmomentstat", "exception", e);
                    }

                return;
            }

            Share.snsData = snsStat;
            Intent intent = new Intent(MainActivity.this, MomentStatActivity.class);
            startActivity(intent);
        }
    }



}
