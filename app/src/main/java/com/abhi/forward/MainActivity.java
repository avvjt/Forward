package com.abhi.forward;


import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // webView to display website in app
        webView = findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("https://avvjt.netlify.app/");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Initialize Firebase components
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if device name is already stored in Firebase
        mDatabase.child("devices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean deviceNameExists = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String deviceName = snapshot.getValue(String.class);
                    if (deviceName != null && deviceName.equals(getDeviceName())) {
                        deviceNameExists = true;
                        break;
                    }
                }
                if (!deviceNameExists) {
                    // Push device name to Firebase only if it doesn't exist
                    pushDeviceNameToFirebase(getDeviceName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });


    }




    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isLowerCase(first)) {
            return Character.toUpperCase(first) + s.substring(1);
        } else {
            return s;
        }
    }

    private void pushDeviceNameToFirebase(String deviceName) {
        // Push device name to Firebase under a specific node
        mDatabase.child("devices").push().setValue(deviceName);
    }


    public class mywebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view,url,favicon);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }


}