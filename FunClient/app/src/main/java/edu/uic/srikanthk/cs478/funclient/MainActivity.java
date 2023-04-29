package edu.uic.srikanthk.cs478.funclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import edu.uic.srikanthk.cs478.funcenter.IMediaPlaybackService;

public class MainActivity extends AppCompatActivity {
    private boolean mIsBound = false;
    protected static final String TAG = "Music_client";
    public static IMediaPlaybackService mMediaPlaybackService;
    ImageView pausePlay, stop;
    public static int musicPos = 0;
    TextView imageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Oscar Tunes");

        String[] imageTempList = {"Select image", "Image 1", "Image 2", "Image 3", "Image 4"};
        String[] musicTempList = {"Select music", "Music 1", "Music 2", "Music 3", "Music 4"};

        // Spinner used to select and display data of a particular item
        ArrayAdapter<String> imageAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, imageTempList);
        ArrayAdapter<String> musicAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, musicTempList);

        final Spinner imageDropdown = findViewById(R.id.image_dropdown);
        final Spinner musicDropdown = findViewById(R.id.music_dropdown);
        pausePlay = findViewById(R.id.pause_play);
        stop = findViewById(R.id.stop);
        imageTitle = findViewById(R.id.image_title);

        checkBindingAndBind();

        imageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imageDropdown.setAdapter(imageAdapter);
        imageDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    ImageView image = findViewById(R.id.imageView);
                    if (selectedItem.equals(imageTempList[0])) {
                        // When "Select image" is selected hide image and title
                        image.setVisibility(view.GONE);
                        imageTitle.setVisibility(view.GONE);
                    } else {
                        // Show image and title
                        String songData = mMediaPlaybackService.getDataById(position);
                        String[] answer = songData.split("&", -1);
                        byte[] imageBytes = Base64.decode(answer[2], Base64.DEFAULT);
                        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        image.setImageBitmap(decodedImage);
                        image.setVisibility(view.VISIBLE);
                        imageTitle.setText(answer[0]);
                        imageTitle.setVisibility(view.VISIBLE);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        musicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        musicDropdown.setAdapter(musicAdapter);
        musicDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    if (selectedItem.equals(musicTempList[0])) {
                        // When "Select music" is selected stop the music
                        mMediaPlaybackService.stopMusic();
                    } else {
                        // Play the corresponding music
                        musicPos = position;
                        pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline);
                        mMediaPlaybackService.selectMusicById(position);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        pausePlay.setOnClickListener(v -> {
            try {
                boolean isPlaying = mMediaPlaybackService.isMusicPlaying();
                if (isPlaying) {
                    pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline);
                } else {
                    pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline);
                }
                mMediaPlaybackService.playPauseMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        stop.setOnClickListener(v -> {
            try {
                mMediaPlaybackService.stopMusic();
                if (mMediaPlaybackService.isMusicPlaying()) {
                    pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder iservice) {
            mMediaPlaybackService = IMediaPlaybackService.Stub.asInterface(iservice);
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mMediaPlaybackService = null;
            mIsBound = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    protected void checkBindingAndBind() {
        if (!mIsBound) {
            boolean b = false;
            Intent i = new Intent(IMediaPlaybackService.class.getName());
            ResolveInfo info = getPackageManager().resolveService(i, 0);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            b = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);
            if (b) {
                Log.i(TAG, "BindService() succeeded!");
            } else {
                Log.i(TAG, "BindService() failed!");
            }
        }
    }
}