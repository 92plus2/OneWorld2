package com.work.project;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.work.project.Model.CountryUtil;
import com.work.project.Model.LanguageItem;
import com.work.project.Model.LanguageUtil;
import com.work.project.Model.User;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {

    CircleImageView image_profile;
    TextView username;

    DatabaseReference currentUserRef;
    private ValueEventListener currentUserListener;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    Spinner spinnerCountries, spinnerLanguages, spinnerGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        image_profile = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

        Button btn_logout = findViewById(R.id.btn_logout);

        btn_logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
                startActivity(intent);
            }
        });

        // ===========================================================================================
        spinnerCountries = findViewById(R.id.spinner_countries);
        spinnerLanguages = findViewById(R.id.spinner_languages);
        spinnerGender = findViewById(R.id.spinner_gender);
        final ArrayList<LanguageItem> countryList = SettingsActivity.createCountryList(getResources());
        final ArrayList<LanguageItem> languageList = SettingsActivity.createLanguageList(getResources());

        SettingsActivity.initializeSpinners(this, spinnerCountries, spinnerLanguages, spinnerGender, countryList, languageList);
        // ==========================================================================================

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        currentUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(SettingsActivity.this).load(user.getImageURL()).into(image_profile);
                }
                int countryID = user.getCountryID();
                int countryPosition = countryList.indexOf(new LanguageItem(getResources(), countryID, true));
                spinnerCountries.setSelection(countryPosition, false);

                int languageID = user.getLanguageID();
                int languagePosition = languageList.indexOf(new LanguageItem(getResources(), languageID, false));
                spinnerLanguages.setSelection(languagePosition, false);

                int genderId = user.getGenderID();
                spinnerGender.setSelection(genderId, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
    }

    public static void initializeSpinners(Context context, Spinner spinnerCountries, Spinner spinnerLanguages, Spinner spinnerGender, ArrayList<LanguageItem> countryList, ArrayList<LanguageItem> languageList) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        LanguageAdapter mCountryAdapter = new LanguageAdapter(context, countryList);
        LanguageAdapter mLanguageAdapter = new LanguageAdapter(context, languageList);
        spinnerCountries.setAdapter(mCountryAdapter);
        spinnerLanguages.setAdapter(mLanguageAdapter);

        spinnerCountries.setOnItemSelectedListener(new FixedItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                super.onItemSelected(parent, view, position, id);
                if(calledTimes == 1)
                    return;
                LanguageItem clickedItem = (LanguageItem) parent.getItemAtPosition(position);
                currentUserRef.child("countryID").setValue(clickedItem.getLanguageId());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerLanguages.setOnItemSelectedListener(new FixedItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                super.onItemSelected(parent, view, position, id);
                if(calledTimes == 1)
                    return;
                LanguageItem clickedItem = (LanguageItem) parent.getItemAtPosition(position);
                currentUserRef.child("languageID").setValue(clickedItem.getLanguageId());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        ArrayAdapter<CharSequence> mGenderAdapter = ArrayAdapter.createFromResource(context, R.array.gender, R.layout.spinner_gender_item);
        mGenderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(mGenderAdapter);
        spinnerGender.setOnItemSelectedListener(new FixedItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                super.onItemSelected(parent, view, position, id);
                if(calledTimes == 1)
                    return;
                currentUserRef.child("genderID").setValue(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUserRef.addValueEventListener(currentUserListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentUserRef.removeEventListener(currentUserListener);
    }


    public static ArrayList<LanguageItem> createCountryList(Resources res) {
        ArrayList<LanguageItem> countryList = new ArrayList<>();
        countryList.add(new LanguageItem(res, CountryUtil.USA, true));
        countryList.add(new LanguageItem(res, CountryUtil.ENGLAND, true));
        countryList.add(new LanguageItem(res, CountryUtil.RUSSIA, true));
        countryList.add(new LanguageItem(res, CountryUtil.GERMANY, true));
        countryList.add(new LanguageItem(res, CountryUtil.SPAIN, true));
        countryList.add(new LanguageItem(res, CountryUtil.FRANCE, true));
        countryList.add(new LanguageItem(res, CountryUtil.ITALY, true));
        countryList.add(new LanguageItem(res, CountryUtil.CHINA, true));
        return countryList;
    }

    public static ArrayList<LanguageItem> createLanguageList(Resources res){
        ArrayList<LanguageItem> languageList = new ArrayList<>();
        languageList.add(new LanguageItem(res, LanguageUtil.ENGLISH, false));
        languageList.add(new LanguageItem(res, LanguageUtil.RUSSIAN, false));
        languageList.add(new LanguageItem(res, LanguageUtil.GERMAN, false));
        languageList.add(new LanguageItem(res, LanguageUtil.SPANISH, false));
        languageList.add(new LanguageItem(res, LanguageUtil.FRENCH, false));
        languageList.add(new LanguageItem(res, LanguageUtil.ITALIAN, false));
        languageList.add(new LanguageItem(res, LanguageUtil.CHINIZE, false));
        return languageList;
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = SettingsActivity.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(SettingsActivity.this);
        pd.setMessage(getString(R.string.uploading_image));
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
                        Toast.makeText(SettingsActivity.this, R.string.failed_to_upload_image, Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SettingsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        } else {
            Toast.makeText(SettingsActivity.this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

            if (uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(SettingsActivity.this, R.string.upload_in_progress, Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        }
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
