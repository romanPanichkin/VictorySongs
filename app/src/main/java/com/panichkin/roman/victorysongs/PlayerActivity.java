package com.panichkin.roman.victorysongs;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    CheckBox chbLoop;
    ArrayList<Song> songsArrayList;
    int songPosition;
    android.widget.SeekBar volumeSeekBar;
    SeekBar trackProgressBar;
    private double startTime = 0;
    private double finalTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        chbLoop = (CheckBox) findViewById(R.id.chbLoop);
        chbLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (mediaPlayer != null)
                    mediaPlayer.setLooping(isChecked);
            }
        });

        releaseMP();
        songsArrayList = (ArrayList<Song>) getIntent().getExtras().getSerializable("list");
        songPosition = getIntent().getExtras().getInt("chosenSong");
        mediaPlayer = MediaPlayer.create(this, (int) songsArrayList.get(songPosition).sourceId);
        mediaPlayer.start();
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        showLyrics(songsArrayList.get(songPosition));

        if (mediaPlayer == null)
            return;

        mediaPlayer.setLooping(chbLoop.isChecked());
        mediaPlayer.setOnCompletionListener(this);

        initVolumeControls();
        initProgressControl();
    }

    private void showLyrics(Song currentSong) {
        TextView songLyrics = (TextView) findViewById(R.id.song_lyrics_textview);
        songLyrics.setText(currentSong.lyrics);

    }

    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onClick(View view) {
        if (mediaPlayer == null)
            return;
        switch (view.getId()) {
            case R.id.btnPause:
                if (mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                else
                    mediaPlayer.start();
                break;

            case R.id.btnBackward:
                if (songPosition != 0)
                    songPosition--;
                else
                    songPosition = songsArrayList.size() - 1;

                releaseMP();
                mediaPlayer = MediaPlayer.create(this, (int) songsArrayList.get(songPosition).sourceId);
                mediaPlayer.start();
                mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
                showLyrics(songsArrayList.get(songPosition));

                if (mediaPlayer == null)
                    return;
                mediaPlayer.setLooping(chbLoop.isChecked());
                mediaPlayer.setOnCompletionListener(this);
                break;

            case R.id.btnForward:
                if (songPosition == (songsArrayList.size() - 1))
                    songPosition = 0;
                else
                    songPosition++;

                releaseMP();
                mediaPlayer = MediaPlayer.create(this, (int) songsArrayList.get(songPosition).sourceId);
                mediaPlayer.start();
                mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
                showLyrics(songsArrayList.get(songPosition));

                if (mediaPlayer == null)
                    return;
                mediaPlayer.setLooping(chbLoop.isChecked());
                mediaPlayer.setOnCompletionListener(this);
                break;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMP();
    }

    private void initVolumeControls() {
        try {
            volumeSeekBar = (SeekBar) findViewById(R.id.volume_seekBar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

            volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0) {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initProgressControl() {
        trackProgressBar = (SeekBar) findViewById(R.id.trackProgress_seekBar);
        trackProgressBar.setClickable(false);
        finalTime = mediaPlayer.getDuration();
        startTime = mediaPlayer.getCurrentPosition();

        TextView finalTimeTextView =(TextView) findViewById(R.id.finalTime_textView);
        TextView startTimeTextView = (TextView) findViewById(R.id.startTime_textView);
        startTimeTextView.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                TimeUnit.MILLISECONDS.toSeconds((long) startTime),
                TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes((long) startTime))));
    }
}