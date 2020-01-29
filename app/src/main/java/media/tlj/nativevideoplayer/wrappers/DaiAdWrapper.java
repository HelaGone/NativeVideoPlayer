package media.tlj.nativevideoplayer.wrappers;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.StreamDisplayContainer;
import com.google.ads.interactivemedia.v3.api.StreamManager;
import com.google.ads.interactivemedia.v3.api.StreamRequest;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;
import com.google.ads.interactivemedia.v3.api.player.VideoStreamPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import media.tlj.nativevideoplayer.players.VideoPlayerManager;

public class DaiAdWrapper implements AdEvent.AdEventListener, AdErrorEvent.AdErrorListener, AdsLoader.AdsLoadedListener {
    private static final String TAG = DaiAdWrapper.class.getSimpleName();

    //LIVE STREAM ASSET KEY LAS ESTRELLAS
    private static final String ASSET_KEY = "_e1s_U52SCGL6zTnlTrbVQ";
    
    //PLAYER TYPE
    private static final String PLAYER_TYPE = "DAIPlayer";

    private String mFallbackUrl = "";
    
    private ImaSdkFactory mImaSdkFactory;
    private AdsLoader mAdsLoader;
    private List<VideoStreamPlayer.VideoStreamPlayerCallback> mPlayerCallbacks;
    
    //PLAYER MANAGER
    private VideoPlayerManager mVideoPlayerManager;
    private Context ctx;
    private ViewGroup mAdUiContainer;
    
    //CONSTRUCTOR
    public DaiAdWrapper(Context context, VideoPlayerManager videoPlayerManager, ViewGroup adUiContainer){
        mVideoPlayerManager = videoPlayerManager;
        ctx = context;
        mAdUiContainer = adUiContainer;
        mImaSdkFactory = ImaSdkFactory.getInstance();
        mPlayerCallbacks = new ArrayList<>();
        createAdsLoader();
        //mStreamDisplayContainer = mImaSdkFactory.createStreamDisplayContainer();
    }

    private void createAdsLoader() {
        ImaSdkSettings settings = mImaSdkFactory.createImaSdkSettings();
        settings.setAutoPlayAdBreaks(true);
        settings.setPlayerType(PLAYER_TYPE);
        StreamDisplayContainer mDisplayContainer = mImaSdkFactory.createStreamDisplayContainer();
        VideoStreamPlayer videoStreamPlayer = createVideoStreamPlayer();
        mVideoPlayerManager.setVideoPlayerCallback(new VideoPlayerManager.VideoPlayerCallback() {
            @Override
            public void onUserTextReceived(String userText) {
                for(VideoStreamPlayer.VideoStreamPlayerCallback callback : mPlayerCallbacks){
                    callback.onUserTextReceived(userText);
                }
            }

            @Override
            public void onSeek(int windowIndex, long positionMs) {
                mVideoPlayerManager.seekTo(windowIndex, positionMs);
            }
        });
        mDisplayContainer.setVideoStreamPlayer(videoStreamPlayer);
        mDisplayContainer.setAdContainer(mAdUiContainer);
        mAdsLoader = mImaSdkFactory.createAdsLoader(ctx, settings, mDisplayContainer);
    }

    public void requestAndPlayAds(){
        mAdsLoader.addAdErrorListener(this);
        mAdsLoader.addAdsLoadedListener(this);
        mAdsLoader.requestStream(buildStreamRequest());
    }

    private StreamRequest buildStreamRequest() {
        return mImaSdkFactory.createLiveStreamRequest(ASSET_KEY, null);
    }

    private VideoStreamPlayer createVideoStreamPlayer() {
        return new VideoStreamPlayer() {
            @Override
            public void loadUrl(String mediaUrl, List<HashMap<String, String>> list) {
                mVideoPlayerManager.setMediaSource(mediaUrl);
                mVideoPlayerManager.startPlaying();
            }

            @Override
            public int getVolume() {
                return 1;
            }

            @Override
            public void addCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                mPlayerCallbacks.add(videoStreamPlayerCallback);
            }

            @Override
            public void removeCallback(VideoStreamPlayerCallback videoStreamPlayerCallback) {
                mPlayerCallbacks.remove(videoStreamPlayerCallback);
            }

            @Override
            public void onAdBreakStarted() {
                mVideoPlayerManager.enableControls(false);
            }

            @Override
            public void onAdBreakEnded() {
                mVideoPlayerManager.enableControls(true);
            }

            @Override
            public void onAdPeriodStarted() {

            }

            @Override
            public void onAdPeriodEnded() {

            }

            @Override
            public void seek(long timeMs) {
                mVideoPlayerManager.seekTo(timeMs);
            }

            @Override
            public VideoProgressUpdate getContentProgress() {
                return new VideoProgressUpdate(mVideoPlayerManager.getCurrentPositionPeriod(), mVideoPlayerManager.getDuration());
            }
        };
    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        mVideoPlayerManager.setMediaSource(mFallbackUrl);
        mVideoPlayerManager.enableControls(true);
        mVideoPlayerManager.startPlaying();
    }

    @Override
    public void onAdEvent(AdEvent adEvent) {
        if(adEvent.getType() == AdEvent.AdEventType.AD_PROGRESS){
            //Do nothing
        }else{
            Log.d(TAG, String.format("Event: %s\n", adEvent.getType()));
        }
    }

    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent event) {
        //private StreamDisplayContainer mStreamDisplayContainer;
        StreamManager mStreamManager = event.getStreamManager();
        mStreamManager.addAdErrorListener(this);
        mStreamManager.addAdEventListener(this);
        mStreamManager.init();
    }

    public void setFallbackUrl(String fallbackUrl){
        mFallbackUrl = fallbackUrl;
    }

}
