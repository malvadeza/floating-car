package io.github.malvadeza.floatingcar.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripAdapter extends ArrayAdapter<TripAdapter.TripHolder> {
    public TripAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    public static class TripHolder {
        private final long mId;
        private final String mStartedAt;
        private final String mFinishedAt;
        private final int mSamples;

        public TripHolder(long id, String startedAt, String finishedAt, int samples) {
            mId = id;
            mStartedAt = parseDate(startedAt);
            mFinishedAt = parseDate(finishedAt);
            mSamples = samples;
        }

        private String parseDate(String dateStr) {
            SimpleDateFormat fromDb = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());
            SimpleDateFormat readable = new SimpleDateFormat("EEEE, d MMM HH:mm", Locale.getDefault());

            try {
                Date dateObject = fromDb.parse(dateStr);

                return readable.format(dateObject);
            } catch (ParseException | NullPointerException e) {
                e.printStackTrace();

                return "";
            }
        }

        @Override
        public String toString() {
            return "Started at: " + mStartedAt + "\nSamples: " + mSamples;
        }
    }


}
