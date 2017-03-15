package io.github.malvadeza.floatingcar.trips;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import io.github.malvadeza.floatingcar.R;
import io.github.malvadeza.floatingcar.adapters.TripAdapter;

public class TripsFragment extends Fragment implements TripsContract.View {
    private static final String TAG = TripsFragment.class.getSimpleName();

    private TripsContract.Presenter tripsPresenter;

    private ProgressBar progressBar;

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
    public void onResume() {
        super.onResume();
        tripsPresenter.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trips_fragment, container, false);

        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.start_trip);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tripsPresenter.startTrip();
            }
        });

        tripAdapter = new TripAdapter(getActivity());
        ListView tripListView = (ListView) view.findViewById(R.id.trip_list);
        tripListView.setAdapter(tripAdapter);

        return view;
    }

    @Override
    public void setPresenter(@NonNull TripsContract.Presenter presenter) {
        tripsPresenter = presenter;
    }

    @Override
    public void showTrips(List<TripAdapter.TripHolder> trips) {
        Log.d(TAG, "showTrips");
        tripAdapter.clear();
        tripAdapter.addAll(trips);
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }
}
