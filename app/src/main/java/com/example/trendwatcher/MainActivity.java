package com.example.trendwatcher;

import com.example.trendwatcher.OsakaTrendAsyncLoader;
import com.example.trendwatcher.GetUserTweet;

import android.hardware.SensorEventListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;
import android.content.Intent;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;



import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;



public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView textView, textInfo;

    @Override
    protected void onResume() {
        super.onResume();
        // Listenerの登録
        Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if(gyro != null){
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_UI);
        }
        else{
            String ns = "No Support";
            textView.setText(ns);
        }
    }

    // 解除するコードも入れる!
    @Override
    protected void onPause() {
        super.onPause();
        // Listenerを解除
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("debug","onSensorChanged");

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float sensorX = event.values[0];
            float sensorY = event.values[1];
            float sensorZ = event.values[2];

            String strTmp = String.format(Locale.US, "Gyroscope\n " +
                    " X: %f\n Y: %f\n Z: %f",sensorX, sensorY, sensorZ);
            textView.setText(strTmp);

            showInfo(event);
        }

    }

    // センサーの各種情報を表示する
    private void showInfo(SensorEvent event){
        // センサー名
        StringBuffer info = new StringBuffer("Name: ");
        info.append(event.sensor.getName());
        info.append("\n");

        // ベンダー名
        info.append("Vendor: ");
        info.append(event.sensor.getVendor());
        info.append("\n");

        // 型番
        info.append("Type: ");
        info.append(event.sensor.getType());
        info.append("\n");

        // 最小遅れ
        int data = event.sensor.getMinDelay();
        info.append("Mindelay: ");
        info.append(String.valueOf(data));
        info.append(" usec\n");

        // 最大遅れ
        data = event.sensor.getMaxDelay();
        info.append("Maxdelay: ");
        info.append(String.valueOf(data));
        info.append(" usec\n");

        // レポートモード
        data = event.sensor.getReportingMode();
        String stinfo = "unknown";
        if(data == 0){
            stinfo = "REPORTING_MODE_CONTINUOUS";
        }else if(data == 1){
            stinfo = "REPORTING_MODE_ON_CHANGE";
        }else if(data == 2){
            stinfo = "REPORTING_MODE_ONE_SHOT";
        }
        info.append("ReportingMode: ");
        info.append(stinfo);
        info.append("\n");

        // 最大レンジ
        info.append("MaxRange: ");
        float fData = event.sensor.getMaximumRange();
        info.append(String.valueOf(fData));
        info.append("\n");

        // 分解能
        info.append("Resolution: ");
        fData = event.sensor.getResolution();
        info.append(String.valueOf(fData));
        info.append(" m/s^2\n");

        // 消費電流
        info.append("Power: ");
        fData = event.sensor.getPower();
        info.append(String.valueOf(fData));
        info.append(" mA\n");

        textInfo.setText(info);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        textInfo = findViewById(R.id.text_info);

        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    public static class PlaceholderFragment extends Fragment implements LoaderCallbacks<String> {
   /* public static class PlaceholderFragment extends Fragment { */

        // Twitterオブジェクト
        private Twitter twitter = null;

        // ローディング表示用ダイアログ
        private ProgressDialog progressDialog = null;

        public PlaceholderFragment() {

            // OAuth認証用設定（1）
            ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();

            configurationBuilder.setOAuthConsumerKey("tVt7q8NvBZJlGFtJC19Mim4rv");
            configurationBuilder.setOAuthConsumerSecret("vTw3OsWpyGaCmyUTZAFPo7pkqNGpKLwUqbMrcFwJJyKJMSCEts");
            configurationBuilder.setOAuthAccessToken("1115672515966160896-jy5SY4ZPJC6ItqoC1KFP5A5UUijUkh");
            configurationBuilder.setOAuthAccessTokenSecret("wnrKoLyHj2fXceLlAeNrRmfElOMbeez5KKfWqO4IxKnJE");

            // Twitterオブジェクトの初期化（2）
            this.twitter =
                    new TwitterFactory(configurationBuilder.build()).getInstance();
        }

        // ローディングダイアログの消去
        private void dialogDismiss(){
            if (this.progressDialog != null) {
                this.progressDialog.dismiss();
                this.progressDialog = null;
            }
        }




        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            super.onActivityCreated(savedInstanceState);

            // 検索ボタンのイベントリスナーを設定する
            ((Button)getActivity().findViewById(R.id.button1))
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // ローダーの開始
                            getLoaderManager().restartLoader(0, null, PlaceholderFragment.this);
                        }
                    });
/*
            ((Button)getActivity().findViewById(R.id.button2))
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // ローダーの開始
                            //getLoaderManager().restartLoader(0, null, PlaceholderFragment.this);

                         //   Intent intent = new Intent(getApplication(), gyro.class);
                          //  startActivity(intent);

                            new AlertDialog.Builder(getActivity())
                                    .setTitle("title")
                                    .setMessage("message")
                                    .setPositiveButton("OK", null)
                                    .show();



                        }


                    });
*/


            setRetainInstance(true);
        }


        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            // ローディングダイアログの表示
            this.progressDialog = ProgressDialog.show(getActivity(), "Please wait", "Loading data...");

            GetUserTweet loader = null;

            switch (id) {
                case 0:
                    // ローダーの初期化
                    loader = new GetUserTweet(getActivity(), this.twitter);
                    loader.forceLoad();
                    break;
            }
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<String> loader,	String data) {

            if (data != null) {
/*
                // アダプターの初期化
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1);

                // アダプターにTweetをセットする
                for (Status tweet : data) {
                    adapter.add(tweet.getText());
                }

                // ListViewにアダプターをセットする
                ((TextView) getView().findViewById(R.id.TextView1)).setAdapter(adapter);
            */

                new AlertDialog.Builder(getActivity())
                        .setTitle("title")
                        .setMessage(data)
                        .setPositiveButton("OK", null)
                        .show();
            }

            // ローディングダイアログの消去
            dialogDismiss();
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
        }

        @Override
        public void onPause() {
            super.onPause();

            // ローディングダイアログの消去
            dialogDismiss();
        }


    }
}