package com.example.integrated;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    TextView textView;
    ImageView imageView;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        textView = view.findViewById(R.id.tvStartScreening);
        imageView = view.findViewById(R.id.imgArrow);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Replace the navigation with appropriate fragment transaction or activity start
                Intent intent = new Intent(getActivity(), InitialScreening.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
