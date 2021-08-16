package ru.stacymay.wordsstartingwithlettergame;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateLetterActivity extends AppCompatActivity {

    TextView randomLetter;
    String[] letters = new String[]{"А","Б","В","Г","Д","Е","Ж","З","И","К","Л","М","Н","О","П","Р","С","Т","У","Ф","Х","Ц","Ч","Ш","Э","Ю","Я"};
    Button generateLetter, startGameBtn;
    Button goBackBtn;
    private final Handler handler = new Handler();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference myRef;
    ArrayList<String> playersId;
    String currUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_letter);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();
        currUserId = mUser.getUid().substring(0,12);

        goBackBtn = findViewById(R.id.goBack);
        goBackBtn.setOnClickListener(v-> onBackPressed());

        Intent intent = getIntent();
        if(intent.hasExtra("players")){
            playersId = new ArrayList<>(intent.getStringArrayListExtra("players"));
        }

        randomLetter = findViewById(R.id.randomLetter);
        startCounting();

        generateLetter = findViewById(R.id.generateLetter);
        generateLetter.setOnClickListener(v->{
            handler.removeCallbacksAndMessages(null);
            generateLetter.setText("Буква сгенерирована!");
            generateLetter.setBackgroundResource(R.drawable.custom_grey_button);
            generateLetter.setTextColor(getColor(R.color.white));
        });

        startGameBtn = findViewById(R.id.startGame);
        startGameBtn.setOnClickListener(v->{

            long currTime = System.currentTimeMillis();
            myRef.child("games").child(currTime+""+currUserId).child("timeStart").setValue(currTime);
            myRef.child("games").child(currTime+""+currUserId).child("letter").setValue(randomLetter.getText().toString());
            myRef.child("games").child(currTime+""+currUserId).child("finished").setValue(false);
            myRef.child("games").child(currTime+""+currUserId).child("players").child(currUserId).child("played").setValue(false);
            myRef.child("users").child(currUserId).child("games").child(currTime + "" + currUserId).child("played").setValue(false);

            for (String s : playersId) {
                myRef.child("games").child(currTime + "" + currUserId).child("players").child(s).child("played").setValue(false);
                myRef.child("users").child(s).child("games").child(currTime + "" + currUserId).child("played").setValue(false);
            }

            Intent intent1 = new Intent(GenerateLetterActivity.this, GameActivity.class);
            intent1.putExtra("gameId",currTime+""+mUser.getUid().substring(0,12));
            startActivity(intent1);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            finish();
        });
    }

    private void startCounting() {
        handler.post(run);
    }

    private final Runnable run = new Runnable() {
        int i=0;
        @Override
        public void run() {
            if(i<letters.length-1){
                i++;
            } else {
                i=0;
            }
            int randomNum = ThreadLocalRandom.current().nextInt(0,letters.length);
            randomLetter.setText(letters[randomNum]);
            handler.postDelayed(this, 30);
        }
    };

}