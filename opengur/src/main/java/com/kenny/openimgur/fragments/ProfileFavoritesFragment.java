package com.kenny.openimgur.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.kenny.openimgur.R;
import com.kenny.openimgur.activities.FullScreenPhotoActivity;
import com.kenny.openimgur.activities.ViewActivity;
import com.kenny.openimgur.adapters.GalleryAdapter;
import com.kenny.openimgur.api.Endpoints;
import com.kenny.openimgur.api.ImgurBusEvent;
import com.kenny.openimgur.classes.ImgurAlbum;
import com.kenny.openimgur.classes.ImgurBaseObject;
import com.kenny.openimgur.classes.ImgurHandler;
import com.kenny.openimgur.classes.ImgurUser;
import com.kenny.openimgur.ui.MultiStateView;

import org.apache.commons.collections15.list.SetUniqueList;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kcampagna on 12/20/14.
 */
public class ProfileFavoritesFragment extends BaseGridFragment {
    private static final String KEY_USER = "user";

    private ImgurUser mSelectedUser;

    public static ProfileFavoritesFragment createInstance(@NonNull ImgurUser user) {
        ProfileFavoritesFragment fragment = new ProfileFavoritesFragment();
        Bundle args = new Bundle(1);
        args.putParcelable(KEY_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gallery, container, false);
    }

    @Override
    protected void saveFilterSettings() {
        // NOOP
    }

    @Override
    public ImgurBusEvent.EventType getEventType() {
        return ImgurBusEvent.EventType.ACCOUNT_GALLERY_FAVORITES;
    }

    @Override
    protected void fetchGallery() {
        boolean isSelf = mSelectedUser.isSelf(app);
        String url;

        if (isSelf) {
            url = String.format(Endpoints.ACCOUNT_FAVORITES.getUrl(), mSelectedUser.getUsername());
        } else {
            url = String.format(Endpoints.ACCOUNT_GALLERY_FAVORITES.getUrl(), mSelectedUser.getUsername(), mCurrentPage);
        }
        makeRequest(url);
    }

    @Override
    protected ImgurHandler getHandler() {
        return mHandler;
    }

    @Override
    protected void onItemSelected(int position, ArrayList<ImgurBaseObject> items) {
        // NOOP see onItemClick
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int headerSize = mGrid.getNumColumns() * mGrid.getHeaderViewCount();
        int adapterPosition = position - headerSize;
        // Don't respond to the header being clicked

        if (adapterPosition >= 0) {
            ImgurBaseObject obj = getAdapter().getItem(adapterPosition);
            Intent intent;

            if (obj instanceof ImgurAlbum || obj.getUpVotes() > Integer.MIN_VALUE) {
                ArrayList<ImgurBaseObject> items = new ArrayList<>(1);
                items.add(obj);
                intent = ViewActivity.createIntent(getActivity(), items, 0);
            } else {
                intent = FullScreenPhotoActivity.createIntent(getActivity(), obj.getLink());
            }

            startActivity(intent);
        }
    }

    private ImgurHandler mHandler = new ImgurHandler() {
        @Override
        public void handleMessage(Message msg) {
            mRefreshLayout.setRefreshing(false);
            switch (msg.what) {
                case ImgurHandler.MESSAGE_ACTION_COMPLETE:
                    List<ImgurBaseObject> items = (List<ImgurBaseObject>) msg.obj;
                    GalleryAdapter adapter = getAdapter();

                    if (adapter == null) {
                        setUpGridTop();
                        setAdapter(new GalleryAdapter(getActivity(), SetUniqueList.decorate(items)));
                    } else {
                        adapter.addItems(items);
                    }

                    // The endpoint returns all favorites for a self user, no need for loading on scroll
                    if (mSelectedUser.isSelf(app)) mHasMore = false;
                    mMultiStateView.setViewState(MultiStateView.ViewState.CONTENT);
                    break;

                case ImgurHandler.MESSAGE_ACTION_FAILED:
                    if (getAdapter() == null || getAdapter().isEmpty()) {
                        mMultiStateView.setErrorText(R.id.errorMessage, (Integer) msg.obj);
                        mMultiStateView.setViewState(MultiStateView.ViewState.ERROR);
                    }
                    break;

                case MESSAGE_EMPTY_RESULT:
                    if (getAdapter() == null || getAdapter().isEmpty()) {
                        String errorMessage = getString(R.string.profile_no_favorites, mSelectedUser.getUsername());
                        mMultiStateView.setErrorText(R.id.errorMessage, errorMessage);
                        mMultiStateView.setViewState(MultiStateView.ViewState.ERROR);
                    }
                    break;
            }

            mIsLoading = false;
            super.handleMessage(msg);
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_USER, mSelectedUser);
    }

    @Override
    protected void onRestoreSavedInstance(Bundle savedInstanceState) {
        super.onRestoreSavedInstance(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedUser = savedInstanceState.getParcelable(KEY_USER);
        } else {
            mSelectedUser = getArguments().getParcelable(KEY_USER);
        }

        if (mSelectedUser == null)
            throw new IllegalArgumentException("Profile must be supplied to fragment");
    }

    @Override
    protected int getAdditionalHeaderSpace() {
        return getResources().getDimensionPixelSize(R.dimen.tab_bar_height);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && mGrid != null && mGrid.getFirstVisiblePosition() <= 1 && mListener != null) {
            mListener.onUpdateActionBar(true);
        }
    }
}
