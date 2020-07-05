package tk.gregory.intrepid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SpeechToText extends MainMenu {
    /**
     * This class serves as a Speech inputName to text
     * Saves voice inputName into text
     * Is child of MainMenu class
     **/
    private String exportText = null;
    private String inputName = null;
    private EditText resultText;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_to_text); //Pointing to the corresponding XML activity

        //Performs permission-check on every launch
        if(checkPermissions()) {

            resultText = findViewById(R.id.resultText);
            final TextView actionText = findViewById(R.id.actionText);
            final ImageButton saveBtn = findViewById(R.id.saveBtn);

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

                @Override
                public void onResults(Bundle bundle) {
                    //Getting matches
                    ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                    if (matches != null) {
                        resultText.append(" " + matches.get(0)); //Displaying the first match
                        resultText.setMovementMethod(new ScrollingMovementMethod());
                        resultText.setSelection(resultText.getText().length());
                        while (resultText.canScrollVertically(1)) {
                            resultText.scrollBy(0, 10);
                        }
                        resultText.setTextColor(getColor(R.color.scarlet));
                    }
                }

                @Override
                public void onPartialResults(Bundle bundle) {}

                @Override
                public void onEvent(int i, Bundle bundle) {}

            });

            //Setting up speechBtn
            findViewById(R.id.speechBtn).setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {

                        //User stops touching the button.
                        case MotionEvent.ACTION_UP:
                            speechRecognizer.stopListening();
                            actionText.setHintTextColor(getColor(R.color.coal));
                            actionText.setHint("You will see your input above");
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
                                actionText.setText("");
                                actionText.setHintTextColor(getColor(R.color.scarlet));
                                actionText.setHint("Listening...");
                            } catch (Exception e){
                                //In case speech is not supported
                                Toast.makeText(getApplicationContext(), "Your device does not support Speech Input", Toast.LENGTH_LONG).show();
                            }
                            break;

                    }
                    return false;
                }
            });

            //Setting up saveBtn
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonClickTiming();
                    openDialog(); //Calls for the dialog pop-up
                }
            });

        } else {
            requestPermissions(); //Requesting permissions if not already registered
        }
    }

    //Method implementing file save
    private void saveToTxtFile(String mText) {
        //Get current time for file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        try {
            File path = Environment.getExternalStorageDirectory(); //path to storage
            File dir = new File(path + "/Intrepid/Text Files/"); //creates folders named "Intrepid" and "Speech" within it
            dir.mkdirs();
            String fileName = inputName + "_" + timeStamp + ".txt"; //file name

            File file = new File(dir, fileName);

            //FileWriter class is used to store characters in file
            FileWriter fw = new FileWriter(file.getAbsolutePath());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(mText);
            bw.close();

            Toast.makeText(SpeechToText.this, "Saved successfully as... " + inputName, Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            //if anything goes wrong
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Method called when user is asked to input a name
    public void openDialog() {
        @SuppressLint("InflateParams") View view = (LayoutInflater.from(SpeechToText.this)).inflate(R.layout.text_name_input, null);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SpeechToText.this);
        alertBuilder.setView(view);
        final EditText userInput = view.findViewById(R.id.userInput);

        alertBuilder.setCancelable(true);
        alertBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Input text from EditText
                exportText = resultText.getText().toString().trim(); //.trim() removes space before and after text
                //Validate content
                if (!exportText.isEmpty()) {
                    inputName = String.valueOf(userInput.getText());
                    if(!inputName.isEmpty()) {
                        saveToTxtFile(exportText);
                    } else {
                        Toast.makeText(SpeechToText.this, "Name field empty...", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    inputName = "Text_file";
                    Toast.makeText(SpeechToText.this, "Input field empty...", Toast.LENGTH_SHORT).show();
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

    //Method programming frequency of acceptable button clicks
    public void buttonClickTiming() {
        //Prevention of button mushing by using threshold of 1000 ms between clicks
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

}