package media.tlj.nativevideoplayer.screens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.google.android.exoplayer2.ui.PlayerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import media.tlj.nativevideoplayer.R;
import media.tlj.nativevideoplayer.models.VideoItemModel;
import media.tlj.nativevideoplayer.players.VideoPlayerManager;
import media.tlj.nativevideoplayer.wrappers.AdWrapper;
import media.tlj.nativevideoplayer.wrappers.DaiAdWrapper;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private static final String FALLBACK_URL = "https://dai.google.com/linear/hls/event/_e1s_U52SCGL6zTnlTrbVQ/master.m3u8";

    private VideoPlayerManager videoPlayerManager;
    private String signal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        PlayerView mPlayerView = findViewById(R.id.exoPlayerView);
        videoPlayerManager = new VideoPlayerManager(this);
        Intent intent = getIntent();
        if(intent != null){
            String senal_estrellas = intent.getStringExtra("senal_estrellas_live");

            if(senal_estrellas != null){
                signal = senal_estrellas;
                final DaiAdWrapper daiAdWrapper = new DaiAdWrapper(this, videoPlayerManager, (ViewGroup) findViewById(R.id.adUiContainer));
                daiAdWrapper.setFallbackUrl(FALLBACK_URL);
                daiAdWrapper.requestAndPlayAds();
                videoPlayerManager.setLiveMediaSource(signal);
            }else {

                Bundle bundle = getIntent().getExtras();
                assert bundle != null;
                ArrayList<VideoItemModel> arrayList = bundle.getParcelableArrayList("playlist");
                assert arrayList != null;
                int playlist_item_position = intent.getIntExtra("playlistItemPosition", 0);

                signal = arrayList.get(playlist_item_position).getV_media_url();


                final AdWrapper adWrapper = new AdWrapper(this, videoPlayerManager, (ViewGroup) findViewById(R.id.adUiContainer));
                adWrapper.requestAndPlayAds(getString(R.string.pubads_prerol));

                videoPlayerManager.setPlaylist(playlist_item_position, arrayList);

            }

        }

        videoPlayerManager.setPlayerView(mPlayerView);
        videoPlayerManager.enableControls(true);

    }//END ON CREATE

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "START");
        if(videoPlayerManager != null){
            videoPlayerManager.initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "RESUME");
        if(videoPlayerManager != null){
            videoPlayerManager.resumePlaying();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "PAUSE");
        if(videoPlayerManager != null){
            videoPlayerManager.releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "STOP");
        if(videoPlayerManager != null){
            videoPlayerManager.releasePlayer();
        }
    }
}
