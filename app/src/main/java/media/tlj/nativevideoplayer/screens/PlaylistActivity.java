package media.tlj.nativevideoplayer.screens;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import media.tlj.nativevideoplayer.R;
import media.tlj.nativevideoplayer.adapters.PlaylistAdapter;
import media.tlj.nativevideoplayer.models.VideoItemModel;
import media.tlj.nativevideoplayer.requests.RequestAssets;

public class PlaylistActivity extends AppCompatActivity {
    private static final String TAG = PlayerActivity.class.getSimpleName();
    private static final String URL_VIDEOS = "https://api.anvato.net/v2/feed/KSNFKBUBPAWUIZU3P2GAGWC/?sort=c_ts_publish_l+desc&count=30";

    //Recycler View\\
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<VideoItemModel> videoItemModelList;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        context = getApplicationContext();

        mRecyclerView = findViewById(R.id.nvp_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);


        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_VIDEOS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                videoItemModelList = new ArrayList<>();
                try {
                    JSONObject object = new JSONObject(response);
                    JSONArray jsonArray = new JSONArray(object.getString("docs"));

                    for(int i = 0; i< jsonArray.length(); i++){
                        JSONObject item_object = new JSONObject(jsonArray.get(i).toString());

                        String id = item_object.getString("obj_id");
                        String title = item_object.getString("c_title_s");
                        String description = item_object.getString("c_description_s");

                        JSONObject info = new JSONObject(item_object.getString("info"));
                        String duration = info.getString("duration");
                        String program = info.getString("program_name");
                        String channel = info.getJSONArray("categories").getJSONObject(0).getString("name");
                        String media_url = item_object.getString("media_url");
                        String thumbnail = item_object.getJSONArray("thumbnails").getJSONObject(0).getString("url");

                        VideoItemModel videoItemModel = new VideoItemModel(id, title, description, duration, program, channel, media_url, thumbnail);
                        videoItemModelList.add(videoItemModel);
                    }

                    mAdapter = new PlaylistAdapter(videoItemModelList, context);
                    ((PlaylistAdapter) mAdapter).setOnItemClickListener(new PlaylistAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                            intent.putExtra("playlistItemPosition", position);

                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("playlist", videoItemModelList);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                    mRecyclerView.setAdapter(mAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        RequestAssets.getInstance(this).addToRequestQueue(stringRequest);

    }//END ON CREATE

}//END CLASS
