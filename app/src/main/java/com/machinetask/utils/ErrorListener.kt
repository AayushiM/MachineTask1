package com.machinetask.utils

import com.android.volley.Response
import com.android.volley.VolleyError

class ErrorListener : Response.ErrorListener {
    override fun onErrorResponse(error: VolleyError) {}
}