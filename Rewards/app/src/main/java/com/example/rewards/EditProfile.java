package com.example.rewards;

import androidx.annotation.NonNull;
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

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditProfile extends AppCompatActivity {
    Intent intentReceived;
    String API_KEY;
    String imageByteData;
    private File currentImageFile;
    private final int REQUEST_IMAGE_GALLERY = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;

    TextView userName;
    ImageView profilePic;

    EditText password;
    EditText firstName;
    EditText lastName;
    EditText department;
    EditText position;

    TextView charCounter;
    EditText story;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.icon);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        intentReceived = getIntent();
        API_KEY = intentReceived.getStringExtra("API_KEY");

        //Not sure why edit and create have opposite IDs, too cumbersome to fix it....
        profilePic = findViewById(R.id.editProfilePic);

        userName = findViewById(R.id.createUsernameField);
        password = findViewById(R.id.createPasswordField);
        firstName = findViewById(R.id.createFnameField);
        lastName = findViewById(R.id.createLnameField);
        department = findViewById(R.id.createDepartment);
        position = findViewById(R.id.createPosition);

        charCounter = findViewById(R.id.editCharCounter);
        story = findViewById(R.id.createBiographyField);

        b64ToImage(intentReceived.getStringExtra("imageBytes"));
        ImageToB64();
        firstName.setText(intentReceived.getStringExtra("firstName"));
        lastName.setText(intentReceived.getStringExtra("lastName"));
        userName.setText(intentReceived.getStringExtra("userName"));
        password.setText(intentReceived.getStringExtra("password"));
        department.setText(intentReceived.getStringExtra("department"));
        position.setText(intentReceived.getStringExtra("position"));
        story.setText(intentReceived.getStringExtra("story"));

        String formatted = String.format("(%s of 360)", story.getText().length());
        charCounter.setText(formatted);

        setupEditText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editprofilemenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.editButton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Save Changes?");
            builder.setIcon(R.drawable.logo);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (validateFields()) editRunnableHelper();
                    else {
                        invalidFieldToast();
                    }
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void invalidFieldToast() {
        Toast.makeText(this, "One or more fields is empty or invalid. Please fix and try again!", Toast.LENGTH_LONG).show();
    }

    private void editRunnableHelper() {
        EditProfileRunnable runnable = new EditProfileRunnable(
            this,
            firstName.getText().toString(),
            lastName.getText().toString(),
            userName.getText().toString(),
            department.getText().toString(),
            story.getText().toString(),
            position.getText().toString(),
            password.getText().toString(),
            intentReceived.getStringExtra("location"),
            imageByteData,
            API_KEY
        );
        new Thread(runnable).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Intent not catched, but exits gracefully
        Intent noData = new Intent();
        setResult(3, noData);
        finish();
    }

    public void catchResponse(JSONObject response) {
        try {
            Log.d("TAGTAG", response.toString());
            Intent editedData = new Intent();
            editedData.putExtra("firstName", response.getString("firstName"));
            editedData.putExtra("lastName", response.getString("lastName"));
            editedData.putExtra("userName", response.getString("userName"));
            editedData.putExtra("department", response.getString("department"));
            editedData.putExtra("story", response.getString("story"));
            editedData.putExtra("position", response.getString("position"));
            editedData.putExtra("password", response.getString("password"));
            editedData.putExtra("remainingPointsToAward", Integer.toString(response.getInt("remainingPointsToAward")));
            editedData.putExtra("location", response.getString("location"));
            editedData.putExtra("imageBytes", response.getString("imageBytes"));
            editedData.putExtra("rewardRecordViews", response.getString("rewardRecordViews"));
            setResult(2, editedData);
            finish();
        }
        catch (Exception e) {e.printStackTrace();}
    }
    
    //TEXT STUFF
    private boolean validateFields() {
        //If ANY field is empty, return false + warn user
        if (firstName.getText().toString().equals("") ||
            lastName.getText().toString().equals("") || 
            userName.getText().toString().equals("") ||
            department.getText().toString().equals("") ||
            story.getText().toString().equals("") ||
            position.getText().toString().equals("") ||
            password.getText().toString().equals("") ||

            intentReceived.getStringExtra("location").equals("") ||
                intentReceived.getStringExtra("location") == null ||
            imageByteData.equals("") ||
                imageByteData == null
        ) return false;

        return true;
    }

    private void setupEditText() {
        story.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(360)
        });

        story.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //When it's changed, update the counter view
                        int currentLength = s.toString().length();
                        String formatted = String.format("(%s of 360)", currentLength);
                        charCounter.setText(formatted);
                    }
                }
        );
    }

    //<-------------------------------- IMAGE STUFF ---------------------------------------->

    private void b64ToImage(String bytes) {
        byte[] imageBytes = Base64.decode(bytes, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        profilePic.setImageBitmap(bitmap);
    }

    public void editPictureDialog(View v) {
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
        int viewWidth = findViewById(R.id.editProfilePic).getWidth();
        int viewHeight = findViewById(R.id.editProfilePic).getHeight();

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

}