package ru.stacymay.wordsstartingwithlettergame;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameResultsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    TextView userText2;
    ImageView userPhoto1, userPhoto2;
    ListView user1List, user2List;
    ArrayList<Word> wordsArrayList1 = new ArrayList<>(), wordsArrayList2 = new ArrayList<>();
    ResultWordsListAdapter wordsAdapter;
    DatabaseReference myRef;
    String currUserId;
    String gameId;
    Button goToMain, goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_results);

        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        currUserId = mUser.getUid().substring(0,12);

        goBack = findViewById(R.id.goBack);
        goToMain = findViewById(R.id.mainPage);
        goBack.setOnClickListener(v-> onBackPressed());
        goToMain.setOnClickListener(v-> onBackPressed());

        Intent intent = getIntent();
        if(intent.hasExtra("gameId")){
            gameId = intent.getStringExtra("gameId");
        }

        userPhoto1 = findViewById(R.id.userPhoto);
        userPhoto2 = findViewById(R.id.userPhoto2);

        userText2 = findViewById(R.id.userName2);

        user1List = findViewById(R.id.user1List);
        user2List = findViewById(R.id.user2List);

        getUserWords();
        getFriendsWords();

    }

    private void getFriendsWords() {

        myRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot != null) {
                    int numOfPlayers = (int) snapshot.child("games").child(gameId).child("players").getChildrenCount();
                    switch (numOfPlayers){
                        case 2:
                                for (DataSnapshot childS : snapshot.child("games").child(gameId).child("players").getChildren()) {
                                    if(!childS.getKey().equals(currUserId)){
                                        String name = snapshot.child("users").child(childS.getKey()).child("name").getValue().toString();
                                        String photoUrl = snapshot.child("users").child(childS.getKey()).child("photoUrl").getValue().toString();
                                        userText2.setText(name);
                                        Glide.with(GameResultsActivity.this).load(photoUrl).into(userPhoto2);
                                        for(DataSnapshot childW: snapshot.child("games").child(gameId).child("players").child(childS.getKey()).child("words").getChildren()){
                                            String category = childW.getKey();
                                            String word = childW.getValue().toString();
                                            boolean correct = false;
                                            if(snapshot.child("words").child(category).getValue().toString().toLowerCase().contains(word.toLowerCase()+",")){
                                                correct = word.length() > 1;
                                            }
                                            wordsArrayList2.add(new Word(category, word, correct));
                                        }
                                    }
                                }
                            break;
                        case 3:
                            RelativeLayout user3Layout = findViewById(R.id.userResultsRL3);
                            user3Layout.setVisibility(View.VISIBLE);

                            String names[] = new String[2];
                            for (DataSnapshot childS : snapshot.child("games").child(gameId).child("players").getChildren()) {
                                if(!childS.getKey().equals(currUserId)){
                                    String name = snapshot.child("users").child(childS.getKey()).child("name").getValue().toString();
                                    String photoUrl = snapshot.child("users").child(childS.getKey()).child("photoUrl").getValue().toString();
                                    userText2.setText(name);
                                    Glide.with(GameResultsActivity.this).load(photoUrl).into(userPhoto2);
                                    for(DataSnapshot childW: snapshot.child("games").child(gameId).child("players").child(childS.getKey()).child("words").getChildren()){
                                        String category = childW.getKey();
                                        String word = childW.getValue().toString();
                                        boolean correct = false;
                                        if(snapshot.child("words").child(category).getValue().toString().toLowerCase().contains(word.toLowerCase()+",")){
                                            correct = word.length() > 1;
                                        }
                                        wordsArrayList2.add(new Word(category, word, correct));
                                    }
                                }
                            }
                            break;
                    }
                    wordsAdapter = new ResultWordsListAdapter(this, wordsArrayList2);
                    user2List.setAdapter(wordsAdapter);
                }
            }
        });
    }

    private void getUserWords() {
        myRef.child("users").child(currUserId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null) {
                    String photoUrl = snapshot.child("photoUrl").getValue().toString();
                    Glide.with(getApplicationContext()).load(photoUrl).into(userPhoto1);
                }
            }
        });
        myRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null)
                    for(DataSnapshot childS: snapshot.child("games").child(gameId).child("players").child(currUserId).child("words").getChildren()){
                        String category = childS.getKey();
                        String word = childS.getValue().toString();
                        boolean correct = false;
                        if(snapshot.child("words").child(category).getValue().toString().toLowerCase().contains(word.toLowerCase()+",")){
                            correct = word.length() > 1;
                        }
                        wordsArrayList1.add(new Word(category, word, correct));
                    }
                wordsAdapter = new ResultWordsListAdapter(this, wordsArrayList1);
                user1List.setAdapter(wordsAdapter);
            }
        });
    }

    @Override
    public void onBackPressed() {
        myRef.child("games").child(gameId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                boolean allPlayed = true;
                if (snapshot!=null)
                    for(DataSnapshot childS: snapshot.child("players").getChildren()){
                        if(childS.child("played").getValue().toString().equals("false")){
                            allPlayed = false;
                        }
                    }
                if(allPlayed){
                    myRef.child("games").child(gameId).child("finished").setValue("true");
                }
            }
        });
        Intent intent = new Intent(GameResultsActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        finish();
    }
}