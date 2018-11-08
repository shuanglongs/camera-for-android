package shuanglong.camera2.interfaces;

/**
 * @author shuanglong 2018/11/8
 * 捕获的接口
 * */
public interface ICapture {

    void openCamera();

    void startPreview();

    void capture();

    void closeCamera();
}
