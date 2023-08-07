package edu.northeastern.rhythmlounge;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import edu.northeastern.rhythmlounge.Post.PostActivity;

public class HomeActivity extends AppCompatActivity {

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewPager = findViewById(R.id.fragmentContainer);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_create_post);
        fab.setOnClickListener(v -> openPostCreationModal());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int currentItem = viewPager.getCurrentItem();
            if (item.getItemId() == R.id.home) {
                if (currentItem == 5 || currentItem == 6) {
                    viewPager.setCurrentItem(0, false);
                } else {
                    viewPager.setCurrentItem(0);
                }
                return true;
            } else if (item.getItemId() == R.id.search) {
                viewPager.setCurrentItem(1);
                return true;
            }  else if (item.getItemId() == R.id.create_event) {
                viewPager.setCurrentItem(2);
                return true;
            } else if (item.getItemId() == R.id.events) {
                viewPager.setCurrentItem(3);
                return true;
            } else if (item.getItemId() == R.id.profile) {
                viewPager.setCurrentItem(4);
                return true;
            }
            return false;
        });


        TextView textViewRhythmLounge = findViewById(R.id.textViewRhythmLounge);
        String text = "hythm Lounge ";

        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable logoDrawable = getResources().getDrawable(R.drawable.logo);

        int logoSizePixels = (int) textViewRhythmLounge.getTextSize();
        logoDrawable.setBounds(0, 0, logoSizePixels, logoSizePixels);
        SpannableString spannableString = new SpannableString("  " + text);
        ImageSpan imageSpan = new ImageSpan(logoDrawable, ImageSpan.ALIGN_BASELINE);
        spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        textViewRhythmLounge.setText(spannableString);

        ImageView imageViewSettings = findViewById(R.id.imageViewSettings);
        imageViewSettings.setOnClickListener(v -> openActivity(SettingsActivity.class));

        ImageView imageViewNotification = findViewById(R.id.imageViewNotification);
        imageViewNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new NotificationsFragment());
            }
        });
    }


    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }


    private static class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final int NUM_PAGES = 5;

        public ViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new UserSearchFragment();
                case 2:
                    return new CreateEventFragment();
                case 3:
                    return new EventsFragment();
                case 4:
                    return new SelfUserPageFragment();
                default:
                    throw new IllegalArgumentException("Invalid position: " + position);
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    private void openPostCreationModal() {
        Intent intent = new Intent(this, PostActivity.class);
        startActivity(intent);
    }
}