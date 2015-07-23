package com.kenny.openimgur.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.RelativeLayout;

import com.kenny.openimgur.R;
import com.kenny.openimgur.classes.FragmentListener;
import com.kenny.openimgur.fragments.GallerySearchFilterFragment;
import com.kenny.openimgur.fragments.GallerySearchFragment;
import com.kenny.openimgur.util.ViewUtils;

import butterknife.InjectView;

/**
 * Created by kcampagna on 3/21/15.
 */
public class GallerySearchActivity extends BaseActivity implements FragmentListener {
    private static final String KEY_QUERY = "query";

    private GallerySearchFragment mFragment;

    @InjectView(R.id.toolBar)
    Toolbar mToolBar;

    public static Intent createIntent(Context context, String query) {
        return new Intent(context, GallerySearchActivity.class).putExtra(KEY_QUERY, query);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_search);
        setStatusBarColorResource(theme.darkColor);
        mFragment = (GallerySearchFragment) getFragmentManager().findFragmentById(R.id.searchFragment);
        String query;

        if (savedInstanceState != null) {
            query = savedInstanceState.getString(KEY_QUERY, null);
        } else {
            query = getIntent().getStringExtra(KEY_QUERY);
        }

        setupToolBar(query);
        mFragment.setQuery(query);
    }

    /**
     * Sets up the tool bar to take the place of the action bar
     */
    private void setupToolBar(String query) {
        mToolBar.setTitle(query);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onUpdateActionBarTitle(String title) {
        if (mToolBar != null) mToolBar.setTitle(title);
    }

    @Override
    public void onUpdateActionBar(boolean shouldShow) {
        setActionBarVisibility(mToolBar, shouldShow);
    }

    @Override
    public void onLoadingStarted() {
        // NOOP
    }

    @Override
    public void onLoadingComplete() {
        // NOOP
    }

    @Override
    public void onError(int errorCode) {
        // NOOP
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().findFragmentByTag("filter") != null) {
            FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentByTag("filter");

            if (fragment instanceof GallerySearchFilterFragment) {
                ((GallerySearchFilterFragment) fragment).dismiss(null, null);
            } else {
                fm.beginTransaction().remove(fragment).commit();
            }

            return;
        }

        super.onBackPressed();
    }

    @Override
    protected int getStyleRes() {
        return theme.isDarkTheme ? R.style.Theme_Translucent_Main_Dark : R.style.Theme_Translucent_Main_Light;
    }
}
