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
        const val BASE_URL = "http://barisoulwrite.atwebpages.com/backend/"
        const val REGISTER_URL = "${BASE_URL}register.php"
        const val LOGIN_URL = "${BASE_URL}login.php"
        const val ADD_JOURNAL_URL = "${BASE_URL}add_journal.php"
        const val GET_JOURNALS_URL = "${BASE_URL}get_journals.php"
        const val UPDATE_JOURNAL_URL = "${BASE_URL}update_journal.php"
        const val DELETE_JOURNAL_URL = "${BASE_URL}delete_journal.php"
        const val SEARCH_JOURNALS_URL = "${BASE_URL}search_journals.php"
        const val UPDATE_PROFILE_URL = "${BASE_URL}update_profile.php"
        const val UPLOAD_IMAGE_URL = "${BASE_URL}upload_image.php"
        const val FOLLOW_USER_URL = "${BASE_URL}follow_user.php"
        const val UNFOLLOW_USER_URL = "${BASE_URL}unfollow_user.php"
        const val GET_FOLLOWERS_URL = "${BASE_URL}get_followers.php"
        const val SEARCH_USERS_URL = "${BASE_URL}search_users.php"
        const val GET_USER_JOURNALS_URL = "${BASE_URL}get_user_journals.php"
        const val GET_FEED_URL = "${BASE_URL}get_feed.php"
        const val LIKE_JOURNAL_URL = "${BASE_URL}like_journal.php"
        const val ADD_COMMENT_URL = "${BASE_URL}add_comment.php"
        const val GET_COMMENTS_URL = "${BASE_URL}get_comments.php"
        const val GET_NOTIFICATIONS_URL = "${BASE_URL}get_notifications.php"
        const val UPDATE_FCM_TOKEN_URL = "${BASE_URL}update_fcm_token.php"

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
        userId: String? = null,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = if (userId != null) {
            "$SEARCH_JOURNALS_URL?query=$query&user_id=$userId"
        } else {
            "$SEARCH_JOURNALS_URL?query=$query"
        }

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
        android.util.Log.d("UploadImage", "Starting upload for: $fileName, size: ${imageData.size} bytes")

        val request = object : com.android.volley.Request<String>(
            Method.POST, UPLOAD_IMAGE_URL,
            com.android.volley.Response.ErrorListener { error ->
                val errorMsg = error.message ?: "Network error"
                android.util.Log.e("UploadImage", "Upload failed: $errorMsg")
                if (error.networkResponse != null) {
                    android.util.Log.e("UploadImage", "Status code: ${error.networkResponse.statusCode}")
                    android.util.Log.e("UploadImage", "Response: ${String(error.networkResponse.data)}")
                }
                onError(errorMsg)
            }
        ) {
            override fun getRetryPolicy(): com.android.volley.RetryPolicy {
                // Increase timeout for image uploads (30 seconds)
                return com.android.volley.DefaultRetryPolicy(
                    30000,
                    com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )
            }

            override fun getBodyContentType(): String {
                return "multipart/form-data; boundary=$BOUNDARY"
            }

            override fun getBody(): ByteArray {
                val outputStream = java.io.ByteArrayOutputStream()

                try {
                    // Add image file
                    outputStream.write(("--$BOUNDARY\r\n").toByteArray())
                    outputStream.write(("Content-Disposition: form-data; name=\"image\"; filename=\"$fileName\"\r\n").toByteArray())
                    outputStream.write(("Content-Type: image/jpeg\r\n\r\n").toByteArray())
                    outputStream.write(imageData)
                    outputStream.write(("\r\n").toByteArray())

                    // End of multipart
                    outputStream.write(("--$BOUNDARY--\r\n").toByteArray())

                    android.util.Log.d("UploadImage", "Multipart body created, total size: ${outputStream.size()} bytes")

                } catch (e: Exception) {
                    android.util.Log.e("UploadImage", "Error creating request body: ${e.message}")
                    e.printStackTrace()
                }

                return outputStream.toByteArray()
            }

            override fun parseNetworkResponse(response: com.android.volley.NetworkResponse?): com.android.volley.Response<String> {
                return try {
                    val jsonString = String(response?.data ?: ByteArray(0), Charsets.UTF_8)
                    // Try to extract JSON if response contains HTML
                    val cleanJson = if (jsonString.contains("{") && jsonString.contains("}")) {
                        val startIndex = jsonString.indexOf("{")
                        val endIndex = jsonString.lastIndexOf("}") + 1
                        jsonString.substring(startIndex, endIndex)
                    } else {
                        jsonString
                    }
                    com.android.volley.Response.success(cleanJson, com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response))
                } catch (e: Exception) {
                    com.android.volley.Response.error(com.android.volley.ParseError(e))
                }
            }

            override fun deliverResponse(response: String) {
                try {
                    // Clean the response
                    val cleanResponse = response.trim()
                    android.util.Log.d("UploadImage", "Response: $cleanResponse")

                    val jsonResponse = JSONObject(cleanResponse)
                    if (jsonResponse.getBoolean("success")) {
                        val imageUrl = jsonResponse.getString("image_url")
                        val thumbnailUrl = jsonResponse.getString("thumbnail_url")
                        onSuccess(imageUrl, thumbnailUrl)
                    } else {
                        onError(jsonResponse.optString("message", "Upload failed"))
                    }
                } catch (e: org.json.JSONException) {
                    android.util.Log.e("UploadImage", "JSON Error: ${e.message}, Response: $response")
                    onError("Invalid JSON response from server")
                } catch (e: Exception) {
                    android.util.Log.e("UploadImage", "Error: ${e.message}")
                    onError("Error parsing response: ${e.message}")
                }
            }
        }

        requestQueue.add(request)
    }

    fun followUser(
        followerId: String,
        followingId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["follower_id"] = followerId
        params["following_id"] = followingId

        val request = object : StringRequest(
            Method.POST, FOLLOW_USER_URL,
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

    fun unfollowUser(
        followerId: String,
        followingId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["follower_id"] = followerId
        params["following_id"] = followingId

        val request = object : StringRequest(
            Method.POST, UNFOLLOW_USER_URL,
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

    fun searchUsers(
        query: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$SEARCH_USERS_URL?query=$query"

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

    fun getUserJournals(
        userId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$GET_USER_JOURNALS_URL?user_id=$userId"

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

    fun getFeed(
        userId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$GET_FEED_URL?user_id=$userId"

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

    fun deleteJournal(
        journalId: String,
        userId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["journal_id"] = journalId
        params["user_id"] = userId

        val request = object : StringRequest(
            Method.POST, DELETE_JOURNAL_URL,
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

    fun updateJournal(
        journalId: String,
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
        params["journal_id"] = journalId
        params["user_id"] = userId
        params["title"] = title
        params["content"] = content
        params["image_url"] = imageUrl
        params["thumbnail_url"] = thumbnailUrl
        params["is_public"] = if (isPublic) "1" else "0"

        val request = object : StringRequest(
            Method.POST, UPDATE_JOURNAL_URL,
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

    fun updateProfile(
        userId: String,
        name: String? = null,
        phone: String? = null,
        profileImage: String? = null,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["user_id"] = userId

        if (name != null) params["name"] = name
        if (phone != null) params["phone"] = phone
        if (profileImage != null) params["profile_image"] = profileImage

        val request = object : StringRequest(
            Method.POST, UPDATE_PROFILE_URL,
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

    fun makeGetRequest(
        url: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                onSuccess(response)
            },
            { error ->
                onError(error.message ?: "Network error")
            }
        )
        requestQueue.add(request)
    }

    fun likeJournal(
        journalId: String,
        userId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["journal_id"] = journalId
        params["user_id"] = userId

        android.util.Log.d("ApiHelper", "Liking journal - Journal: $journalId, User: $userId")

        val request = object : StringRequest(
            Method.POST, LIKE_JOURNAL_URL,
            { response ->
                try {
                    android.util.Log.d("ApiHelper", "Raw like response: $response")

                    // Clean the response - remove any HTML/PHP errors
                    val cleanResponse = response.trim()
                        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("<[^>]+>"), "")

                    // Find JSON in response if it's wrapped in HTML
                    val jsonStart = cleanResponse.indexOf("{")
                    val jsonEnd = cleanResponse.lastIndexOf("}") + 1

                    val jsonString = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                        cleanResponse.substring(jsonStart, jsonEnd)
                    } else {
                        cleanResponse
                    }

                    android.util.Log.d("ApiHelper", "Cleaned JSON: $jsonString")
                    val jsonResponse = JSONObject(jsonString)
                    onSuccess(jsonResponse)
                } catch (e: Exception) {
                    android.util.Log.e("ApiHelper", "Error parsing like response: ${e.message}")
                    android.util.Log.e("ApiHelper", "Raw response was: $response")
                    onError("Server error. Check if journal is public and you're not the owner.")
                }
            },
            { error ->
                android.util.Log.e("ApiHelper", "Network error liking journal: ${error.message}")
                onError(error.message ?: "Network error")
            }
        ) {
            override fun getParams(): Map<String, String> = params
        }

        requestQueue.add(request)
    }

    fun addComment(
        journalId: String,
        userId: String,
        commentText: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["journal_id"] = journalId
        params["user_id"] = userId
        params["comment_text"] = commentText

        android.util.Log.d("ApiHelper", "Adding comment - Journal: $journalId, User: $userId")

        val request = object : StringRequest(
            Method.POST, ADD_COMMENT_URL,
            { response ->
                try {
                    android.util.Log.d("ApiHelper", "Raw comment response: $response")

                    // Clean the response - remove any HTML/PHP errors
                    val cleanResponse = response.trim()
                        .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("<[^>]+>"), "")

                    // Find JSON in response if it's wrapped in HTML
                    val jsonStart = cleanResponse.indexOf("{")
                    val jsonEnd = cleanResponse.lastIndexOf("}") + 1

                    val jsonString = if (jsonStart >= 0 && jsonEnd > jsonStart) {
                        cleanResponse.substring(jsonStart, jsonEnd)
                    } else {
                        cleanResponse
                    }

                    android.util.Log.d("ApiHelper", "Cleaned JSON: $jsonString")
                    val jsonResponse = JSONObject(jsonString)
                    onSuccess(jsonResponse)
                } catch (e: Exception) {
                    android.util.Log.e("ApiHelper", "Error parsing comment response: ${e.message}")
                    android.util.Log.e("ApiHelper", "Raw response was: $response")
                    onError("Server error. Check if journal is public and you're not the owner.")
                }
            },
            { error ->
                android.util.Log.e("ApiHelper", "Network error adding comment: ${error.message}")
                onError(error.message ?: "Network error")
            }
        ) {
            override fun getParams(): Map<String, String> = params
        }

        requestQueue.add(request)
    }

    fun getComments(
        journalId: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$GET_COMMENTS_URL?journal_id=$journalId"

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

    fun updateFCMToken(
        userId: String,
        fcmToken: String,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val params = HashMap<String, String>()
        params["user_id"] = userId
        params["fcm_token"] = fcmToken

        val request = object : StringRequest(
            Method.POST, UPDATE_FCM_TOKEN_URL,
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
}
