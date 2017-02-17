package com.aka.assignment.ui.activity;

/**
 * Created by akshayaggarwal99 on 17-02-2017.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.aka.assignment.R;
import com.aka.assignment.model.Captures;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddImageActivity extends AppCompatActivity implements View.OnClickListener {
    //a constant to track the file chooser intent
    private static final int PICK_IMAGE_REQUEST = 234;
    public static final int MEDIA_TYPE_IMAGE = 1;
    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Captures";
    private ProgressDialog mProgressDialog;


    private Uri fileUri; // file url to store image/video

    private ImageView imgPreview;
    private ImageView btnCapturePicture;
    private Button next;
    private Bundle bundle;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    StorageReference storage;
    EditText name;
    Uri uri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addproduct_image);
//        bundle = getIntent().getExtras().getBundle("temp");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("images");

        storage = FirebaseStorage.getInstance().getReference();


        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        btnCapturePicture = (ImageView) findViewById(R.id.iv_CapturePicture);
        next = (Button) findViewById(R.id.b_next);
        name = (EditText) findViewById(R.id.edit_name);

        mProgressDialog = new ProgressDialog(this);

        btnCapturePicture.setOnClickListener(this);
        next.setOnClickListener(this);
    }

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            // successfully captured the image
            // display it in image view
            previewCapturedImage();
        } else if (resultCode == RESULT_CANCELED) {
            // user cancelled Image capture
            Toast.makeText(getApplicationContext(),
                    "User cancelled image capture", Toast.LENGTH_SHORT)
                    .show();
        } else {
            // failed to capture image
            Toast.makeText(getApplicationContext(),
                    "Sorry! Failed to Load image", Toast.LENGTH_SHORT)
                    .show();
        }
    }


    private void previewCapturedImage() {
        try {
            imgPreview.setVisibility(View.VISIBLE);

            // Get the dimensions of the View
            int targetW = imgPreview.getWidth();
            int targetH = imgPreview.getHeight();

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileUri.getPath(), bounds);

            int photoW = bounds.outWidth;
            int photoH = bounds.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            Log.d("outWidth", String.valueOf(bounds.outWidth));
            // Decode the image file into a Bitmap sized to fill the View
            bounds.inJustDecodeBounds = false;
            bounds.inSampleSize = scaleFactor;
            bounds.inPurgeable = true;

            BitmapFactory.Options opts = new BitmapFactory.Options();
            Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath(), opts);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(fileUri.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            Matrix matrix = new Matrix();
            matrix.setRotate(rotationAngle, (float) targetW, (float) targetH);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);

            imgPreview.setImageBitmap(rotatedBitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    /**
     * ------------ Helper Methods ----------------------
     * */

    /**
     * Creating file uri to store image/video
     */

    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
 * returning image / video
 */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_CapturePicture:
                showFileChooser();
                break;
            case R.id.b_next:
                uploadProductImg();
                break;
            default:
                return;
        }
    }

    private void uploadProductImg() {
        // Get the data from an ImageView as bytes
        imgPreview.setDrawingCacheEnabled(true);
        imgPreview.buildDrawingCache();
        Bitmap bitmap = imgPreview.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storage.child("images/" + fileUri.getLastPathSegment()).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                mProgressDialog.setMessage("Uploading ...");
                mProgressDialog.show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                String downloadUrl = String.valueOf(taskSnapshot.getDownloadUrl());
                String key = mDatabase.push().getKey();
                mProgressDialog.cancel();
                writeNewImage(name.getText().toString() + " ", key, downloadUrl);
                Toast.makeText(AddImageActivity.this, "Upload Completed", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddImageActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void writeNewImage(String name, String key, String url) {
        Captures captures = new Captures(name, key, url);

        mDatabase.child(key).setValue(captures);
    }

}