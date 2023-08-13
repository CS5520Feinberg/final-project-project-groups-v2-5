package edu.northeastern.rhythmlounge;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Check if the Fragment is already added. If not, add it.
        if (getSupportFragmentManager().findFragmentByTag(SelfUserPageFragment.class.getSimpleName()) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SelfUserPageFragment(), SelfUserPageFragment.class.getSimpleName())
                    .commit();
        }
    }
}
