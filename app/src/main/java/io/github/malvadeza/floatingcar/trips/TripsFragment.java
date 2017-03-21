package io.github.malvadeza.floatingcar.trips;

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
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import io.github.malvadeza.floatingcar.BaseView;
import io.github.malvadeza.floatingcar.R;
import io.github.malvadeza.floatingcar.adapters.TripAdapter;
import io.github.malvadeza.floatingcar.data.Trip;
import io.github.malvadeza.floatingcar.tripdetail.TripDetailsActivity;

public class TripsFragment extends Fragment implements BaseView<TripsPresenter> {
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
    public void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();
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
        swipeRefreshLayout.setRefreshing(false);
        tripAdapter.clear();
        tripAdapter.addAll(trips);
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    public void showTripDetailsActivity(long tripId) {
        Intent intent = new Intent(getContext(), TripDetailsActivity.class);
        intent.putExtra(TripDetailsActivity.TRIP_ID, tripId);
        startActivity(intent);
    }

}
