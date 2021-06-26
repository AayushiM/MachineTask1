package com.machinetask.Activities


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.machinetask.R
import com.machinetask.utils.Utility
import com.machinetask.utils.XmlToJson
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity() {
    private var arrayList = ArrayList<Datamodel>()
    var rvEarthquake: RecyclerView? = null
    lateinit var tvEarthquakeTitle : TextView
    lateinit var tvEarthquakeTime : TextView
    lateinit var tvTryAgain : TextView
    lateinit var layoutError : RelativeLayout
     var mswipe : SwipeRefreshLayout ? =null
    var jsonObject : JSONObject? = null

    internal var earthquakeAdapter: EarthquakeAdapter ? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvEarthquake = findViewById(R.id.rvEarthquake)
        tvEarthquakeTitle = findViewById(R.id.tvEarthquakeTitle)
        tvEarthquakeTime = findViewById(R.id.tvEarthquakeTime)
        tvTryAgain = findViewById(R.id.tvTryAgain)
        layoutError = findViewById(R.id.layoutError)
        mswipe = findViewById(R.id.mswipe)
        rvEarthquake!!.setHasFixedSize(true)
            rvEarthquake!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)

        tvEarthquakeTitle.setOnClickListener {
            tvEarthquakeTitle.setTypeface(null, Typeface.BOLD);
//            Collections.sort(arrayList,
//                Comparator { o1, o2 -> o2.title!!.toInt() - o1.title!!.toInt()
//                }
//            )
            Collections.sort(arrayList, Comparator { obj1, obj2 ->
                // ## Ascending order
                obj1.title!!.compareTo(obj2.title!!) // To compare string values
                // return Integer.valueOf(obj1.empId).compareTo(Integer.valueOf(obj2.empId)); // To compare integer values

                // ## Descending order
                // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
            })
            earthquakeAdapter!!.notifyDataSetChanged()
        }
        tvEarthquakeTime.setOnClickListener {
            tvEarthquakeTime.setTypeface(null, Typeface.BOLD);
//            Collections.sort(arrayList,
//                Comparator { o1, o2 -> o2.title!!.toInt() - o1.title!!.toInt()
//                }
//            )
            Collections.sort(arrayList, Comparator { obj1, obj2 ->
                // ## Ascending order
//                obj1.content!!.compareTo(obj2.content!!) // To compare string values
                // return Integer.valueOf(obj1.empId).compareTo(Integer.valueOf(obj2.empId)); // To compare integer values

                // ## Descending order
                  obj2.content!!.compareTo(obj1.content!!); // To compare string values
                // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values
            })
            earthquakeAdapter!!.notifyDataSetChanged()
        }

        if (isNetworkConnectionAvailable()){
            GetWishListAPI(true);
        }else {
            try {
                GetWishListAPI(true);
                  jsonObject  = JSONObject(Utility.json!!)

            } catch (e:JSONException) {
                e.printStackTrace();
            }
        }
