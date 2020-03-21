package nigeriandailies.com.ng.skypeclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button saveBtn;
    private EditText userBioEditext, usernameEditText;
    private CircleImageView profileImage;
    private static int GallaryPick = 1;

    private Uri imageUri;
    private StorageReference profileImageRef;
    private String downloadUri;
    private DatabaseReference userRef;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profileImageRef = FirebaseStorage.getInstance().getReference().child("profile image");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        saveBtn = findViewById(R.id.save_settings_btn);
        userBioEditext = findViewById(R.id.bio_settings);
        usernameEditText = findViewById(R.id.username_setting);
        profileImage = findViewById(R.id.settings_profile_image);

        loadingBar = new ProgressDialog(this);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent,GallaryPick );
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserData();
            }
        });
        retreiveUserInfo();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==GallaryPick && resultCode==RESULT_OK && data !=null){

            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
    private void saveUserData() {
        final String getUserName = usernameEditText.getText().toString();
        final  String getUserStatus = userBioEditext.getText().toString();

        if (imageUri==null){

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")){
                        
                        saveInfo();

                    }else {
                        Toast.makeText(SettingsActivity.this, "Please set your profile image first.", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        else if (getUserName.equals("")){

            Toast.makeText(this, "Please write your name.", Toast.LENGTH_SHORT).show();
        }
        else if(getUserStatus.equals("")){
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Account Settings");
            loadingBar.setMessage("Please wait....");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            final StorageReference filePath = profileImageRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            final UploadTask uploadTask = filePath.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    downloadUri = filePath.getDownloadUrl().toString();
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        downloadUri = task.getResult().toString();

                        HashMap<String, Object> profileMap = new HashMap<>();
                        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name", getUserName);
                        profileMap.put("status", getUserStatus);
                        profileMap.put("image", downloadUri);

                        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    loadingBar.dismiss();
                                    Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void saveInfo() {
        final String getUserName = usernameEditText.getText().toString();
        final String getUserStatus = userBioEditext.getText().toString();


        if (getUserName.equals("")) {

            Toast.makeText(this, "Please write your name.", Toast.LENGTH_SHORT).show();
        } else if (getUserStatus.equals("")) {
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Account Settings");
            loadingBar.setMessage("Please wait....");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("name", getUserName);
            profileMap.put("status", getUserStatus);

            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        loadingBar.dismiss();
                        Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            });
        }
    }
    private void retreiveUserInfo(){
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String imageDb = dataSnapshot.child("image").getValue().toString();
                    String nameDb = dataSnapshot.child("name").getValue().toString();
                    String bioDb = dataSnapshot.child("status").getValue().toString();

                    usernameEditText.setText(nameDb);
                    userBioEditext.setText(bioDb);
                    Picasso.get().load(imageDb).placeholder(R.drawable.profile_image).into(profileImage);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
