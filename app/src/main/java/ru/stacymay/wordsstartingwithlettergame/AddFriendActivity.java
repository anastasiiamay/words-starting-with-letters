package ru.stacymay.wordsstartingwithlettergame;

import android.content.Intent;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddFriendActivity extends AppCompatActivity {

    Button goBackBtn, findFriend, addFriend;
    DatabaseReference myRef;
    EditText userIdET;
    RelativeLayout friendRel;
    FirebaseAuth mAuth;
    String currUserId;
    String userToFindId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();

        mAuth = FirebaseAuth.getInstance();
        currUserId = mAuth.getCurrentUser().getUid().substring(0, 12);

        goBackBtn = findViewById(R.id.goBack);
        goBackBtn.setOnClickListener(v -> onBackPressed());

        userIdET = findViewById(R.id.editId);

        friendRel = findViewById(R.id.friendRel);

        findFriend = findViewById(R.id.findFriend);
        findFriend.setOnClickListener(v -> {
            userToFindId= userIdET.getText().toString();
            if(userToFindId.length()!=12){
                Toast.makeText(this, "Пожалуйста, проверьте количество символов в ID", Toast.LENGTH_LONG).show();
                friendRel.setVisibility(View.INVISIBLE);
            } else if(userToFindId.equals(currUserId)){
                Toast.makeText(this, "Это Ваш собственный ID", Toast.LENGTH_LONG).show();
                friendRel.setVisibility(View.INVISIBLE);
            }
            else {
                myRef.child("users").get().addOnCompleteListener(task->{
                    if (task.isSuccessful()) {
                        DataSnapshot snapshot = task.getResult();
                        if(snapshot!=null) {
                            if (snapshot.hasChild(userToFindId)) {
                                if(snapshot.child(userToFindId).child("friends").hasChild(currUserId)){
                                    Toast.makeText(this, "Пользователь с таким ID уже добавлен в друзья", Toast.LENGTH_SHORT).show();
                                } else {
                                    friendRel.setVisibility(View.VISIBLE);
                                    String nameS = snapshot.child(userToFindId).child("name").getValue().toString();
                                    TextView name = findViewById(R.id.userName);
                                    name.setText(nameS);
                                    TextView id = findViewById(R.id.userId);
                                    id.setText(userToFindId);
                                    ImageView photo = findViewById(R.id.userPhoto);
                                    Glide.with(AddFriendActivity.this).load(snapshot.child(userToFindId).child("photoUrl").getValue().toString()).into(photo);
                                }

                            } else {
                                Toast.makeText(this, "Пользователя с таким ID не существует", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });

        addFriend = findViewById(R.id.addFriend);
        addFriend.setOnClickListener(v->{
            myRef.child("users").child(userToFindId).child("friends").child(currUserId).setValue("friend");
            myRef.child("users").child(currUserId).child("friends").child(userToFindId).setValue("friend");
            Toast.makeText(this, "Пользователь успешно добавлен в друзья. Теперь Вы можете играть вместе!", Toast.LENGTH_SHORT).show();
            friendRel.setVisibility(View.INVISIBLE);
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddFriendActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

}