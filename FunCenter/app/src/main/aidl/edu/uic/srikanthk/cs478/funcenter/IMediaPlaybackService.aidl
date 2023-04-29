// IMediaPlaybackService.aidl
package edu.uic.srikanthk.cs478.funcenter;

// Declare any non-default types here with import statements

interface IMediaPlaybackService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    String[] getData();
    String getDataById(int pos);
    void selectMusicById(int pos);
    boolean isMusicPlaying();
    void playPauseMusic();
    void stopMusic();
}