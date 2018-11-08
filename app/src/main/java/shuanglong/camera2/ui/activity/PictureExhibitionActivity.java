package shuanglong.camera2.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import shuanglong.camera2.R;

/**
 * Created by shuanglong on 2018/4/25.
 * 照片展示界面
 */

public class PictureExhibitionActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView mShow;
    private Button mReturns;
    private static final String PICTURE_FILE = "PICTURE_FILE";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_exhibition);
        initView();
        initEvent();
        initData();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return:
                this.finish();
                break;
        }
    }

    private void initView() {
        mShow = findViewById(R.id.iv_show);
        mReturns = findViewById(R.id.btn_return);
    }

    private void initEvent() {
        mReturns.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            final String file = intent.getStringExtra(PICTURE_FILE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap bitmap = BitmapFactory.decodeFile(file);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mShow.setImageBitmap(bitmap);
                                }
                            });
                        }
                    }).start();
                }
            }, 800);
        }
    }

}
