package com.work.project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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


public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;
    private List<String> userIds;

    DatabaseReference inSearch;
    String currentUserId;
    final static int MAX_USERS = 10;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        inSearch = FirebaseDatabase.getInstance().getReference("InSearch");

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
        inSearch.child(currentUserId).setValue(0);

        inSearch.limitToFirst(MAX_USERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateSearch(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private int toUpdate = 0;

    private void updateSearch(DataSnapshot userIdsSnapshot){
        if(toUpdate > 0)
            return;
        List<String> newIds = new ArrayList<>();
        for(DataSnapshot ds : userIdsSnapshot.getChildren()) {
            String userId = ds.getKey();
            if(!userId.equals(currentUserId))
                newIds.add(userId);
        }

        for(int i = mUsers.size() - 1; i >= 0; i--){
            User user = mUsers.get(i);
            if(!newIds.contains(user.getId())){
                mUsers.remove(i);
                userAdapter.notifyItemRemoved(i);
            }
            else
                newIds.remove(user.getId());
        }

        userIds.addAll(newIds);
        toUpdate = newIds.size();

        DatabaseReference users = FirebaseDatabase.getInstance().getReference("Users");

        for(String newId : newIds){
            users.child(newId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User newUser = dataSnapshot.getValue(User.class);
                    mUsers.add(newUser);
                    userAdapter.notifyItemInserted(mUsers.size() - 1);
                    toUpdate--;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        inSearch.child(currentUserId).removeValue();
    }
}
