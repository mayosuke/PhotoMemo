package jp.mayosuke.android.photomemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends Activity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String TAG = "PhotoMemo.MainActivity";
    private static final String KEY_OUTPUT_URI = "outputUri";

    private ImageView mImageView = null;
    private Bitmap mBitmap = null;
    private Uri mOutputUri = null;

    private enum AppState {
        IDLE,
        TAKING_PICTURE,
        VIEWING_PICTURE
    }
    private static AppState sState;

    static {
        changeState(AppState.IDLE);
    }

    public MainActivity() {
        Log.d(TAG, "new");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate:savedInstanceState=" + savedInstanceState + ",sState=" + sState);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image);
        mImageView.setVisibility(View.INVISIBLE);

        switch (sState) {
        case IDLE:
            changeState(AppState.TAKING_PICTURE);
            // create Intent to take a picture and return control to the calling application
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mOutputUri = getOutputMediaFileUri();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputUri);
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            break;
        case TAKING_PICTURE:
            if (savedInstanceState != null) {
                mOutputUri = savedInstanceState.getParcelable(KEY_OUTPUT_URI);
            } else {
                mOutputUri = null;
            }
            break;
        case VIEWING_PICTURE:
            if (savedInstanceState != null) {
                mOutputUri = savedInstanceState.getParcelable(KEY_OUTPUT_URI);
            } else {
                mOutputUri = null;
            }
            if (mOutputUri != null) {
                mBitmap = BitmapFactory.decodeFile(mOutputUri.getPath());
                setRequestedOrientation(getOrientation(mBitmap));
                mImageView.setImageBitmap(mBitmap);
            } else {
                mImageView.setBackgroundColor(Color.WHITE);
            }
            mImageView.setVisibility(View.VISIBLE);
            break;
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        final int changingConfig = getChangingConfigurations();
        Log.d(TAG, "onDestroy:changingConfig=" + changingConfig);
        if (mImageView != null) {
            mImageView.setImageBitmap(null);
            mImageView = null;
        }
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final ActivityResult result = ActivityResult.parseResultCode(resultCode);
        Log.d(TAG, "onActivityForResult:requestCode=" + requestCode + ",result=" + result + ",data=" + data);
        if (sState == AppState.TAKING_PICTURE) {
            changeState(AppState.VIEWING_PICTURE);
            if (result.isOk()) {
                mBitmap = BitmapFactory.decodeFile(mOutputUri.getPath());
                setRequestedOrientation(getOrientation(mBitmap));
                mImageView.setImageBitmap(mBitmap);
            } else {
                mOutputUri = null;
                mImageView.setBackgroundColor(Color.WHITE);
            }
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mOutputUri != null) {
            outState.putParcelable(KEY_OUTPUT_URI, mOutputUri);
        }
        Log.d(TAG, "onSaveInstanceState:outState=" + outState);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        changeState(AppState.IDLE);
        super.onBackPressed();
    }

    private static void changeState(AppState newState) {
        Log.d(TAG, "changeState:oldState=" + sState + ",newState=" + newState);
        sState = newState;
    }

    private enum ActivityResult {
        OK, CANCELED;

        private static ActivityResult parseResultCode(int resultCode) {
            if (resultCode == Activity.RESULT_OK) {
                return OK;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                return CANCELED;
            }
            throw new IllegalArgumentException();
        }

        private boolean isOk() {
            return this == OK;
        }
    }

    private String getImageFilePathFromUri(final Uri imageUri) {
        final String columnName = MediaStore.Images.Media.DATA;
        final String[] projection = {columnName};
        final Cursor cursor = getContentResolver().query(imageUri, projection, null, null, null);
        final int columnIndex = cursor.getColumnIndexOrThrow(columnName);
        cursor.moveToFirst();
        final String imageFilePath = cursor.getString(columnIndex);
        Log.d(TAG, "  imageFilePath=" + imageFilePath);
        return imageFilePath;
    }

    private int getOrientation(final Bitmap bitmap) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        Log.d(TAG, "getOrientation:bitmap=" + bitmap + ",height=" + height + ",width=" + width);
        if (height >= width) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
    }

    private Bitmap getBitmap(final Uri uri) {
        return BitmapFactory.decodeFile(getImageFilePathFromUri(uri));
    }

    private static Uri getOutputMediaFileUri(){
        Log.e(TAG, "getOutputMediaFileUri");

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "photomemo");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        Log.d(TAG, "  mediaStorageDir=" + mediaStorageDir.getAbsolutePath());

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.e(TAG, "  failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");

        Uri mediaFileUri = Uri.fromFile(mediaFile);
        Log.d(TAG, "  mediaFile=" + mediaFile + ",mediaFileUri=" + mediaFileUri);

        return mediaFileUri;
    }
}
