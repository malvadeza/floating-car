package io.github.malvadeza.floatingcar.trips;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.github.malvadeza.floatingcar.BaseView;
import io.github.malvadeza.floatingcar.R;
import io.github.malvadeza.floatingcar.data.Trip;
import io.github.malvadeza.floatingcar.tripdetail.TripDetailsActivity;
import io.github.malvadeza.floatingcar.triplogging.TripLoggingActivity;

public class TripsFragment extends Fragment implements TripsView {
    private static final String TAG = TripsFragment.class.getSimpleName();

    private TripsPresenter presenter;

    private ProgressBar progressBar;

    private SwipeRefreshLayout swipeRefreshLayout;

    private TripAdapter tripAdapter;


    public TripsFragment() {
    }

    public static TripsFragment newInstance() {
        TripsFragment fragment = new TripsFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.trips_fragment, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.refreshTrips();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.start_trip);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.startTrip();
            }
        });

        tripAdapter = new TripAdapter(getActivity());
        ListView tripListView = (ListView) view.findViewById(R.id.trip_list);
        tripListView.setAdapter(tripAdapter);
        tripListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                presenter.openTripDetails(tripAdapter.getItem(position));
            }
        });

        return view;
    }

    @Override
    public void setPresenter(TripsPresenter presenter) {
        this.presenter = presenter;
    }

    public void clearTrips() {
        tripAdapter.clear();
    }

    public void showTrips(List<Trip> trips) {
        tripAdapter.clear();
        tripAdapter.addAll(trips);
    }

    @Override
    public void showLoadingStatus() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingStatus() {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void openTripDetailsActivity(long tripId) {
        Intent intent = new Intent(getContext(), TripDetailsActivity.class);
        intent.putExtra(TripDetailsActivity.TRIP_ID, tripId);
        startActivity(intent);
    }

    @Override
    public void startTripLoggingActivity() {
        Intent intent = new Intent(getContext(), TripLoggingActivity.class);
        startActivity(intent);
    }


    private static class TripAdapter extends ArrayAdapter<Trip> {
        private static final String TAG = TripAdapter.class.getSimpleName();

        private static final DateFormat startDayTimeFmt = new SimpleDateFormat("EEEE, HH:mm", Locale.getDefault());
        private static final DateFormat startDateFmt = new SimpleDateFormat("MMMM dd", Locale.getDefault());

        TripAdapter(Context context) {
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

}
