package com.example.rewards;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class CreateProfile extends AppCompatActivity {

    private File currentImageFile;
    Intent intentReceived;

    ImageView profilePic;
    EditText usernameField;
    EditText passwordField;
    EditText fnameField;
    EditText lnameField;
    EditText departmentField;
    EditText titleField;
    EditText biographyField;
    TextView textCounter;
    ActionBar bar;

    String API_KEY = "";
    String imageByteData = "";
    String location;

    private final int REQUEST_IMAGE_GALLERY = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        intentReceived = getIntent();
        API_KEY = intentReceived.getStringExtra("API_KEY");
        location = intentReceived.getStringExtra("location");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        profilePic = findViewById(R.id.createProfilePic);
        usernameField = findViewById(R.id.createUsernameField);
        passwordField = findViewById(R.id.createPasswordField);
        fnameField = findViewById(R.id.createFnameField);
        lnameField = findViewById(R.id.createLnameField);
        departmentField = findViewById(R.id.createDepartment);
        titleField = findViewById(R.id.createPosition);
        biographyField = findViewById(R.id.createBiographyField);
        textCounter = findViewById(R.id.createCharCounter);
        setupEditText();

        getSupportActionBar().setTitle("Create Profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.createprofilemenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveProfile:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Save Changes?");
                builder.setIcon(R.drawable.logo);
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postProfileData();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void postProfileData() {
        //TODO: ADD VALIDATE FUNCTION FOR EDIT TEXT FIELDS
        //PASSWORD HAS TO BE MIN 8 CHARS
        CreateProfileAPIRunnable runnable = new CreateProfileAPIRunnable(
                this,
                imageByteData,
                usernameField.getText().toString(),
                passwordField.getText().toString(),
                fnameField.getText().toString(),
                lnameField.getText().toString(),
                departmentField.getText().toString(),
                titleField.getText().toString(),
                biographyField.getText().toString(),
                API_KEY,
                location
        );
        new Thread(runnable).start();
    }

    //ONLY called on successful (201) profile creation. Passes all data to create the activity
    public void successfulProfileCreation(JSONObject response) throws JSONException {
        Intent profileViewIntent = new Intent(this, ViewProfile.class);
        profileViewIntent
                .putExtra("fname",
                String.format("%s %s", response.getString("firstName"), response.getString("lastName")));
        profileViewIntent.putExtra("location", location);
        profileViewIntent.putExtra("uname", usernameField.getText().toString());
        profileViewIntent.putExtra("profilePic", imageByteData);
        profileViewIntent.putExtra("pointsAwarded", "0"); //New Profile will always start with 0
        profileViewIntent.putExtra("department", response.getString("department"));
        profileViewIntent.putExtra("position", response.getString("position"));
        profileViewIntent.putExtra("story", response.getString("story"));
        profileViewIntent.putExtra("pointsToAward", response.getInt("remainingPointsToAward"));
        profileViewIntent.putExtra("password", response.getString("password"));
        profileViewIntent.putExtra("API_KEY", API_KEY);

        finish(); //Close the create profile activity
        startActivity(profileViewIntent);
    }

    public void catchHTMLCodes(int code) {
        if (code == HttpURLConnection.HTTP_CONFLICT) {
            Toast.makeText(this, "This username already exists. Please try another", Toast.LENGTH_SHORT).show();
        }
        else if (code == HttpURLConnection.HTTP_BAD_REQUEST) {
            Toast.makeText(this, "One or more fields are empty.", Toast.LENGTH_SHORT).show();
        }
    }

    //<------------------------- PHOTO STUFF -------------------------------------->

    public void createPictureDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Take picture from: ");
        builder.setTitle("Profile Picture");
        builder.setIcon(R.drawable.logo);

        builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                galleryAction(v);
            }
        });

        builder.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cameraAction(v);
            }
        });

        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void cameraAction(View v) {
        try {
            currentImageFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI = FileProvider.getUriForFile(
                this, "com.example.rewards", currentImageFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    public void galleryAction(View v) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK) {
            try {
                processGallery(data);
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                processFullCameraImage();
            } catch (Exception e) {
                Toast.makeText(this, "onActivityResult: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    private void processFullCameraImage() {

        Uri selectedImage = Uri.fromFile(currentImageFile);
        profilePic.setImageURI(selectedImage);

        ImageToB64();
        /// The below is not necessary - it's only done for example purposes
        Bitmap bm = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
    }

    private void processGallery(Intent data) {
        Uri galleryImageUri = data.getData();
        if (galleryImageUri == null)
            return;

        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(galleryImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        int viewWidth = findViewById(R.id.createProfilePic).getWidth();
        int viewHeight = findViewById(R.id.createProfilePic).getHeight();

        Bitmap finalBitmap = Bitmap.createScaledBitmap(selectedImage, viewWidth, viewHeight, false);
        profilePic.setImageBitmap(finalBitmap);
        ImageToB64();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "image+";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",    /* suffix */
                storageDir      /* directory */
        );
    }

    private void ImageToB64() {
        BitmapDrawable drawable = (BitmapDrawable) profilePic.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] byteArray = baos.toByteArray();
        imageByteData = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.d("tag", "image successfully compressed");
    }

    private void setupEditText() {
        biographyField.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(360)
        });

        biographyField.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        //Dont need anything here
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        //Dont need anything
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //When it's changed, update the counter view
                        int currentLength = s.toString().length();
                        String formatted = String.format("(%s of 360)", currentLength);
                        textCounter.setText(formatted);
                    }
                }
        );
    }
}