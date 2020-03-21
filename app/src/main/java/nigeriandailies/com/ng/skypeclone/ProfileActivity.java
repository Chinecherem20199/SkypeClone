package nigeriandailies.com.ng.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserID = "", receiverUserName = "", receiverUserImage = "";
    private CircleImageView backgroundProfileView;
    private TextView nameProfile;
    private Button addFriends;
    private Button cancelFriends;

    private FirebaseAuth mAuth;
    private DatabaseReference friendRequestRef, contactsRef;
    private String senderUserId;
    private String currentState = "new";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("friend requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        receiverUserName = getIntent().getExtras().get("profile_image").toString();
        receiverUserImage = getIntent().getExtras().get("profile_name").toString();

        backgroundProfileView = findViewById(R.id.background_profile_view);
        nameProfile = findViewById(R.id.name_profile);
        addFriends = findViewById(R.id.add_friends);
        cancelFriends = findViewById(R.id.cancel_friends);

        Picasso.get().load(receiverUserImage).into(backgroundProfileView);
        nameProfile.setText(receiverUserName);

        manageEventClick();

    }

    private void manageEventClick() {
        friendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)){
                    String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                    if (requestType.equals("sent")){

                        currentState = "request_sent";
                        addFriends.setText("Cancel Friend Request");
                    }
                   else if (requestType.equals("received")){

                        currentState = "request_received";
                        addFriends.setText("Accept Friend Request");

                        cancelFriends.setVisibility(View.VISIBLE);
                        cancelFriends.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelFriendRequest();
                            }
                        });
                    }
                }
                else {
                    contactsRef.child(senderUserId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(receiverUserID)){
                                        currentState = "friends";
                                        addFriends.setText("Delete Contact");
                                    }else {
                                        currentState = "new";
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (senderUserId.equals(receiverUserID)){
            addFriends.setVisibility(View.GONE);
        }else {
            addFriends.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (currentState.equals("new")){

                        sendFriendRequest();
                    }
                    if( currentState.equals("request_sent")){

                        cancelFriendRequest();
                    }
                    if( currentState.equals("request_received")){

                        acceptFriendRequest();
                    }
                    if( currentState.equals("request_sent")){

                        cancelFriendRequest();
                    }
                }
            });
        }
    }

    private void acceptFriendRequest() {
        contactsRef.child(senderUserId).child(receiverUserID)
                .child("contacts").setValue("saved").addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){

                            contactsRef.child(receiverUserID).child(senderUserId)
                                    .child("contacts").setValue("saved").addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){


                                                friendRequestRef.child(senderUserId).child(receiverUserID)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){
                                                            friendRequestRef.child(receiverUserID).child(senderUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        currentState = "friends";
                                                                        addFriends.setText("Delete Contacts");

                                                                        cancelFriends.setVisibility(View.GONE);
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
        );
    }

    private void cancelFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserID)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    friendRequestRef.child(receiverUserID).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                currentState = "new";
                                addFriends.setText("Add Friend");
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserID)
                .child("request_type").setValue("sent").addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserID).child(senderUserId)
                                    .child("request_type").setValue("received").addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                currentState = "request_sent";
                                                addFriends.setText("Cancel Friend Request");
                                            }

                                        }
                                    }
                            );
                        }
                    }
                }
        );

    }
}
