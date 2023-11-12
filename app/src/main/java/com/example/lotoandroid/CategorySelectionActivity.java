package com.example.lotoandroid;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;


public class CategorySelectionActivity extends AppCompatActivity {
    ViewPager viewPager;
    TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_selection);

        viewPager = findViewById(R.id.viewPager);
        tabs = findViewById(R.id.tabs);

        CategoryPagerAdapter adapter = new CategoryPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tabs.setupWithViewPager(viewPager);
    }

    private class CategoryPagerAdapter extends FragmentStatePagerAdapter {
        private final String[] categories = {"Animals.txt", "Common Phrases.txt", "Common Words.txt"};

        public CategoryPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            // Return the fragment for the corresponding category
            return CategoryFragment.newInstance(categories[position]);
        }

        @Override
        public int getCount() {
            return categories.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Set tab titles
            return categories[position];
        }
    }
}