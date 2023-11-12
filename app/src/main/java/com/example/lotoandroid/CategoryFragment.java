package com.example.lotoandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class CategoryFragment extends Fragment {
    private String category;

    public CategoryFragment() {
        // Required empty public constructor
    }

    public static CategoryFragment newInstance(String category) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString("category", category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            category = getArguments().getString("category");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the appropriate layout for the category
        int layoutResId = R.layout.fragment_animals; // Default to "Animals"
        if ("Common Phrases.txt".equals(category)) {
            layoutResId = R.layout.fragment_common_phrases;
        } else if ("Common Words.txt".equals(category)) {
            layoutResId = R.layout.fragment_common_words;
        }

        View rootView = inflater.inflate(layoutResId, container, false);
        // Initialize and set up views specific to this category
        // ...

        return rootView;
    }
}