package com.example.vaccinefinderapp2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
Button searchVaccine;
EditText pinCodeIn;
DatePicker dateIn;
private Spinner dropdown;
private ProgressBar progress;
static String Message=null;
final static Gson gson = new Gson();
NotificationManager notificationManager;
private static ObjectMapper mapper = new ObjectMapper();
static boolean centerFound=false;
static ArrayList<String>  AvailableCentres = new ArrayList<>();
RadioButton radio1, radio2;
String searchType=null;
String URL=null;
ExecutorService executor = Executors.newFixedThreadPool(2);
Network network;
Cache cache;
static boolean musicEnabled=false;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchVaccine = findViewById(R.id.Search);
        Button one = (Button) this.findViewById(R.id.Search);
        pinCodeIn = findViewById(R.id.PinCode);
        radio1 = findViewById(R.id.radioButton);
        radio2 = findViewById(R.id.radioButton2);
        dateIn = findViewById(R.id.date);
        progress= findViewById(R.id.progressBar);
        dropdown = findViewById(R.id.spinner1);
        String[] items = new String[]{"45", "18"};
        progress.setMax(Integer.MAX_VALUE-1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        searchVaccine.setEnabled(true);
        checkBatteryOptimize();
         searchVaccine.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 network = new BasicNetwork(new HurlStack());
                 cache = new DiskBasedCache(getCacheDir(), 2 * 2);
                 if ((radio1.isChecked() || radio2.isChecked())&&!pinCodeIn.getText().toString().isEmpty())
                 {
                     executeOnClick();
             }
                 else
                 {
                     radio2.setError("Please Select SearchType");
                     pinCodeIn.setError("Please enter search code");
                 }
         }

        });
    }

    public synchronized void performRecursiveSearch(DatePicker dateIn ,Network network,Cache cache)
    {

        {
            final boolean[] count = {true};
            String currentDate = getCurrentDate(dateIn);
            if(searchType.equalsIgnoreCase("pincode"))
                URL = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/calendarByPin?pincode=" + pinCodeIn.getText().toString()
                    + "&date=" + currentDate;
            else
                URL = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/calendarByDistrict?district_id=" + pinCodeIn.getText().toString()
                        + "&date=" + currentDate;
            try {

                JsonObjectRequest objectRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        URL,
                        null,
                        new Response.Listener<JSONObject>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("Rest Response", response.toString());
                                findCentres(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("Rest Response ", error.toString());
                            }
                        }

                );
                RequestQueue queue = new RequestQueue(cache, network);
                queue.start();
                queue.add(objectRequest);
            }
            catch (Exception e)
            {}
        }
    }
        @RequiresApi(api = Build.VERSION_CODES.O)
        public synchronized void findCentres(JSONObject response)
        {
            try {
                Integer capactity = 0;
                Example example = gson.fromJson(response.toString(), Example.class);
                try {
                    example = buildObjectFromJSON(response.toString(), Example.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                Integer ageLimit = Integer.parseInt(dropdown.getSelectedItem().toString());
                for (Center c : example.getCenters()) {
                    if (null != c.getSessions() && c.getSessions().size() > 0) {
                        for (Session s : c.getSessions()) {
                            if (null != s.getAvailableCapacity() && s.getAvailableCapacity() > 2 && s.getMinAgeLimit() <= ageLimit) {
                                System.out.println("Slot available at ::" + c.getName() + " time ::" + s.getDate() + " for vaccine ::" + s.getVaccine());
                                Message="Slot available at::" + c.getName() + " for vaccine ::" + s.getVaccine();
                                if(!AvailableCentres.contains(c.getName() + " for vaccine ::" + s.getVaccine()+" ")) {
                                    AvailableCentres.add(c.getName() + " for vaccine ::" + s.getVaccine() + " ");
                                }
                                centerFound=true;
                                try {
                                    if(!musicEnabled)
                                    {
                                        musicEnabled=true;
                                        addNotification(Message);
                                        MediaPlayer mediaPlayer = new MediaPlayer();
                                        mediaPlayer = MediaPlayer.create(this, R.raw.thor);
                                        mediaPlayer.start();
                                        Thread.sleep(30000L);
                                        mediaPlayer.stop();
                                        mediaPlayer.release();
                                    }
                                    searchVaccine.setEnabled(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                capactity += s.getAvailableCapacity();

                            }
                        }
                    }
                }
                System.out.println("time:: " + new Date() + " capacity found ::" + capactity);
                System.out.println(new Date());
            }
            catch (Exception e)
            {}
        }

        public static <T> T buildObjectFromJSON(String json, Class<T> t) throws JsonProcessingException {
            ObjectMapper mapperObj = mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            return (T) mapperObj.readValue(json, t);

        }

    public String getCurrentDate(DatePicker date){
        StringBuilder builder=new StringBuilder();;
        builder.append(date.getDayOfMonth()+"-");
        builder.append((date.getMonth() + 1)+"-");
        builder.append(date.getYear());
        return builder.toString();
    }

    public void MessageBox(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkBatteryOptimize()
    {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addNotification(String message) throws PendingIntent.CanceledException {


        Intent intent = new Intent(this, NotificationView.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "com.example.vaccine")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Vaccine Finder Notification")
                .setContentText(AvailableCentres.toString())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(101, builder.build());
        String notification = getResources().getString(R.string.notification_activity);
        builder.setContentIntent(pendingIntent);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void createNotificationChannel(String id, String name,
                                             String description) {

        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel =
                new NotificationChannel(id, name, importance);

        channel.setDescription(description);
        channel.enableLights(true);
        channel.setLightColor(Color.RED);
        channel.enableVibration(true);
        channel.setVibrationPattern(
                new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        notificationManager.createNotificationChannel(channel);
    }

    public String onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioButton:
                if (checked)
                    searchType="pincode";
                    break;
            case R.id.radioButton2:
                if (checked)
                    searchType="district";
                    break;
        }
        return searchType;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void executeOnClick()
    {

        searchVaccine.setEnabled(false);
        notificationManager =
                (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel("com.example.vaccine", "Vaccine Finder", "Vaccine Availbale Notify");

        int toastDurationInMilliSeconds = 20000;

        MessageBox("Thanks We will notify you with a music once vaccine gets available");
        try {
            executor.submit(new Runnable() {
                @Override
                public void run() {

                    if (!centerFound) {
                        for (int i = 0; i < Integer.MAX_VALUE; i++) {
                            final int value = i;
                            try {
                                Thread.sleep(20000L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            performRecursiveSearch(dateIn, network, cache);

                            progress.post(new Runnable() {
                                @Override
                                public void run() {
                                    // pinCodeIn.setText("Updating");
                                    progress.setProgress(value);
                                    System.out.println(value);
                                }
                            });
                        }
                    } else {
                        Thread.interrupted();
                    }
                }
            });

        } catch (Exception e) {
        }
    }


}

