package media.tlj.nativevideoplayer.players;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.concurrent.TimeUnit;

public class ExoVideoPlayer {
    private static final String TAG = ExoVideoPlayer.class.getSimpleName();

    private Context ctx;

    private SimpleExoPlayer mPlayer;
    private long contentPosition;
    private Handler mHandler = new Handler();

    //constructor
    public ExoVideoPlayer(Context context){
        super();
        ctx = context;
    }

    private void init(){
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        float bandwidthFraction = 0.3f;
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(1, 5, 11, bandwidthFraction);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        trackSelector.setParameters(new DefaultTrackSelector.ParametersBuilder().setRendererDisabled(C.TRACK_TYPE_VIDEO, false).build());

        //Create player instance
        mPlayer = ExoPlayerFactory.newSimpleInstance(ctx, trackSelector);
        mPlayer.isCurrentWindowDynamic();

    }

    public void startPlayer(PlayerView playerView, String videoUrl){
        init();

        if(mPlayer != null){
            playerView.setPlayer(mPlayer);
            playerView.requestFocus();

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(ctx, Util.getUserAgent(ctx, "ExoVideoPlayer"));
            MediaSource contentMediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(videoUrl));

            //Audio Manager - Request Audio Focus
            AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if(mPlayer != null){
                        switch (focusChange){
                            case AudioManager.AUDIOFOCUS_LOSS:
                                mPlayer.stop();
                                mHandler.postDelayed(delayedStopRunnable, TimeUnit.SECONDS.toMillis(30));
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                                mPlayer.stop();
                                break;
                            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                                mPlayer.setVolume((float) 0.3);
                                break;
                            case AudioManager.AUDIOFOCUS_GAIN:
                                mPlayer.setVolume((float) 1.0);
                                if(!mPlayer.getPlayWhenReady() && mPlayer.getPlaybackState() != Player.STATE_READY){
                                    mPlayer.setPlayWhenReady(true);
                                }
                                break;
                        }
                    }
                }
            };

            assert audioManager != null;
            int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if(result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
                if(mPlayer!=null){
                    mPlayer.seekTo(contentPosition);
                    mPlayer.prepare(contentMediaSource);
                    mPlayer.addListener(new Player.EventListener() {
                        @Override
                        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                            Log.d(TAG, String.valueOf(playbackState));
                            Log.d(TAG, String.valueOf(playWhenReady));
                            if(playbackState == 4){
                                Log.d(TAG, "playbackState = " + playbackState);
                            }
                        }

                        @Override
                        public void onPlayerError(ExoPlaybackException error) {
                            Log.d(TAG, String.valueOf(error));
                        }
                    });

                    mPlayer.setPlayWhenReady(true);
                }
            }
        }
    } //End Start Player

    public void pausePlayer(){
        if(mPlayer != null){
            mPlayer.setPlayWhenReady(false);
            mPlayer.getPlaybackState();
        }
    }

    public void resumePlayer(){
        if(mPlayer != null){
            mPlayer.setPlayWhenReady(true);
            mPlayer.getPlaybackState();
        }
    }

    public void resetPlayer(){
        if(mPlayer != null){
            contentPosition = mPlayer.getContentPosition();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void releasePlayer(){
        if(mPlayer != null){
            mPlayer.release();
            mPlayer = null;
        }
    }

    public long getVideoDuration(){
        if(mPlayer != null){
            return mPlayer.getDuration();
        }
        return 0;
    }

    public long getContentPosition(){
        if(mPlayer != null){
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    private Runnable delayedStopRunnable = new Runnable() {
        @Override
        public void run() {
            if(mPlayer != null){
                mPlayer.stop();
            }
        }
    };
}
