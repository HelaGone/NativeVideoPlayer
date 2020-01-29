package media.tlj.nativevideoplayer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Locale;
import media.tlj.nativevideoplayer.R;
import media.tlj.nativevideoplayer.models.VideoItemModel;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private List<VideoItemModel> dataset;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


    public PlaylistAdapter(List<VideoItemModel> dataset, Context ctx){
        this.dataset = dataset;
        this.context = ctx;
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder{
        TextView title, description, duration, program, channel;
        ImageView thumbnail;

        private PlaylistViewHolder(View view){
            super(view);
            thumbnail = view.findViewById(R.id.v_thumbnail);
            title = view.findViewById(R.id.v_title);
            description = view.findViewById(R.id.v_description);
            duration = view.findViewById(R.id.v_duration);
            program = view.findViewById(R.id.v_program);
            channel = view.findViewById(R.id.v_channel);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(view, position);
                        }
                    }
                }
            });
        }

        void bindVideoItem(final VideoItemModel videoItemModel){

            title.setText(videoItemModel.getV_title());
            description.setText(videoItemModel.getV_description());
            duration.setText(segToTimeFormat(videoItemModel.getV_duration()));
            program.setText(videoItemModel.getV_program_name());
            channel.setText(videoItemModel.getV_channel());

            Glide.with(context).load(videoItemModel.getV_thumbnail()).into(thumbnail);

        }

    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistAdapter.PlaylistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        final VideoItemModel mVideoItemModel = dataset.get(position);
        holder.bindVideoItem(mVideoItemModel);
    }

    @Override
    public int getItemCount() {
        return null != dataset ? dataset.size() : 0;
    }

    private String segToTimeFormat(String segs){
        String duration = "";

        int intSegs = Integer.parseInt(segs);
        int hours = intSegs / 3600;
        int minutes = (intSegs % 3600) / 60;
        int seconds = intSegs % 60;

        duration = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

        return duration;
    }
}
