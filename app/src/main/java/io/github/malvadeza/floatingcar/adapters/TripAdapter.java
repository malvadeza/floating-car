package io.github.malvadeza.floatingcar.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.malvadeza.floatingcar.R;

public class TripAdapter extends ArrayAdapter<TripAdapter.TripHolder> {
    public TripAdapter(Context context) {
        super(context, R.layout.trip_list_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.trip_list_item, parent, false);
        }

        TripHolder trip = getItem(position);

        if (trip != null) {
            TextView startDayTime = (TextView) view.findViewById(R.id.start_day_time);
            startDayTime.setText(getContext().getString(R.string.start_day_time, trip.getDay(), trip.getHour()));

            TextView startDate = (TextView) view.findViewById(R.id.start_date);
            startDate.setText(getContext().getString(R.string.start_date, trip.getMonth(), trip.getDate()));

            TextView duration = (TextView) view.findViewById(R.id.trip_duration);

            TextView samples = (TextView) view.findViewById(R.id.trip_samples);
            samples.setText(getContext().getString(R.string.trip_samples, trip.mSamples));

            TextView distance = (TextView) view.findViewById(R.id.trip_distance);
        }

        return view;
    }

    public static class TripHolder {
        private static final SimpleDateFormat sHour = new SimpleDateFormat("HH:mm", Locale.getDefault());
        private static final SimpleDateFormat sMonth = new SimpleDateFormat("MMMM", Locale.getDefault());
        private static final SimpleDateFormat sDay = new SimpleDateFormat("EEEE", Locale.getDefault());
        private static final SimpleDateFormat sDate = new SimpleDateFormat("dd", Locale.getDefault());
        private static final SimpleDateFormat fromDb = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.getDefault());

        private final long mId;
        private final Date mStartedAt;
        private final Date mFinishedAt;
        private final int mSamples;


        public TripHolder(long id, String startedAt, String finishedAt, int samples) {
            mId = id;
            mStartedAt = parseDate(startedAt);
            mFinishedAt = parseDate(finishedAt);
            mSamples = samples;
        }

        private Date parseDate(String dateStr) {
            try {
                return fromDb.parse(dateStr);
            } catch (ParseException | NullPointerException e) {
                e.printStackTrace();

                return null;
            }
        }

        private String getMonth() {
            return sMonth.format(mStartedAt);
        }

        private String getHour() {
            return sHour.format(mStartedAt);
        }

        private String getDay() {
            return sDay.format(mStartedAt);
        }

        private String getDate() {
            return sDate.format(mStartedAt);
        }
    }


}
