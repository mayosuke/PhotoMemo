package jp.mayosuke.android.photomemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;


public class MainActivity extends Activity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final String TAG = "PhotoMemo";

    private ImageView mImageView;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image);
        mImageView.setVisibility(View.VISIBLE);

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
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
        final ActivityResult result = ActivityResult.parseInt(resultCode);
        Log.d(TAG, "onActivityForResult:requestCode=" + requestCode + ",result=" + result + ",data=" + data);
        if (resultCode == RESULT_CANCELED || data == null) {
            return;
        }
        final Bundle extras = data.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d(TAG, "  data.getExtras[" + key + "]=" + extras.get(key));
            }
        }
        mBitmap = data.getParcelableExtra("data");
        final Uri uri = data.getData();
        Log.d(TAG, "  data.getData=" + uri);
        if (mBitmap != null) {
            setRequestedOrientation(getOrientation(mBitmap));
            mImageView.setImageBitmap(mBitmap);
            mImageView.setVisibility(View.VISIBLE);
            return;
        }
        if (uri != null) {
            mBitmap = getBitmap(uri);
            setRequestedOrientation(getOrientation(mBitmap));
            mImageView.setImageBitmap(mBitmap);
            mImageView.setVisibility(View.VISIBLE);
            return;
        }
    }

    private enum ActivityResult {
        OK, CANCELED;

        private static ActivityResult parseInt(int resultCode) {
            if (resultCode == Activity.RESULT_OK) {
                return OK;
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                return CANCELED;
            }
            throw new IllegalArgumentException();
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
}
