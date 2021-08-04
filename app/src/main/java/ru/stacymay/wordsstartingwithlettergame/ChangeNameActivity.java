package ru.stacymay.wordsstartingwithlettergame;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeNameActivity extends AppCompatActivity {

    Button goBackBtn, saveNameBtn;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference myRef;
    EditText editNameET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        goBackBtn = findViewById(R.id.goBack);
        goBackBtn.setOnClickListener(v-> onBackPressed());

        saveNameBtn = findViewById(R.id.changeName);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        FirebaseDatabase db = FirebaseDatabase.getInstance(getString(R.string.firebase_db_path));
        myRef = db.getReference();

        editNameET = findViewById(R.id.editName);
        myRef.child("users").child(mUser.getUid().substring(0,12)).child("name").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot!=null) {
                    String name = snapshot.getValue().toString();
                    editNameET.setText(name);
                }
            }
        });

        saveNameBtn.setOnClickListener(v->{
            if(editNameET.getText().toString().length()>0){
                myRef.child("users").child(mUser.getUid().substring(0,12)).child("name").setValue(editNameET.getText().toString());
                Toast.makeText(this, "Имя успешно изменено!", Toast.LENGTH_SHORT).show();
                onBackPressed();
            } else {
                Toast.makeText(this, "Вы ничего не ввели", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChangeNameActivity.this, ProfileActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
        finish();
    }


}