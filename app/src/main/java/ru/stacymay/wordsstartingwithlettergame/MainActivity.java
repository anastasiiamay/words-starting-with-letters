package ru.stacymay.wordsstartingwithlettergame;

import android.content.Intent;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    ImageView userPhoto;
    TextView userName, goToProfile;
    Button newGameBtn;
    TextView emptyMyTurn, emptyFriendTurn;

    DatabaseReference myRef;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseUser mUser;

    FullLengthListView userTurnList, friendTurnListV, finishedGamesListView;
    ArrayList<Game> gamesArrayList = new ArrayList<>(), friendTurnArrayList = new ArrayList<>(), finishedGamesArrayList = new ArrayList<>();
    GameInvitationListAdapter gamesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();

        newGameBtn = findViewById(R.id.newGameBtn);
        newGameBtn.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, NewGameSettingsActivity.class);
            startActivity(intent);
            finish();
        });

        userPhoto = findViewById(R.id.userPhoto);
        userName = findViewById(R.id.userName);
        goToProfile = findViewById(R.id.goToProfile);

        goToProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            startActivity(intent);
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
            Glide.with(this).load(photoUrl).into(userPhoto);
        }

        emptyMyTurn = findViewById(R.id.emptyUserTurnText);
        emptyFriendTurn = findViewById(R.id.emptyFriendTurnText);

        userTurnList = findViewById(R.id.userTurnList);
        friendTurnListV = findViewById(R.id.friendTurnList);
        finishedGamesListView = findViewById(R.id.endedGamesList);

        userTurnList.setEmptyView(emptyMyTurn);
        friendTurnListV.setEmptyView(emptyFriendTurn);

        findUserGames();
        findFriendsGames();
        findFinishedGames();

    }

    private void findFinishedGames() {
        long timeNow = System.currentTimeMillis();
        myRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null) {
                        for (DataSnapshot childS: snapshot.child("users").child(mUser.getUid().substring(0, 12)).child("games").getChildren()) {
                            String gameId = childS.getKey();
                            long timeStart = Long.parseLong(gameId.substring(0,13));
                            if(timeNow-timeStart<604800000)
                            if(snapshot.child("games").child(gameId).child("finished").getValue().toString().equals("true")){
                                String organizerId = gameId.substring(13);
                                String orgName = snapshot.child("users").child(organizerId).child("name").getValue().toString();
                                String orgPhoto = snapshot.child("users").child(organizerId).child("photoUrl").getValue().toString();
                                finishedGamesArrayList.add(new Game(gameId, orgName, orgPhoto));
                            }
                        }
                        Collections.reverse(finishedGamesArrayList);
                        gamesAdapter = new GameInvitationListAdapter(this, finishedGamesArrayList, "finished");
                        finishedGamesListView.setAdapter(gamesAdapter);
                }
            }
        });
    }

    private void findUserGames() {
        myRef.child("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null) {
                    if(snapshot.child(mUser.getUid().substring(0, 12)).hasChild("games")){
                        for (DataSnapshot childS : snapshot.child(mUser.getUid().substring(0, 12)).child("games").getChildren()) {
                            if (childS.child("played").getValue().toString().equals("false")) {
                                String organizerId = childS.getKey().substring(13);
                                String orgName = snapshot.child(organizerId).child("name").getValue().toString();
                                String orgPhoto =snapshot.child(organizerId).child("photoUrl").getValue().toString();
                                gamesArrayList.add(new Game(childS.getKey(), orgName, orgPhoto));
                            }
                        }
                        gamesAdapter = new GameInvitationListAdapter(this, gamesArrayList, "invite");
                        userTurnList.setAdapter(gamesAdapter);
                    }
                }
            }
        });
    }

    private void findFriendsGames(){
        myRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null) {
                        for (DataSnapshot childS : snapshot.child("games").getChildren()) {
                            if (childS.child("finished").getValue().toString().equals("false")) {
                                if (childS.child("players").hasChild(mUser.getUid().substring(0, 12))) {
                                    if(childS.child("players").child(mUser.getUid().substring(0, 12)).child("played").getValue().toString().equals("true")){
                                        String organizerId = childS.getKey().substring(13);
                                        String orgName = snapshot.child("users").child(organizerId).child("name").getValue().toString();
                                        String orgPhoto =snapshot.child("users").child(organizerId).child("photoUrl").getValue().toString();
                                        friendTurnArrayList.add(new Game(childS.getKey(), orgName, orgPhoto));
                                    }
                                }
                            }
                        }
                        gamesAdapter = new GameInvitationListAdapter(this, friendTurnArrayList, "waitFriend");
                        friendTurnListV.setAdapter(gamesAdapter);

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isOnline(false);
    }

    private void isOnline(boolean b) {
        myRef.child("users").child(mUser.getUid().substring(0,12)).child("isOnline").setValue(b);
    }

    @Override
    public void onResume() {
        super.onResume();
        isOnline(true);
    }

}