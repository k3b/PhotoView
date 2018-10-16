package uk.co.senab.photoview.sample;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import uk.co.senab.photoview.IPhotoView;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.log.LogManager;

/** activity demonstrating swipable ViewPager from media-db.
 *
 * Simplified version of de.k3b.android.androFotoFinder.imagedetail.ImagePagerAdapterFromCursor
 * from https://github.com/k3b/APhotoManager/
 *
 * Created by k3b on 13.06.2016.
 */
public class ViewPagerActivityFromMediaDB extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {
    public static final String LOG_TAG = "ViewPagerAct-MediaDB";

    private static final int PERMISSION_REQUEST_ID = 0;
    private static final String NEEDED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    private static final String[] PERMISSIONS = {NEEDED_PERMISSION};

    private HackyViewPager mViewPager;

    private final Handler handler = new Handler();
    private boolean rotating = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);

        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        setContentView(mViewPager);


        showData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rotate_menue, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem debug = menu.findItem(R.id.logging_enabled);
        debug.setChecked(LogManager.isDebugEnabled());

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_Rotate_10_Right:
                getCurrentPhotoView().setRotationBy(10);
                return true;
            case R.id.menu_Rotate_10_Left:
                getCurrentPhotoView().setRotationBy(-10);
                return true;
            case R.id.menu_Toggle_automatic_rotation:
                toggleRotation();
                return true;
            case R.id.menu_Reset_to_0:
                getCurrentPhotoView().setRotationTo(0);
                return true;
            case R.id.menu_Reset_to_90:
                getCurrentPhotoView().setRotationTo(90);
                return true;
            case R.id.menu_Reset_to_180:
                getCurrentPhotoView().setRotationTo(180);
                return true;
            case R.id.menu_Reset_to_270:
                getCurrentPhotoView().setRotationTo(270);
                return true;
            case R.id.logging_enabled: {
                LogManager.setDebugEnabled(!LogManager.isDebugEnabled());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleRotation() {
        if (rotating) {
            handler.removeCallbacksAndMessages(null);
        } else {
            rotateLoop();
        }
        rotating = !rotating;
    }

    private void rotateLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getCurrentPhotoView().setRotationBy(1);
                rotateLoop();
            }
        }, 15);
    }

    private IPhotoView getCurrentPhotoView() {
        int pageID = mViewPager.getCurrentItem();
        return (PhotoView) mViewPager.getChildAt(pageID);
    }

    private void showData() {
        if (ActivityCompat.checkSelfPermission(this, NEEDED_PERMISSION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "showData granted");

            Snackbar.make(mViewPager,
                    R.string.permission_media_write_available,
                    Snackbar.LENGTH_SHORT).show();

            // this is a demo. todo use try catch for error handling. do in seperate non-gui thread
            Cursor cursor = MediaStore.Images.Media.query(getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{}
                    , null, null, MediaStore.Images.Media.DATE_TAKEN + " DESC");

            final SamplePagerAdapter adapter = new SamplePagerAdapter(this, cursor);
            mViewPager.setAdapter(adapter);
        } else {
            Log.d(LOG_TAG, "showData denyed");

            // Permission is missing and must be requested.
            requestMediaWritePermission();
        }
    }

    private void requestMediaWritePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                NEEDED_PERMISSION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            // onOk proceed
            Snackbar.make(mViewPager, R.string.permission_media_write_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    Log.d(LOG_TAG, "requestCameraPermission shouldShowRequestPermissionRationale");

                    ActivityCompat.requestPermissions(ViewPagerActivityFromMediaDB.this,
                            PERMISSIONS,
                            PERMISSION_REQUEST_ID);
                }
            }).show();
        } else {
            Log.d(LOG_TAG, "requestCameraPermission not shouldShowRequestPermissionRationale");
            ActivityCompat.requestPermissions(this,
                    PERMISSIONS, PERMISSION_REQUEST_ID);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            int[] grantResults) {
        Log.d(LOG_TAG, "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_ID) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.

                Snackbar.make(mViewPager, R.string.permission_media_write_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();

                Log.d(LOG_TAG, "onRequestPermissionsResult granted");
               showData();
            } else {
                // Permission request was denied.
                // show some text
                Snackbar.make(mViewPager, R.string.permission_media_write_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
                Log.d(LOG_TAG, "onRequestPermissionsResult denyed");
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    static class SamplePagerAdapter extends PagerAdapter {
        private final Activity mActivity;
        private Cursor mCursor = null; // the content of the page
        SamplePagerAdapter(Activity activity, Cursor cursor) {
            mActivity = activity;
            mCursor = cursor;
        }

        /**
         * Implementation for PagerAdapter:
         * Return the number of views available.
         */
        @Override
        public int getCount() {
            int result = 0;
            if (this.mCursor != null) {
                result = this.mCursor.getCount();
            }

            return result;
        }


        /**
         * Implementation for PagerAdapter:
         * Determines whether a page View is associated with a specific key object
         * as returned by {@link #instantiateItem(ViewGroup, int)}. This method is
         * required for a PagerAdapter to function properly.
         *
         * @param view Page View to check for association with <code>object</code>
         * @param object Object to check for association with <code>view</code>
         * @return true if <code>view</code> is associated with the key object <code>object</code>
         */
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());

            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            int resolutionKind = MediaStore.Images.Thumbnails.MINI_KIND;


            final BitmapFactory.Options options = new BitmapFactory.Options();
            final ContentResolver contentResolver = photoView.getContext().getContentResolver();

            // this is a demo. todo use try catch for error handling
            this.mCursor.moveToPosition(position);

            long imageID = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Images.Media._ID));
            String imageFilePathFullResolution = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));

            Log.d(LOG_TAG, "loading " + imageID +
                    ":" + imageFilePathFullResolution +
                    "");
            // first display with low resolution which is much faster for swiping
            Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(
                contentResolver,
                imageID,
                resolutionKind,
                options);

            photoView.setImageBitmap(thumbnail);
            photoView.setMaximumScale(20);
            photoView.setMediumScale(5);

            // this image will be loaded when zooming starts
            photoView.setImageReloadFile(new File(imageFilePathFullResolution));

            return photoView;
        }

        /**
         * Implementation for PagerAdapter:
         * Called to inform the adapter of which item is currently considered to
         * be the "primary", that is the one show to the user as the current page.
         *
         * @param container The containing View from which the page will be removed.
         * @param position The page position that is now the primary.
         * @param object The same object that was returned by
         * {@link #instantiateItem(View, int)}.
         */
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            // this is a demo. todo use try catch for error handling
            this.mCursor.moveToPosition(position);

            String imageFilePathFullResolution = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            mActivity.setTitle(imageFilePathFullResolution);
        }


        /**
         * Implementation for PagerAdapter:
         * Remove a page for the given position.  The adapter is responsible
         * for removing the view from its container, although it only must ensure
         * this is done by the time it returns from {@link #finishUpdate(ViewGroup)}.
         *
         * @param container The containing View from which the page will be removed.
         * @param position The page position to be removed.
         * @param object The same object that was returned by
         * {@link #instantiateItem(View, int)}.
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


    }
}
