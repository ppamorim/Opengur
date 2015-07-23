package com.kenny.openimgur.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.kenny.openimgur.BuildConfig;
import com.kenny.openimgur.R;
import com.kenny.openimgur.classes.ImgurUser;
import com.kenny.openimgur.classes.OpengurApp;
import com.kenny.openimgur.util.LogUtil;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.util.AsyncExecutor;

/**
 * Created by kcampagna on 6/14/14.
 */
public class ApiClient {
    private static final String TAG = ApiClient.class.getSimpleName();

    public enum HttpRequest {
        GET,
        POST,
        DELETE
    }

    public static final int STATUS_OK = 200;

    public static final int STATUS_INVALID_PARAM = 400;

    public static final int STATUS_INVALID_PERMISSIONS = 401;

    public static final int STATUS_FORBIDDEN = 403;

    public static final int STATUS_NOT_FOUND = 404;

    public static final int STATUS_RATING_LIMIT = 429;

    public static final int STATUS_INTERNAL_ERROR = 500;

    public static final int STATUS_OVER_CAPACITY = 503;

    // These are custom error codes not given from the server
    public static final int STATUS_IO_EXCEPTION = 600;

    public static final int STATUS_JSON_EXCEPTION = 700;

    public static final int STATUS_EMPTY_RESPONSE = 800;

    public static final String KEY_SUCCESS = "success";

    public static final String KEY_STATUS = "status";

    public static final String KEY_DATA = "data";

    public static final String CLIENT_ID = BuildConfig.API_CLIENT_ID;

    public static final String CLIENT_SECRET = BuildConfig.API_CLIENT_SECRET;

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final long DEFAULT_TIMEOUT = 15;

    private static boolean sIsFetchingToken = false;

    private String mUrl;

    private OkHttpClient mClient = new OkHttpClient();

    private HttpRequest mRequestType = HttpRequest.GET;

    public ApiClient(String url, HttpRequest requestType) {
        mRequestType = requestType;
        mUrl = url;
        mClient.setConnectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    public void setRequestType(HttpRequest requestType) {
        this.mRequestType = requestType;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    /**
     * Executes a GET HTTP Request
     *
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject get() throws IOException, JSONException {
        Request request = new Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, getAuthorizationHeader())
                .get()
                .url(mUrl)
                .build();

        return makeRequest(request);
    }

    /**
     * Executes a POST HTTP Request
     *
     * @param body
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject post(@NonNull RequestBody body) throws IOException, JSONException {
        Request request = new Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, getAuthorizationHeader())
                .post(body)
                .url(mUrl)
                .build();

        return makeRequest(request);
    }

    /**
     * Executes a DELETE HTTP Request
     *
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject delete() throws IOException, JSONException {
        Request request = new Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, getAuthorizationHeader())
                .delete()
                .url(mUrl)
                .build();

        return makeRequest(request);
    }

    /**
     * Makes the request and returns the result
     *
     * @param request
     * @return
     * @throws IOException
     * @throws JSONException
     */
    private JSONObject makeRequest(Request request) throws IOException, JSONException {
        JSONObject json;
        LogUtil.v(TAG, "Making request to " + mUrl);
        Response response = mClient.newCall(request).execute();

        if (response.isSuccessful()) {
            LogUtil.v(TAG, "Request to " + request.urlString() + " Successful with status code " + response.code());
            String serverResponse = response.body().string();
            response.body().close();

            // Sometimes the Api response with an empty string when it is experiencing problems
            if (TextUtils.isEmpty(serverResponse)) {
                LogUtil.w(TAG, "Response body is empty");
                json = new JSONObject();
                json.put(KEY_SUCCESS, false);
                json.put(KEY_STATUS, STATUS_EMPTY_RESPONSE);
            } else {
                json = new JSONObject(serverResponse);
            }
        } else {
            int statusCode = response.code();
            LogUtil.w(TAG, "Request to " + request.urlString() + " Failed with status code " + statusCode);
            json = new JSONObject();
            json.put(KEY_SUCCESS, false);
            json.put(KEY_STATUS, statusCode);
            response.body().close();

            if (!sIsFetchingToken && statusCode == ApiClient.STATUS_FORBIDDEN && OpengurApp.getInstance().getUser() != null) {
                // User tokens are no longer valid, invalidate their profile
                OpengurApp.getInstance().onLogout();
            }
        }

        return json;
    }

    /**
     * Calls the appropriate method based on the HTTPRequest type
     *
     * @param type       The Type of event
     * @param id         An optional unique id for the EventBus
     * @param postParams Items to be posted. MUST be supplied if RequestType is POST
     * @throws IOException
     * @throws JSONException
     */
    public void doWork(final ImgurBusEvent.EventType type, @Nullable String id, final @Nullable RequestBody postParams) {
        if (mUrl == null) throw new NullPointerException("Url is null");

        final String requestId = TextUtils.isEmpty(id) ? String.valueOf(System.currentTimeMillis()) : id;

        switch (mRequestType) {
            case POST:
                if (postParams == null) {
                    throw new NullPointerException("Post params can not be null when making a POST call");
                }

                AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                    @Override
                    public void run() throws Exception {
                        EventBus.getDefault().post(new ImgurBusEvent(post(postParams), type, HttpRequest.POST, requestId));
                    }
                });
                break;

            case DELETE:
                AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                    @Override
                    public void run() throws Exception {
                        EventBus.getDefault().post(new ImgurBusEvent(delete(), type, HttpRequest.DELETE, requestId));
                    }
                });
                break;

