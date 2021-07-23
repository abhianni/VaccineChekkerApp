package com.example.vaccinefinderapp2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

public class NotificationView extends MainActivity{
    TextView textView;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onNewIntent (Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras() ;
        setContentView(R.layout.activity_notification_view);
      {
                String message=AvailableCentres.toString();
                textView = findViewById(R.id.textView);
                textView.setText(message);

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState) ;
        setContentView(R.layout. activity_notification_view ) ;
        onNewIntent(getIntent()) ;
    }
}
