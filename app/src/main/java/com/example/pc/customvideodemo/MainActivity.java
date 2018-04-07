package com.example.pc.customvideodemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

    private static final String VIDEO_PATH = "https://inducesmile.com/wp-content/uploads/2016/05/small.mp4";
    private static final String VIDEO_PATH1 = "http://movies.apple.com/media/us/mac/getamac/2009/apple-mvp-biohazard_suit-us-20090419_480x272.mov";
    private static final String VIDEO_PATH2 = "https://ia800201.us.archive.org/22/items/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4";

    ProgressDialog progressDialog;

    private ImageView ivPlay;
    private ImageView ivNext;
    private ImageView ivPrevious;
    private SeekBar seekBar;
    private SeekBar sbVolume;
    private TextView txtTotalTime;
    private TextView txtRemainingTime;
    private ProgressBar progressBar;
    private ImageView ivVolumemute;

    private RelativeLayout relativeLayout1;
    private RelativeLayout relativeLayout2;

    private boolean isPlay = false;
    private boolean running = true;
    private boolean isHide = false;
    private boolean isMute = false;
    private int duration = 0;
    private int current = 0;

    private SurfaceView mSurfaceView;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder mSurfaceHolder;

    private AudioManager audioManager;
    private int maxVolume;
    private int curVolume;


    private Runnable onEverySecond = new Runnable() {
        @Override
        public void run() {
            if (true == running) {
                if (seekBar != null && mMediaPlayer != null) {
                    seekBar.setProgress(mMediaPlayer.getCurrentPosition());
                }

                if (seekBar != null && mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        updateTime();
                        seekBar.postDelayed(onEverySecond, 1000);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {

        ivPlay = (ImageView) findViewById(R.id.play);
        ivPrevious = (ImageView) findViewById(R.id.previous);
        ivNext = (ImageView) findViewById(R.id.next);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        sbVolume = (SeekBar) findViewById(R.id.volume);
        txtTotalTime = (TextView) findViewById(R.id.totaltime);
        txtRemainingTime = (TextView) findViewById(R.id.remainingtime);
        progressBar = (ProgressBar) findViewById(R.id.pb);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        relativeLayout2 = (RelativeLayout) findViewById(R.id.child2);
        relativeLayout1 = (RelativeLayout) findViewById(R.id.child1);
        ivVolumemute = (ImageView) findViewById(R.id.volumeup);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        if (audioManager != null) {
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            sbVolume.setMax(maxVolume);
            sbVolume.setProgress(curVolume);
        }

      /*  getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);*/

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(MainActivity.this);
        //progressDialog = ProgressDialog.show(this, "Loading...", "Please wait...", true, false);

        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //not hidden so will hide it
                if (isHide) {
                    animationShow(relativeLayout2);
                    isHide = false;
                } else {
                    animationHide(relativeLayout2);
                    isHide = true;
                }
            }
        });

        ivVolumemute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMute) {
                    ivVolumemute.setImageResource(R.drawable.ic_volume_off_black_24dp);
                    mMediaPlayer.setVolume(1, 1);
                    sbVolume.setVisibility(View.VISIBLE);
                    isMute = false;
                } else {
                    ivVolumemute.setImageResource(R.drawable.ic_volume_up_black_24dp);
                    mMediaPlayer.setVolume(0, 0);
                    sbVolume.setVisibility(View.GONE);
                    isMute = true;
                }
            }
        });

        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser && audioManager != null) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                    //mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser) {
                    updateTime();
                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isPlay) {
                    ivPlay.setImageResource(R.drawable.ic_pause_black_24dp);
                    isPlay = false;
                    seekBar.setProgress(mMediaPlayer.getCurrentPosition());
                    seekBar.postDelayed(onEverySecond, 1000);
                    mMediaPlayer.start();
                    // Toast.makeText(MainActivity.this, "STARTED at " + mMediaPlayer.getCurrentPosition(), Toast.LENGTH_SHORT).show();
                } else {
                    ivPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                    isPlay = true;
                    mMediaPlayer.pause();
                    //Toast.makeText(MainActivity.this, "Paused at " + mMediaPlayer.getCurrentPosition(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void animationShow(final View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    private void animationHide(View view) {
        // Prepare the View for the animation
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        progressBar.setVisibility(View.GONE);
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        ivPlay.setImageResource(R.drawable.ic_pause_black_24dp);
        mp.start();
        seekBar.postDelayed(onEverySecond, 1000);
        seekBar.setProgress(0);
        seekBar.setMax(mp.getDuration());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setDisplay(mSurfaceHolder);
        try {
            mMediaPlayer.setDataSource(VIDEO_PATH2);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(MainActivity.this);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (mMediaPlayer != null) {
                duration = mMediaPlayer.getDuration();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ivPlay != null) {
            ivPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }

    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void updateTime() {
        do {
            current = mMediaPlayer.getCurrentPosition();
            System.out.println("duration - " + duration + " current- " + current);
            int dSeconds = (int) (duration / 1000) % 60;
            int dMinutes = (int) ((duration / (1000 * 60)) % 60);
            int dHours = (int) ((duration / (1000 * 60 * 60)) % 24);

            int cSeconds = (int) (current / 1000) % 60;
            int cMinutes = (int) ((current / (1000 * 60)) % 60);
            int cHours = (int) ((current / (1000 * 60 * 60)) % 24);

            if (dHours == 0) {
                //  txtTotalTime.setText(String.format("%02d:%02d / %02d:%02d", cMinutes, cSeconds, dMinutes, dSeconds));
                txtTotalTime.setText(String.format("%02d:%02d", cMinutes, cSeconds));
                int remain = mMediaPlayer.getDuration() - mMediaPlayer.getCurrentPosition();
                String time = String.format("-%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(remain),
                        TimeUnit.MILLISECONDS.toSeconds(remain) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remain)));
                txtRemainingTime.setText(time);
            } else {
                //  txtTotalTime.setText(String.format("%02d:%02d:%02d / %02d:%02d:%02d", cHours, cMinutes, cSeconds, dHours, dMinutes, dSeconds));
                txtTotalTime.setText(String.format("%02d:%02d:%02d", cHours, cMinutes, cSeconds, dHours, dMinutes, dSeconds));
                txtRemainingTime.setText(String.format("%02d:%02d:%02d ", dHours, dMinutes, dSeconds));
            }

            try {
                Log.d("Value: ", String.valueOf((int) (current * 100 / duration)));
                if (seekBar.getProgress() >= 100) {
                    break;
                }
            } catch (Exception e) {
            }
        } while (seekBar.getProgress() <= 100);
    }

}
