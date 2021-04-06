package com.example.mynoteshw2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class noteCreationActivity extends AppCompatActivity {

    private EditText titleInput;
    private EditText noteContentInput;
    private String titleOnStart = "";
    private String contentOnStart = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.createnotelayout);
        titleInput = findViewById(R.id.editTitle);
        noteContentInput = findViewById(R.id.editNoteContent);

        Intent intentReceived = getIntent();
        if (intentReceived.hasExtra("edit_title")) {
            String titleReceived = intentReceived.getStringExtra("edit_title");
            String contentReceived = intentReceived.getStringExtra("edit_content");
            titleInput.setText(titleReceived);
            noteContentInput.setText(contentReceived);
        }
        setTitle("New Note");
        titleOnStart = titleInput.getText().toString();
        contentOnStart = noteContentInput.getText().toString();
    }

    @Override
    public void onBackPressed() {
        /*was getting weird results if i had titleInput.getText.toString inside if condition
         so I declared the strings explicitly...
        */
        String titleString = titleInput.getText().toString();
        String contentString = noteContentInput.getText().toString();
        if ( (titleOnStart.equals(titleString)) && (contentOnStart.equals(contentString)))
            finish();
        else {
            final Intent data = new Intent();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (titleInput.getText().toString().length() > 0) {
                        data.putExtra("title_input", titleInput.getText().toString());
                        data.putExtra("note_content", noteContentInput.getText().toString());
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "No title was entered. Note was not saved.", Toast.LENGTH_SHORT);
                        toast.show();
                        finish();
                    }
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setMessage("There are unsaved changes!\nDo you want to save before exiting?");
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.createnotemenu, menu);
        return true;
    }

    public void noteSaved() {
        if (titleInput.getText().toString().length() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "No title was entered. Note was not saved.", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        else if (titleOnStart.equals(titleInput.getText().toString()) && contentOnStart.equals(noteContentInput.getText().toString()))
            finish();

        else { //only save if changes made + title > 0
            Intent data = new Intent();
            data.putExtra("title_input", titleInput.getText().toString());
            data.putExtra("note_content", noteContentInput.getText().toString());
            setResult(RESULT_OK, data);
            finish(); //close noteCreation
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.saveNote) {
            noteSaved();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
