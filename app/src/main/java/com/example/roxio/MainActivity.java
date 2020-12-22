package com.example.roxio;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity{

    private TextView processText;
    private EditText countryCodeEdit , phoneNumberEdit;
    private FirebaseAuth auth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        getSupportActionBar().hide();
*/

        Button mSendOtpBtn = findViewById(R.id.send_codebtn);
        processText = findViewById(R.id.text_process);
        countryCodeEdit = findViewById(R.id.input_country_code);
        phoneNumberEdit = findViewById(R.id.input_phone);

        auth = FirebaseAuth.getInstance();


        mSendOtpBtn.setOnClickListener(v -> {
            String country_code = countryCodeEdit.getText().toString();
            String phone = phoneNumberEdit.getText().toString();
            String phoneNumber = "+" + country_code + "" + phone;
            if (!country_code.isEmpty() || !phone.isEmpty()) {
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(MainActivity.this)
                        .setCallbacks(mCallBacks)
                        .build();
                PhoneAuthProvider.verifyPhoneNumber(options);

            }else{
                processText.setText("Please Enter Country Code and Phone Number");
                processText.setTextColor(Color.CYAN);
                processText.setVisibility(View.VISIBLE);
            }
        });

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                processText.setText(e.getMessage());
                processText.setTextColor(Color.CYAN);
                processText.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                processText.setText("OTP has been Sent");
                processText.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> {
                    Intent otpIntent = new Intent(MainActivity.this, VerificationActivity.class);
                    otpIntent.putExtra("auth", s);
                    startActivity(otpIntent);
                },10000);


            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null){
            sendToMain();
        }
    }
    private void sendToMain(){
        Intent intent = new Intent(MainActivity.this, VerificationActivity.class);
        startActivity(intent);
        finish();
    }

    private void signIn(PhoneAuthCredential credential){
        auth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                sendToMain();
            }else{
                processText.setText(task.getException().getMessage());
                processText.setTextColor(Color.CYAN);
                processText.setVisibility(View.VISIBLE);

            }
        });
    }

}
