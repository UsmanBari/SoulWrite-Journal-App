package com.uh.smdprojectsoulwrite

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ApiHelper(private val context: Context) {

    // ALL constants are now in this single companion object
    companion object {
        // AwardSpace hosted URL
        private const val BASE_URL = "http://barisoulwrite.atwebpages.com/backend/"
        const val REGISTER_URL = "${BASE_URL}register.php"
        const val LOGIN_URL = "${BASE_URL}login.php"
        const val ADD_JOURNAL_URL = "${BASE_URL}add_journal.php"
        const val GET_JOURNALS_URL = "${BASE_URL}get_journals.php"
        const val UPDATE_JOURNAL_URL = "${BASE_URL}update_journal.php"
        const val DELETE_JOURNAL_URL = "${BASE_URL}delete_journal.php"
        const val SEARCH_JOURNALS_URL = "${BASE_URL}search_journals.php"
        const val UPDATE_PROFILE_URL = "${BASE_URL}update_profile.php"
        const val UPLOAD_IMAGE_URL = "${BASE_URL}upload_image.php"

        // The boundary constant was moved here from the second companion object
        private const val BOUNDARY = "----WebKitFormBoundary7MA4YWxkTrZu0gW"
    }

    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    fun registerUser(
        name: String,
        email: String,
        phone: String,
        password: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["name"] = name
        params["email"] = email
        params["phone"] = phone
        params["password"] = password

        val request = object : StringRequest(
            Method.POST, REGISTER_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    onSuccess(jsonResponse)
                } catch (e: Exception) {
                    onError(e.message ?: "Error parsing response")
                }
            },
            { error ->
                onError(error.message ?: "Network error")
            }
        ) {
            override fun getParams(): Map<String, String> = params
        }

        requestQueue.add(request)
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["email"] = email
        params["password"] = password

        val request = object : StringRequest(
            Method.POST, LOGIN_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    onSuccess(jsonResponse)
                } catch (e: Exception) {
                    onError(e.message ?: "Error parsing response")
                }
            },
            { error ->
                onError(error.message ?: "Network error")
            }
        ) {
            override fun getParams(): Map<String, String> = params
        }

        requestQueue.add(request)
    }

    fun addJournal(
        userId: String,
        title: String,
        content: String,
        imageUrl: String,
        thumbnailUrl: String,
        isPublic: Boolean,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["user_id"] = userId
        params["title"] = title
        params["content"] = content
        params["image_url"] = imageUrl
        params["thumbnail_url"] = thumbnailUrl
        params["is_public"] = if (isPublic) "1" else "0"

        val request = object : StringRequest(
            Method.POST, ADD_JOURNAL_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    onSuccess(jsonResponse)
                } catch (e: Exception) {
                    onError(e.message ?: "Error parsing response")
                }
            },
            { error ->
                onError(error.message ?: "Network error")
            }
        ) {
            override fun getParams(): Map<String, String> = params
        }

        requestQueue.add(request)
    }

    fun getJournals(
        userId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$GET_JOURNALS_URL?user_id=$userId"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                onSuccess(response)
            },
            { error ->
                onError(error.message ?: "Network error")
            }
        )

        requestQueue.add(request)
    }

    fun searchJournals(
        query: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$SEARCH_JOURNALS_URL?query=$query"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                onSuccess(response)
            },
            { error ->
                onError(error.message ?: "Network error")
            }
        )

        requestQueue.add(request)
    }

    fun uploadImage(
        imageData: ByteArray,
        fileName: String,
        onSuccess: (imageUrl: String, thumbnailUrl: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = object : com.android.volley.Request<JSONObject>(
            Method.POST, UPLOAD_IMAGE_URL,
            { error ->
                onError(error.message ?: "Network error")
            }
        ) {
            override fun getBodyContentType(): String {
                return "multipart/form-data; boundary=$BOUNDARY"
            }

            override fun getBody(): ByteArray {
                val outputStream = java.io.ByteArrayOutputStream()
                val writer = java.io.PrintWriter(outputStream.writer())

                // Add image file
                writer.append("--$BOUNDARY\r\n")
                writer.append("Content-Disposition: form-data; name=\"image\"; filename=\"$fileName\"\r\n")
                writer.append("Content-Type: image/jpeg\r\n\r\n")
                writer.flush()
                outputStream.write(imageData)
                writer.append("\r\n")
                writer.flush()

                // End of multipart
                writer.append("--$BOUNDARY--\r\n")
                writer.flush()

                return outputStream.toByteArray()
            }

            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse?): com.android.volley.Response<JSONObject> {
                return try {
                    val jsonString = String(response?.data ?: ByteArray(0))
                    val jsonResponse = JSONObject(jsonString)
                    com.android.volley.Response.success(jsonResponse, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response))
                } catch (e: Exception) {
                    com.android.volley.Response.error(com.android.volley.ParseError(e))
                }
            }

            override fun deliverResponse(response: JSONObject) {
                try {
                    if (response.getBoolean("success")) {
                        val imageUrl = response.getString("image_url")
                        val thumbnailUrl = response.getString("thumbnail_url")
                        onSuccess(imageUrl, thumbnailUrl)
                    } else {
                        onError(response.getString("message"))
                    }
                } catch (e: Exception) {
                    onError(e.message ?: "Error parsing response")
                }
            }
        }

        requestQueue.add(request)
    }
}