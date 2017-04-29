package ie.fleetmatics.moviematics.fragment;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import ie.fleetmatics.moviematics.R;
import ie.fleetmatics.moviematics.adapter.MovieAdapter;
import ie.fleetmatics.moviematics.model.Movie;
import ie.fleetmatics.moviematics.util.MessageUtils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Movie Fragment responsible of calling the API, fetch and showing the content
 */
public class MovieFragment extends BaseFragment {

    private static final String ARG_SORT_BY = "sortBy";
    private static final String ARG_PAGE_NUMBER = "pageNumber";

    private String mSortBy;
    private int mPageNumber;

    private CompositeSubscription mSubscription = new CompositeSubscription();

    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;

    private LinearLayoutManager linearLayoutManager;

    public MovieFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sortBy Parameter 1.
     * @param pageNumber Parameter 2.
     * @return A new instance of fragment MovieFragment.
     */
    public static MovieFragment newInstance(String sortBy, int pageNumber) {
        MovieFragment fragment = new MovieFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SORT_BY, sortBy);
        args.putInt(ARG_PAGE_NUMBER, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSortBy = getArguments().getString(ARG_SORT_BY);
            mPageNumber = getArguments().getInt(ARG_PAGE_NUMBER);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);

        showProgressDialog();

        addObserver(mSubscription);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSubscription.unsubscribe();
    }

    /**
     * Method that sets an Observer object to listen on the list of movies
     * @param subscription
     */
    private void addObserver(CompositeSubscription subscription) {
        subscription.add(apiContext.getMovies(mSortBy, mPageNumber)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Movie>>() {
                    @Override
                    public void onCompleted() {
                        //MessageUtils.showMessage(getActivity(), R.string.call_successful, Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgressDialog();
                        MessageUtils.showMessage(getActivity(), R.string.call_error, Toast.LENGTH_LONG);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<Movie> movies) {
                        mAdapter = new MovieAdapter(movies, getActivity());
                        mRecyclerView.setAdapter(mAdapter);
                        hideProgressDialog();
                    }
                }));
    }

}