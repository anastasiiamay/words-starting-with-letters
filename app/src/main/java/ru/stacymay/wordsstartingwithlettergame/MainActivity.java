package ru.stacymay.wordsstartingwithlettergame;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ImageView userPhoto;
    TextView userName, goToProfile;
    Button newGameBtn;
    TextView emptyMyTurn, emptyFriendTurn;

    DatabaseReference myRef;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseUser mUser;

    public static final String APP_PREFERENCES = "mySettings";
    SharedPreferences mSettings;
    DBGamesHelper dbGamesHelper;

    FullLengthListView userTurnList, friendTurnListV, finishedGamesListView;
    ArrayList<Game> gamesArrayList = new ArrayList<>(), friendTurnArrayList = new ArrayList<>(), finishedGamesArrayList = new ArrayList<>();
    GameInvitationListAdapter gamesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация пользователя
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        // Инициализация SharedPrefs и локальной базы
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        dbGamesHelper = new DBGamesHelper(MainActivity.this);

        // Поиск виджетов
        userPhoto = findViewById(R.id.userPhoto);
        userName = findViewById(R.id.userName);
        goToProfile = findViewById(R.id.goToProfile);

        newGameBtn = findViewById(R.id.newGameBtn);

        emptyMyTurn = findViewById(R.id.emptyUserTurnText);
        emptyFriendTurn = findViewById(R.id.emptyFriendTurnText);

        userTurnList = findViewById(R.id.userTurnList);
        friendTurnListV = findViewById(R.id.friendTurnList);
        finishedGamesListView = findViewById(R.id.endedGamesList);

        // Подключение профиля Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Подключение к базе Firebase Database
        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();

        // Кнопка "Начать новую игру"
        newGameBtn.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, NewGameSettingsActivity.class);
            startActivity(intent);
            finish();
        });

        // "Кнопка" "Перейти в свой профиль"
        goToProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            startActivity(intent);
            finish();
        });

        // При изменении имени в Firebase сразу отобразить его в приложении и записать в SharedPrefs
        myRef.child("users").child(mUser.getUid().substring(0,12)).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = Objects.requireNonNull(snapshot.getValue()).toString();
                userName.setText(name);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putString("name", name);
                editor.apply();
                Toast.makeText(MainActivity.this, "Имя изменено", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DatabaseError", error.toString());
            }
        });

        // Загрузка фотографии профиля
        Glide.with(this).load(mUser.getPhotoUrl()).into(userPhoto);

        // Когда списки пустые, отображать другие виджеты
        userTurnList.setEmptyView(emptyMyTurn);
        friendTurnListV.setEmptyView(emptyFriendTurn);

        showOfflineGamesData();

        findFriendsGames();
        findFinishedGames();
        // Обновить списки "Мой ход", "Ход соперника" и "Завершенные игры", если в Firebase новые данные
        myRef.child("users").child(mUser.getUid().substring(0,12)).child("games").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                findUserGames();
                Toast.makeText(MainActivity.this, "Игры изменены", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //myRef.child("games").child()

    }

    private void showOfflineGamesData() {
        Toast.makeText(this, "Показаны игры из локальной БД", Toast.LENGTH_SHORT).show();
        SQLiteDatabase database = dbGamesHelper.getReadableDatabase();
        Cursor cursor = database.query(DBGamesHelper.TABLE_GAMES,null,null,null,null,null,null);

        ArrayList<Game> listItem = new ArrayList<>();
        if(cursor.getCount()>0)
        while (cursor.moveToNext()){
            int idid = cursor.getColumnIndex(DBGamesHelper.KEY_GAME_ID);
            int nameId = cursor.getColumnIndex(DBGamesHelper.KEY_ORG_NAME);
            int photoUrlId = cursor.getColumnIndex(DBGamesHelper.KEY_ORG_PHOTO_URL);
            listItem.add(new Game(cursor.getString(idid), cursor.getString(nameId), cursor.getString(photoUrlId)));
        }
        cursor.close();

        gamesAdapter = new GameInvitationListAdapter(this, listItem, "invite");
        userTurnList.setAdapter(gamesAdapter);

// **************** WRITE DATA TO DB ******************
//        SQLiteDatabase database = dbHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(DBHelper.KEY_WEIGHT,Integer.valueOf(tvWeight.getText().toString()));
//        values.put(DBHelper.KEY_DAY,day);
//        values.put(DBHelper.KEY_MONTH,month);
//        values.put(DBHelper.KEY_YEAR,year);
//        database.insert(DBHelper.TABLE_WEIGHT, null, values);
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
                            if(snapshot.child("games").hasChild(gameId))
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
        Toast.makeText(this, "Показаны игры из Firebase, записываются в локальную БД", Toast.LENGTH_SHORT).show();
        SQLiteDatabase database = dbGamesHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
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

                                values.put(DBGamesHelper.KEY_GAME_ID,childS.getKey());
                                values.put(DBGamesHelper.KEY_ORG_NAME,orgName);
                                values.put(DBGamesHelper.KEY_ORG_PHOTO_URL,orgPhoto);
                                database.insert(DBGamesHelper.TABLE_GAMES, null, values);
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