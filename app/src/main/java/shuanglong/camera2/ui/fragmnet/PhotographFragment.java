package shuanglong.camera2.ui.fragmnet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import shuanglong.camera2.R;
import shuanglong.camera2.ui.activity.PictureExhibitionActivity;

/**
 * Created by shuanglong on 2018/4/26.
 * 拍照界面
 */

public class PhotographFragment extends Fragment {

    private static final String TAG = "PHOTOGRAPH_FRAGMENT";

    private static int CAMERA_STATE = 0;
    private static final int CAMERA_STATE_PREVIEW = 0;
    private static final int CAMERA_STATE_PICTURE = 1;
    private static final int CAMERA_STATE_PICTURE_FINISH = 2;
    private static final String PICTURE_FILE = "PICTURE_FILE";

    private CaptureRequest.Builder mCaptureRequest;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private ImageReader mImageReader;
    private Size[] mOutputSizes;
    private TextureView mTextureView;
    private File mSavePictureFile;
    private Integer mInteger;
    private Boolean isFlash;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        initView(view);
        initEvent();
        return view;
    }

    private void initView(View view) {
        mTextureView = view.findViewById(R.id.tv_preview);
        String externalStorageState = Environment.getExternalStorageState();
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            mSavePictureFile = new File("mnt/sdcard/DCIM/test.jpg");
        }
    }

    private void initEvent() {
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    /**
     * 拍照，获取一张静态图片
     */
    public void picture() {
        try {
            //CONTROL_AF_MODE :相机设备是否会为此请求触发自动对焦。
            //CONTROL_AF_MODE_CONTINUOUS_PICTURE:在这种模式下，AF算法会不断修改镜头位置，以尝试提供持续聚焦的图像流。
            mCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            CAMERA_STATE = CAMERA_STATE_PICTURE;
            mCameraCaptureSession.capture(mCaptureRequest.build(), mCaptureSessionCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "TextureView.SurfaceTextureListener --> onSurfaceTextureAvailable");
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "TextureView.SurfaceTextureListener --> onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            Log.d(TAG, "TextureView.SurfaceTextureListener --> onSurfaceTextureDestroyed");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            Log.d(TAG, "TextureView.SurfaceTextureListener --> onSurfaceTextureUpdated");
        }
    };

    @SuppressLint("MissingPermission")
    private void openCamera() {
        try {
            CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String id : cameraIdList) {
                //CameraCharacteristics :描述CameraDevice的属性。
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(id);
                //StreamConfigurationMap ：查询CameraDevice的属性结果
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }
                mInteger = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                isFlash = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                //获取预览界面使用的宽高数组，此功能仅返回PRIVATE的尺寸
                mOutputSizes = map.getOutputSizes(SurfaceTexture.class);
                cameraManager.openCamera(id, mStateCallback, null);
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //相机设备状态监听
    CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {//相机开启
            Log.d(TAG, "CameraDevice.StateCallback --> onOpened");
            mCameraDevice = camera;
            try {
                List<Surface> surfaceList = new ArrayList<>();
                //返回此视图使用的SurfaceTexture, 如果视图未附加到窗口或表面纹理尚未初始化，则此方法可能会返回null。
                SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
                assert surfaceTexture != null;
                //设置图像缓冲区的默认大小
                surfaceTexture.setDefaultBufferSize(mOutputSizes[0].getWidth(), mOutputSizes[0].getHeight());
                //创建ImageReader,ImageReader类允许应用程序直接访问渲染到Surface中的图像数据
                mImageReader = ImageReader.newInstance(mOutputSizes[0].getWidth(), mOutputSizes[0].getHeight(), ImageFormat.JPEG, 1);
                //设置获取图片的监听
                mImageReader.setOnImageAvailableListener(mImageAvailableListener, null);
                //处理由屏幕合成器管理的原始缓冲区。
                //Surface通常是由图像缓冲区的消费者（例如SurfaceTexture，MediaRecorder或Allocation）创建的，
                // 并被交给某种生产者（如OpenGL，MediaPlayer或CameraDevice）来绘制。
                surfaceList.add(new Surface(surfaceTexture));
                surfaceList.add(mImageReader.getSurface());
                //创建一个适合相机预览窗口的请求。
                mCaptureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                //将surface添加到此请求的目标列表中
                mCaptureRequest.addTarget(surfaceList.get(0));
                //通过向摄像机设备提供Surfaces的目标输出集，创建新的摄像头捕获会话。
                camera.createCaptureSession(surfaceList, mCameraCaptureSessionStateCallback, null);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {//相机断开
            Log.d(TAG, "CameraDevice.StateCallback --> onDisconnected");
            if (camera != null) {
                //以最快的速度断开相机连接状态
                camera.close();
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {//相机发生错误
            Log.d(TAG, "CameraDevice.StateCallback --> onError -->" + switchCameraDeviceError(error));
            if (camera != null) {
                camera.close();
            }
        }
    };

    //ImageReader :image是否可用监听
    ImageReader.OnImageAvailableListener mImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "ImageReader.OnImageAvailableListener --> onImageAvailable");
            //如果图片可用就保存到本地
            new ImageSaver(reader.acquireLatestImage(), mSavePictureFile).start();
        }
    };

    //相机配置状态的回调
    CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "CameraCaptureSession.StateCallback --> onConfigured");
            mCameraCaptureSession = session;
            try {
                //CONTROL_AF_MODE :相机设备是否会为此请求触发自动对焦。
                //CONTROL_AF_MODE_CONTINUOUS_PICTURE:在这种模式下，AF算法会不断修改镜头位置，以尝试提供持续聚焦的图像流。
                mCaptureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                if (isFlash)
                    mCaptureRequest.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                //通过此捕获会话请求无休止地重复捕获图像
                session.setRepeatingRequest(mCaptureRequest.build(), mCaptureSessionCaptureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "CameraCaptureSession.StateCallback --> onConfigureFailed");
            if (session != null) {
                session.close();
            }
        }
    };

    //相机会话捕获的监听
    CameraCaptureSession.CaptureCallback mCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Log.d(TAG, "CameraCaptureSession.CaptureCallback --> onCaptureStarted");
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Log.d(TAG, "CameraCaptureSession.CaptureCallback --> onCaptureProgressed");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.d(TAG, "CameraCaptureSession.CaptureCallback --> onCaptureCompleted");
            try {
                switch (CAMERA_STATE) {
                    case CAMERA_STATE_PREVIEW:
                        //CONTROL_AE_PRECAPTURE_TRIGGER :相机设备在处理该请求时是否会触发预捕获计量序列。
                        //CONTROL_AE_PRECAPTURE_TRIGGER_START :预捕测量序列将由相机设备启动。
                        mCaptureRequest.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                        session.capture(mCaptureRequest.build(), mCaptureSessionCaptureCallback, null);

                        break;
                    case CAMERA_STATE_PICTURE:
                        CaptureRequest.Builder captureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        captureRequest.addTarget(mImageReader.getSurface());
                        //CONTROL_AF_MODE :相机设备是否会为此请求触发自动对焦。
                        //CONTROL_AF_MODE_CONTINUOUS_PICTURE:在这种模式下，AF算法会不断修改镜头位置，以尝试提供持续聚焦的图像流。
                        captureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        //开启闪光灯
                        if (isFlash)
                            captureRequest.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
                        //开启光学防抖动
                        captureRequest.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
                        //设置拍照后的图片方向
                        captureRequest.set(CaptureRequest.JPEG_ORIENTATION, mInteger);
                        CAMERA_STATE = CAMERA_STATE_PICTURE_FINISH;
                        session.capture(captureRequest.build(), mCaptureSessionCaptureCallback, null);
                        break;
                    case CAMERA_STATE_PICTURE_FINISH:
                        Toast.makeText(getActivity(), "SaveFile -->" + mSavePictureFile, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), PictureExhibitionActivity.class);
                        intent.putExtra(PICTURE_FILE, mSavePictureFile.toString());
                        startActivity(intent);
                        CAMERA_STATE = CAMERA_STATE_PREVIEW;
                        break;
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Log.d(TAG, "CameraCaptureSession.CaptureCallback --> onCaptureFailed");
        }

        @Override
        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
            Log.d(TAG, "CameraCaptureSession.CaptureCallback --> onCaptureSequenceCompleted");
        }

        @Override
        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
            Log.d(TAG, "CameraCaptureSession.CaptureCallback --> onCaptureSequenceAborted");
        }

        @Override
        public void onCaptureBufferLost(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull Surface target, long frameNumber) {
            super.onCaptureBufferLost(session, request, target, frameNumber);
            Log.d(TAG, "CameraCaptureSession.CaptureCallback --> onCaptureBufferLost");
        }
    };


    private String switchCameraDeviceError(int error) {
        String errors = "";
        switch (error) {
            case CameraDevice.StateCallback.ERROR_CAMERA_IN_USE:
                errors = "ERROR_CAMERA_IN_USE";
                break;
            case CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE:
                errors = "ERROR_MAX_CAMERAS_IN_USE";
                break;
            case CameraDevice.StateCallback.ERROR_CAMERA_DISABLED:
                errors = "ERROR_CAMERA_DISABLED";
                break;
            case CameraDevice.StateCallback.ERROR_CAMERA_DEVICE:
                errors = "ERROR_CAMERA_DEVICE";
                break;
            case CameraDevice.StateCallback.ERROR_CAMERA_SERVICE:
                errors = "ERROR_CAMERA_SERVICE";
                break;
        }
        return errors;
    }





    /**
     * 保存照片到指定路径
     */
    private static class ImageSaver extends Thread {

        private final Image mImage;
        private final File mFile;

        ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }

    }
}
