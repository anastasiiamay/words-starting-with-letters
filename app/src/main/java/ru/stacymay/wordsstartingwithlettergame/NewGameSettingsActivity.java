package ru.stacymay.wordsstartingwithlettergame;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class NewGameSettingsActivity extends AppCompatActivity {

    Button goBackBtn, startGame;
    Button players2, players3, players4;
    public static int numOfPlayers;
    ArrayList<String> chosenPlayers = new ArrayList<>();;
    ArrayList<Friend> friendArrayList = new ArrayList<>();
    FriendListAdapter friendAdapter;
    ListView lvFriends;
    DatabaseReference myRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game_settings);

        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        goBackBtn = findViewById(R.id.goBack);
        goBackBtn.setOnClickListener(v-> onBackPressed());
        lvFriends = findViewById(R.id.friendsList);

        startGame = findViewById(R.id.startGame);

        players2 = findViewById(R.id.btn2);
        players3 = findViewById(R.id.btn3);
        players4 = findViewById(R.id.btn4);

        players2.setBackgroundResource(R.drawable.custom_button);
        numOfPlayers = 2;

        View.OnClickListener numOfPlayersListener = v -> {
            if(v.getId() == R.id.btn2){
                players2.setBackgroundResource(R.drawable.custom_button);
                players3.setBackgroundResource(R.drawable.custom_google_button);
                players4.setBackgroundResource(R.drawable.custom_google_button);
                numOfPlayers = 2;
            } else if(v.getId() == R.id.btn3){
                players3.setBackgroundResource(R.drawable.custom_button);
                players2.setBackgroundResource(R.drawable.custom_google_button);
                players4.setBackgroundResource(R.drawable.custom_google_button);
                numOfPlayers = 3;
            } else if(v.getId() == R.id.btn4){
                players4.setBackgroundResource(R.drawable.custom_button);
                players2.setBackgroundResource(R.drawable.custom_google_button);
                players3.setBackgroundResource(R.drawable.custom_google_button);
                numOfPlayers = 4;
            }
        };

        players2.setOnClickListener(numOfPlayersListener);
        players3.setOnClickListener(numOfPlayersListener);
        players4.setOnClickListener(numOfPlayersListener);

        findFriends();

        startGame.setOnClickListener(v->{
            chosenPlayers.clear();
            for(Friend friend: friendArrayList){
                if(friend.isChosen()) chosenPlayers.add(friend.getId());
            }
            if(numOfPlayers-1!=chosenPlayers.size()){
                Toast.makeText(this, "Количество игроков не совпадает с выбранным", Toast.LENGTH_SHORT).show();
            } else{
                Intent intent = new Intent(NewGameSettingsActivity.this, GenerateLetterActivity.class);
                intent.putStringArrayListExtra("players", chosenPlayers);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                //finish();
            }
        });

    }

    private void findFriends() {
        myRef.child("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null)
                    for(DataSnapshot childS: snapshot.child(mUser.getUid().substring(0,12)).child("friends").getChildren()){
                        String friendName = snapshot.child(childS.getKey()).child("name").getValue().toString();
                        String friendPhoto = snapshot.child(childS.getKey()).child("photoUrl").getValue().toString();
                        boolean isOnline = Boolean.parseBoolean(snapshot.child(childS.getKey()).child("isOnline").getValue().toString());
                        friendArrayList.add(new Friend(friendName,childS.getKey(),friendPhoto,isOnline));
                    }
                friendAdapter = new FriendListAdapter(this, friendArrayList,true);
                lvFriends.setAdapter(friendAdapter);
            }
        });
    }


}