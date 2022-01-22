//  https://www.youtube.com/watch?v=3WRm2DveWJE

package com.example.clock_plus;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TextClock textClock, textDate;

    private TextView mTextViewResult;
    private RequestQueue mQueue;
    private TextView update_request_time;
    private ImageView plotImageView;

    int dayOfWeek = 7;

    @SuppressLint("WrongViewCast")

    //  < ************  loop interval

    private int mInterval = 500000; // 5 seconds by default, can be changed later
    private int loops = 0; // counter

    private Handler mHandler; //    loop interval

    private Timer autoUpdate;

    //  < ************  loop interval

    @Override

    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();

        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        jsonParse();
                        moveWeekDay();
                        plot();
                    }
                });
            }
        }, 0, mInterval); // updates each 40 secs
    }

    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
    }

    //  /> ************  loop with delay

    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_main);


//  ***************    https://coderoad.ru/25556668/Два-варианта-обновления-TextView-действия-из-другого-класса-с-помощью-TimerTask

        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);

        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(LayoutParams.FLAG_KEEP_SCREEN_ON, LayoutParams.FLAG_KEEP_SCREEN_ON);

        textClock = findViewById(R.id.simpleDigitalClockTime);
        textDate = findViewById(R.id.simpleDigitalClockDay);

        TextView textDebug = (TextView) findViewById(R.id.debugText);
        textDebug.setText("");

//        ll = getApplicationContext().getAssets();
//        Log.d ("ll","ll");
//        Typeface myfont = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/cambriab.ttf");
//        textClock.setTypeface(myfont);
//        textDate.setTypeface(myfont);

        mTextViewResult = findViewById(R.id.debugText);
        Button buttonParse = findViewById(R.id.button_parse);

        mQueue = Volley.newRequestQueue(this);
        jsonParse();
        moveWeekDay();
//        mHandler = new Handler();
//        startRepeatingTask();

        buttonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
//                Log.d("@@ 02. onClick", "01. On click");
                moveWeekDay();
                jsonParse();
                plot();
            }
        });

    }

    //    private void batteryLevel()
