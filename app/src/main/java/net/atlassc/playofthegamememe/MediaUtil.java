package net.atlassc.playofthegamememe;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ShinChven on 2014/10/28.
 * Used to create path for media file saving when using camera
 * sometimes device camera you called won't return taking,
 * you should assign a path to store media file before using camera, and
 * find files created by camera in path your assigned.
 */
public class MediaUtil {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final String DIR = "playofthegamememe";

    /**
     * Create a file Uri for saving an image or video
     */
    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.


        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), DIR);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                LogUtil.i("creating directory failed");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Uri saveMediaEntry(Context context, String imagePath, String title, String description, long dateTaken, int orientation, Location loc) {
        ContentValues v = new ContentValues();
        v.put(MediaStore.Images.Media.TITLE, title);
        v.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        v.put(MediaStore.Images.Media.DESCRIPTION, description);
        v.put(MediaStore.Images.Media.DATE_ADDED, dateTaken);
        v.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
        v.put(MediaStore.Images.Media.DATE_MODIFIED, dateTaken);
        v.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        //  v.put(MediaStore.Images.Media.ORIENTATION, orientation);

        File f = new File(imagePath);
        File parent = f.getParentFile();
        String path = parent.toString().toLowerCase();
        String name = parent.getName().toLowerCase();
        v.put(MediaStore.Images.ImageColumns.BUCKET_ID, path.hashCode());
        v.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
        v.put(MediaStore.Images.Media.SIZE, f.length());
        f = null;

        if (loc != null) {
            v.put(MediaStore.Images.Media.LATITUDE, loc.getLatitude());
            v.put(MediaStore.Images.Media.LONGITUDE, loc.getLongitude());
        }
        v.put("_data", imagePath);
        ContentResolver c = context.getContentResolver();
        return c.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
    }
}
