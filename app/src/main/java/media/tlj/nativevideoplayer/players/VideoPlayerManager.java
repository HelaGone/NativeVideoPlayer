package media.tlj.nativevideoplayer.players;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import java.util.ArrayList;
import java.util.List;

import media.tlj.nativevideoplayer.models.VideoItemModel;

public class VideoPlayerManager {

    private static final String TAG = VideoPlayerManager.class.getSimpleName();
    private PlaybackStateListener playbackStateListener;

    private Context ctx;

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private boolean playWhenReady = true;
    private boolean canSeek;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private String videoUrlString = "";
    private List<VideoItemModel> itemModelList;
    private int listItemPosition;

    private VideoPlayerCallback mVideoPlayerCallback;

    private Timeline.Period mPeriod = new Timeline.Period();

    /*CONSTRUCTOR*/
    public VideoPlayerManager(Context context){
        ctx = context;
    }

    /*INTERFACE FOR DAI IMPLEMENTATION*/
    public interface VideoPlayerCallback{
        void onUserTextReceived(String userText);
        void onSeek(int windowIndex, long positionMs);
    }

    /*INITIALIZE PLAYER*/
    public void initializePlayer(){
        if(player == null){
            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd());
            player = ExoPlayerFactory.newSimpleInstance(ctx, trackSelector);
        }

        playerView.setPlayer(player);
        playbackStateListener = new PlaybackStateListener();
        player.addListener(playbackStateListener);

        /*AUDIO MANAGER*/
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch(focusChange){
                    case AudioManager.AUDIOFOCUS_LOSS:
                        if(player != null){
                            player.stop();
                        }
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        if(player != null){
                            player.stop();
                        }
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        if(player != null){
                            player.setVolume((float) 0.5);
                        }
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if(player != null){
                            player.setVolume((float) 1.0);
                            if(!player.getPlayWhenReady() && player.getPlaybackState() != Player.STATE_READY){
                                player.setPlayWhenReady(playWhenReady);
                            }
                        }
                }
            }
        };

        assert audioManager != null;
        int afResult = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if(afResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            if(videoUrlString.equals("")){
                //VOD signal without DAI
                List<MediaSource> mediaSourceList = new ArrayList<>();

                for(int i = 0; i<itemModelList.size(); i++){
                    Uri videoUri = Uri.parse(itemModelList.get(i).getV_media_url());
                    MediaSource mediaSource = buildMediaSource(videoUri);
                    mediaSourceList.add(mediaSource);
                }

                ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
                concatenatingMediaSource.addMediaSources(mediaSourceList);

                player.setPlayWhenReady(false);
                player.prepare(concatenatingMediaSource, false, false);
                player.seekTo(listItemPosition, C.TIME_UNSET);

            }else{
                //Live signal with DAI
                Uri videoUri = Uri.parse(videoUrlString);
                MediaSource mediaSource = buildMediaSource(videoUri);
                player.setPlayWhenReady(false);
                player.prepare(mediaSource, false, false);
                player.seekTo(currentWindow, playbackPosition);
            }
        }

    }

    /*CONTROL METHODS*/
    public void startPlaying() {
        if(player != null){
            player.setPlayWhenReady(playWhenReady);
        }
    }

    public void pausePlaying(){
        if(player != null){
            player.setPlayWhenReady(false);
        }
    }

    public void seekTo(int windowIndex, long positionMs) {
        if(player != null){
            player.seekTo(windowIndex, positionMs);
        }
    }
    public void seekTo(long positionMs){
        if(player != null){
            player.seekTo(positionMs);
        }
    }

    public void releasePlayer(){
        if(player != null){
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(playbackStateListener);
            player.release();
            player = null;
        }
    }

    public void resumePlaying(){
        if(player != null){
            player.seekTo(playbackPosition);
            player.setPlayWhenReady(playWhenReady);
        }
    }

    public void enableControls(boolean doEnable) {
        if(doEnable){
            playerView.showController();
        }else{
            playerView.hideController();
        }
        canSeek = doEnable;
    }

    /*GETTER METHODS*/
    public long getDuration() {
        if(player != null){
            return player.getDuration();
        }
        return 0;
    }

    public long getCurrentPositionPeriod() {
        if(player!= null){
            long position = player.getContentPosition();
            Timeline currentTimeline = player.getCurrentTimeline();
            if(!currentTimeline.isEmpty()){
                position -= currentTimeline.getPeriod(player.getCurrentPeriodIndex(), mPeriod).getPositionInWindowMs();
            }
            return position;
        }
        return 0;
    }

    public long getCurrentPosition(){
        if(player != null){
            return player.getCurrentPosition();
        }
        return 0;
    }


    /*SETTER METHODS*/
    public void setMediaSource(String videoUrl){
        videoUrlString = videoUrl;
    }

    public void setPlaylist(int position, List<VideoItemModel> itemModelList){
        this.itemModelList = itemModelList;
        this.listItemPosition = position;
    }

    public void setPlayerView(PlayerView view){
        playerView = view;
    }

    public void setVideoPlayerCallback(VideoPlayerCallback callback){
        mVideoPlayerCallback = callback;
    }

    /*BUILD MEDIA SOURCE*/
    private MediaSource buildMediaSource(Uri videoUri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(ctx, "native-video-player");
        HlsMediaSource.Factory mediaSourceFactory = new HlsMediaSource.Factory(dataSourceFactory);
        return mediaSourceFactory.createMediaSource(videoUri);
    }

    /*PLAYBACK STATE LISTENER*/
    private class PlaybackStateListener implements Player.EventListener {

        @Override
        public void onPositionDiscontinuity(int reason) {
            String reasonString = "";
            switch(reason){
                case Player.DISCONTINUITY_REASON_AD_INSERTION:
                    reasonString = "DISCONTINUITY_REASON_AD_INSERTION";
                    break;
                case Player.DISCONTINUITY_REASON_INTERNAL:
                    reasonString = "DISCONTINUITY_REASON_INTERNAL";
                    break;
                case Player.DISCONTINUITY_REASON_PERIOD_TRANSITION:
                    reasonString = "DISCONTINUITY_REASON_PERIOD_TRANSITION";
                    break;
                case Player.DISCONTINUITY_REASON_SEEK:
                    reasonString = "DISCONTINUITY_REASON_SEEK";
                    break;
                case Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT:
                    reasonString = "DISCONTINUITY_REASON_SEEK_ADJUSTMENT";
                    break;
                    default: reasonString = "DD";
                    break;
            }
            Log.d(TAG, "Reason: " + reasonString);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            //Log.d(TAG, String.valueOf(playbackState));
            String stateString;
            switch(playbackState){
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED";
                    break;
                default:
                    stateString = "UNKNOWN_STATE";
                    break;
            }
            Log.d(TAG, "State changed to: "+stateString + " play when ready " + playWhenReady);
        }
    }



}
