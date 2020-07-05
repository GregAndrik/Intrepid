package tk.gregory.intrepid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;

//Implementing multiple Listeners efficiently
public class MainMenu extends AppCompatActivity implements View.OnClickListener {
    /**
     * This class serves as the project's main class and the application's welcome screen
     * All permission checks-requests are created here for global use
     * Is father of classes AudioRecorder, SpeechToText, SpeechToWeb and AudioPlayer by extending AudioRecorder
     **/
    public static final int REQUEST_PERMISSION_CODE = 1000;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu); //Pointing to the corresponding XML activity

        Button audioRecorder = findViewById(R.id.audioRecorder);
        Button speechToText = findViewById(R.id.speechToText);
        Button speechToWeb = findViewById(R.id.speechToWeb);
        Button voiceCommands = findViewById(R.id.voiceCommands);

        audioRecorder.setOnClickListener(this);
        speechToText.setOnClickListener(this);
        speechToWeb.setOnClickListener(this);
        voiceCommands.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        //Picking an activity to load
        switch (v.getId()) {

            case R.id.audioRecorder:
                buttonClickTiming();
                startActivity(new Intent(MainMenu.this, AudioRecorder.class));
                break;

            case R.id.speechToText:
                buttonClickTiming();
                startActivity(new Intent(MainMenu.this, SpeechToText.class));
                break;

            case R.id.speechToWeb:
                buttonClickTiming();
                startActivity(new Intent(MainMenu.this, SpeechToWeb.class));
                break;

            case R.id.voiceCommands:
                buttonClickTiming();
                startActivity(new Intent(MainMenu.this, VoiceCommands.class));
                break;

        }
    }

    //Method programming frequency of acceptable button clicks
    public void buttonClickTiming() {
        //Prevention of button mushing by using threshold of 1000 ms between clicks
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

    //Method requesting permissions
    public void requestPermissions() {
        ActivityCompat.requestPermissions(this,new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
        }, REQUEST_PERMISSION_CODE);
    }

    //Method checking whether permissions have been granted
    public boolean checkPermissions() {
        int writeExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int recordAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return writeExternalStorage == PackageManager.PERMISSION_GRANTED && recordAudio == PackageManager.PERMISSION_GRANTED;
    }

    //Method returning user's input regarding requested permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
                finish(); //Killing current activity so permissions are set
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}