package com.example.help.ui.signIn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.help.MainActivity;
import com.example.help.R;
import com.example.help.databinding.NewSignUpBinding;
import com.example.help.util.FirestoreUserHelper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class NewSignUpActivity extends AppCompatActivity {

    private NewSignUpBinding mBinding;
    TextView phoneTextView;
    Button confirmButton;

    private static final String TAG = "NewSignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = NewSignUpBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        phoneTextView = findViewById(R.id.edit_text_phone);
        confirmButton = findViewById(R.id.confirm_button);
        FirestoreUserHelper userHelper = new FirestoreUserHelper();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: " + phoneTextView.getText());
                userHelper.createUserDoc(phoneTextView.getText().toString());
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


}
