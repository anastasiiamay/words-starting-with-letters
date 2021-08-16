package ru.stacymay.wordsstartingwithlettergame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;

import java.util.ArrayList;
import java.util.Date;

public class GameInvitationListAdapter extends ArrayAdapter<Game> {
    Context context;
    ArrayList<Game> gamesList;
    String mode;

    GameInvitationListAdapter(Context cont, ArrayList<Game> games, String mode) {
        super(cont, 0, games);
        context = cont;
        gamesList = games;
        this.mode = mode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.game_item, parent, false);
        }

        Game game = gamesList.get(position);
        String organizerPlayerName = game.getOrganizerName();

        TextView name = view.findViewById(R.id.userName);
        ImageView photo = view.findViewById(R.id.userPhoto);
        Button playBtn = view.findViewById(R.id.play);

        Glide.with(context).load(game.getOrganizerPhotoUrl()).into(photo);

        if(mode.equals("invite")) {
            name.setText(organizerPlayerName + " приглашает Вас на игру");
            playBtn.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Сделать ход");
                builder.setMessage(organizerPlayerName + " уже сгенерировал(а) букву и сделал(а) свой ход. \n\nКогда вы нажмете \"Начать игру\", у Вас будет 1 минута для того, чтобы придумать и записать по 1 слову на эту букву для 5-ти категорий.");
                builder.setNegativeButton("Позже", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.setPositiveButton("Начать игру", (dialog, which) -> {
                    Intent intent = new Intent(context, GameActivity.class);
                    intent.putExtra("gameId", game.getId());
                    ((Activity) context).startActivity(intent);
                    ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                });
                builder.create();
                builder.show();
            });
        } else if (mode.equals("waitFriend")) {
            name.setText("Сейчас ход соперника(-ов)");
            playBtn.setOnClickListener(v->{
                Intent intent = new Intent(context, GameResultsActivity.class);
                intent.putExtra("gameId", game.getId());
                ((Activity) context).startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        } else {
            name.setText("Игра завершена!\nНажмите, чтобы посмотреть результаты");
            playBtn.setOnClickListener(v->{
                Intent intent = new Intent(context, GameResultsActivity.class);
                intent.putExtra("gameId", game.getId());
                ((Activity) context).startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        return view;
    }

}