            case GET:
            default:
                AsyncExecutor.create().execute(new AsyncExecutor.RunnableEx() {
                    @Override
                    public void run() throws Exception {
                        EventBus.getDefault().post(new ImgurBusEvent(get(), type, HttpRequest.GET, requestId));
                    }
                });
                break;
        }

    }

    /**
     * Calls the appropriate method based on the HTTPRequest type. This does not fire an event through EventBus
     *
     * @param postParams Items to be posted. MUST be supplied if RequestType is POST
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public JSONObject doWork(@Nullable RequestBody postParams) throws IOException, JSONException {
        if (mUrl == null) {
            throw new NullPointerException("Url is null");
        }

        switch (mRequestType) {
            case POST:
                /* OKHttp does not allow for empty POST params when making a POST request. The Imgur Api has some POST calls
                 that can be sent with empty POST params, so we need to just send something so OKHttp doesn't crash*/
                if (postParams == null) {
                    throw new NullPointerException("Post params can not be null when making a POST call");
                }

                return post(postParams);

            case DELETE:
                return delete();

            case GET:
            default:
                return get();
        }

    }

    /**
     * Returns the header for the Authorization header
     *
     * @return
     */
    private String getAuthorizationHeader() {
        ImgurUser user = OpengurApp.getInstance().getUser();

        if (user != null) {
            if (user.isAccessTokenValid()) {
                LogUtil.v(TAG, "Access Token present and valid");
                return "Bearer " + user.getAccessToken();
            } else if (!sIsFetchingToken) {
                sIsFetchingToken = true;
                // Our token is no longer valid, attempt to get a fresh one
                String token = refreshToken(user);
                if (!TextUtils.isEmpty(token)) return "Bearer " + token;
            }
        }

        LogUtil.v(TAG, "No access token present, using Client-ID");
        return "Client-ID " + CLIENT_ID;
    }

    private String refreshToken(ImgurUser user) {
        final RequestBody requestBody = new FormEncodingBuilder()
                .add("client_id", ApiClient.CLIENT_ID)
                .add("client_secret", ApiClient.CLIENT_SECRET)
                .add("refresh_token", user.getRefreshToken())
                .add("grant_type", "refresh_token").build();

        Request request = new Request.Builder()
                .addHeader(AUTHORIZATION_HEADER, CLIENT_ID)
                .post(requestBody)
                .url(Endpoints.REFRESH_TOKEN.getUrl())
                .build();

        try {
            LogUtil.v(TAG, "Requesting new access token");
            JSONObject json = makeRequest(request);
            String accessToken = json.getString(ImgurUser.KEY_ACCESS_TOKEN);
            String refreshToken = json.getString(ImgurUser.KEY_REFRESH_TOKEN);
            long expiration = System.currentTimeMillis() + (json.getLong(ImgurUser.KEY_EXPIRES_IN) * DateUtils.SECOND_IN_MILLIS);
            user.setTokens(accessToken, refreshToken, expiration);
            OpengurApp app = OpengurApp.getInstance();
            app.getSql().updateUserTokens(accessToken, refreshToken, expiration);
            app.setUser(user);
            LogUtil.v(TAG, "New refresh token received");
            sIsFetchingToken = false;
            return user.getAccessToken();
        } catch (JSONException ex) {
            LogUtil.e(TAG, "Error parsing user tokens", ex);
        } catch (IOException ex) {
            LogUtil.e(TAG, "Error parsing user tokens", ex);
        }

        sIsFetchingToken = false;
        return null;
    }

    /**
     * Returns the string resource for the given error code
     *
     * @param statusCode
     * @return
     */
    @StringRes
    public static int getErrorCodeStringResource(int statusCode) {
        switch (statusCode) {
            case ApiClient.STATUS_FORBIDDEN:
                return R.string.error_403;

            case ApiClient.STATUS_INVALID_PERMISSIONS:
                return R.string.error_401;

            case ApiClient.STATUS_RATING_LIMIT:
                return R.string.error_429;

            case ApiClient.STATUS_OVER_CAPACITY:
                return R.string.error_503;

            case ApiClient.STATUS_EMPTY_RESPONSE:
                return R.string.error_800;

            case ApiClient.STATUS_IO_EXCEPTION:
                return R.string.error_600;

            case ApiClient.STATUS_JSON_EXCEPTION:
            case ApiClient.STATUS_NOT_FOUND:
            case ApiClient.STATUS_INTERNAL_ERROR:
            case ApiClient.STATUS_INVALID_PARAM:
            default:
                return R.string.error_generic;
        }
    }
}
