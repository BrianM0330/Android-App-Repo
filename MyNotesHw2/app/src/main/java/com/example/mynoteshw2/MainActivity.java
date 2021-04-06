package com.example.mynoteshw2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonWriter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private List<Note> noteList = new ArrayList<>(); //Notes stored here
    private Note toEdit = new Note();

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try { loadNoteFromJSON(); }
        catch (IOException | JSONException e) { e.printStackTrace(); }
        sortList();

        recyclerView = findViewById(R.id.recycler);
        noteAdapter = new NoteAdapter(noteList, this);
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNoteToJSON();
    }

    private void sortList() {
        Collections.sort(noteList, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                int toReturn = 0;
                try {
                    if (o1.getFormattedDate().before(o2.getFormattedDate()))
                        toReturn = 1;
                    else if (o1.getFormattedDate().after(o2.getFormattedDate()))
                        toReturn = -1;
                    else
                        toReturn = 0;
                } catch (ParseException e) { e.printStackTrace(); }
                return toReturn;
            }
        });
    }

    private void loadNoteFromJSON() throws IOException, JSONException {
        InputStream is = getApplicationContext().openFileInput(getString(R.string.file_name));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null)
            sb.append(line);
        JSONArray jsonarray = new JSONArray(sb.toString());

        for (int i = 0; i < jsonarray.length(); i++) { //add notes from jsonArray to noteList
            Note toAdd = new Note();
            toAdd.setTitle(jsonarray.getJSONObject(i).getString("title"));
            toAdd.setNoteContent(jsonarray.getJSONObject(i).getString("noteContent"));
            toAdd.setDateCreated(jsonarray.getJSONObject(i).getString("dateCreated"));
            noteList.add(toAdd);
        }
        setTitle("My Notes " + "(" + (noteList.size()) + ")");
    }

    private void saveNoteToJSON() {
        try {
            FileOutputStream fos =
                    getApplicationContext().openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);
            OutputStreamWriter outWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            JsonWriter writer = new JsonWriter(outWriter);
            writer.setIndent("  ");
            writer.beginArray();

            for (int i=0; i < noteList.size(); i++) {
                writer.beginObject();
                writer.name("title").value(noteList.get(i).getTitle());
                writer.name("noteContent").value(noteList.get(i).getNoteContent());
                writer.name("dateCreated").value(noteList.get(i).getNoteDate());
                writer.endObject();
            }
            writer.endArray();
            writer.close();
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void deleteNoteFromJSON(Note toDelete) throws JSONException {
        for (int i=0; i < noteList.size(); i++) { //find and delete from noteList
            Note curr = noteList.get(i);
            if (curr.getTitle().equals(toDelete.getTitle())) {
                if (curr.getNoteContent().equals(toDelete.getNoteContent())) {
                    noteList.remove(noteList.get(i));
                    noteAdapter.notifyDataSetChanged();
                    setTitle("My Notes " + "(" + (noteList.size()) + ")");
                    break;
                }
            }
        }

        try {
            FileOutputStream fos =
                    getApplicationContext().openFileOutput(getString(R.string.file_name), Context.MODE_PRIVATE);
            OutputStreamWriter outWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
            JsonWriter writer = new JsonWriter(outWriter);

            writer.setIndent("  ");
            writer.beginArray();
            for (int i=0; i < noteList.size(); i++) {
                Note curr = noteList.get(i);
                writer.beginObject();
                writer.name("title").value(curr.getTitle());
                writer.name("noteContent").value(curr.getNoteContent());
                writer.name("dateCreated").value(curr.getNoteDate());
                writer.endObject();
            }
            writer.endArray();
            writer.close();
        }
        catch (Exception e)  { e.printStackTrace(); }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createNoteButton:
                Intent noteCreationIntent = new Intent(MainActivity.this, noteCreationActivity.class);
                startActivityForResult(noteCreationIntent, 1);
                return true;
            case R.id.aboutButton:
                Intent aboutActivity = new Intent(MainActivity.this, aboutActivity.class);
                startActivity(aboutActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        toEdit = noteList.get(pos); //set class var toEdit as the note that was clicked

        Intent noteEditIntent = new Intent(MainActivity.this, noteCreationActivity.class);
        noteEditIntent.putExtra("edit_title", toEdit.getTitle());
        noteEditIntent.putExtra("edit_content", toEdit.getNoteContent());
        startActivityForResult(noteEditIntent, 2);
    }

    @Override
    public boolean onLongClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        final Note toDelete = noteList.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    deleteNoteFromJSON(toDelete);
                } catch (JSONException e) { e.printStackTrace(); }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //doNothing
            }
        });

        builder.setMessage("Delete Note " + "'" + toDelete.getTitle() + "'?");
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) { //createdNote
            if (resultCode == RESULT_OK) {
                String title = data.getStringExtra("title_input");
                String noteContent = data.getStringExtra("note_content");
                Note n = new Note(title, noteContent);
                noteList.add(n);
                setTitle("My Notes " + "(" + (noteList.size()) + ")");
                sortList();
                noteAdapter.notifyDataSetChanged();
            }
        }

        if (requestCode == 2) { //editedNote
            if (resultCode == RESULT_OK) {
                String newTitle = data.getStringExtra("title_input");
                String newNoteContent = data.getStringExtra("note_content");
                for (int i = 0; i < noteList.size(); i++) {
                    Note n = noteList.get(i);
                    if (n.getTitle().equals(toEdit.getTitle())) {
                        toEdit.setTitle(newTitle);
                        toEdit.setNoteContent(newNoteContent);
                        toEdit.setDateCreated(new Date().toString()); //set date to last edit time
                        noteList.set(i, toEdit);
                        sortList();
                        noteAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}