package com.kenny.openimgur.fragments;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kenny.openimgur.R;
import com.kenny.openimgur.activities.ViewActivity;
import com.kenny.openimgur.adapters.ProfileCommentAdapter2;
import com.kenny.openimgur.api.ApiClient;
import com.kenny.openimgur.api.Endpoints;
import com.kenny.openimgur.api.ImgurBusEvent;
import com.kenny.openimgur.classes.ImgurComment;
import com.kenny.openimgur.classes.ImgurFilters.CommentSort;
import com.kenny.openimgur.classes.ImgurHandler;
import com.kenny.openimgur.classes.ImgurUser;
import com.kenny.openimgur.ui.MultiStateView;
import com.kenny.openimgur.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.util.ThrowableFailureEvent;

/**
 * Created by kcampagna on 12/22/14.
 */
public class ProfileCommentsFragment extends BaseFragment implements View.OnClickListener {
    private static final String KEY_SORT = "sort";

    private static final String KEY_USER = "user";

    private static final String KEY_ITEMS = "items";

    private static final String KEY_POSITION = "position";

    private static final String KEY_PAGE = "page";

    @InjectView(R.id.multiView)
    MultiStateView mMultiStatView;

    @InjectView(R.id.commentList)
    RecyclerView mListView;

    private int mPage = 0;

    private boolean mHasMore = true;

    private boolean mIsLoading = false;

    private ImgurUser mSelectedUser;

    private ProfileCommentAdapter2 mAdapter;

    private ApiClient mClient;

    private CommentSort mSort;

