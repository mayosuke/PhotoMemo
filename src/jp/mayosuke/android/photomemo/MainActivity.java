package jp.mayosuke.android.photomemo;

import android.app.Activity;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.image);

        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onCreate");
        super.onResume();
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
        if (data == null) {
            return;
        }
        final Bundle extras = data.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Log.d(TAG, "  data.getExtras[" + key + "]=" + extras.get(key));
            }
        }
        Log.d(TAG, "  data.getData=" + data.getData());
        mImageView.setImageURI(data.getData());
        mImageView.setVisibility(View.VISIBLE);
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
}
