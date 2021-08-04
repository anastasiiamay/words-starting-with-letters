package ru.stacymay.wordsstartingwithlettergame;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    EditText et1, et2, et3, et4, et5;
    TextView c1, c2, c3, c4, c5;
    TextView timeLeft;
    TextView firstLetter;
    Button readyBtn;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference myRef;
    String gameId, userId;
    int playerTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid().substring(0,12);

        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();
        timeLeft = findViewById(R.id.timeLeft);
        firstLetter = findViewById(R.id.letter);

        Intent intent = getIntent();
        if(intent.hasExtra("gameId")){
            gameId = intent.getStringExtra("gameId");
        }

        myRef.child("games").child(gameId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null) {
                    String letter = snapshot.child("letter").getValue().toString();
                    firstLetter.setText("Буква: "+letter);
                    ArrayList<String> readyCategoriesList = new ArrayList<>();
                    for(DataSnapshot childS: snapshot.child("players").child(gameId.substring(13)).child("words").getChildren()){
                        String category = childS.getKey();
                        readyCategoriesList.add(category);
                    }
                    if(readyCategoriesList.size()==5) {
                        c1.setText(readyCategoriesList.get(0));
                        c2.setText(readyCategoriesList.get(1));
                        c3.setText(readyCategoriesList.get(2));
                        c4.setText(readyCategoriesList.get(3));
                        c5.setText(readyCategoriesList.get(4));
                    }
                }
            }
        });

        et1 = findViewById(R.id.et1);
        et2 = findViewById(R.id.et2);
        et3 = findViewById(R.id.et3);
        et4 = findViewById(R.id.et4);
        et5 = findViewById(R.id.et5);

        c1 = findViewById(R.id.category1);
        c2 = findViewById(R.id.category2);
        c3 = findViewById(R.id.category3);
        c4 = findViewById(R.id.category4);
        c5 = findViewById(R.id.category5);

        readyBtn = findViewById(R.id.readyBtn);
        readyBtn.setOnClickListener(v->{
            saveAllResults();
        });

        if(userId.equals(gameId.substring(13))) generateRandomCategories();
        startCounting();
    }

    private void saveAllResults() {
        handler.removeCallbacks(run);
        myRef.child("users").child(userId).child("games").child(gameId).child("played").setValue(true);
        myRef.child("games").child(gameId).child("players").child(userId).child("played").setValue(true);
        myRef.child("games").child(gameId).child("players").child(userId).child("time").setValue(playerTime);
        myRef.child("games").child(gameId).child("players").child(userId).child("words")
                .child(c1.getText().toString()).setValue(et1.getText().toString());
        myRef.child("games").child(gameId).child("players").child(userId).child("words")
                .child(c2.getText().toString()).setValue(et2.getText().toString());
        myRef.child("games").child(gameId).child("players").child(userId).child("words")
                .child(c3.getText().toString()).setValue(et3.getText().toString());
        myRef.child("games").child(gameId).child("players").child(userId).child("words")
                .child(c4.getText().toString()).setValue(et4.getText().toString());
        myRef.child("games").child(gameId).child("players").child(userId).child("words")
                .child(c5.getText().toString()).setValue(et5.getText().toString());
        Intent intent1 = new Intent(GameActivity.this, GameResultsActivity.class);
        intent1.putExtra("gameId", gameId);
        startActivity(intent1);
        finish();
    }

    private void generateRandomCategories() {
        ArrayList<String> categoriesList = new ArrayList<>();
        ArrayList<String> randomCategoriesList = new ArrayList<>();

        myRef.child("words").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null) {
                    for(DataSnapshot childS: snapshot.getChildren()){
                        String category = childS.getKey();
                        categoriesList.add(category);
                    }
                    while(randomCategoriesList.size()!=5) {
                        Random random = new Random();
                        int pos = random.nextInt(categoriesList.size());
                        if (!randomCategoriesList.contains(categoriesList.get(pos)))
                            randomCategoriesList.add(categoriesList.get(pos));
                    }

                    c1.setText(randomCategoriesList.get(0));
                    c2.setText(randomCategoriesList.get(1));
                    c3.setText(randomCategoriesList.get(2));
                    c4.setText(randomCategoriesList.get(3));
                    c5.setText(randomCategoriesList.get(4));
                }
            }
        });
    }

    private void startCounting() {
        handler.post(run);
    }

    private final Runnable run = new Runnable() {
        int i=60;
        @Override
        public void run() {
            if(i>0) {
                handler.postDelayed(this, 1000);
                timeLeft.setText("Осталось " + i +" сек.");
                i--;
                playerTime++;
            } else {
                Toast.makeText(GameActivity.this, "Время вышло!", Toast.LENGTH_SHORT).show();
                saveAllResults();
            }
        }
    };

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Сейчас Вы не можете выйти. Сначала закончите игру", Toast.LENGTH_SHORT).show();
    }
}