//    {
//        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
//        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
//    }

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();


    private void plot()
    {
        plotImageView = findViewById(R.id.plotImageView);

        String  plotImageUrl = "https://gpxlab.ru/api/yw.php";
        Glide.with(this).load(plotImageUrl)
//                .transition(DrawableTransitionOptions.withCrossFade(factory))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .thumbnail(
//                        Glide // this thumbnail request has to have the same RESULT cache key
//                                .with(this) // as the outer request, which usually simply means
//                                .load(plotImageUrl) // same size/transformation(e.g. centerCrop)/format(e.g. asBitmap)
//                                .fitCenter() // have to be explicit here to match outer load exactly
//                                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                                .skipMemoryCache(true)
//                )
                .skipMemoryCache(true)
                .into(plotImageView);
    }


    private void moveWeekDay() {

        TextView movedView = findViewById(R.id.WeekDayMoved);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int screenHeight = metrics.heightPixels;
        int screenWidth = metrics.widthPixels;

        Calendar c = Calendar.getInstance();
        dayOfWeek = c.get(Calendar.DAY_OF_WEEK) + 5;
        movedView.setX((float) ((dayOfWeek % 7 * screenWidth) / 6.6));

    }

    private void jsonParse() {
//        String url = "http://gpxlab.ru/data/yandex_weather_forecast.json";

//        String url = "https://api.weather.yandex.ru/v2/informers?lat=55.692&lon=37.347";

        String url = "https://gpxlab.ru/api/clock.php?lat=55.692&lon=37.347";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {

                    @Override

                    public void onResponse(JSONObject response) {

                        Log.d("@@ 06.loop end=", String.valueOf(dayOfWeek));

                        SimpleDateFormat format = new SimpleDateFormat("yy MM dd HH:mm:ss");

                        Calendar c = Calendar.getInstance();
                        String update_string = format.format(c.getTime());

                        update_request_time = (TextView) findViewById(R.id.update_request_time);
                        update_request_time.setText(update_string);

                        JSONArray jsonArray = null;
                        JSONObject jsonObj = null;


                        try {
                            String viewName = "now_dt";
                            String stringMetricValue = null;
                            String intMetricValue = null;

                            int id = 0;
                            TextView view = null;

                            stringMetricValue = response.getString(viewName);

                            String utcTime = stringMetricValue;

                            id = getResources().getIdentifier(viewName, "id", getPackageName());
                            view = (TextView) findViewById(id);
                            view.setText(utcTime.substring(0, 19));

                            jsonObj = response.getJSONObject("fact");

                            String[] weatherMetrics = {"temp", "humidity", "pressure_pa"};

                            for (int i = 0; i < weatherMetrics.length; i++) {

                                viewName = weatherMetrics[i];
                                stringMetricValue = jsonObj.getString(viewName);
                                id = getResources().getIdentifier(viewName, "id", getPackageName());
//                                Log.d("@@ json value", intMetricValue + ", " + String.valueOf(id));
                                view = (TextView) findViewById(id);
                                view.setText(stringMetricValue);

                            }

                            jsonObj = response.getJSONObject("forecast");

                            id = getResources().getIdentifier("sunrise", "id", getPackageName());
                            String sunriseString  = jsonObj.getString("sunrise");
                            view = (TextView) findViewById(id);
                            view.setText(sunriseString);

                            id = getResources().getIdentifier("sunset", "id", getPackageName());
                            String sunsetString  = jsonObj.getString("sunset");
                            view = (TextView) findViewById(id);
                            view.setText(sunsetString);


                            format = new SimpleDateFormat("HH:mm");
                            Date sunriseStringParsed = format.parse(sunriseString);
                            Date sunsetStringParsed = format.parse(sunsetString);

                            long diff =  sunsetStringParsed.getTime() - sunriseStringParsed.getTime();
                            int hours = (int) (diff / (1000 * 60 * 60));
                            int minutes = (int) (diff / (1000 * 60))%60;



                            id = getResources().getIdentifier("duration", "id", getPackageName());
                            view = (TextView) findViewById(id);
                            view.setText( String.format("%02d:%02d",hours, minutes));

                            jsonArray = jsonObj.getJSONArray("parts");
                            JSONObject forecast2 = jsonArray.getJSONObject(1);

//                            {
//                                "part_name":"morning", "temp_min":-11, "temp_avg":-11, "temp_max":
//                                -10, "wind_speed":2.5, "wind_gust":4.6, "wind_dir":
//                                "s", "pressure_mm":738, "pressure_pa":983, "humidity":89, "prec_mm":
//                                0, "prec_prob":0, "prec_period":360, "icon":"ovc", "condition":
//                                "overcast", "feels_like":-16, "daytime":"d", "polar":false
//                            }


                            String[] foreCastMetric = {
                                    "feels_like",
                                    "prec_mm", "prec_prob",
                                    "wind_dir", "wind_speed",
                                    "temp_avg", "temp_min",
                                    "pressure_pa",
                                    "humidity"};

//                            Log.d("@@ 07.", String.valueOf(forecast2));

                            for (int i = 0; i < foreCastMetric.length; i++) {

                                viewName = foreCastMetric[i];
                                Log.d("@@ 07.", String.valueOf(viewName));
                                stringMetricValue = forecast2.getString(viewName);
                                id = getResources().getIdentifier("forecast_" + viewName, "id", getPackageName());
                                view = (TextView) findViewById(id);
                                view.setText(stringMetricValue);

                            }


                            String now_dt = response.getString("now_dt").substring(0, 10);
                            mTextViewResult.setText(now_dt + "\n");


//                            Log.e("myTag", "05." + String.valueOf(jsonObj.length()));

                            jsonArray = jsonObj.getJSONArray("parts");
//                            Log.e("myTag", "06." + String.valueOf(jsonArray.length()));

                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject parts = null;
                            try {
                                parts = jsonArray.getJSONObject(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            String part_name = null;
                            try {
                                part_name = parts.getString("part_name");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            int temp_avg = 0;
                            try {
                                temp_avg = parts.getInt("temp_avg");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            mTextViewResult.append(part_name + ",  " + String.valueOf(temp_avg) + "\n");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("@@ 04.", "onErrorResponse" + error);
                        error.printStackTrace();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("X-Yandex-API-Key", "3623bcf2-1366-4096-93ea-2d2186989a5c");
                return params;
            }
        };

        mQueue.add(request);

    }

}