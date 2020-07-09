package com.work.project.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.work.project.Adapter.UserAdapter;
import com.work.project.Model.Chatlist;
import com.work.project.Model.User;
import com.work.project.Notifications.Token;
import com.work.project.R;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers = new ArrayList<>();

    FirebaseUser fuser;
    DatabaseReference chatlistRef;
    ImageView ghost;
    List<Chatlist> usersList = new ArrayList<>();
    private ValueEventListener chatlistListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        ghost = view.findViewById(R.id.ghost);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        chatlistRef = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid());
        chatlistListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Chatlist> newUsersList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    newUsersList.add(chatlist);
                }

                usersList = newUsersList;

                if (usersList.isEmpty()) {
                    ghost.setVisibility(View.VISIBLE);
                } else {
                    ghost.setVisibility(View.GONE);
                }

                updateChatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        chatlistRef.addValueEventListener(chatlistListener);

        updateToken(FirebaseInstanceId.getInstance().getToken());


        return view;
    }

    private void updateToken(String token){
        DatabaseReference tokensRef = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        tokensRef.child(fuser.getUid()).setValue(token1);
    }

    private void updateChatList() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Chatlist chatlist : usersList){
                        if (user.getId().equals(chatlist.getId())){
                            mUsers.add(user);
                        }
                    }
                }

                userAdapter = new UserAdapter(getContext(), mUsers, UserAdapter.CHATS);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chatlistRef.removeEventListener(chatlistListener);
    }
}
