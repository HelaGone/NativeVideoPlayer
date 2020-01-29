package media.tlj.nativevideoplayer.requests;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class RequestAssets {
    private static final String TAG = RequestAssets.class.getSimpleName();
    private Context context;
    private RequestQueue requestQueue;
    private static final ThreadLocal<RequestAssets> mInstance = new ThreadLocal<RequestAssets>();

    private RequestAssets(Context ctx){
        context = ctx;
        requestQueue = getRequestQueue();
    }

    public static synchronized RequestAssets getInstance(Context context){
        if(mInstance.get() == null){
            mInstance.set(new RequestAssets(context));
        }
        return mInstance.get();
    }

    public RequestQueue getRequestQueue() {
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    public<T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }


}
