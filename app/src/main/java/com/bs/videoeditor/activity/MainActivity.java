package com.bs.videoeditor.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bs.videoeditor.R;
import com.bs.videoeditor.fragment.DetailsSelectFileFragment;
import com.bs.videoeditor.fragment.ListVideoFragment;
import com.bs.videoeditor.fragment.StudioFragment;
import com.bs.videoeditor.statistic.Statistic;
import com.bsoft.core.AdmobBannerHelper;
import com.bsoft.core.AdmobFullHelper;
import com.bsoft.core.AdmobNativeHelper;
import com.bsoft.core.AppRate;
import com.bsoft.core.BUtils;
import com.bsoft.core.CrsDialogFragment;
import com.bsoft.core.DialogExitApp;
import com.google.android.gms.ads.AdSize;

import java.util.List;


public class MainActivity extends AbsActivity {
    public static final int INDEX_CUTTER = 0;
    public static final int INDEX_SPEED = 1;
    public static final int INDEX_MERGER = 2;
    public static final int INDEX_ADD_MUSIC = 3;
    public static final int INDEX_STUDIO = 4;

    private DialogExitApp dialogExitApp;
    private AdmobFullHelper admobFullHelper;
    private ImageView ivBg;
    private FrameLayout viewAds;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                return;
            }

            switch (intent.getAction()) {
                case Statistic.OPEN_CUTTER_STUDIO:
                    addFragmentStudio(INDEX_CUTTER);
                    break;

                case Statistic.OPEN_MERGER_STUDIO:
                    addFragmentStudio(INDEX_MERGER);
                    break;

                case Statistic.OPEN_SPEED_STUDIO:
                    addFragmentStudio(INDEX_SPEED);
                    break;

                case Statistic.OPEN_ADD_MUSIC_STUDIO:
                    addFragmentStudio(INDEX_ADD_MUSIC);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CrsDialogFragment.loadData(this);
        admobFullHelper = new
                AdmobFullHelper(this).setAdUnitId(getString(R.string.admod_full_id)).setShowAfterLoaded(false);
        admobFullHelper.load();

        BUtils.buildAppRate(this, this::finish);

        if (!AppRate.isShowRateDialog()) {
            dialogExitApp = new DialogExitApp(this, getString(R.string.admod_native_id), true, () -> finish());
        }

        initActions();
        initView();
        loadAdsBanner();
        loadAdsNative();
    }

    private void initActions() {
        IntentFilter it = new IntentFilter();
        it.addAction(Statistic.OPEN_CUTTER_STUDIO);
        it.addAction(Statistic.OPEN_MERGER_STUDIO);
        it.addAction(Statistic.OPEN_SPEED_STUDIO);
        it.addAction(Statistic.OPEN_ADD_MUSIC_STUDIO);
        registerReceiver(receiver, it);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void loadAdsBanner() {
        FrameLayout flAdBanner = findViewById(R.id.fl_ad_banner);
        AdmobBannerHelper admobBannerHelper = new AdmobBannerHelper(this, flAdBanner)
                .setAdSize(AdSize.BANNER)
                .setAdUnitId(getString(R.string.admod_banner_id));
        admobBannerHelper.loadAd();
    }

    public void setBackGroundAds(int id) {
        viewAds.setBackgroundColor(id);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            if (AppRate.showRateDialogIfMeetsConditions(this)) {

            } else {
                dialogExitApp.show();
            }
        }
    }

    private void loadAdsNative() {
        AdmobNativeHelper admobNativeHelper = new AdmobNativeHelper.Builder(this)
                .setParentView((FrameLayout) findViewById(R.id.fl_ad_native))
                .setLayoutAdNative(R.layout.layout_ad_native)
                .setAdNativeId(getString(R.string.admod_native_id))
                .build();

        admobNativeHelper.loadAd();

        admobNativeHelper.setNativeAdListener(new AdmobNativeHelper.OnNativeAdListener() {
            @Override
            public void onNativeAdLoaded() {
                ivBg.setVisibility(View.GONE);
            }

            @Override
            public void onAdFailedToLoad(int i) {
                ivBg.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int setView() {
        return R.layout.activity_main;
    }

    private void initView() {
        ivBg = findViewById(R.id.iv_bg);
        viewAds = findViewById(R.id.fl_ad_banner);

        findViewById(R.id.iv_cutter).setOnClickListener(view -> addFragmentListVideo(INDEX_CUTTER));
        findViewById(R.id.iv_speed).setOnClickListener(view -> addFragmentListVideo(INDEX_SPEED));
        findViewById(R.id.iv_merger).setOnClickListener(view -> addFragmentMerger());
        findViewById(R.id.iv_add_music).setOnClickListener(view -> addFragmentListVideo(INDEX_ADD_MUSIC));
        findViewById(R.id.iv_studio).setOnClickListener(view -> addFragmentStudio(0));
        findViewById(R.id.iv_more_app).setOnClickListener(view -> moreApp());
    }


    private void addFragmentStudio(int indexFragmentOpen) {
        Bundle bundle = new Bundle();
        bundle.putInt(Statistic.CHECK_OPEN_STUDIO, Statistic.FROM_MAIN);
        bundle.putInt(Statistic.OPEN_FRAGMENT, indexFragmentOpen);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left
                        , R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left)
                .add(R.id.view_container, StudioFragment.newInstance(bundle))
                .addToBackStack(null)
                .commit();
    }

    private void moreApp() {
        BUtils.showMoreAppDialog(getSupportFragmentManager());
    }

    private void addFragmentListVideo(int fragment) {
        Bundle bundle = new Bundle();
        bundle.putInt(Statistic.ACTION, fragment);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left
                        , R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left)
                .add(R.id.view_container, ListVideoFragment.newInstance(bundle))
                .addToBackStack(null)
                .commit();
    }

    private void addFragmentMerger() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left
                        , R.anim.animation_left_to_right
                        , R.anim.animation_right_to_left)
                .add(R.id.view_container, DetailsSelectFileFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }
}