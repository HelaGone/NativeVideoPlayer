package media.tlj.nativevideoplayer.models;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoItemModel implements Parcelable {
    private String v_id;
    private String v_title;
    private String v_description;
    private String v_duration;
    private String v_program_name;
    private String v_channel;
    private String v_media_url;
    private String v_thumbnail;

    public VideoItemModel(String v_id, String v_title, String v_description, String v_duration, String v_program_name, String v_channel, String v_media_url, String v_thumbnail){
        this.v_id = v_id;
        this.v_title = v_title;
        this.v_description = v_description;
        this.v_duration = v_duration;
        this.v_program_name = v_program_name;
        this.v_channel = v_channel;
        this.v_media_url = v_media_url;
        this.v_thumbnail = v_thumbnail;
    }

    public String getV_id() {
        return v_id;
    }

    public String getV_title() {
        return v_title;
    }

    public String getV_description() {
        return v_description;
    }

    public String getV_duration() {
        return v_duration;
    }

    public String getV_program_name() {
        return v_program_name;
    }

    public String getV_channel() {
        return v_channel;
    }

    public String getV_media_url() {
        return v_media_url;
    }

    public String getV_thumbnail() {
        return v_thumbnail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeString(v_id);
        dest.writeString(v_title);
        dest.writeString(v_description);
        dest.writeString(v_duration);
        dest.writeString(v_program_name);
        dest.writeString(v_channel);
        dest.writeString(v_media_url);
        dest.writeString(v_thumbnail);
    }

    private VideoItemModel(Parcel in){
        v_id = in.readString();
        v_title = in.readString();
        v_description = in.readString();
        v_duration = in.readString();
        v_program_name = in.readString();
        v_channel = in.readString();
        v_media_url = in.readString();
        v_thumbnail = in.readString();
    }

    public static final Parcelable.Creator<VideoItemModel> CREATOR = new Parcelable.Creator<VideoItemModel>(){
      public VideoItemModel createFromParcel(Parcel in){
          return new VideoItemModel(in);
      }
      public VideoItemModel[] newArray(int size){
          return new VideoItemModel[size];
      }
    };
}
