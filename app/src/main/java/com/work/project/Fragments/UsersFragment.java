package com.work.project.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.work.project.Adapter.UserAdapter;
import com.work.project.Model.User;
import com.work.project.R;

import java.util.ArrayList;
import java.util.List;

import static androidx.recyclerview.widget.RecyclerView.*;


public class UsersFragment extends Fragment {

    private MyRecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<String> userIds;
    Button but;
    DatabaseReference inSearch;
    String currentUserId;
    final static int MAX_USERS = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        inSearch = FirebaseDatabase.getInstance().getReference();

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsers = new ArrayList<>();
        userIds = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(),  mUsers, false);
        recyclerView.setAdapter(userAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        inSearch.child("InSearch").child(currentUserId).setValue(0);
        final List<String> likesIds = new ArrayList<>();
        inSearch.child("Likes").child("YouWereLikedBy").child(currentUserId).limitToFirst(MAX_USERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userId = ds.getKey();
                    likesIds.add(userId);
                   // Toast.makeText(getContext(), "Hello", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        inSearch.child("InSearch").limitToFirst(MAX_USERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateSearch(dataSnapshot, likesIds);
                //Toast.makeText(getContext(), "ByeBye", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

    }

    private int toUpdate = 0;

    private void updateSearch(DataSnapshot userIdsSnapshot, List<String> likesIds) {
        if (toUpdate > 0)
            return;
        List<String> newIds = new ArrayList<>();


        for (DataSnapshot ds : userIdsSnapshot.getChildren()) {
            String userId = ds.getKey();
            if (!userId.equals(currentUserId))
                newIds.add(userId);
        }

        for (int i = mUsers.size() - 1; i >= 0; i--) {
            User user = mUsers.get(i);
            if (!newIds.contains(user.getId())) {
                mUsers.remove(i);
                userAdapter.notifyItemRemoved(i);
            } else
                newIds.remove(user.getId());
        }
        userIds.addAll(likesIds);
        userIds.addAll(newIds);
        toUpdate = newIds.size();

        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");

        for (String newId : newIds) {
            users.child(newId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User newUser = dataSnapshot.getValue(User.class);
                    mUsers.add(newUser);
                    userAdapter.notifyItemInserted(mUsers.size() - 1);
                    toUpdate--;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        inSearch.child("InSearch").child(currentUserId).removeValue();
    }

    public static class MyRecyclerView extends RecyclerView {
        int currentItem = 0;

        public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public MyRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            if(!super.onTouchEvent(e))
                return false;
            if(e.getActionMasked() == MotionEvent.ACTION_UP) {
                int scrollY = computeVerticalScrollOffset();

                if (scrollY % getHeight() != 0) {
                    int oldScrollY = currentItem * getHeight();
                    int dy = scrollY - oldScrollY;
                    if(Math.abs(dy) >= getHeight() * 0.15) {
                        if(dy > 0)
                            currentItem++;
                        else
                            currentItem--;
                    }

                    int itemCount = getAdapter().getItemCount();
                    if(currentItem >= itemCount)
                        currentItem = itemCount - 1;

                    stopScrollAnimation(e.getY());
                    smoothScrollToPosition(currentItem);
                }
            }
            return true;
        }

        private void stopScrollAnimation(float eventY){
            long time = SystemClock.uptimeMillis();

            MotionEvent eventDown = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN,
                    0, eventY, 0);
            super.onTouchEvent(eventDown);
            eventDown.recycle();

            MotionEvent eventUp = MotionEvent.obtain(time, time, MotionEvent.ACTION_UP,
                    0, eventY, 0);
            super.onTouchEvent(eventUp);
            eventUp.recycle();
        }
    }
}
