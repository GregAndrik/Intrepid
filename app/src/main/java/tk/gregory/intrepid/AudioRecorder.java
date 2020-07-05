package tk.gregory.intrepid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AudioRecorder extends MainMenu {
    /**
     * This class serves as an audio recorder
     * Is child of MainMenu class
     **/
    public Button recordBtn;
    public ImageButton nameBtn;
    public TextView recordLabel;
    public String mediaFileName = null;
    public static String inputName = "recorded_audio"; //Default file name
    public static String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());

    private MediaRecorder mediaRecorder;
    private Chronometer chronometer;
    private long pauseOffset;
    private long lastClickTime = 0;
    private boolean running = false;
    private boolean isRecording = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_recorder); //Pointing to the corresponding XML activity

        recordBtn = findViewById(R.id.recordBtn);
        nameBtn = findViewById(R.id.nameBtn);
        recordLabel = findViewById(R.id.recordLabel);
        chronometer = findViewById(R.id.chronometer);

        //Performs permission-check on every launch
        if (checkPermissions()) {

            chronometer.setBase(SystemClock.elapsedRealtime());

            nameBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialog();
                }
            });

            filePathMaking();

            //Setting up recordBtn
            recordBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {

                        //User stops touching the button
                        case MotionEvent.ACTION_UP:
                            if (isRecording) {
                                stopRecording();
                                recordLabel.setTextColor(getColor(R.color.coal));
                                recordLabel.setText(getString(R.string.hold_the_mic_and_record));
                                chronometer.setTextColor(getColor(R.color.coal));
                                stopChronometer();

                                File file = new File(mediaFileName);
                                int file_size = Integer.parseInt(String.valueOf(file.length()));

                                if(file_size >= 4096) {
                                    //Checking the created file's size in bytes to determine whether the recorded clip is long enough to be reproducible by the MediaPlayer API
                                    finish(); //Finishes activity.
                                    startActivity(getIntent()); //Restarts activity in order to reset chronometer
                                    startActivity(new Intent(AudioRecorder.this, AudioPlayer.class)); //Once recording is complete loads AudioPlayer activity
                                } else {
                                    resetChronometer();
                                }
                                isRecording = false;
                            }
                            break;

                        //User touches the button
                        case MotionEvent.ACTION_DOWN:
                            //Prevention of button mushing by using threshold of 1000 ms between clicks
                            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                return true;
                            }
                            lastClickTime = SystemClock.elapsedRealtime();

                            if (!isRecording) {
                                startRecording();
                                recordLabel.setTextColor(getColor(R.color.scarlet));
                                recordLabel.setText(getString(R.string.recording));
                                chronometer.setTextColor(getColor(R.color.scarlet));
                                resetChronometer();
                                startChronometer();
                                isRecording = true;
                            }
                            break;

                    }
                    return false;
                }
            });

        } else {
            requestPermissions(); //Requesting permissions if not already registered
        }
    }

    //Method for audio recording
    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setOutputFile(mediaFileName);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(96000);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(AudioRecorder.this, "Unable to record...", Toast.LENGTH_SHORT).show();
        }
        mediaRecorder.start();
    }

    //Method stopping audio recording
    private void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Method starting the chronometer
    public void startChronometer() {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }

    //Method stopping the chronometer
    public void stopChronometer() {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    //Method resetting the chronometer
    public void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }

    //Method called when the app is required to set/use the directory path
    public void filePathMaking() {
        String folder_main = "Intrepid/Recordings"; //Creates folder path
        File file = new File(Environment.getExternalStorageDirectory(), folder_main);
        file.mkdirs();
        mediaFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mediaFileName += "/Intrepid/Recordings/" + inputName + "_" + timeStamp + ".mp3"; //Specifies file path on the device and the name of the created audio file
    }

    //Method called when user is asked to input a name
    public void openDialog() {
        @SuppressLint("InflateParams") View view = (LayoutInflater.from(AudioRecorder.this)).inflate(R.layout.audio_name_input, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(AudioRecorder.this);
        alertBuilder.setView(view);
        final EditText userInput = view.findViewById(R.id.userInput);

        alertBuilder.setCancelable(true);
        alertBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                inputName = String.valueOf(userInput.getText());
                if (!inputName.isEmpty()) {
                    Toast.makeText(AudioRecorder.this, "Next audio clip will be named... " + inputName, Toast.LENGTH_SHORT).show();
                    filePathMaking();
                } else {
                    inputName = "recorded_audio";
                    Toast.makeText(AudioRecorder.this, "Input field empty, next audio clip will be named... " + inputName, Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });

        AlertDialog dialog = alertBuilder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(Color.parseColor("#FF494949"));

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(Color.parseColor("#FF494949"));
    }

}