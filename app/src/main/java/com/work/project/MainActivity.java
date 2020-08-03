package com.work.project;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseReference;
import com.work.project.Fragments.ChatsFragment;
import com.work.project.Fragments.ProfileFragment;
import com.work.project.Fragments.UsersFragment;
import com.work.project.Model.User;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class MainActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    DatabaseReference userRef;

    public static final String FRIEND_REQUESTS = "friendRequests";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        userRef = User.getCurrentUserReference();

        final TabLayout tabLayout = findViewById(R.id.tab_layout);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        viewPagerAdapter.addFragment(UsersFragment.newInstance(true), null);
        viewPagerAdapter.addFragment(UsersFragment.newInstance(false), null);
        viewPagerAdapter.addFragment(new ChatsFragment(), null);
        viewPagerAdapter.addFragment(new ProfileFragment(), null);

        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        for(int i = 0; i < tabLayout.getTabCount(); i++){
            if(i == 0)
                tabLayout.getTabAt(i).setIcon(R.drawable.ic_baseline_star_24);
            if(i == 1)
                tabLayout.getTabAt(i).setIcon(R.drawable.ic_baseline_notifications_24);
            if(i == 2)
                tabLayout.getTabAt(i).setIcon(R.drawable.ic_baseline_chat_bubble_24);
            if(i == 3)
                tabLayout.getTabAt(i).setIcon(R.drawable.ic_baseline_account_circle_24);
        }
        Intent intent = getIntent();
        if(intent.hasExtra("fragment")){
            if(intent.getStringExtra("fragment").equals(FRIEND_REQUESTS)) {
                viewPager.post(new Runnable() {
                    @Override
                    public void run() {
                        viewPager.setCurrentItem(1);
                    }
                });
            }
        }
    }




    static class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm, int behaviour){
            super(fm, behaviour);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status){
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        userRef.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        status("offline");
        super.onPause();
    }
}
