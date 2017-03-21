package io.github.malvadeza.floatingcar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.malvadeza.floatingcar.R;
import io.github.malvadeza.floatingcar.data.Trip;

public class TripAdapter extends ArrayAdapter<Trip> {
    private static final String TAG = TripAdapter.class.getSimpleName();

    private static final DateFormat startDayTimeFmt = new SimpleDateFormat("EEEE, HH:mm", Locale.getDefault());
    private static final DateFormat startDateFmt = new SimpleDateFormat("MMMM dd", Locale.getDefault());

    public TripAdapter(Context context) {
        super(context, R.layout.trip_list_item);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.trip_list_item, parent, false);
        }

        Trip trip = getItem(position);

        if (trip != null) {
            TextView startDayTime = (TextView) view.findViewById(R.id.start_day_time);
            startDayTime.setText(startDayTimeFmt.format(trip.getStartedAt()));

            TextView startDate = (TextView) view.findViewById(R.id.start_date);
            startDate.setText(startDateFmt.format(trip.getStartedAt()));

            TextView duration = (TextView) view.findViewById(R.id.trip_duration);
            duration.setText(getContext().getString(R.string.trip_duration, trip.getDurationInMinutes()));

            TextView samples = (TextView) view.findViewById(R.id.trip_samples);
            samples.setText(getContext().getString(R.string.trip_samples, trip.getSamples().size()));

            TextView distance = (TextView) view.findViewById(R.id.trip_distance);
        }

        return view;
    }

}
