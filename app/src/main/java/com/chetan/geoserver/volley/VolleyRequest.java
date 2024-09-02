
package com.chetan.geoserver.volley;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

public class VolleyRequest {

    private final String URL;
    private final Response.Listener SuccessResponse;
    private final Response.ErrorListener ErrorResponse;
    private RequestQueue queue ;

    public VolleyRequest( String URL, Response.Listener successResponse, Response.ErrorListener errorResponse) {
        this.URL = URL;
        SuccessResponse = successResponse;
        ErrorResponse = errorResponse;
    }

    public void SimpleVolleyGetRequest() {

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                URL,
                response -> {
                    //System.out.println("SimpleVolleyGetRequest response: " + response);
                    SuccessResponse.onResponse(response);
                },
                error -> {
                    System.out.println("SimpleVolleyGetRequest error: " + error);
                    ErrorResponse.onErrorResponse(error);
                });

        queue= VolleySingleton.getInstance().getRequestQueue();
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(stringRequest);
    }

}