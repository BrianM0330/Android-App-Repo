package com.example.mynoteshw2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<noteViewHolder> {

    private static final String TAG = "NoteAdapter";
    private List<Note> noteList;
    private MainActivity mainAct;

    NoteAdapter(List<Note> noteList, MainActivity ma) {
        this.noteList = noteList;
        mainAct = ma;
    }

    @NonNull
    @Override
    public noteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: MAKING NEW VIEWHOLDER");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_layout, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new noteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull noteViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: FILLING VIEW FOLDER NOTE " + position);

        Note note = noteList.get(position);

        holder.title.setText(note.getTitle());
        holder.noteContent.setText(note.getNoteContent());
        holder.dateCreated.setText(note.getNoteDate());
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }
}
