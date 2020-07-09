package com.work.project.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.work.project.Adapter.LanguageAdapter;
import com.work.project.Model.LanguageItem;
import com.work.project.Model.User;
import com.work.project.R;
import com.work.project.StartActivity;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

    CircleImageView image_profile;
    TextView username;

    DatabaseReference currentUserRef;
    FirebaseUser fuser;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    int languageID = 0;
    String language;
    public static final int RU = 0, EN = 1, DE = 2, ES = 3, FR = 4, IT = 5, ZH = 6;

    private ArrayList<LanguageItem> mLanguageList;
    private LanguageAdapter mLanguageAdapter;
    private Spinner spinnerLanguages;
    private Spinner spinnerGender;
    private ValueEventListener currentUserListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

        Button btn_logout = view.findViewById(R.id.btn_logout);

        btn_logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), StartActivity.class);
                startActivity(intent);
            }
        });


        //adding languages to list
        initList();
        spinnerLanguages = view.findViewById(R.id.spinner_languages);
        mLanguageAdapter = new LanguageAdapter(getContext(), mLanguageList);
        spinnerLanguages.setAdapter(mLanguageAdapter);
        spinnerLanguages.setOnItemSelectedListener(new FixedItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                super.onItemSelected(parent, view, position, id);
                if(calledTimes == 1)
                    return;
                LanguageItem clickedItem = (LanguageItem) parent.getItemAtPosition(position);
                String clickedLanguageName = clickedItem.getLanguageName();
                currentUserRef.child("languageID").setValue(String.valueOf(position));
                currentUserRef.child("language").setValue(clickedLanguageName);
                //Toast.makeText(getContext(), clickedLanguageName + " selected", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerGender = view.findViewById(R.id.spinner_gender);
        ArrayAdapter<CharSequence> mGenderAdapter = ArrayAdapter.createFromResource(getContext(), R.array.gender, R.layout.spinner_gender_item);
        mGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(mGenderAdapter);
        spinnerGender.setOnItemSelectedListener(new FixedItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                super.onItemSelected(parent, view, position, id);
                if(calledTimes == 1)
                    return;
                currentUserRef.child("genderId").setValue(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        currentUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                languageID = Integer.parseInt(user.getLanguageID());
                language = user.getLanguage();
                spinnerLanguages.setSelection(languageID, false);
                int genderId = user.getGenderId();
                //Log.d("oneworld", "gender id: " + genderId);
                spinnerGender.setSelection(genderId, false);

                if (user.getImageURL().equals("default")) {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getContext()).load(user.getImageURL()).into(image_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        currentUserRef.addValueEventListener(currentUserListener);

        return view;
    }

    private void initList() {
        mLanguageList = new ArrayList<>();
        mLanguageList.add(new LanguageItem("RU", R.drawable.russian));
        mLanguageList.add(new LanguageItem("EN", R.drawable.english));
        mLanguageList.add(new LanguageItem("DE", R.drawable.german));
        mLanguageList.add(new LanguageItem("ES", R.drawable.spanish));
        mLanguageList.add(new LanguageItem("FR", R.drawable.french));
        mLanguageList.add(new LanguageItem("IT", R.drawable.italian));
        mLanguageList.add(new LanguageItem("ZH", R.drawable.chinese));
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        if (imageUri != null){
            final  StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }

                    return  fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        currentUserRef.updateChildren(map);

                        pd.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        currentUserRef.removeEventListener(currentUserListener);
    }

    private static class FixedItemSelectedListener implements AdapterView.OnItemSelectedListener {
        int calledTimes = 0;

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            calledTimes++;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }
}
