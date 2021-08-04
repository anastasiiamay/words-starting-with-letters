package ru.stacymay.wordsstartingwithlettergame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FriendListAdapter extends ArrayAdapter<Friend> {
    Context context;
    ArrayList<Friend> friendList;
    boolean withBtn;
    int numOfChosenFriends = 0;

    FriendListAdapter(Context cont, ArrayList<Friend> friends, boolean withBtn) {
        super(cont, 0, friends);
        context = cont;
        friendList = friends;
        this.withBtn = withBtn;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            if(withBtn)
                view = LayoutInflater.from(context).inflate(R.layout.friend_with_btn_item, parent, false);
            else
                view = LayoutInflater.from(context).inflate(R.layout.friend_item, parent, false);
        }

        Friend friend = friendList.get(position);
        TextView name = view.findViewById(R.id.userName);
        ImageView photo = view.findViewById(R.id.userPhoto);

        name.setText(friend.getName());
        Glide.with(context).load(friend.getPhotoUrl()).into(photo);

        if(withBtn){
            if (friend.isOnline()){
                name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle, 0, 0, 0);
            } else{
                name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            Button choose = view.findViewById(R.id.chooseFriend);
            choose.setOnClickListener(v->{
                int maxChosen = NewGameSettingsActivity.numOfPlayers;
                if(friend.isChosen()){
                    friend.setChosen(false);
                    choose.setBackgroundResource(R.drawable.custom_button);
                    choose.setTextColor(context.getResources().getColor(R.color.black));
                    choose.setText("Выбрать");
                    numOfChosenFriends--;
                } else if(numOfChosenFriends<maxChosen-1){
                    if(!friend.isChosen()){
                        friend.setChosen(true);
                        choose.setBackgroundResource(R.drawable.custom_grey_button);
                        choose.setTextColor(context.getResources().getColor(R.color.white));
                        choose.setText("Игрок выбран");
                        numOfChosenFriends++;
                    }
                } else {
                    Toast.makeText(context, "Вы выбрали игру с "+maxChosen+" игроками. Вы не можете выбрать больше соперников.", Toast.LENGTH_SHORT).show();
                }

            });
        }

        return view;
    }

}