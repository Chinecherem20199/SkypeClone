package nigeriandailies.com.ng.skypeclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsActivity extends AppCompatActivity {
    BottomNavigationView navView;
    private RecyclerView myContactsList;
    private ImageView findPeopleBtn;

    private DatabaseReference contactsRef, userRef;

    private FirebaseAuth mAuth;
    private String currentUserId;
    private String userName = "", profileImage = "";
    private String  calledBy = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        navView = findViewById(R.id.nav_view);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        contactsRef = FirebaseDatabase.getInstance().getReference().child("contacts");
        userRef = FirebaseDatabase.getInstance().getReference().child("users");



        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        findPeopleBtn = findViewById(R.id.find_people_img);
        myContactsList = findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent findPeopleIntent = new Intent(ContactsActivity.this, FindPeopleActivity.class);
                startActivity(findPeopleIntent);
            }
        });


    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.navigation_home:
                    Intent intent = new Intent(ContactsActivity.this, ContactsActivity.class);
                    startActivity(intent);
                    break;

                case R.id.navigation_setting:
                    Intent intent1 = new Intent(ContactsActivity.this, SettingsActivity.class);
                    startActivity(intent1);
                    break;

                case R.id.navigation_notifications:
                    Intent intent2 = new Intent(ContactsActivity.this, NotificationsActivity.class);
                    startActivity(intent2);
                    break;

                case R.id.navigation_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent intent3 = new Intent(ContactsActivity.this, RegistrationActivity.class);
                    startActivity(intent3);
                    finish();
                    break;

            }

            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        checkForReceivingCall();
        validateUser();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef.child(currentUserId), Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts contacts) {

                        final String listUserId = getRef(position).getKey();
                        userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){
                                    userName = dataSnapshot.child("name").getValue().toString();
                                    profileImage= dataSnapshot.child("image").getValue().toString();


                                    holder.userNameText.setText(userName);
                                    Picasso.get().load(profileImage).into(holder.profileImageView);
                                }
                                holder.callBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent callingIntent = new Intent(ContactsActivity.this, CallingActivity.class);
                                        callingIntent.putExtra("visitUserId", listUserId);
                                        startActivity(callingIntent);
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);

                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }




    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImageView;
        TextView userNameText;
        Button callBtn;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.image_contact);
            userNameText = itemView.findViewById(R.id.name_contact);
            callBtn = itemView.findViewById(R.id.call_btn);





        }
    }
       private void validateUser() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    Intent settingIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(settingIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void checkForReceivingCall() {
        userRef.child(currentUserId).child("ringing")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("rigging")){
                            calledBy = dataSnapshot.child("rigging").getValue().toString();

                            Intent callingIntent1 = new Intent(ContactsActivity.this, CallingActivity.class);
                            callingIntent1.putExtra("visitUserId", calledBy);
                            startActivity(callingIntent1);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
