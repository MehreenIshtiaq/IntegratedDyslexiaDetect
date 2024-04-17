package com.example.integrated;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActivityFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<ActivityItem> activityList; // data model

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        recyclerView = view.findViewById(R.id.activitiesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        activityList = new ArrayList<>();
        initializeActivityData();


        adapter = new ActivityAdapter(activityList, new ActivityAdapter.OnItemClickListener() {
            Intent intent;
            @Override
            public void onItemClick(ActivityItem item) {
                switch (item.getName()) {
                    case "RHYMING ACTIVITY":
                        intent = new Intent(getActivity(), Rhyme.class);
                        break;
                    case "WORD HARMONY ACTIVITY":
                        intent = new Intent(getActivity(), WordHarmony.class);
                        break;
                    case "AUDIO RHYME ACTIVITY":
                        intent = new Intent(getActivity(), AudioRhyme.class);
                        break;
                    // Add more cases as needed
                    case "READING ACTIVITY":
                        intent = new Intent(getActivity(), ReadingActivity.class);
                        break;
                    default:
                        intent = new Intent(getActivity(), AudioRhyme.class); // A default activity
                        break;
                }
                if (intent != null) {
                    startActivity(intent);
                } else {
                    Log.e("ActivityFragment", "Intent is null. Cannot start activity.");
                }
            }
        });
        recyclerView.setAdapter(adapter);

        // Find the search view and set up the search functionality
        SearchView searchView = view.findViewById(R.id.searchView);
        setupSearch(searchView); // Now calling setupSearch method to initialize search functionality
        return view;
    }

    private void initializeActivityData() {
        // Your method to add data to the activityList
        activityList.add(new ActivityItem("RHYMING ACTIVITY", R.drawable.rhyming));
        activityList.add(new ActivityItem("WORD HARMONY ACTIVITY", R.drawable.word_harmony));
        activityList.add(new ActivityItem("AUDIO RHYME ACTIVITY", R.drawable.audio_rhyme));
        activityList.add(new ActivityItem("MISSING LETTERS ACTIVITY", R.drawable.missing_letters));
        activityList.add(new ActivityItem("READING ACTIVITY", R.drawable.reading));
        // Add more items as you did
    }


    private void setupSearch(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true; // Indicates the query text has been handled
            }
        });
    }
}
