package com.chetan.geoserver.volley;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.chetan.geoserver.MyApplication;

public class VolleySingleton {

    //static private instance of class
    private static VolleySingleton mInstance;

    private final RequestQueue mRequestQueue;

    //private constructor
    private VolleySingleton() {
        mRequestQueue = Volley.newRequestQueue(MyApplication.getAppContext());
    }

    // Static method to create instance of Singleton class
    public static  synchronized VolleySingleton getInstance(){
        if(mInstance==null){
            mInstance=new VolleySingleton();
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }
}