//        GetWishListAPI(true)

        mswipe!!.setOnRefreshListener {
            GetWishListAPI(false)
        }

        tvTryAgain.setOnClickListener {
            GetWishListAPI(true)
        }
    }
    fun isNetworkConnectionAvailable(): Boolean {
        val cm: ConnectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info: NetworkInfo = cm.getActiveNetworkInfo() ?: return false
        val network: NetworkInfo.State = info.getState()
        return network === NetworkInfo.State.CONNECTED || network === NetworkInfo.State.CONNECTING
    }

    private var mRequestQueue: RequestQueue? = null
    private var mStringRequest: StringRequest? = null
    private val url =
        "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.atom"

    //GetWishListAPI
    open fun GetWishListAPI( boolean: Boolean): Unit {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_login)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        if(boolean){
            dialog.show()
        }
        else mswipe!!.isRefreshing = false

        //RequestQueue initialized
        mRequestQueue = Volley.newRequestQueue(this)

        //String Request initialized
        mStringRequest = StringRequest(
            Request.Method.GET,
            url,
            Response.Listener { response ->
                dialog.dismiss()
                layoutError.visibility = View.GONE
                rvEarthquake!!.visibility = View.VISIBLE
                val xmlToJson =
                    XmlToJson.Builder(response).build()

                // convert to a formatted Json String

                // convert to a formatted Json String
                val aaa = xmlToJson.toFormattedString()
                Utility.setjson(aaa!!)
                // convert to a JSONObject
                 jsonObject = xmlToJson.toJson()
                try {
                    val jsonArray =
                        jsonObject!!.getJSONObject("feed").getJSONArray("entry")
                    Log.e("jsonarraysize", jsonArray.length().toString() + "")
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject1 =
                            jsonArray.getJSONObject(i).getString("title")
                        val jsonObject2 =
                            jsonArray.getJSONObject(i).getString("id")
                       val georss =
                            jsonArray.getJSONObject(i).getString("georss:point")
                        val jsonObject3 =
                            jsonArray.getJSONObject(i).getJSONObject("link")
                        val  href = jsonObject3.getString("href")
                       val summary =
                            jsonArray.getJSONObject(i).getJSONObject("summary")
                        val  content = summary.getString("content")



                        Log.e("jsonObject1", jsonObject1.toString() + "")
                        val datamodel: Datamodel =
                            Datamodel()
                        datamodel.title = jsonObject1
                        datamodel.description = jsonObject2
                        datamodel.href = href
                        datamodel.georss = georss
                        datamodel.content = content
                        arrayList.add(datamodel)
                    }

                    earthquakeAdapter = EarthquakeAdapter(this,arrayList)
                    rvEarthquake!!.adapter = earthquakeAdapter
//                    rvEarthquake!!.setAdapter(
//                        EarthquakeAdapter(
//                            this, arrayList
//                        )
//                    )

                    Log.e("arraylist", arrayList.size.toString() + "")

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                // convert to a Json String
                val jsonString = xmlToJson.toString()

                // convert to a formatted Json String
                val formatted = xmlToJson.toFormattedString()
                Log.d("qwerty", "vishal$formatted")
//                Toast.makeText(
//                    applicationContext,
//                    "Response :$response",
//                    Toast.LENGTH_LONG
//                ).show() //display the response on screen
            },
            Response.ErrorListener {

                    error ->
                dialog.dismiss()
                layoutError.visibility = View.VISIBLE
                rvEarthquake!!.visibility = View.GONE
                Log.i("fghj", "Error :$error")
            })
        mRequestQueue!!.add(mStringRequest)
    }

    internal class Datamodel {
        var title: String? = null
        var description: String? = null
        var href: String? = null
        var georss: String? = null
        var content: String? = null

    }

    internal class EarthquakeAdapter(val context: Context
                               , val mlist:ArrayList<Datamodel>
    ) :
        RecyclerView.Adapter<EarthquakeAdapter.MyViewHolder>() {
        internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var tvEarthquakeId: TextView = view.findViewById(R.id.tvEarthquakeId)
            var tvEarthquakeTitle: TextView = view.findViewById(R.id.tvEarthquakeTitle)
            var tvEarthquakeTerms: TextView = view.findViewById(R.id.tvEarthquakeTerms)
            var tvEarthquakeLabel: TextView = view.findViewById(R.id.tvEarthquakeLabel)
            var tvEarthquakeLink: TextView = view.findViewById(R.id.tvEarthquakeLink)

        }

        @NonNull
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_row, parent, false)
            return MyViewHolder(itemView)
        }


        @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {



            holder.tvEarthquakeId.setText("ID: "+mlist.get(position).description)
            holder.tvEarthquakeTitle.setText("TITLE: "+mlist.get(position).title)
            holder.tvEarthquakeLink.setText(mlist.get(position).href)
            holder.tvEarthquakeTerms.setText("georss:point- "+mlist.get(position).georss)
            holder.tvEarthquakeLabel.setText(Html.fromHtml(mlist.get(position).content))



        }

        override fun getItemCount(): Int {
            return mlist.size
        }

    }
}