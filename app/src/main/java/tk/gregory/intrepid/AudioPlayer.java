package tk.gregory.intrepid;

import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.os.Handler;
import android.widget.SeekBar;

import java.io.IOException;

//Implementing multiple Listeners efficiently
public class AudioPlayer extends AudioRecorder implements View.OnClickListener {
    /**
     * This class serves as a Media Player which playbacks previously recorded audio clips
     * Audio clips can be played as they are, or after having been processed
     * Is child of AudioRecorder class
     **/
    public Runnable runnable;
    private boolean playingStarted = false;
    private ImageButton normalBtn, highBtn, lowBtn, playBtn, rewindBtn, forwardBtn;
    private TextView descriptionText;
    private String mediaFileName = null;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Handler handler;
    private String profileID = null; //Token identifying the voice profile loaded on the MediaPlayer each time

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_player); //Pointing to the corresponding XML activity

        descriptionText = findViewById(R.id.descriptionText);
        normalBtn = findViewById(R.id.normalBtn);
        highBtn = findViewById(R.id.highBtn);
        lowBtn = findViewById(R.id.lowBtn);
        playBtn = findViewById(R.id.playBtn);
        rewindBtn = findViewById(R.id.rewindBtn);
        forwardBtn = findViewById(R.id.forwardBtn);
        seekBar = findViewById(R.id.seekBar);

        handler = new Handler();

        //Performs permission-check on every launch
        if (checkPermissions()) {

            mediaFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mediaFileName += "/Intrepid/Recordings/" + inputName + "_" + timeStamp + ".mp3";

            normalBtn.setOnClickListener(this);
            highBtn.setOnClickListener(this);
            lowBtn.setOnClickListener(this);
            playBtn.setOnClickListener(this);
            forwardBtn.setOnClickListener(this);
            rewindBtn.setOnClickListener(this);

            //Styling seekBar
            seekBar.getProgressDrawable().setColorFilter(getColor(R.color.scarlet), PorterDuff.Mode.SRC_IN);
            seekBar.getThumb().setColorFilter(getColor(R.color.scarlet), PorterDuff.Mode.SRC_IN);
            seekBar.setMinimumHeight(150);

            //Detecting seekBar's movement
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                    if (fromUser) {
                        if (mediaPlayer != null) {
                            if (playingStarted) { //The SeekBar is only usable when mediaPlayer is either successfully initiated or paused
                                mediaPlayer.seekTo(progress); //Procedural link of mediaPlayer and seekBar
                            } else {
                                seekBar.setProgress(0); //Dictates that seekBar is unusable and progress is unmovable from 0% whenever mediaPlayer is inoperable
                                disableVoiceButtons();
                                disableConsoleButtons();
                            }
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar bar) {}

                @Override
                public void onStopTrackingTouch(SeekBar bar) {}
            });

            normalBtn.performClick(); //Simulates button click the moment activity is loaded so users listen to their recorded message automatically the first time

        } else {
            requestPermissions(); //Requesting permissions if not already registered
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.normalBtn:
                useNormalBtn(); //Play Terran voice button
                break;

            case R.id.highBtn:
                useHighBtn(); //Play Grey voice button
                break;

            case R.id.lowBtn:
                useLowBtn(); //Play Darth Vader voice button
                break;

            case R.id.playBtn:
                usePlayBtn(); //Play/Pause button
                break;

            case R.id.forwardBtn:
                useForwardBtn(); //Forward button
                break;

            case R.id.rewindBtn:
                useRewindBtn(); //Rewind Button
                break;

        }
    }

    //Method called when normalBtn is pressed
    private void useNormalBtn() {
        profileID = "normal";
        normalBtn.setEnabled(true);
        mediaPlayer = new MediaPlayer();
        disableVoiceButtons();
        playBtn.setImageResource(R.drawable.pause);
        descriptionText.setTextColor(getColor(R.color.scarlet));
        descriptionText.setText(getString(R.string.normal_pitch));
        normalBtn.setColorFilter(getColor(R.color.scarlet));

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(mediaFileName);
            mediaPlayer.prepare();

            //Setting up seekBar's readiness and starting mediaPlayer
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    changeSeekBar();
                    playingStarted = true;
                }
            });
        }
        catch (IOException e) {
            //In case of mediaPlayer's operational failure
            completion();
        }

        //Detects mediaPlayer's task completion
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                completion();
            }
        });
    }

    //Method called when highBtn is pressed
    private void useHighBtn() {
        profileID = "high";
        highBtn.setEnabled(true);
        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(mediaFileName));
        if (mediaPlayer != null) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            disableVoiceButtons();
            playBtn.setImageResource(R.drawable.pause);
            descriptionText.setTextColor(getColor(R.color.scarlet));
            descriptionText.setText(getString(R.string.high_pitch));
            highBtn.setColorFilter(getColor(R.color.scarlet));

            //Setting up params element and loading mediaPlayer
            PlaybackParams params = new PlaybackParams();
            params.setPitch(2.2f);
            params.setSpeed(1.4f);
            mediaPlayer.setPlaybackParams(params);

            //Setting up seekBar's readiness and starting mediaPlayer
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    changeSeekBar();
                    playingStarted = true;
                }
            });

            //Detects mediaPlayer's task completion
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    completion();
                }
            });
        }
    }

    //Method called when lowBtn is pressed
    private void useLowBtn() {
        profileID = "low";
        lowBtn.setEnabled(true);
        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), Uri.parse(mediaFileName));
        if (mediaPlayer != null) {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            disableVoiceButtons();
            playBtn.setImageResource(R.drawable.pause);
            descriptionText.setTextColor(getColor(R.color.scarlet));
            descriptionText.setText(getString(R.string.low_pitch));
            lowBtn.setColorFilter(getColor(R.color.scarlet));

            //Setting up params element and loading mediaPlayer
            PlaybackParams params = new PlaybackParams();
            params.setPitch(0.7f);
            params.setSpeed(0.8f);
            mediaPlayer.setPlaybackParams(params);

            //Setting up seekBar's readiness and starting mediaPlayer
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    seekBar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    changeSeekBar();
                    playingStarted = true;
                }
            });

            //Detects mediaPlayer's task completion
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    completion();
                }
            });
        }
    }

    //Method called when playBtn is pressed
    private void usePlayBtn() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                disableVoiceButtons();
                playBtn.setImageResource(R.drawable.play);

                //Detects mediaPlayer's task completion
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        enableVoiceButtons();
                        completion();
                    }
                });
            } else {
                //Using the profileID token to identify currently playing voice profile
                switch (profileID) {

                    case "normal":
                        descriptionText.setTextColor(getColor(R.color.scarlet));
                        descriptionText.setText(getString(R.string.normal_pitch));
                        normalBtn.setColorFilter(getColor(R.color.scarlet));
                        break;

                    case "high":
                        descriptionText.setTextColor(getColor(R.color.scarlet));
                        descriptionText.setText(getString(R.string.high_pitch));
                        highBtn.setColorFilter(getColor(R.color.scarlet));
                        break;

                    case "low":
                        descriptionText.setTextColor(getColor(R.color.scarlet));
                        descriptionText.setText(getString(R.string.low_pitch));
                        lowBtn.setColorFilter(getColor(R.color.scarlet));
                        break;

                }
                mediaPlayer.start();
                changeSeekBar();
                playingStarted = true;
                playBtn.setImageResource(R.drawable.pause);
                disableVoiceButtons();
            }
        }
    }

    //Method called when forwardBtn is pressed
    private void useForwardBtn() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 2000); //Pressing the button forwards audio clip by 2 seconds
            changeSeekBar();
        }
    }

    //Method called when rewindBtn is pressed
    private void useRewindBtn() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 2000); //Pressing the button reverts audio clip by 2 seconds
            changeSeekBar();
        }
    }

    //Method called whenever MediaPlayer is interrupted or has completed task
    private void completion() {
        enableVoiceButtons();
        normalBtn.setColorFilter(getColor(R.color.coal));
        highBtn.setColorFilter(getColor(R.color.coal));
        lowBtn.setColorFilter(getColor(R.color.coal));
        descriptionText.setTextColor(getColor(R.color.coal));
        descriptionText.setText(getString(R.string.choose_a_voice_profile));
        playBtn.setImageResource(R.drawable.play);
    }

    private void enableVoiceButtons() {
        normalBtn.setEnabled(true);
        highBtn.setEnabled(true);
        lowBtn.setEnabled(true);
    }

    private void disableVoiceButtons() {
        normalBtn.setEnabled(false);
        highBtn.setEnabled(false);
        lowBtn.setEnabled(false);
    }

    private void disableConsoleButtons() {
        playBtn.setEnabled(false);
        rewindBtn.setEnabled(false);
        forwardBtn.setEnabled(false);
    }

    //Method that changes seekBar's progress in correspondence to mediaPlayer's progress
    private void changeSeekBar() {
        seekBar.setProgress(mediaPlayer.getCurrentPosition());

        if (mediaPlayer.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    changeSeekBar();
                }
            };
            handler.postDelayed(runnable, 10); //Controls the flow of the SeekBar's movement (fewer ms means that the SeekBar's progress refreshes faster and motion appears smoother)
        }
    }

    //Method called whenever user taps the Back button of the device
    public void onBackPressed() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            completion();
        }
        super.onBackPressed(); //Kills activity
    }

}