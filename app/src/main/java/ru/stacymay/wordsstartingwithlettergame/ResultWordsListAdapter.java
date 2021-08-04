package ru.stacymay.wordsstartingwithlettergame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class ResultWordsListAdapter extends ArrayAdapter<Word> {
    Context context;
    ArrayList<Word> wordsList;

    ResultWordsListAdapter(Context cont, ArrayList<Word> words) {
        super(cont, 0, words);
        context = cont;
        wordsList = words;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.word_item, parent, false);
        }

        Word word = wordsList.get(position);

        TextView category = view.findViewById(R.id.category);
        TextView wordTv = view.findViewById(R.id.word);
        TextView star = view.findViewById(R.id.star);

        category.setText(word.getCategory());
        wordTv.setText(word.getWord());

        if(word.isCorrect()){
            star.setTextColor(ContextCompat.getColor(context, R.color.golden));
        } else if(!word.isCorrect()) {
            star.setTextColor(ContextCompat.getColor(context, R.color.isabelline));
        }

        return view;
    }

}