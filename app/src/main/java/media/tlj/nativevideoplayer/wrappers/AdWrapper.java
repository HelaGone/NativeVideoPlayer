package media.tlj.nativevideoplayer.wrappers;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdErrorEvent;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManager;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider;
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate;

import media.tlj.nativevideoplayer.players.VideoPlayerManager;

public class AdWrapper implements AdEvent.AdEventListener, AdErrorEvent.AdErrorListener {
    private static final String TAG = AdWrapper.class.getSimpleName();
    private ImaSdkFactory mSdkFactory;
    private AdsLoader mAdsLoader;
    private AdsManager mAdsManager;
    private ViewGroup mAdUiContainer;
    private boolean mIsAdDisplayed;

    /*PLAYER MANAGER*/
    private VideoPlayerManager mVideoPlayerManager;
    private Context context;


    public AdWrapper(Context ctx, VideoPlayerManager playerManager, ViewGroup viewGroup){
        context = ctx;
        mVideoPlayerManager = playerManager;
        mAdUiContainer = viewGroup;
        createAdsLoader();
    }

    private void createAdsLoader(){
        mSdkFactory = ImaSdkFactory.getInstance();
        ImaSdkSettings settings = mSdkFactory.createImaSdkSettings();
        AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(mAdUiContainer);

        mAdsLoader = mSdkFactory.createAdsLoader(context, settings, adDisplayContainer);

        mAdsLoader.addAdsLoadedListener(new AdsLoader.AdsLoadedListener() {
            @Override
            public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
                Log.d(TAG, "Manager Loaded");
                mAdsManager = adsManagerLoadedEvent.getAdsManager();
                mAdsManager.addAdErrorListener(AdWrapper.this);
                mAdsManager.addAdEventListener(AdWrapper.this);
                mAdsManager.init();
            }
        });
    }

    @Override
    public void onAdError(AdErrorEvent adErrorEvent) {
        Log.d(TAG, "AdErrorEvent: "+adErrorEvent);
    }

    @Override
    public void onAdEvent(AdEvent adEvent) {
        //Log.d(TAG, "AdEvent: "+adEvent.getType());
        switch(adEvent.getType()){
            case CONTENT_PAUSE_REQUESTED:
                mVideoPlayerManager.pausePlaying();
                break;
            case CONTENT_RESUME_REQUESTED:
                mVideoPlayerManager.startPlaying();
                break;
        }
    }

    public void requestAndPlayAds(String adTagUrl) {
        AdDisplayContainer adDisplayContainer = mSdkFactory.createAdDisplayContainer();
        adDisplayContainer.setAdContainer(mAdUiContainer);

        //Create Ads Request
        AdsRequest request = mSdkFactory.createAdsRequest();
        request.setAdTagUrl(adTagUrl);
        request.setContentProgressProvider(new ContentProgressProvider() {
            @Override
            public VideoProgressUpdate getContentProgress() {
                if(mIsAdDisplayed || mVideoPlayerManager == null || mVideoPlayerManager.getDuration() <= 0){
                    return VideoProgressUpdate.VIDEO_TIME_NOT_READY;
                }
                return new VideoProgressUpdate(mVideoPlayerManager.getCurrentPosition(), mVideoPlayerManager.getDuration());
            }
        });

        mAdsLoader.requestAds(request);
    }
}
