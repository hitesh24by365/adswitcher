package com.egoclean.adswitcher.template;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import com.egoclean.adswitcher.AdHolder;

import java.lang.reflect.*;

/**
 * This is a template for <a href='http://www.admob.com/'>AdMob</a>. The constructor for this class receives
 * <code>Context</code> and <code>String</code>; the String parameter is the API Key you get from the dashboard
 * panel of AdMob. Make sure to pass it to the configuration object:
 * <pre>List&lt;AdParameter&gt; parameters = new ArrayList&lt;AdParameter&gt;();
parameters.add(new AdParameter(String.class, "the-api-key"));
AdConfiguration config = new AdConfiguration.Builder()
    .addAd(com.egoclean.adswitcher.template.AdmobAd.class, parameters)
    .build();</pre>
 Also, there's a constructor that receives a boolean which indicates whether the ad will be set in testing mode:
 <pre>
final boolean testingMode = true;
List&lt;AdParameter&gt; parameters = new ArrayList&lt;AdParameter&gt;();
parameters.add(new AdParameter(String.class, "the-api-key"));
parameters.add(new AdParameter(Boolean.class, testingMode));
AdConfiguration config = new AdConfiguration.Builder()
 .addAd(com.egoclean.adswitcher.template.AdmobAd.class, parameters)
    .build();</pre>
 */
public class AdmobAd extends AdHolder{

    private Method mLoadAd;
    private Class<?> mAdRequestClass;
    private Object mAdView;
    private final boolean mTesting;
    private Method mSetTesting;

    public AdmobAd(Context context, String id) {
        this(context, id, false);
    }

    public AdmobAd(Context context, String id, boolean testing) {
        this(context, id, (Boolean) testing);
    }

    public AdmobAd(Context context, String id, Boolean testing) {
        super(context);
        mTesting = testing;
        try {
            // 1. Get the needed classes
            Class<?> adViewClass = Class.forName("com.google.ads.AdView");
            Class<?> adListenerClass = Class.forName("com.google.ads.AdListener");
            Class<?> adSizeClass = Class.forName("com.google.ads.AdSize");
            Field banner = adSizeClass.getField("BANNER");

            mAdRequestClass = Class.forName("com.google.ads.AdRequest");

            // 2. Instantiate the Ad View object
            Constructor<?> constructor = adViewClass.getConstructor(Activity.class, adSizeClass, String.class);
            mAdView = constructor.newInstance(context, banner.get(null), id);

            // 3. Instantiate the Ad Listener interface
            Object adListener = Proxy.newProxyInstance(adListenerClass.getClassLoader(),
                    new Class[]{adListenerClass}, new AdMobListenerProxy());

            // 4. Set the Ad Listener callback to the Ad View object
            Method setAdListener = adViewClass.getMethod("setAdListener", adListenerClass);
            setAdListener.invoke(mAdView, adListener);

            // 5. Extract other needed methods and refresh
            mLoadAd = adViewClass.getMethod("loadAd", mAdRequestClass);
            mSetTesting = mAdRequestClass.getMethod("setTesting", boolean.class);
            refresh();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("I could not find a needed class (" + e.getMessage() + "); make sure you have AdMob installed correctly");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("I could not find a needed method (" + e.getMessage() + "); make sure you have AdMob installed correctly");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            somethingWentWrong(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            somethingWentWrong(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            somethingWentWrong(e);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            somethingWentWrong(e);
        }
    }

    private class AdMobListenerProxy implements InvocationHandler{

        private static final String ON_RECEIVED_AD = "onReceiveAd";
        private static final String ON_FAILED_TO_RECEIVE_AD = "onFailedToReceiveAd";
        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if (ON_FAILED_TO_RECEIVE_AD.equals(method.getName())) {
                setAvailable(false);
            } else if (ON_RECEIVED_AD.equals(method.getName())) {
                setAvailable(true);
            }
            return null;
        }
    }
    @Override
    public View getView() {
        return (View) mAdView;
    }

    @Override
    public void refresh() {
        try {
            Constructor<?> constructor = mAdRequestClass.getConstructor();
            Object adRequest = constructor.newInstance();
            mSetTesting.invoke(adRequest, mTesting);
            mLoadAd.invoke(mAdView, adRequest);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void somethingWentWrong(Exception e) {
        throw new RuntimeException("Sigh, something went wrong (" + e.getMessage() + "); make sure you have AdMob installed correctly");
    }
}