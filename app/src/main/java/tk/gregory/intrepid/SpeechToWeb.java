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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class SpeechToWeb extends MainMenu {
    /**
     * This class serves as a speech input to web browsing for existing websites
     * Is child of MainMenu class
     **/
    private WebView webView;
    private String resultString;
    private long lastClickTime = 0;
    private static final Pattern DOMAIN_NAMES = Pattern.compile("\\.(?i:com|gr|us|uk|ru|net|tk|tv|co|org|info|website|tech|store)$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_to_web); //Pointing to the corresponding XML activity

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

                        webView = findViewById(R.id.webView);

                        //noinspection deprecation
                        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

                        //Stores a link and replaces symbols
                        String link = resultString
                                .replaceAll("α","a").replaceAll("η","i").replaceAll("ν","n").replaceAll("τ","t")
                                .replaceAll("β","b").replaceAll("θ","th").replaceAll("ξ","ks").replaceAll("υ","u")
                                .replaceAll("ι","i").replaceAll("ο","o").replaceAll("φ","f").replaceAll("δ","d")
                                .replaceAll("κ","k").replaceAll("π","p").replaceAll("χ","x").replaceAll("ε","e")
                                .replaceAll("λ","l").replaceAll("ρ","r").replaceAll("ψ","ps").replaceAll("ζ","z")
                                .replaceAll("μ","m").replaceAll("σ","s").replaceAll("ω","o").replaceAll("ς","s")
                                .replaceAll("γκ","g").replaceAll("γγ","g").replaceAll("ντ","d").replaceAll("ά","a")
                                .replaceAll("ώ","o").replaceAll("έ","e").replaceAll("ή","i").replaceAll("ί","i")
                                .replaceAll("ό","o").replaceAll("ύ","u").replaceAll(" ","").replaceAll("dot",".");
                        resultString.replaceAll("γ","g");

                        //Checks whether the link contains domain name
                        if (DOMAIN_NAMES.matcher(link).find()) {
                            webView.loadUrl("https://www." + link);
                        } else {
                            webView.loadUrl("https://www." + link + ".com");
                        }

                        webView.setForeground(null);
                        webView.setWebViewClient(new WebViewClient());
                        WebSettings webSettings = webView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
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

    //Method programming frequency of acceptable button clicks
    public void buttonClickTiming() {
        //Prevention of button mushing by using threshold of 1000 ms between clicks
        if (SystemClock.elapsedRealtime() - lastClickTime < 1000) {
            return;
        }
        lastClickTime = SystemClock.elapsedRealtime();
    }

}