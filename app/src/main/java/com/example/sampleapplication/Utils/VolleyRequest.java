package com.example.sampleapplication.Utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleyRequest {

    private static RequestQueue requestQueue;

    public static RequestQueue init(Context context) {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
            return requestQueue;
        } else
            return requestQueue;
    }

    public static void addRequest(Request volleyRequest){
        requestQueue.add(volleyRequest);
    }
}
