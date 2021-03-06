package com.example.help.ui.signIn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.help.MainActivity;
import com.example.help.R;
import com.example.help.util.FirestoreUserHelper;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult);

    private static final String TAG = "SignInActivity";

    private final FirebaseAuth.AuthStateListener mAuthListener = firebaseAuth -> {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            Log.d("SigninActivity", "on Listener: a user signed in, back to main act");
//            startActivity(new Intent(this, MainActivity.class));
            FirestoreUserHelper userHelper = new FirestoreUserHelper();
            userHelper.doesUserDocExist(new FirestoreUserHelper.ResultListener() {
                @Override
                public void onTrue() {
                    // existing user, go to home screen
                    Log.d(TAG, "onTrue: ");
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }

                @Override
                public void onFalse() {
                    Log.d(TAG, "onFalse: ");
                    // new user, need to add userDoc to firebase
                    startActivity(new Intent(getApplicationContext(), NewSignUpActivity.class));
                }
            });
            finish();
        } else {
            executeSignInAction();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth.addAuthStateListener(mAuthListener);
        // If there is no signed in user, launch FirebaseUI
        // Otherwise head to MainActivity
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d("SignInActivity", "onCreate: no user logged in ");
            executeSignInAction();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("SignInActivity", "onRestart: called ");

    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(mAuthListener);
        // If there is no signed in user, launch FirebaseUI
        // Otherwise head to MainActivity
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.d("SignInActivity", "onStart: no user logged in ");
            executeSignInAction();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
//    private void executeSignInAction(){
//        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.EmailBuilder().build());
//
//        Intent signInIntent = AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .setTheme(R.style.LoginTheme)
//                .setIsSmartLockEnabled(false)
//                .build();
//        signInLauncher.launch(signInIntent);
//    }

    private void executeSignInAction(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .setAlwaysShowSignInMethodScreen(true)
                .build();
        signInLauncher.launch(signInIntent);
    }
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        Log.d("SignInActivity.java", "onSignInResult: fired!");
//        IdpResponse response = result.getIdpResponse();
        Log.d("SignInActivity", "onSignInResult: Responded "+result.getResultCode());
        if (result.getResultCode() == RESULT_OK) {
            Log.d("SignInActivity", "onSignInResult: Responded ok");
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String displayName = user.getDisplayName()==null?"ANONYMOUS":user.getDisplayName();
            Log.d("SignInActivity.java","onSignInResult: "+"sign in succeed, as user: "+displayName);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
//            result.getResultCode();
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Log.d("SignInActivity.java", "onSignInResult: "+"sign in failed");
        }
    }


}
