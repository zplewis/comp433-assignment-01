package com.example.comp433_assignment_01;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> imageList = new ArrayList<>();
    int imageIndex = -1;

    String currentPhotoPath;

    // Used to uniquely identify the "session of using the camera" to capture an image
    int REQUEST_IMAGE_CAPTURE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        updateAllImages(-1);

        // Upon opening the app, get the list of files
        populateImageList();

        // Update all images using the most recent image, if available
        updateAllImages(0);
    }

    private void sortFiles(File[] files) {
        Arrays.sort(files, (f1, f2) -> {

            if (f1 == null && f2 == null) {
                return 0;
            }

            if (f1 == null) {
                return -1;
            }

            // compare by date in descending order
            int comparison = Long.compare(f2.lastModified(), f1.lastModified());
            if (comparison != 0) {
                return comparison;
            }

            // If there are equal last modified dates, then compare by name
            return f2.getName().compareToIgnoreCase(f1.getName());
        });
    }

    private File[] getFileList() {
        // 1. Get the picture directory
        File pictureDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // If this directory is valid, add all of the items to the arraylist in descending order.
        if (pictureDirectory == null || !pictureDirectory.isDirectory()) {
            return new File[0];
        }

        File[] files = pictureDirectory.listFiles();
        if (files == null || files.length == 0) {
            return new File[0];
        }

        return files;
    }

    /**
     *
     */
    private void populateImageList() {

        File[] files = getFileList();

        if (files.length == 0) {
            return;
        }

        // Sort the files in descending order by date then name
        sortFiles(files);

        // Add the absolute path for all files to the arraylist
        for (File file: files) {
            if (file == null || !file.isFile() || !file.exists()) {
                continue;
            }

            // This adds the images to the list in descending order
            imageList.add(file.getAbsolutePath());

            // Reaching this code means that there is at least one image
            imageIndex = 0;
        }
    }

    private void resetImageView(ImageView v) {
        if (v == null) {
            return;
        }
        v.setImageBitmap(null);
        v.setBackground(Drawable.createFromPath("@drawable/ic_launcher_background"));
        v.setForeground(Drawable.createFromPath("@drawable/ic_launcher_foreground"));
    }

    private void updateAllImages(int newImageIndex) {

        final int numImageViews = 4;

        int specifiedImageIndex = newImageIndex;

        int numberOfImages = imageList.size();

        // Stop here if the new image index is invalid
        if (newImageIndex < 0 || newImageIndex >= numberOfImages) {
            newImageIndex = -1;
        }

        // update the class variable with the new image index
        imageIndex = newImageIndex;

        for (int i = 0; i < numImageViews; i++) {

            // 0. Construct the view ID and get the ImageView
            String idName = "image" + i;
            Log.v("updateAllImages", "ImageView ID:         " + idName);
            int resId = getResources().getIdentifier(idName, "id", getPackageName());

            if (resId == 0) {
                return;
            }

            // Get the ImageView
            ImageView v = findViewById(resId);

            // Get the image index from the arraylist or this imageview, if applicable
            int tempIndex = newImageIndex + i;

            Log.v("updateAllImages", "specified image index: " + specifiedImageIndex);
            Log.v("updateAllImages", "newImageIndex:         " + newImageIndex);
            Log.v("updateAllImages", "imageList.size():      " + numberOfImages);
            Log.v("updateAllImages", "for loop index:        " + i);
            Log.v("updateAllImages", "tempIndex:             " + tempIndex);

            if (newImageIndex == -1 || newImageIndex + i >= numberOfImages) {
                resetImageView(v);
                continue;
            }

            // Update the imageview with the appropriate image
            Bitmap image = BitmapFactory.decodeFile(imageList.get(tempIndex));
            v.setImageBitmap(image);
        }

        TextView x = findViewById(R.id.tv);
        x.setText("Number of photos: " + numberOfImages);
    }

    public void imageClick(View view) {
        int numImages = imageList.size();

        if (view.getId() == R.id.image1 && numImages >= 2) {
            Log.v("imageClick", "Small Imageview #1 (Image1) was clicked.");
            updateAllImages(imageIndex + 1);
        }

        if (view.getId() == R.id.image2 && numImages >= 3) {
            Log.v("imageClick", "Small Imageview #2 (Image2) was clicked.");
            updateAllImages(imageIndex + 2);
        }

        if (view.getId() == R.id.image3 && numImages >= 4) {
            Log.v("imageClick", "Small Imageview #3 (Image3) was clicked.");
            updateAllImages(imageIndex + 3);
        }
    }

    /**
     * Returns a File object for saving the full-size photo.
     * @return File
     * @throws IOException
     * <a href="https://developer.android.com/media/camera/camera-deprecated/photobasics#TaskPath">...</a>
     */
    private File createImageFile() throws IOException {
        // Create the filename first
        // The Locale.US is optional, sets the timezone for the date
        String timeStamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
        ).format(new Date());
        String imageFileName = "IMG_" + timeStamp + ".png";

        // Seems like you have to create a File object for the parent directory of the photo
        // that will be returned from the camera
        File imageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                imageDir      /* directory */
        );

        // save the absolute path of the image file (just in case, I'm not sure it's needed)
        currentPhotoPath = image.getAbsolutePath();

        return image;
    }

    /**
     * This method waits for the picture to be returned from the camera and then updates
     * the imageview. Without using this, the application will be checking for the photo
     * before it exists yet.
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.v("takePicture", "The camera activity has been returned.");

            File recentPhoto = new File(currentPhotoPath);

            if (!recentPhoto.isFile()) {
                Log.v("onActivityResult", "The file for the newest photo does NOT exist.");
                return;
            }

            // add the newest picture to the arraylist at the top
            imageList.add(0, currentPhotoPath);

            // update all images
            updateAllImages(0);
        }

        // reset the absolute path for the next photo
        currentPhotoPath = "";

        // set the image index
        imageIndex = 0;

        // increment the REQUEST_IMAGE_CAPTURE by 1
        REQUEST_IMAGE_CAPTURE++;
    }

    /**
     * Handles the "onClick" event for the "Take Picture" button.
     * @param view
     * AndroidManifest.xml has XML added to it to enable saving images, etc. This is required.
     */
    public void takePicture(View view) {

        // There are two types of Intent objects: explicit (when you specify the class),
        // and implicit, when you are asking for whether an app can meet the need without having
        // to know the class
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check to see if there is an app that can handle this intent. If not, then return.
        // There is a warning here:
        // Consider adding a <queries> declaration to your manifest when calling this method
        // Why? Never did it and this works.
        ComponentName componentName = takePictureIntent.resolveActivity(getPackageManager());

        // Stop here if componentName is null; this means that no activity from any other app
        // matches our requested Intent type
        if (componentName == null) {
            Log.v("takePicture", "No app found to take the picture.");
            return;
        }

        // Create the File where the photo should go
        File photoFile;

        try {
            // This will always be not null unless an error occurs
            photoFile = createImageFile();

        } catch (IOException ex) {

            Log.v("takePicture", "Error occurred creating the image file.");
            return;
        }

        Uri photoURI = FileProvider.getUriForFile(this,
                "com.example.comp433_assignment_01.fileprovider", // "com.example.android.fileprovider",
                photoFile);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

    }

    public void deleteAllPictures(View view) {
        File[] files = getFileList();

        for (File file: files) {
            if (file.isFile()) {
                file.delete();
                continue;
            }
        }

        currentPhotoPath = "";

        imageList.clear();

        imageIndex = -1;

        // update all images
        updateAllImages(-1);
    }
}