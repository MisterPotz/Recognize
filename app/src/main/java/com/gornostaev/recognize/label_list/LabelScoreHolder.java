package com.gornostaev.recognize.label_list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.gornostaev.recognize.R;

public class LabelScoreHolder extends RecyclerView.ViewHolder {
    private TextView label;
    private TextView score;

    public LabelScoreHolder(@NonNull View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.labels);
        score = itemView.findViewById(R.id.scores);
    }

    //выставить значения
    public void setDetails(String label, double score) {
        this.label.setText(label);
        this.score.setText(Double.toString(score));
    }
}
