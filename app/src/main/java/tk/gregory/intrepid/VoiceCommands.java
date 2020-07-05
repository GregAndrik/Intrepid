package tk.gregory.intrepid;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceCommands extends MainMenu {
    /**
     * This class serves as an implementation of voice commands
     * Is child of MainMenu class
     * */
    public TextView speechLabel;
    private boolean isListening = false;
    private long lastClickTime = 0;
    private String resultString;
    private Intent launch = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_commands); //Pointing to the corresponding XML activity

        speechLabel = findViewById(R.id.speechLabel);

        //Performs permission-check on every launch
        if(checkPermissions()) {

            //Creating and setting up the SpeechRecognizer
            final SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "el-GR");

            speechRecognizer.setRecognitionListener(new RecognitionListener() {

                @Override
                public void onReadyForSpeech(Bundle bundle) {}

                @Override
                public void onBeginningOfSpeech() {}

                @Override
                public void onRmsChanged(float v) {}

                @Override
                public void onBufferReceived(byte[] bytes) {}

                @Override
                public void onEndOfSpeech() {}

                @Override
                public void onError(int i) {}

                @SuppressLint({"SetJavaScriptEnabled"})
                @Override
                public void onResults(Bundle bundle) {
                    //Getting matches
                    ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    if (matches != null) {
                        //Displaying the first match
                        resultString = matches.get(0);
                        openActivity();
                    }
                }

                @Override
                public void onPartialResults(Bundle bundle) {}

                @Override
                public void onEvent(int i, Bundle bundle) {}

            });
            //Setting up speechBtn states
            findViewById(R.id.speechBtn).setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {

                        //User stops touching the button.
                        case MotionEvent.ACTION_UP:
                            speechRecognizer.stopListening();

                            if (isListening) {
                                readiness();
                            }
                            break;

                        //User touches the button.
                        case MotionEvent.ACTION_DOWN:
                            //Prevention of button mushing by using threshold of 1000 ms between clicks
                            if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
                                return true;
                            }
                            lastClickTime = SystemClock.elapsedRealtime();

                            try {
                                speechRecognizer.startListening(speechRecognizerIntent);
                                speechLabel.setTextColor(getColor(R.color.scarlet));
                                speechLabel.setText(getString(R.string.listening));
                                isListening = true;
                            } catch (Exception e){
                                //In case speech is not supported
                                Toast.makeText(getApplicationContext(), "Your device does not support Speech Input", Toast.LENGTH_LONG).show();
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

    //Method searches and opens activity by package
    public void openActivity() {
        if (resultString.contains("camera") || resultString.contains("κάμερα") || resultString.contains("φωτογραφική μηχανή") || resultString.contains("φωτογραφική")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.camera");
            packageNotFound();

        } else if (resultString.contains("calculator") || resultString.contains("αριθμομηχανή")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.popupcalculator");
            packageNotFound();

        } else if (resultString.contains("calendar") || resultString.contains("ημερολόγιο")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.samsung.android.calendar");
            packageNotFound();

        } else if (resultString.contains("Chrome") || resultString.contains("browser") || resultString.contains("φυλλομετρητής")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.android.chrome");
            packageNotFound();

        } else if (resultString.contains("Firefox")) {
            launch = getPackageManager().getLaunchIntentForPackage("org.mozilla.firefox");
            packageNotFound();

        } else if (resultString.contains("clock") || resultString.contains("ρολόι") || resultString.contains("ξυπνητήρι")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.clockpackage");
            packageNotFound();

        } else if (resultString.contains("messages") || resultString.contains("message") || resultString.contains("μηνύματα") || resultString.contains("μήνυμα")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.samsung.android.messaging");
            packageNotFound();

        } else if (resultString.contains("contacts") || resultString.contains("επαφές") || resultString.contains("τηλέφωνα") || resultString.contains("αριθμοί") || resultString.contains("κατάλογος")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.samsung.android.contacts");
            packageNotFound();

        } else if (resultString.contains("email")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.samsung.android.email.provider");
            packageNotFound();

        } else if (resultString.contains("files") || resultString.contains("file") || resultString.contains("αρχεία") || resultString.contains("αρχείο")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.myfiles");
            packageNotFound();

        } else if (resultString.contains("Gallery") || resultString.contains("φωτογραφίες")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.sec.android.gallery3d");
            packageNotFound();

        } else if (resultString.contains("Play Store")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.android.vending");
            packageNotFound();

        } else if (resultString.contains("Instagram")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
            packageNotFound();

        } else if (resultString.contains("messenger")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.facebook.orca");
            packageNotFound();

        } else if (resultString.contains("Facebook")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
            packageNotFound();

        } else if (resultString.contains("Skype")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.skype.raider");
            packageNotFound();

        } else if (resultString.contains("Viber")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.viber.voip");
            packageNotFound();

        } else if (resultString.contains("YouTube")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube");
            packageNotFound();

        } else if (resultString.contains("e-food")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.venturegeeks.efood");
            packageNotFound();

        } else if (resultString.contains("Clash of Clans")) {
            launch = getPackageManager().getLaunchIntentForPackage("com.supercell.clashofclans");
            packageNotFound();

        } else {
            speechLabel.setTextColor(getColor(R.color.coal));
            speechLabel.setText(getString(R.string.sorry_i_did_not_catch_that));
            launch = null;
        }
    }

    //Method used in case of attempting to launch an application that is not installed
    public void packageNotFound() {
        readiness();
        if (launch != null) {
            startActivity(launch); //null pointer check in case package name was not found
        }
    }

    //Method ensuring that speechLabel is not stack on "listening" state
    public void readiness() {
        speechLabel.setTextColor(getColor(R.color.coal));
        speechLabel.setText(getString(R.string.hold_the_mic_and_call_an_app));
    }

    //Method programming frequency of acceptable button clicks
    public void buttonClickTiming() {
        //Prevention of button mushing by using threshold of 1000 ms between clicks
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

}