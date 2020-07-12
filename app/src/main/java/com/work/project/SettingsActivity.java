package com.work.project;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class SettingsActivity extends AppCompatActivity {
    // SettingsActivity также используется и в регистрации пользователя
    public final static String FROM_REGISTRATION = "fromRegistration";
    CircleImageView image_profile;
    TextView username;

    DatabaseReference currentUserRef;
    private ValueEventListener currentUserListener;

    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    Spinner spinnerCountries, spinnerLanguages, spinnerGender;
    private ArrayList<LanguageItem> countryList, languageList;

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserRef = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        image_profile = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImage();
            }
        });

        Button logOut = findViewById(R.id.btn_logout);
        // если мы пришли из регистрации, заменяем кнопку Log out на Next
        if(getIntent().hasExtra(FROM_REGISTRATION)){
            logOut.setText(R.string.finish_registration);
            logOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }
        else {
            logOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }

        countryList = createCountryList(getResources());
        languageList = createLanguageList(getResources());

        spinnerCountries = findViewById(R.id.spinner_countries);
        LanguageAdapter mCountryAdapter = new LanguageAdapter(this, countryList);
        spinnerCountries.setAdapter(mCountryAdapter);


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

        spinnerLanguages = findViewById(R.id.spinner_languages);
        LanguageAdapter mLanguageAdapter = new LanguageAdapter(this, languageList);
        spinnerLanguages.setAdapter(mLanguageAdapter);
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

        spinnerGender = findViewById(R.id.spinner_gender);
        ArrayAdapter<CharSequence> mGenderAdapter = ArrayAdapter.createFromResource(this, R.array.gender, R.layout.spinner_gender_item);
        mGenderAdapter.setDropDownViewResource(R.layout.spinner_gender_item);
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

        mDisplayDate = findViewById(R.id.select_date);
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                DatePickerDialog dialog = new DatePickerDialog(
                        SettingsActivity.this,
                        android.R.style.Theme_Black,
                        mDateSetListener, year, month, day);
                dialog.getDatePicker().setMaxDate(new Date().getTime());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month++;
                String date = "";
                if (month < 10) {
                    date += "0";
                }
                date += month + "/";
                if (day < 10) {
                    date += "0";
                }
                date += day + "/" + year;
                currentUserRef.child("dateOfBirth").setValue(date);
                mDisplayDate.setText(date);
            }
        };


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

                String date = user.getDateOfBirth();
                if (date != null) {
                    mDisplayDate.setText(user.getDateOfBirth());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
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
        countryList.add(new LanguageItem(res, CountryUtil.UK, true));
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
