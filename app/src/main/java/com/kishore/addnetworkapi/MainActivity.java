package com.kishore.addnetworkapi;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText mSSIDEditText;
    private EditText mPasswordEditText;
    private String ssidName = "";
    private String password = "";
    List<WifiNetworkSuggestion> suggestionList = null;
    WifiManager wifiManager = null;
    final static private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSSIDEditText = (EditText) findViewById(R.id.editText);
        mPasswordEditText = (EditText) findViewById(R.id.editText2);
        Button mConnectButton = (Button) findViewById(R.id.button);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ssidName = mSSIDEditText.getText().toString();
                password = mPasswordEditText.getText().toString();
                buildSuggestion();
            }
        });

    }

    @TargetApi(Build.VERSION_CODES.Q)
    private void buildSuggestion() {
        //      Configuring wifi using WifiNetworkSuggestion
        WifiNetworkSuggestion suggestion = null;
        if(password.equals("")){

            suggestion = new WifiNetworkSuggestion.Builder()
                    .setSsid(ssidName)
                    .build();

        }else{
            suggestion =
                    new WifiNetworkSuggestion.Builder()
                            .setSsid(ssidName)
                            .setWpa2Passphrase(password)
                            .setIsAppInteractionRequired(true)
                            .setPriority(1000)
                            .build();
        }

        suggestionList = new ArrayList<WifiNetworkSuggestion>();
        suggestionList.add(suggestion);
        configureNetworkSuggestion();

    }

    //    To Receive the result from Setting
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void onActivityResult(ActivityResult result) {

                    if(result.getResultCode() == Activity.RESULT_OK){
                        Toast.makeText(getApplicationContext(), "Configuration success", Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"Configuration success");
                        if(result.getData() != null && result.getData().hasExtra(Settings.EXTRA_WIFI_NETWORK_RESULT_LIST)){
                            for (Integer code : result.getData().getIntegerArrayListExtra(Settings.EXTRA_WIFI_NETWORK_RESULT_LIST))
                            {
                                if (code == Settings.ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED){
                                    Log.d(TAG, "Result of ACTION_WIFI_ADD_NETWORKS : Invalid configuration");
                                }
                                else
                                {
                                    if(code == Settings.ADD_WIFI_RESULT_SUCCESS){
                                        Log.d(TAG, "Result of ACTION_WIFI_ADD_NETWORKS : Configuration saved or modified");
                                    } else if(code == Settings.ADD_WIFI_RESULT_ALREADY_EXISTS){
                                        Log.d(TAG, "Result of ACTION_WIFI_ADD_NETWORKS : Configuration existed on device, nothing changed");
                                    }
                                }
                            }
                        }
                    }
                    else if(result.getResultCode() == Activity.RESULT_CANCELED){
                        Toast.makeText(getApplicationContext(), "Failed to Configure", Toast.LENGTH_SHORT).show();
                        Log.d("Main","Configuration Not success");
                        Log.d(TAG,"User Denied, Add Network");
                    }
                }
            });

    @TargetApi(Build.VERSION_CODES.Q)
    private void configureNetworkSuggestion() {
        //------------------------WifiSuggestionApi-----------------------------

        if (Build.VERSION.SDK_INT > 30) {
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(Settings.EXTRA_WIFI_NETWORK_LIST, (ArrayList<? extends Parcelable>) suggestionList);
            final Intent intent = new Intent(Settings.ACTION_WIFI_ADD_NETWORKS);
            intent.putExtras(bundle);
            activityResultLauncher.launch(intent);
        }
        else {
            printStatus(wifiManager.addNetworkSuggestions(suggestionList));
            printStatus(wifiManager.removeNetworkSuggestions(suggestionList));
        }
    }

    public void printStatus(int status){
        if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS){
            Log.i("Main","success");
        }else if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_DUPLICATE){
            Log.i("Main","Duplicate");
        }else if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_ADD_EXCEEDS_MAX_PER_APP){
            Log.i("Main","Exceeds max per app");
        }else if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_APP_DISALLOWED){
            Log.i("Main","Disallowed");
        }else if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_INTERNAL){
            Log.i("Main","Internal");
        }else if(status == WifiManager.STATUS_NETWORK_SUGGESTIONS_ERROR_REMOVE_INVALID){
            Log.i("Main","Remove Invalid");
        }else {
            Log.i("Main","Not Success");
        }
    }
}