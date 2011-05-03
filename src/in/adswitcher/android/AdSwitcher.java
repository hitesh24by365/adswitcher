package in.adswitcher.android;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import android.widget.ViewFlipper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a custom view that will contain and display the ads according to the specified configuration. This view
 * will refresh automatically your views and hide itself if there's no ad available... similarly, it will be visible
 * if one of your ads become available.
 */
public class AdSwitcher extends ViewFlipper implements OnAdAvailabilityChanged {
    private final static int REFRESH_AD = 123;
    private final static int ROTATE_AD = 321;
    private static final String AD_SWITCHER_ID = "adSwitcherId";
    private static final String TAG = "AD_SWITCHER";

    private final String mAdSwitcherId;
    private List<AdHolder> mAdHolders;
    private AdConfiguration mConfiguration;
    private boolean mStarted;
    private boolean mRunning;
    private boolean mVisible;
    private Handler mHandler;

    /**
     * @param context      the context used to inflate the view
     * @param adSwitcherId the configuration ID
     */
    public AdSwitcher(Context context, String adSwitcherId) {
        super(context);
        mAdSwitcherId = adSwitcherId;
        init();
    }

    public AdSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.GONE);// hidden by default
        mAdSwitcherId = attrs.getAttributeValue(null, AD_SWITCHER_ID);
        if (mAdSwitcherId == null) {
            throw new RuntimeException("AdSwitcher view must contain an 'adSwitcherId' attribute");
        }
        init();
    }

    private void init() {
        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        mConfiguration = AdSettings.getConfiguration(mAdSwitcherId);
        mAdHolders = buildAds();
        for (AdHolder adHolder : mAdHolders) {
            ViewParent parent = adHolder.getView().getParent();
            if (parent != null) {
                ((AdSwitcher) parent).removeView(adHolder.getView());
            }
            addView(adHolder.getView(), params);
        }
        mHandler = new Handler() {
            public void handleMessage(Message m) {
                if (mRunning) {
                    switch (m.what) {
                        case REFRESH_AD:
                            Log.i(getLogTag(), "Refresh AdHolder message received.");
                            for (AdHolder adHolder : mAdHolders) {
                                adHolder.refresh();
                            }
                            showAds();
                            if (mConfiguration.getRefreshInterval() != -1) {
                                sendMessageDelayed(Message.obtain(this, REFRESH_AD), mConfiguration.getRefreshInterval());
                            }
                            break;
                        case ROTATE_AD:
                            Log.i(getLogTag(), "Rotate AdHolder message received.");
                            rotateAd();
                            if (mConfiguration.getRotateInterval() != -1) {
                                sendMessageDelayed(Message.obtain(this, ROTATE_AD), mConfiguration.getRotateInterval());
                            }
                            break;
                    }
                }
            }

            @Override
            public String toString() {
                return AdSwitcher.this.toString() + ":" + getContext();
            }
        };
        showAds();
    }

    private List<AdHolder> buildAds() {
        List<AdHolder> adHolders = new ArrayList<AdHolder>();
        List<Class<? extends AdHolder>> adsClasses = mConfiguration.getAdHolders();
        for (Class<? extends AdHolder> adClass : adsClasses) {
            try {
                AdHolder adHolder;
                List<AdParameter> params = mConfiguration.getParams(adClass);
                if (params == null) {
                    // constructor is simple
                    Constructor<? extends AdHolder> constructor = adClass.getConstructor(Context.class);
                    adHolder = constructor.newInstance(getContext());
                } else {
                    List<Class> clazzes = new ArrayList<Class>();
                    clazzes.add(Context.class);
                    for (AdParameter param : params) {
                        clazzes.add(param.getKey());
                    }
                    Constructor<? extends AdHolder> constructor = adClass.getConstructor(clazzes.toArray(new Class[clazzes.size()]));
                    // set values
                    List<Object> values = new ArrayList<Object>();
                    values.add(getContext());
                    for (AdParameter param : params) {
                        values.add(param.getValue());
                    }
                    adHolder = constructor.newInstance(values.toArray(new Object[values.size()]));
                }
                adHolder.setOnAdAvailabilityChanged(this);
                adHolders.add(adHolder);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return adHolders;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mStarted = true;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mStarted = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = visibility == VISIBLE;
        updateRunning();
    }

    private void showAds() {
        boolean showAds = false;
        for (AdHolder adHolder : mAdHolders) {
            if (adHolder.isAvailable()) {
                setCurrentAd(adHolder);
                showAds = true;
                break;
            }
        }
        if (!showAds) {
            Log.i(getLogTag(), "Hiding ads");
            setVisibility(View.GONE);
        }
    }

    private void rotateAd() {
        if (getVisibility() != VISIBLE) {
            return;
        }
        int displayedChild = getDisplayedChild();
        int size = mAdHolders.size();
        for (int i = 0, j = displayedChild + 1; i < size - 1; i++, j++) {
            AdHolder adHolder = mAdHolders.get(j % size);
            if (adHolder.isAvailable()) {
                setCurrentAd(adHolder);
            }
        }
    }

    private void setCurrentAd(AdHolder adHolder) {
        Log.i(getLogTag(), "Showing adHolder " + adHolder);
        setDisplayedChild(mAdHolders.indexOf(adHolder));
        if (getContext() instanceof Activity) {
            Activity activity = (Activity) getContext();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setVisibility(VISIBLE);
                }
            });
        } else {
            setVisibility(VISIBLE);
        }
    }

    private String getLogTag() {
        return TAG + ":" + mAdSwitcherId;
    }

    public void onAdAvailabilityChanged(AdHolder adHolder) {
        if (!adHolder.isAvailable() || getVisibility() == VISIBLE) {
            return;
        }
        boolean atLeastOneAvailable = false;
        for (AdHolder anAdHolder : mAdHolders) {
            if (anAdHolder != adHolder && anAdHolder.isAvailable()) {
                atLeastOneAvailable = true;
            }
        }
        if (!atLeastOneAvailable) {
            setCurrentAd(adHolder);
        }
    }

    private void updateRunning() {
        boolean running = mVisible && mStarted;
        if (running != mRunning) {
            if (running) {
                if (mConfiguration.getRefreshInterval() != -1) {
                    mHandler.sendMessageDelayed(Message.obtain(mHandler, REFRESH_AD), mConfiguration.getRefreshInterval());
                }
                if (mConfiguration.getRotateInterval() != -1) {
                    mHandler.sendMessageDelayed(Message.obtain(mHandler, ROTATE_AD), mConfiguration.getRotateInterval());
                }
            } else {
                mHandler.removeMessages(REFRESH_AD);
                mHandler.removeMessages(ROTATE_AD);
            }
            mRunning = running;
        }
    }
}