package ru.stacymay.wordsstartingwithlettergame;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.facebook.*;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import de.hdodenhof.circleimageview.CircleImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView userPhoto;
    TextView userName, userEmail, userGameId;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    Button socialBtn, goBackBtn, addFriendBtn;
    GoogleSignInClient mGoogleSignInClient;
    ArrayList<Friend> friendArrayList = new ArrayList<>();
    FriendListAdapter friendAdapter;
    DatabaseReference myRef;
    ListView lvFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();

        lvFriends = findViewById(R.id.friendsList);

        userPhoto = findViewById(R.id.userPhoto);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userGameId = findViewById(R.id.userGameId);

        goBackBtn = findViewById(R.id.goBack);
        goBackBtn.setOnClickListener(v-> onBackPressed());

        addFriendBtn = findViewById(R.id.addFriend);
        addFriendBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, AddFriendActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            finish();
        });

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        socialBtn = findViewById(R.id.fb);
        socialBtn.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Выход из профиля");
            builder.setMessage("Ваш прогресс будет сохранён, но Вам нужно будет снова зайти в свой профиль. Вы уверены, что хотите выйти?");
            builder.setNegativeButton("Нет, не выходить", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.setPositiveButton("Да, выйти", (dialog, which) -> {
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                LoginManager.getInstance().logOut();
                Intent intent = new Intent(ProfileActivity.this, StartActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                finish();
            });
            builder.create();
            builder.show();
        });

        checkProvider();

        userGameId.setOnClickListener(v->{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("copied", userGameId.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "\uD83D\uDCC4 ID скопирован в буфер обмена", Toast.LENGTH_SHORT).show();
        });

        userName.setOnClickListener(v->{
            Intent intent = new Intent(ProfileActivity.this, ChangeNameActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            finish();
        });

        if(mUser != null){
            myRef.child("users").child(mUser.getUid().substring(0,12)).child("name").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot!=null) {
                        String name = snapshot.getValue().toString();
                        userName.setText(name);
                    }
                }
            });
            String photoUrl = mUser.getPhotoUrl().toString();
            String email = mUser.getEmail();
            String id = mUser.getUid().substring(0,12);
            Glide.with(this).load(photoUrl).into(userPhoto);

            userEmail.setText(email);
            userGameId.setText(id);
        }

        findFriends();

    }

    private void findFriends() {
        myRef.child("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null)
                for(DataSnapshot childS: snapshot.child(mUser.getUid().substring(0,12)).child("friends").getChildren()){
                    String friendName = snapshot.child(childS.getKey()).child("name").getValue().toString();
                    String friendPhoto = snapshot.child(childS.getKey()).child("photoUrl").getValue().toString();
                    friendArrayList.add(new Friend(friendName,childS.getKey(),friendPhoto,false));
                }
                friendAdapter = new FriendListAdapter(this, friendArrayList, false);
                lvFriends.setAdapter(friendAdapter);
            }
        });
    }

    private void checkProvider() {
        assert mUser != null;
        mUser.getIdToken(false).addOnSuccessListener(getTokenResult -> {
            String provider = getTokenResult.getSignInProvider();
            if(provider!=null)
                if(provider.contains("facebook")){
                    socialBtn.setText("Выполнен вход через Facebook\n(нажмите, чтобы выйти)");
                    socialBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_facebook, 0, 0, 0);
                    socialBtn.setBackgroundResource(R.drawable.custom_fb_button);
                    socialBtn.setTextColor(getResources().getColor(R.color.white));
                } else if(provider.contains("google")){
                    socialBtn.setText("Выполнен вход через Google\n(нажмите, чтобы выйти)");
                    socialBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0);
                    socialBtn.setBackgroundResource(R.drawable.custom_google_button);
                    socialBtn.setTextColor(getResources().getColor(R.color.black));
                }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        finish();
    }




}