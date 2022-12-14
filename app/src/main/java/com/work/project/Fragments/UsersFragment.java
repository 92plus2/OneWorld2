package com.work.project.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.work.project.MessageActivity;
import com.work.project.Model.Chatlist;
import com.work.project.Model.User;
import com.work.project.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class UsersFragment extends Fragment {
    final static int MAX_USERS = 300;
    final static boolean STAY_IN_SEARCH = true;  // оставаться в поиске. Пока что так, потому что мало пользователей
    private MyRecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;
    private Set<String> peopleWhoLikedUs;  // id пользователей, которые нас лайкнули. Используется только в "Search Users"
    private Set<String> ourLikes;  // id пользователей, которых мы лайкнули. Используется только в "Search Users"
    private DatabaseReference reference;
    private String currentUserId;
    TextView StarTip;
    List<Chatlist> chatlists = new ArrayList<>();
    ImageView ghost;
    private Map<DatabaseReference, ValueEventListener> listeners = new HashMap<>();

    public static UsersFragment newInstance(boolean searchUsers){
        Bundle args = new Bundle();
        args.putBoolean("searchUsers", searchUsers);
        UsersFragment fragment = new UsersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private boolean isSearchUsers(){
        return getArguments().getBoolean("searchUsers");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        reference = FirebaseDatabase.getInstance().getReference();

        View view = inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUsers = new ArrayList<>();
        peopleWhoLikedUs = new HashSet<>();
        ourLikes = new HashSet<>();
        ghost = view.findViewById(R.id.ghost);
        userAdapter = new UserAdapter(getContext(),  mUsers, isSearchUsers()? UserAdapter.SEARCH_USERS : UserAdapter.FRIEND_REQUESTS);
        recyclerView.setAdapter(userAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(isSearchUsers()) {
            startListeningForLikes();
            startListeningForChatlist();
        }
        startListeningForUsersUpdate();
        return view;
    }

    private void startListeningForLikes(){

        ValueEventListener likesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                peopleWhoLikedUs.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userId = ds.getKey();
                    peopleWhoLikedUs.add(userId);
                }
                deleteUsersWithLikes();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        DatabaseReference likesRef = reference.child("Likes").child("YouWereLikedBy").child(currentUserId);
        likesRef.limitToLast(MAX_USERS).addValueEventListener(likesListener);
        listeners.put(likesRef, likesListener);


        ValueEventListener ourLikesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ourLikes.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String userId = ds.getKey();
                    ourLikes.add(userId);
                    Log.d(MessageActivity.TAG, "our like: " + userId);
                }
                deleteUsersWithLikes();
                /*if(!ourLikes.isEmpty()){
                    StarTip.setVisibility(View.GONE);
                }*/


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }

        };
        DatabaseReference ourLikesRef = reference.child("Likes").child("YourLikes").child(currentUserId);
        ourLikesRef.limitToLast(MAX_USERS).addValueEventListener(ourLikesListener);
        listeners.put(ourLikesRef, ourLikesListener);
    }

    // если пользователь поставил нам лайк, то он не отображается во вкладке Search Users
    private void deleteUsersWithLikes(){
        for (int i = mUsers.size() - 1; i >= 0; i--) {
            User user = mUsers.get(i);
            if (peopleWhoLikedUs.contains(user.getId()) || ourLikes.contains(user.getId())) {
                mUsers.remove(i);
                userAdapter.notifyItemRemoved(i);
            }
        }
        updateGhostVisibility();
    }

    private void startListeningForUsersUpdate(){
        DatabaseReference users = isSearchUsers()? reference.child("InSearch") :
                                               reference.child("Likes").child("YouWereLikedBy").child(currentUserId);

        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateUsers(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        users.limitToFirst(MAX_USERS).addValueEventListener(usersListener);
        listeners.put(users, usersListener);
    }

    private void startListeningForChatlist(){
        DatabaseReference chatlistRef = reference.child("Chatlist").child(currentUserId);
        ValueEventListener chatlistListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Chatlist> newUsersList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    newUsersList.add(chatlist);
                }

                chatlists = newUsersList;

                DatabaseReference inSearch = reference.child("InSearch");
                inSearch.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        updateUsers(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        chatlistRef.addValueEventListener(chatlistListener);
        listeners.put(chatlistRef, chatlistListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isSearchUsers()) {
            reference.child("InSearch").child(currentUserId).setValue(0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(!STAY_IN_SEARCH) {
            if (isSearchUsers()) {
                reference.child("InSearch").child(currentUserId).removeValue();
            }
        }
    }

    // сколько ещё пользователей мы должны загрузить из базы данных
    private int leftUsersToLoad = 0;

    private void updateUsers(DataSnapshot userIdsSnapshot) {
        if (leftUsersToLoad > 0)
            return;
        List<String> newIds = new ArrayList<>();


        for (DataSnapshot ds : userIdsSnapshot.getChildren()) {
            String userId = ds.getKey();
            if (shouldShowUser(userId))
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
        leftUsersToLoad = newIds.size();

        if(newIds.isEmpty())
            updateGhostVisibility();
        else {
            Collections.shuffle(newIds);  // чтобы был случайный порядок

            DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");

            for (String newId : newIds) {
                users.child(newId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User newUser = dataSnapshot.getValue(User.class);
                        if (System.currentTimeMillis() / 1000 <= 1599198126 || (System.currentTimeMillis() - newUser.getLastVisit()) < 259200000) {
                            mUsers.add(newUser);
                            userAdapter.notifyItemInserted(mUsers.size() - 1);
                        }
                        leftUsersToLoad--;

                        if (leftUsersToLoad == 0)
                            updateGhostVisibility();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    // если мы переписывались с пользователем, то он не должен отображаться
    // если пользователь нас лайкнул, то он не должен отображаться во вкладке "Search users"
    private boolean shouldShowUser(String userId){
        if(userId.equals(currentUserId))
            return false;
        if(peopleWhoLikedUs.contains(userId) || ourLikes.contains(userId))
            return false;

        // проверяем, переписывались ли мы с пользователем
        for(Chatlist chatlist : chatlists){
            if(chatlist.id.equals(userId))
                return false;
        }
        return true;
    }
    private void updateGhostVisibility(){
        if(mUsers.isEmpty())
            ghost.setVisibility(View.VISIBLE);
        else
            ghost.setVisibility(View.GONE);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        for(Map.Entry<DatabaseReference, ValueEventListener> kv : listeners.entrySet()){
            kv.getKey().removeEventListener(kv.getValue());
        }
    }
}
