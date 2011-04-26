package in.adswitcher.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class Placeholder extends Ad {
    private final TextView view;
    private final boolean showRandomly;

    public Placeholder(Context context) {
        this(context, "Ad placeholder");
    }

    public Placeholder(Context context, String label) {
        this(context, label, getDefaultClickListener());
    }

    private static View.OnClickListener getDefaultClickListener() {
        return new View.OnClickListener() {
            public void onClick(View view) {
                view.setBackgroundColor(new Random().nextInt());
            }
        };
    }

    public Placeholder(Context context, String label, View.OnClickListener clickListener) {
        this(context, label, clickListener, false);
    }

    public Placeholder(Context context, String label, View.OnClickListener clickListener, Boolean showRandomly) {
        this.showRandomly = showRandomly;
        view = new TextView(context);
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