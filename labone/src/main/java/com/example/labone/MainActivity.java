package com.example.labone;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;

import java.io.File;

public class MainActivity extends FragmentActivity {

    Button playBtn;
    SeekBar positionBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    MediaPlayer mp;
    int totalTIme;

    private MediaPlayer getMusic(){
        MediaPlayer music;
        String musicPath = Environment.getExternalStorageDirectory().getPath() + "/Music/Music.mp3";
        Log.d("Main", " PATH : " + musicPath);
        File file = new File(musicPath);
        Log.d("Main", " Music exists: " + file.exists() + ", can read : " + file.canRead());
        music = MediaPlayer.create(this, Uri.parse(musicPath));
        return music;

    }

    private boolean isPermissionGranted(){
        int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(isPermissionGranted()) {
            setContentView(R.layout.activity_main);
            playBtn = (Button) findViewById(R.id.playBtn);
            elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
            remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);
            mp = getMusic();
//        mp = MediaPlayer.create(this, R.raw.music);
            mp.setLooping(true);
            mp.seekTo(0);
            mp.setVolume(0.5f, 0.5f);
            totalTIme = mp.getDuration();

            setPositionBar();
            setVolumeBar();
            runThread();
        } else {
            setContentView(R.layout.add_permission_notice);
        }
    }

    private void setPositionBar(){
        positionBar = (SeekBar) findViewById(R.id.positionBar);
        positionBar.setMax(totalTIme);
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser){
                            mp.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

    private void setVolumeBar(){
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        mp.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

    private void runThread(){
        Thread checker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null){
                    try{
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch(InterruptedException e) {

                    }
                }
            }
        });
        checker.start();

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            positionBar.setProgress(currentPosition);

            String elapsedTime = createTimeLabel(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);

            String remainingTime = createTimeLabel(totalTIme-currentPosition);
            remainingTimeLabel.setText("- " + remainingTime);
        }
    };

    public String createTimeLabel(int time){
        String timeLabel = "";
        int min  = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }

    public void playBtnClick(View view){

        if (!mp.isPlaying()){
            mp.start();
            playBtn.setBackgroundResource(R.drawable.pause);
        } else {
            mp.pause();
            playBtn.setBackgroundResource(R.drawable.play);
        }

    }
}
