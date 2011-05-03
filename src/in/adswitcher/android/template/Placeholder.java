package in.adswitcher.android.template;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import in.adswitcher.android.AdHolder;

import java.util.Random;

/**
 * Test Ad View. This is just a placeholder ad which can be used to test the ad switcher.
 */
public class Placeholder extends AdHolder {
    private final TextView view;
    private final boolean showRandomly;

    public Placeholder(Context context) {
        this(context, "AdHolder placeholder");
    }

    /**
     * @param label the text to show in the ad
     */
    public Placeholder(Context context, String label) {
        this(context, label, getDefaultClickListener());
    }

    /**
     * @param showRandomly if true, the ad will report to be available or unavailable randomly. if false, the ad
     * will always be available
     */
    public Placeholder(Context context, Boolean showRandomly) {
        this(context, "AdHolder placeholder", getDefaultClickListener(), showRandomly);
    }

    /**
     * @param showRandomly if true, the ad will report to be available or unavailable randomly. if false, the ad
     * will always be available
     */
    public Placeholder(Context context, boolean showRandomly) {
        this(context, "AdHolder placeholder", getDefaultClickListener(), showRandomly);
    }

    /**
     * @param label the text to show in the ad
     * @param clickListener the click listener to set to the ad
     */
    public Placeholder(Context context, String label, View.OnClickListener clickListener) {
        this(context, label, clickListener, false);
    }

    /**
     * @param label the text to show in the ad
     * @param clickListener the click listener to set to the ad
     * @param showRandomly if true, the ad will report to be available or unavailable randomly. if false, the ad
     * will always be available
     */
    public Placeholder(Context context, String label, View.OnClickListener clickListener, Boolean showRandomly) {
        super(context);
        this.showRandomly = showRandomly;
        view = new TextView(context);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        defaultDisplay.getMetrics(metrics);
        view.setHeight((int) (metrics.density * 50));// 50dip height
        view.setBackgroundColor(Color.RED);
        view.setTextColor(Color.WHITE);
        view.setText(label);
        view.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setTextSize(20);
        if (clickListener == null) {
            view.setOnClickListener(getDefaultClickListener());
        } else {
            view.setOnClickListener(clickListener);
        }
        refresh();
    }

    private static View.OnClickListener getDefaultClickListener() {
        return new View.OnClickListener() {
            public void onClick(View view) {
                view.setBackgroundColor(new Random().nextInt());
            }
        };
    }

    @Override
    public View getView() {
        return view;
    }

    @Override
    public void refresh() {
        if (!showRandomly) {
            setAvailable(true);
        } else {
            setAvailable(new Random().nextBoolean());
        }
    }
}