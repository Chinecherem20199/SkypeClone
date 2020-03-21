package nigeriandailies.com.ng.skypeclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView notificationList;

    private DatabaseReference friendRequestRef, contactsRef, userRef;

    private FirebaseAuth mAuth;
    private String currentUserId;
//   private String listUserId = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("friend requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        
        notificationList = findViewById(R.id.notifications_list);
        notificationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(friendRequestRef.child(currentUserId), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder> recyclerAdapter = new FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationsViewHolder holder, int position, @NonNull Contacts contacts) {

                holder.acceptBtn.setVisibility(View.VISIBLE);
                holder.cancelBtn.setVisibility(View.VISIBLE);

                final String listUserId = getRef(position).getKey();
                DatabaseReference requestTypeRef =getRef(position).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String type = dataSnapshot.getValue().toString();

                            if (type.equals("received")){
                                holder.cardView.setVisibility(View.VISIBLE);

                                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.hasChild("image")){
                                            final String imageStr = dataSnapshot.child("image").getValue().toString();
//                                            final String nameStr = dataSnapshot.child("name").getValue().toString();

                                            Picasso.get().load(imageStr).into(holder.profileImageView);
//                                            holder.userNameText.setText(nameStr);

                                        }
                                        final String nameStr = dataSnapshot.child("name").getValue().toString();
                                        holder.userNameText.setText(nameStr);

//                                        holder.acceptBtn.setVisibility(View.VISIBLE);
//                                        holder.cancelBtn.setVisibility(View.VISIBLE);

                                        holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                contactsRef.child(currentUserId).child(listUserId)
                                                        .child("contacts").setValue("saved").addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){

                                                                    contactsRef.child(listUserId).child(currentUserId)
                                                                            .child("contacts").setValue("saved").addOnCompleteListener(
                                                                            new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()){


                                                                                        friendRequestRef.child(currentUserId).child(listUserId)
                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                if (task.isSuccessful()){
                                                                                                    friendRequestRef.child(listUserId).child(currentUserId)
                                                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){

                                                                                                                Toast.makeText(NotificationsActivity.this, "Contact Saved.", Toast.LENGTH_SHORT).show();
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
                                        });
                                        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                friendRequestRef.child(currentUserId).child(listUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){
                                                            friendRequestRef.child(listUserId).child(currentUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        Toast.makeText(NotificationsActivity.this, "Request Cancelled.", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }else {
                                holder.cardView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friends_design, parent, false);

                NotificationsViewHolder viewHolder = new NotificationsViewHolder(view);

                return viewHolder;
            }
        };
        notificationList.setAdapter(recyclerAdapter);
        recyclerAdapter.startListening();
    }

    public static class NotificationsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView userNameText;
        Button acceptBtn, cancelBtn;
        RelativeLayout cardView;

        public NotificationsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.image_notification);
            userNameText = itemView.findViewById(R.id.name_notification);
            acceptBtn = itemView.findViewById(R.id.accept_request_btn);
            cancelBtn = itemView.findViewById(R.id.decline_request_btn);
            cardView = itemView.findViewById(R.id.card_view);





        }
    }
//    private void acceptFriendRequest() {
//
//    }
//    private void cancelFriendRequest() {
//
//    }

}
