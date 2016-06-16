package com.example.jessejohnson.abstracttheworld;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Jesse Johnson on 12/2/2015.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Camera.Parameters parameters;
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private LinkedList<byte[]> mQueue = new LinkedList<byte[]>();
    private static final int MAX_BUFFER = 15;
    private byte[] mLastFrame = null;
    private int texture;
    private byte[] currentFrame = null;

    private SurfaceTexture dummySurface;

    private TextView lText, rText;


    public CameraPreview(Context context) {
        super(context);
    }

    public void startCamera() {
        dummySurface = new SurfaceTexture(texture);

        mCamera = Camera.open();
        parameters = mCamera.getParameters();
        List<Camera.Size> list = parameters.getSupportedPictureSizes();
        Camera.Size size = list.get(0);
        int minSize = 999999;
        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).height + list.get(i).width < minSize) {
                size = list.get(i);
                minSize = list.get(i).height + list.get(i).width;
            }
        }
        parameters.setPreviewSize(size.width, size.height);
        parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
        mCamera.setParameters(parameters);

        mCamera.setPreviewCallback(
                new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        // TODO Auto-generated method stub
                        setCurrentFrame(data);
                    }
                });

        try {
            mCamera.setPreviewTexture(dummySurface);
            mCamera.startPreview();

        } catch (IOException ioe) {
            Log.v("MainActivity", "CAM LAUNCH FAILED");
        }
    }

    private void setCurrentFrame(byte[] data){
        Camera.Parameters parameters = mCamera.getParameters();
        int format = parameters.getPreviewFormat();
        System.out.println("format: "+format);
        //YUV formats require more conversion
        //if (format == ImageFormat.NV21 || format == ImageFormat.YUY2 || format == ImageFormat.NV16) {
            int w = parameters.getPreviewSize().width;
            int h = parameters.getPreviewSize().height;
            // Get the YuV image
            YuvImage yuv_image = new YuvImage(data, format, w, h, null);
            // Convert YuV to Jpeg
            Rect rect = new Rect(0, 0, w, h);
            ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
            yuv_image.compressToJpeg(rect, 100, output_stream);
            currentFrame = output_stream.toByteArray();

       // }
    }
    public byte[] getCurrentFrame(){
        return currentFrame;
    }

    static private int createTexture()
    {
        int[] texture = new int[1];

        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        return texture[0];
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        texture = createTexture();
        startCamera();
    }


    public void surfaceDestroyed(SurfaceHolder holder) {

        if (mCamera != null) {
            synchronized (this) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;
            }
        }
        onPreviewStopped();
    }

    protected void onPreviewStopped() {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null){
            return;
        }

        try {
            mCamera.stopPreview();
            resetBuff();

        } catch (Exception e){

        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

    }
    public void onPause() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
        }
        resetBuff();
    }
    private void resetBuff() {

        synchronized (mQueue) {
            mQueue.clear();
            mLastFrame = null;
        }
    }
    public byte[] getImageBuffer() {
        synchronized (mQueue) {
            if (mQueue.size() > 0) {
                mLastFrame = mQueue.poll();
            }
        }

        return mLastFrame;
    }
}