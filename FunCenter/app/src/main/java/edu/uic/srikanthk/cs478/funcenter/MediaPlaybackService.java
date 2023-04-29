package edu.uic.srikanthk.cs478.funcenter;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Base64;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

public class MediaPlaybackService extends Service {
    private static String CHANNEL_ID = "Music_Player";
    private Notification notification;
    private static final int NOTIFICATION_ID = 1;
    MediaPlayer player = null;
    Context context;
    private static int[] music_list = {R.raw.oo_antava_mava, R.raw.naatu_naatu, R.raw.eminem_lose_yourself, R.raw.saadda_haq};

    //Foreground service and notification channel for server
    @Override
    public void onCreate() {
        super.onCreate();
        this.createNotificationChannel();

        context = getApplicationContext();

        notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID).setSmallIcon(android.R.drawable.ic_media_play).setOngoing(true).setContentTitle("Music Playing").setContentText("Click to Access Music Player").setTicker("Music is playing!").build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because the NotificationChannel class is new and not in the old library
        CharSequence name = "Music player notification";
        String description = "The channel for music player notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription(description);
        }
        // Register the channel with the system; you can't change the importance or other notification behaviors after this
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    //  Set of already assigned IDs
    private final static Set<Info> mIDs = new HashSet<Info>();

    // Implement the Stub for this Object
    private final IMediaPlaybackService.Stub mBinder = new IMediaPlaybackService.Stub() {

        // Remote method for fetching all the data
        public String[] getData() {
            Info[] arr = new Info[4];
            Drawable d = getResources().getDrawable(R.drawable.ooantava);
            arr[0] = new Info("Oo Antava Oo Oo Antava", "M. M. Keeravani, Chandrabose", convertToString(d));
            d = getResources().getDrawable(R.drawable.naatu_naatu);
            arr[1] = new Info("Naatu Naatu", "Devi Sri Prasad, Chandrabose", convertToString(d));
            d = getResources().getDrawable(R.drawable.mnm);
            arr[2] = new Info("Lose Yourself", "Eminem,Jeff,BassLuisResto", convertToString(d));
            d = getResources().getDrawable(R.drawable.sadda_haq_song);
            arr[3] = new Info("Sadda Haq", "A. R. Rahman, Irshad Kamil", convertToString(d));

            Info id;

            synchronized (mIDs) {
                do {
                    id = arr[0];
                } while (mIDs.contains(id));
                mIDs.add(id);
            }

            String[] data = new String[4];
            for (int i = 0; i < 4; i++) {
                id = arr[i];
                data[i] = id.getInfo();
            }
            return data;
        }

        // Remote method for fetching data by the ID provided by the client
        @Override
        public String getDataById(int pos) {
            Info[] arr = new Info[4];
            Drawable d = getResources().getDrawable(R.drawable.ooantava);
            arr[0] = new Info("Oo Antava Oo Oo Antava", "M. M. Keeravani, Chandrabose", convertToString(d));
            d = getResources().getDrawable(R.drawable.naatu_naatu);
            arr[1] = new Info("Naatu Naatu", "Devi Sri Prasad, Chandrabose", convertToString(d));
            d = getResources().getDrawable(R.drawable.mnm);
            arr[2] = new Info("Lose Yourself", "Eminem,Jeff,BassLuisResto", convertToString(d));
            d = getResources().getDrawable(R.drawable.sadda_haq_song);
            arr[3] = new Info("Sadda Haq", "A. R. Rahman, Irshad Kamil", convertToString(d));

            Info id;
            synchronized (mIDs) {
                do {
                    id = arr[0];
                } while (mIDs.contains(id));
                mIDs.add(id);
            }

            String data;
            id = arr[pos - 1];
            data = id.getInfo();
            return data;
        }

        @Override
        public void selectMusicById(int pos) throws RemoteException {
            if (player != null && player.isPlaying()) {
                player.reset();
            }
            player = MediaPlayer.create(context, music_list[pos - 1]);
            player.start();
        }

        @Override
        public boolean isMusicPlaying() throws RemoteException {
            if (player != null) {
                return player.isPlaying();
            }
            return false;
        }

        @Override
        public void playPauseMusic() throws RemoteException {
            if (player != null) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
            }
        }

        @Override
        public void stopMusic() throws RemoteException {
            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }
        }
    };

    // Return the Stub defined above
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // Converting every image from bitmap to string in order to pass it to client
    public String convertToString(Drawable d) {
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // In case you want to compress your image, here it's at 40%
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
        }
    }
}