    public static Fragment createInstance(@NonNull ImgurUser user) {
        ProfileCommentsFragment fragment = new ProfileCommentsFragment();
        Bundle args = new Bundle(1);
        args.putParcelable(KEY_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_comments_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort:
                final String[] items = getResources().getStringArray(R.array.comments_sort);

                new AlertDialog.Builder(getActivity(), theme.getAlertDialogTheme())
                        .setTitle(R.string.sort_by)
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSort = CommentSort.getSortType(items[which]);
                                if (mAdapter != null) mAdapter.clear();
                                mPage = 0;
                                mHasMore = true;
                                mIsLoading = false;
                                fetchComments();
                                mMultiStatView.setViewState(MultiStateView.ViewState.LOADING);
                            }
                        }).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                // Need to pad one as the position will be base 0 and the adapter count will not
                int lastVisible = manager.findLastCompletelyVisibleItemPosition() + 1;
                int totalItemCount = mAdapter.getItemCount();

                if (!mIsLoading && mHasMore && totalItemCount > 0 && lastVisible >= totalItemCount) {
                    mIsLoading = true;
                    mPage++;
                    fetchComments();
                }
            }
        });
        handleArgs(getArguments(), savedInstanceState);
    }

    private void handleArgs(Bundle args, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSelectedUser = savedInstanceState.getParcelable(KEY_USER);
            mSort = CommentSort.getSortType(savedInstanceState.getString(KEY_SORT, null));

            if (savedInstanceState.containsKey(KEY_ITEMS)) {
                ArrayList<ImgurComment> comments = savedInstanceState.getParcelableArrayList(KEY_ITEMS);
                mPage = savedInstanceState.getInt(KEY_PAGE, 0);
                mAdapter = new ProfileCommentAdapter2(getActivity(), comments, this);
                mListView.setAdapter(mAdapter);
                mListView.scrollToPosition(savedInstanceState.getInt(KEY_POSITION, 0));
                mMultiStatView.setViewState(MultiStateView.ViewState.CONTENT);
            }
        } else {
            if (args == null || !args.containsKey(KEY_USER)) {
                throw new IllegalArgumentException("Bundle can not be null and must contain a user");
            }

            mSelectedUser = args.getParcelable(KEY_USER);
            mSort = CommentSort.getSortType(app.getPreferences().getString(KEY_SORT, null));
        }

        if (mSelectedUser == null) {
            throw new IllegalArgumentException("A Profile must be supplied to the fragment");
        }
    }

    @Override
    public void onClick(View v) {
        ImgurComment comment = mAdapter.getItem(mListView.getLayoutManager().getPosition(v));
        String url = "https://imgur.com/gallery/" + comment.getImageId();
        startActivity(ViewActivity.createIntent(getActivity(), url, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (mAdapter == null || mAdapter.isEmpty()) {
            fetchComments();
        }
    }

    private void fetchComments() {
        String url = String.format(Endpoints.ACCOUNT_COMMENTS.getUrl(), mSelectedUser.getUsername(), mSort.getSort(), mPage);

        if (mClient == null) {
            mClient = new ApiClient(url, ApiClient.HttpRequest.GET);
        } else {
            mClient.setUrl(url);
        }

        mClient.doWork(ImgurBusEvent.EventType.ACCOUNT_COMMENTS, null, null);
    }

    @Override
    public void onDestroyView() {
        app.getPreferences().edit().putString(KEY_SORT, mSort.getSort()).apply();
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_USER, mSelectedUser);
        outState.putString(KEY_SORT, mSort.getSort());

        if (mAdapter != null && !mAdapter.isEmpty()) {
            LinearLayoutManager manager = (LinearLayoutManager) mListView.getLayoutManager();
            outState.putInt(KEY_POSITION, manager.findFirstVisibleItemPosition());
            outState.putParcelableArrayList(KEY_ITEMS, mAdapter.retainItems());
            outState.putInt(KEY_PAGE, mPage);
        }
    }

    public void onEventAsync(@NonNull ImgurBusEvent event) {
        if (event.eventType == ImgurBusEvent.EventType.ACCOUNT_COMMENTS) {
            try {
                int status = event.json.getInt(ApiClient.KEY_STATUS);

                if (status == ApiClient.STATUS_OK) {
                    JSONArray data = event.json.getJSONArray(ApiClient.KEY_DATA);
                    List<ImgurComment> comments = new ArrayList<>(data.length());

                    for (int i = 0; i < data.length(); i++) {
                        comments.add(new ImgurComment(data.getJSONObject(i)));
                    }

                    if (comments.isEmpty()) {
                        mHandler.sendEmptyMessage(ImgurHandler.MESSAGE_EMPTY_RESULT);
                    } else {
                        mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_COMPLETE, comments);
                    }

                } else {
                    mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(status));
                }

            } catch (JSONException ex) {
                LogUtil.e(TAG, "Error parsing profile comments", ex);
                mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(ApiClient.STATUS_JSON_EXCEPTION));
            }
        }
    }

    public void onEventMainThread(ThrowableFailureEvent event) {
        Throwable e = event.getThrowable();

        if (e instanceof IOException) {
            mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(ApiClient.STATUS_IO_EXCEPTION));
        } else if (e instanceof JSONException) {
            mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(ApiClient.STATUS_JSON_EXCEPTION));
        } else {
            mHandler.sendMessage(ImgurHandler.MESSAGE_ACTION_FAILED, ApiClient.getErrorCodeStringResource(ApiClient.STATUS_INTERNAL_ERROR));
        }

        LogUtil.e(TAG, "Error received from Event Bus", e);

    }

    private ImgurHandler mHandler = new ImgurHandler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_ACTION_COMPLETE:
                    List<ImgurComment> comments = (List<ImgurComment>) msg.obj;

                    if (mAdapter == null) {
                        mAdapter = new ProfileCommentAdapter2(getActivity(), comments, ProfileCommentsFragment.this);
                        mListView.setAdapter(mAdapter);
                    } else {
                        mAdapter.addItems(comments);
                    }

                    mMultiStatView.setViewState(MultiStateView.ViewState.CONTENT);

                    if (mPage == 0) {
                        mMultiStatView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mListView != null) mListView.scrollToPosition(0);
                            }
                        });
                    }
                    break;

                case MESSAGE_ACTION_FAILED:
                    // Only show Error view when the user has posted no comments
                    if (mAdapter == null || mAdapter.isEmpty()) {
                        mMultiStatView.setErrorText(R.id.errorMessage, (Integer) msg.obj);
                        mMultiStatView.setViewState(MultiStateView.ViewState.ERROR);
                    }
                    break;

                case MESSAGE_EMPTY_RESULT:
                    mHasMore = false;
                    LogUtil.v(TAG, "No more comments to be fetched");

                    // Only show empty view when the user has posted no comments
                    if (mAdapter == null || mAdapter.isEmpty()) {
                        mMultiStatView.setEmptyText(R.id.emptyMessage, getString(R.string.profile_no_comments, mSelectedUser.getUsername()));
                        mMultiStatView.setViewState(MultiStateView.ViewState.EMPTY);
                    }
                    break;
            }

            mIsLoading = false;
            super.handleMessage(msg);
        }
    };
}
