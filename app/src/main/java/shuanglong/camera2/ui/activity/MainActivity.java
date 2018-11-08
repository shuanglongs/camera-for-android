package shuanglong.camera2.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import shuanglong.camera2.R;
import shuanglong.camera2.ui.fragmnet.PermissionDialogFragment;
import shuanglong.camera2.interfaces.IClickDialogYesBtuuon;
import shuanglong.camera2.ui.fragmnet.PhotographFragment;
import shuanglong.camera2.ui.fragmnet.PhotographyFragment;

/**
 * Created by shuanglong on 2018/4/26.
 * 首页
 */
public class MainActivity extends AppCompatActivity implements IClickDialogYesBtuuon, View.OnClickListener {

    private static final String TAG = "MAIN_ACTIVITY";
    private static final int REQUEST_CAMERA = 100;
    private Button mPicture;
    private Button mPhotography;
    private Button mReturns;

    private boolean isPicture = false;
    private boolean isPhotography = false;
    private boolean isClickPhotography = false;
    private PhotographFragment mPhotographFragment;
    private PhotographyFragment mPhotographyFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        isRequestPermissions();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_picture://拍照
                if (isPicture) {
                    mPhotographFragment.picture();
                } else {
                    isPicture = true;
                    mPhotography.setVisibility(View.GONE);
                    mPicture.setVisibility(View.VISIBLE);
                    mPicture.setText("拍照");
                    mReturns.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.btn_photography://摄影
                if (isPhotography) {
                    if (isClickPhotography) {
                        mPhotographyFragment.stopRecordingVideo();
                        mPhotography.setText("开始录制");
                        isClickPhotography = false;
                    } else {
                        mPhotographyFragment.startRecordingVideo();
                        mPhotography.setText("结束录制");
                        isClickPhotography = true;
                    }
                } else {
                    mPicture.setVisibility(View.GONE);
                    mPhotography.setVisibility(View.VISIBLE);
                    mReturns.setVisibility(View.VISIBLE);
                    mPhotography.setText("开始录制");
                    FragmentManager supportFragmentManager2 = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction2 = supportFragmentManager2.beginTransaction();
                    fragmentTransaction2.replace(R.id.fl_context, mPhotographyFragment);
                    fragmentTransaction2.commit();
                    isPhotography = true;
                }

                break;
            case R.id.btn_returns:
                mPicture.setVisibility(View.VISIBLE);
                mPhotography.setVisibility(View.VISIBLE);
                mReturns.setVisibility(View.GONE);
                mPhotography.setText("摄影");
                mPicture.setText("照相");
                isPicture = false;
                isPhotography = false;
                FragmentManager supportFragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fl_context, mPhotographFragment);
                fragmentTransaction.commit();
                break;
        }
    }

    private void initView() {
        mPicture = findViewById(R.id.btn_picture);
        mPhotography = findViewById(R.id.btn_photography);
        mReturns = findViewById(R.id.btn_returns);
    }

    private void initEvent() {
        mPhotographFragment = new PhotographFragment();
        mPhotographyFragment = new PhotographyFragment();
        mPicture.setOnClickListener(this);
        mPhotography.setOnClickListener(this);
        mReturns.setOnClickListener(this);
    }

    private void isRequestPermissions() {
        int checkSelfPermissionCamera = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int checkSelfPermissionReadExternalStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int checkSelfPermissionRecordAudio = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (checkSelfPermissionCamera != PackageManager.PERMISSION_GRANTED
                || checkSelfPermissionReadExternalStorage != PackageManager.PERMISSION_GRANTED
                || checkSelfPermissionRecordAudio != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                PermissionDialogFragment dialogFragment = PermissionDialogFragment.getInstance(this, "点击 “YES” 就好！");
                dialogFragment.show(getSupportFragmentManager(), "exitDialog");
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                PermissionDialogFragment dialogFragment = PermissionDialogFragment.getInstance(this, "点击 “YES” 就好！");
                dialogFragment.show(getSupportFragmentManager(), "exitDialog");
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                PermissionDialogFragment dialogFragment = PermissionDialogFragment.getInstance(this, "点击 “YES” 就好！");
                dialogFragment.show(getSupportFragmentManager(), "exitDialog");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA);
            }
        } else {
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fl_context, mPhotographFragment);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void clickDialogYesBtuuon() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA) {
            if (permissions[0].equals(Manifest.permission.CAMERA)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fl_context, mPhotographFragment);
                    fragmentTransaction.commit();
                }
            }
        }
    }

}
