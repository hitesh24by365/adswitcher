package in.adswitcher.android.template;

import android.content.Context;
import android.view.View;
import in.adswitcher.android.AdHolder;

import java.lang.reflect.*;
import java.util.Arrays;

public class MobclixAd extends AdHolder {

    private Method mGetAdMethod;
    private Object mMobclixAd;

    public MobclixAd(Context context) {
        super(context);
        try {
            // 1. Get the needed classes
            Class<?> mobclixClass = Class.forName("com.mobclix.android.sdk.MobclixMMABannerXLAdView");
            Class<?> mobclixAdViewListenerClass = Class.forName("com.mobclix.android.sdk.MobclixAdViewListener");

            // 2. Implement the MobclixAdViewListener class
            Object mobclixAdViewListener = Proxy.newProxyInstance(mobclixAdViewListenerClass.getClassLoader(),
                    new Class[]{mobclixAdViewListenerClass}, new ProxyAdViewListener());

            // 3. Create an instance of the mobclix ad view
            Constructor<?> mobclixConstructor = mobclixClass.getConstructor(Context.class);
            mMobclixAd = mobclixConstructor.newInstance(context);

            // 4. Add the MobclixAdViewListener callback
            Method addMobclixAdViewListener = mobclixClass.getMethod("addMobclixAdViewListener", mobclixAdViewListenerClass);
            addMobclixAdViewListener.invoke(mMobclixAd, mobclixAdViewListener);

            // 5. Extract extra needed methods
            mGetAdMethod = mobclixClass.getMethod("getAd");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("I was not able to load the Mobclix Ad class... are you sure you set up Mobclix correctly");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("I was not able to load the default Mobclix Ad constructor... are you sure you set up Mobclix correctly");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating Mobclix Ad... are you sure you set up Mobclix correctly");
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating Mobclix Ad... are you sure you set up Mobclix correctly");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating Mobclix Ad... are you sure you set up Mobclix correctly");
        }
    }

    private class ProxyAdViewListener implements InvocationHandler {
        private final String[] METHODS_TO_EXECUTE = new String[]{"onSuccessfulLoad", "onFailedLoad"};

        @Override
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            try {
                Class<? extends ProxyAdViewListener> thisClass = getClass();
                Method[] methods = thisClass.getMethods();
                for (Method method : methods) {
                    // if it's a method to by pass, invoke it without parameters
                    if (method.getName().equals(m.getName()) && Arrays.asList(METHODS_TO_EXECUTE).contains(m.getName())) {
                        method.invoke(this);
                        return null;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("unexpected invocation exception: " + e.getMessage() + "; processing: " + m.getName());
            }
            return m.invoke(new Object(), args);
        }

        public void onSuccessfulLoad() {
            setAvailable(true);
        }

        public void onFailedLoad() {
            setAvailable(false);
        }
    }

    @Override
    public View getView() {
        return (View) mMobclixAd;
    }

    @Override
    public void refresh() {
        try {
            mGetAdMethod.invoke(mMobclixAd);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
