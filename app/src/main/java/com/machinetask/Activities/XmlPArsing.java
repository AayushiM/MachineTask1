package com.machinetask.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.machinetask.R;
import com.machinetask.utils.Utility;
import com.machinetask.utils.XmlToJson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class XmlPArsing extends AppCompatActivity {

    ArrayList<Datamodel> arrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GetWishListAPI();
//        JSONObject obj = new JSONObject(Utility.);
//                Log.d(
//                    "qwerty",
//                    "vishal" + obj.getJSONObject("feed").getJSONArray("entry").length() + "___"
//                );
//                Log.d("qwerty", "no internet___");
    }
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private String url = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.atom";
    //GetWishListAPI
    public void GetWishListAPI() {


        //RequestQueue initialized
        mRequestQueue = Volley.newRequestQueue(this);

        //String Request initialized
        mStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                XmlToJson xmlToJson = new XmlToJson.Builder(response.toString()).build();
                // convert to a JSONObject
                JSONObject jsonObject = xmlToJson.toJson();

                try {
                    JSONArray jsonArray = jsonObject.getJSONObject("feed").getJSONArray("entry");
                    Log.e("jsonarraysize",jsonArray.length()+"");
                    for (int i=0; i<jsonArray.length();i++){
                        String jsonObject1 = jsonArray.getJSONObject(i).getString("title");
                        String jsonObject2 = jsonArray.getJSONObject(i).getString("id");
                        Log.e("jsonObject1",jsonObject1.toString()+"");
                        Datamodel datamodel = new Datamodel();
                        datamodel.setTitle(jsonObject1);
                        datamodel.setTitle(jsonObject2);
                        arrayList.add(datamodel);
                    }

                    Log.e("arraylist",arrayList.size()+"");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // convert to a Json String
                String jsonString = xmlToJson.toString();

                // convert to a formatted Json String
                String formatted = xmlToJson.toFormattedString();
                Log.d("qwerty", "vishal" + formatted);
                Toast.makeText(getApplicationContext(),"Response :" + response.toString(), Toast.LENGTH_LONG).show();//display the response on screen

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.i("fghj","Error :" + error.toString());
            }
        });

        mRequestQueue.add(mStringRequest);
    }

    class Datamodel {
        public String title;
        public String description;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}