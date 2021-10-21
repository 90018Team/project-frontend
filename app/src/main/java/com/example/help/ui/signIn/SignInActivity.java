package com.example.help.ui.signIn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.help.MainActivity;
import com.example.help.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            result -> onSignInResult(result)
    );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If there is no signed in user, launch FirebaseUI
        // Otherwise head to MainActivity
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d("SignInActivity", "onCreate: no user logged in ");
            executeSignInAction();
            Intent signInIntent=executeSignInAction();
            signInLauncher.launch(signInIntent);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }
    @Override
    protected void onStart() {
        super.onStart();

        // If there is no signed in user, launch FirebaseUI
        // Otherwise head to MainActivity
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d("SignInActivity", "onStart: no user logged in ");
            Intent signInIntent=executeSignInAction();
            signInLauncher.launch(signInIntent);
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }
    private Intent executeSignInAction(){
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build();
        return signInIntent;
    }
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        Log.d("SignInActivity", "onSignInResult: Responded "+result.getResultCode());
        if (result.getResultCode() == RESULT_OK) {
            Log.d("SignInActivity", "onSignInResult: Responded ok");
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String displayName = user.getDisplayName()==null?"ANONYMOUS":user.getDisplayName();
            Log.d("SignInActivity.java","onSignInResult: "+"sign in succeed, as user: "+displayName);
            startActivity(new Intent(this, MainActivity.class));
        } else if(result.getResultCode()==null){
            Log.d("SignInActivity.java", "onSignInResult: "+"sign in cancelled");
        }
        else{
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.d("SignInActivity.java", "onSignInResult: "+"sign in failed");
        }
    }
}
