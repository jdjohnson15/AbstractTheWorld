package com.example.jessejohnson.abstracttheworld;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;


public class ServerActivity extends AppCompatActivity {
    private Handler handler = new Handler();
    private TextView rtext;
    private TextView ltext;
    private ImageView rimage;
    private ImageView limage;

    private Socket socket;
    private int socketCount = 0;


    private String DeviceName = "Device";
    private Camera mCamera;
    private Camera.Parameters parameters;
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private byte[] currentFrame = null;
    private SurfaceTexture dummySurfaceTexture;
    private int texture;
    private boolean socketFound = false;
    private boolean recordingStarted = false;
    private CameraPreview cp;
    private ServerSocket serverSocket;

    private void runNewServer() {

        try {
            serverSocket = new ServerSocket(9000);

            log(getIPAddress(true));
            socket= serverSocket.accept();

            ++socketCount;

            socketFound = true;
            DeviceName = "walker";
            if(socketCount>1)
                log(socketCount+" people are now in control");
            else
                log("someone has assumed control");
        } catch (IOException e) {
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        initText();
        // input = (EditText) findViewById(R.id.input);
        // send = (Button) findViewById(R.id.send);
        initImages();
        cp = new CameraPreview(this);
        cp.startCamera();
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while(true){
                    try {

                        runNewServer();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    while (true) {
                        if (socketCount > 0){
                            try {
                                //log("current frame: "+cp.getCurrentFrame());
                                //System.out.println("did it1");
                                sendImage(cp.getCurrentFrame());
                                // System.out.println("did it2");
                                InputStream in = socket.getInputStream();
                                if (in.available() != 0){
                                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                    DataInputStream dis = new DataInputStream(in);
                                    int len = dis.readInt();
                                    if (len < 0) {
                                        String Message = br.readLine();
                                        if (Message != null) {
                                            updateText(Message.substring(0, Message.length()));
                                        }
                                    }
                                    else{
                                        readImage(dis, len);
                                    }
                                }


                                //System.out.println("did it5");
                            }catch(IOException e){
                                //log("IOException :(");
                            }
                        }

                    }
                }
            }

        });
        thread.start();
    }

    private void log(final String message) {
        handler.post(new Runnable() {

            @Override
            public void run() {

                ltext.setText(message);
                rtext.setText(message);

            }
        });
    }

    public void readImage(DataInputStream dis, int len) throws IOException {

        byte[] data = new byte[len];

        dis.readFully(data);
        //ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, len);

        updateImage(bitmap);

    }
    public void readText(BufferedReader br, DataInputStream dis) throws IOException{

/*       // for (int i = 0; i < socketCount; ++i){
            InputStream in = socket.getInputStream();
            if(in.available() != 0) {
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataInputStream dis = new DataInputStream(in);
                int len = dis.readInt();
                if (len < 0) {
                    String Message = br.readLine();
                    if (Message != null && Message.charAt(len-1) == 't') {
                        updateText(Message.substring(0, Message.length()-1));
                    }
                }
            }
       // }*/
    }

    public void sendImage(byte[] myByteArray) throws IOException {
        int start = 0;

        int len = myByteArray.length;

        if (len < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        if (start < 0 || start >= myByteArray.length)
            throw new IndexOutOfBoundsException("Out of bounds: " + start);

        // May be better to save the streams in the support class;
        // just like the socket variable.
        //for (int i = 0; i < socketCount; ++i) {
            OutputStream out = socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeInt(len);
            if (len > 0) {
                dos.write(myByteArray, start, len);
            }
        //}
    }



    private void initText(){

        ltext = (TextView)findViewById(R.id.lText);
        rtext = (TextView)findViewById(R.id.rText);
        ltext.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        rtext.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ltext.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
        rtext.setTextSize(TypedValue.COMPLEX_UNIT_PX, 50);
    }
    private void initImages(){
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        System.out.println("width: " + size.x + " | height: " + size.y);

        limage = (ImageView)findViewById(R.id.lImage);
        rimage = (ImageView)findViewById(R.id.rImage);
    }

    private void updateImage(final Bitmap bmp){
        handler.post(new Runnable() {

            @Override
            public void run() {

                limage.setImageBitmap(bmp);

                rimage.setImageBitmap(bmp);

            }
        });
    }

    private void updateText(final String s){
        handler.post(new Runnable() {

            @Override
            public void run() {

                ltext.setText(s);
                rtext.setText(s);

            }
        });
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

}



