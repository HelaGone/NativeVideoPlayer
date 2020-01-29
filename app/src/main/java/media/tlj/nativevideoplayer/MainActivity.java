package media.tlj.nativevideoplayer;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import media.tlj.nativevideoplayer.screens.PlayerActivity;
import media.tlj.nativevideoplayer.screens.PlaylistActivity;

public class MainActivity extends AppCompatActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        ImageButton lasEstrellas_play = findViewById(R.id.img_play_btn_estrellas);
        ImageButton vod_play = findViewById(R.id.img_play_btn_vod);

        lasEstrellas_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("senal_estrellas_live", getString(R.string.senal_dai_estrellas));
                startActivity(intent);
            }
        });

        vod_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlaylistActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }
}
