package ru.stacymay.wordsstartingwithlettergame;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class StartActivity extends AppCompatActivity {

    CallbackManager mCallbackManager;
    Button signInFBBtn, signInGoogleBtn;
    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    final static int RC_SIGN_IN = 120;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        FacebookSdk.sdkInitialize(StartActivity.this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();

        mCallbackManager = CallbackManager.Factory.create();
        signInFBBtn = findViewById(R.id.signInFB);
        signInGoogleBtn = findViewById(R.id.signInGoogle);

        signInFBBtn.setOnClickListener(v -> {
            LoginManager.getInstance().logInWithReadPermissions(StartActivity.this, Arrays.asList("email", "public_profile"));
            LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    FirebaseAuth.getInstance().signOut();
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {}

                @Override
                public void onError(FacebookException error) {}
            });
        });

        signInGoogleBtn.setOnClickListener(v -> {
            signIn();
        });

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUI(currentUser);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if(task.isSuccessful()) {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                }
            } else {
                Toast.makeText(StartActivity.this, "Ошибка входа через Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        if(task.getException()!=null)
                        if (task.getException().getClass() == FirebaseAuthUserCollisionException.class){
                            Toast.makeText(StartActivity.this, "Ошибка входа через Facebook. Этот email уже привязан к способу входа через Google. Пожалуйста, воспользуйтесь им.", Toast.LENGTH_LONG).show();
                        } else updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null){
            myRef.child("users").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot!=null) {
                        if(!snapshot.hasChild(mAuth.getCurrentUser().getUid().substring(0,12))){
                            myRef.child("users").child(user.getUid().substring(0,12)).child("name").setValue(user.getDisplayName());
                            myRef.child("users").child(user.getUid().substring(0,12)).child("photoUrl").setValue(user.getPhotoUrl().toString());

                        }
                    }
                }
            });
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            finish();
        } else {
            Toast.makeText(StartActivity.this, "Пожалуйста, войдите в свой профиль, чтобы продолжить", Toast.LENGTH_SHORT).show();
        }
    }
}