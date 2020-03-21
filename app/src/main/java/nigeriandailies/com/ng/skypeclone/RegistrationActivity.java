package nigeriandailies.com.ng.skypeclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;


public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";

    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueAndNextBtn;
    private String checker = "", phoneNumber ="";
    private RelativeLayout relativeLayout;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
//    private FirebaseUser mUser;

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initializedFields();

        continueAndNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (continueAndNextBtn.getText().equals("Submit") || checker.equals("code sent")){

                    String varificationCode = codeText.getText().toString();
                    if (varificationCode.equals("")){
                        Toast.makeText(RegistrationActivity.this, "Please enter your code...", Toast.LENGTH_SHORT).show();
                    }else {
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("Please wait.....");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, varificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
                else {
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals("")){
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("Please wait.....");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,        // Phone number to verify
                                60,                 // Timeout duration
                                TimeUnit.SECONDS,   // Unit of timeout
                                RegistrationActivity.this,               // Activity (for callback binding)
                                mCallbacks);        // OnVerificationStateChangedCallbacks

                    }else {

                    }
                }
            }
        });
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                String error = e.getMessage();
                Toast.makeText(RegistrationActivity.this, "Invalid Phone number " + error, Toast.LENGTH_SHORT).show();
                relativeLayout.setVisibility(View.VISIBLE);
                continueAndNextBtn.setText("Continue");
                codeText.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                mVerificationId = s;
                mResendToken = forceResendingToken;

                relativeLayout.setVisibility(View.GONE);
                checker = "code sent";
                continueAndNextBtn.setText("Submit");
                codeText.setVisibility(View.VISIBLE);

                loadingBar.dismiss();
                Toast.makeText(RegistrationActivity.this, "Code sent", Toast.LENGTH_SHORT).show();


            }
        };
    }

    private void initializedFields() {

        mAuth = FirebaseAuth.getInstance();


        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        phoneText  = findViewById(R.id.phoneText);
        codeText  = findViewById(R.id.codeText);
        continueAndNextBtn  = findViewById(R.id.continueNextButton);
        relativeLayout = findViewById(R.id.phoneAuth);

        ccp.registerCarrierNumberEditText(phoneText);

        loadingBar = new ProgressDialog(this);





    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            sendUserToMainActivity();
                        } else {
                            loadingBar.dismiss();
                            String error = task.getException().toString();
                            Toast.makeText(RegistrationActivity.this, "Error " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void sendUserToMainActivity(){
        Intent loginIntent = new Intent(RegistrationActivity.this, ContactsActivity.class);
        startActivity(loginIntent);
        fileList();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null){
            Intent homeIntent = new Intent(RegistrationActivity.this, ContactsActivity.class);
            homeIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        }
    }
}
