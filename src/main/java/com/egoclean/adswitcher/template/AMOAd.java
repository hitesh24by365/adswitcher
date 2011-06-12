package com.egoclean.adswitcher.template;

import android.content.Context;
import android.view.View;
import com.egoclean.adswitcher.AdHolder;

import java.lang.reflect.*;

/**
 * Temporary template for Addictive Mobility ads, which suck by the way.
 */
public class AMOAd extends AdHolder {
    private Object mAmoView;
    private Method mShowAdd;

    public AMOAd(Context context) {
        super(context);
        try {
            // 1. Get the necessary classes and methods
            Class<?> amoAdBannerClass = Class.forName("com.atc.adnetwork.AMOAdBanner");
            mShowAdd = amoAdBannerClass.getMethod("showAdd");

            // 2. Instantiate the banner
            Constructor<?> constructor = amoAdBannerClass.getConstructor(Context.class);
            mAmoView = constructor.newInstance(context);

            // 3. set a proxy to handle banner visibility
            Proxy.newProxyInstance(amoAdBannerClass.getClassLoader(), new Class[]{amoAdBannerClass}, new AvailabilityListener());

            // 4. try to the the add
            refresh();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView() {
        return (View) mAmoView;
    }

    @Override
    public void refresh() {
        try {
            mShowAdd.invoke(mAmoView);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private class AvailabilityListener implements InvocationHandler {
        private static final String WILL_SHOW = "willShowAd";
        private static final String WONT_SHOW = "willNotShowAd";

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if (WILL_SHOW.equals(method.getName())) {
                setAvailable(true);
            } else if (WONT_SHOW.equals(method.getName())) {
                setAvailable(false);
            }
            return null;
        }
    }
}
/**
 * <!-- Addictive Mobilility -->
 <meta-data android:name="AMOA.APP_KEY" android:value="3f7e5e49e045d4a267356c7f25ca54cb" />
 <activity android:name="com.atc.adnetwork.microApp" android:screenOrientation="portrait"/>
 */