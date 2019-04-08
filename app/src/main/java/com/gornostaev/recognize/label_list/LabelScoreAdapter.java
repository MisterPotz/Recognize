package com.gornostaev.recognize.label_list;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gornostaev.recognize.R;


public class LabelScoreAdapter extends RecyclerView.Adapter<LabelScoreHolder> {

    private ContainerImageLabels containerImageLabels;

    public LabelScoreAdapter(ContainerImageLabels containerImageLabels) {
        this.containerImageLabels = containerImageLabels;
    }

    @NonNull
    @Override
    public LabelScoreHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //стандартное заполнение View
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_label_list,
                viewGroup, false);
        return new LabelScoreHolder(view);
    }

    @Override
    public int getItemCount() {
        return containerImageLabels.getImageLabels().length;
    }

    @Override
    public void onBindViewHolder(@NonNull final LabelScoreHolder holder, int position) {
        //получение ImageLabel по номеру position
        ImageLabel imageLabel = containerImageLabels.getImageLabels()[position];
        //выставлений всей нужной информации
        holder.setDetails(imageLabel.getLabel(), imageLabel.getScore());
    }
}
