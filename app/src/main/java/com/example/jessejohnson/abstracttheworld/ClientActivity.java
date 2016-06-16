package com.example.jessejohnson.abstracttheworld;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class ClientActivity extends AppCompatActivity {
/*
    Handler updateConversationHandler;

    Thread serverThread = null;

    Socket socket;

    private ImageView imageView;//  private TextView text;

    public static final int port = 9000;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        imageView=(ImageView) findViewById(R.id.imageView);//text = (TextView) findViewById(R.id.textView01);

        updateConversationHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

    }
    private boolean connect(String ip, int port) {

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("192.168.1.152", port), 150000); /////DONT FORGET TO MAKE DYNAMIC

            return true;
        } catch (Exception e) {
        }
        return false;
    }


    class ServerThread implements Runnable {

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {


                    if(!connect("192.168.1.152", port)){
                        System.out.println("failed to connect");
                    }
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();


            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private DataInputStream input;//private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                //this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

                InputStream in = this.clientSocket.getInputStream();
                this.input = new DataInputStream(in);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            System.out.println("hello");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] data;//String read = input.readLine();
                    int len= this.input.readInt();
                    data = new byte[len];
                    if (len > 0) {
                        this.input.readFully(data,0,data.length);
                    }
                        /*
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] data;
                        int length = 0;
                        while ((length = this.input.read(data))!=-1) {
                            out.write(data,0,length);
                        }
                           data=out.toByteArray();


                    updateConversationHandler.post(new updateUIThread(data));//updateConversationHandler.post(new updateUIThread(read));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    class updateUIThread implements Runnable {
        private byte[] byteArray;//private String msg;

        public updateUIThread(byte[] array){    //public updateUIThread(String str) {
            this.byteArray=array;   //this.msg = str;
        }

        @Override
        public void run() {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray , 0, byteArray .length);
            imageView.setImageBitmap(bitmap);//text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
        }
    }

}*/

    Intent intent;
    DataOutputStream out;
    private SurfaceHolder holder;
    private Handler handler = new Handler();
    private TextView text;
    private EditText input;
    private Button sendButton;
    private Socket socket;
    private Socket textSocket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private String DeviceName = "Device";
    private int port = 9000;
    private int textPort = 9001;
    private MediaPlayer mMediaPlayer;
    private ImageView imageView;
    private DrawView drawView;


    private boolean connect(String ip, int port, int textPort) {
        log("Connecting");
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 150000); /////DONT FORGET TO MAKE DYNAMIC
            DeviceName += "1";
            Log.i("Server", DeviceName);
            log("Connected");
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        input = (EditText) findViewById(R.id.input);
        sendButton = (Button) findViewById(R.id.send);
        intent = getIntent();
        text = (TextView)findViewById(R.id.clientText);
        imageView = (ImageView)findViewById(R.id.imageView);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = 480;
        options.outWidth = 640;
        options.inSampleSize = 3;

        drawView = (DrawView)findViewById(R.id.drawing);


        /*
        SurfaceView mPreview = (SurfaceView)findViewById(R.id.surfaceView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        holder = mPreview.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);*/
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    if(!connect(intent.getStringExtra(MainActivity.EXTRA_MESSAGE), port, textPort)){
                    //if(!connect("192.168.1.152", port, textPort)){
                        log("Failed to connect. Try again");
                    }
                    else{
                      // outputStream = new DataOutputStream(textSocket.getOutputStream());
                        //inputStream = new BufferedInputStream(new DataInputStream(socket.getInputStream()),480*640);


                       while (true) {
                            readImage();
                        }

                    }

                } catch (IOException e) {
                    //log("Error: IO Exception");
                    e.printStackTrace();
                }
            }
        });
        thread.start();

    }

    public void readImage() throws IOException {
        // Again, probably better to store these objects references in the support class
        InputStream in = socket.getInputStream();

        DataInputStream dis = new DataInputStream(in);

        int len = dis.readInt();
       // int w = 0;
       // int h = 0;

        byte[] data = new byte[len];
        if (len > 0) {
            //w = dis.readInt();
            //h = dis.readInt();
            dis.readFully(data);
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        updateImage(bitmap);
    }

    public void send(View view) throws IOException{
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        if (out == null) {
            return;
        }
        try {
            out.writeInt(-1);
            String Message = input.getText().toString() + "\n";
            out.write(Message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        input.setText("");


    }

    public void sendImage(View view) throws IOException {

        OutputStream out = socket.getOutputStream();

        if (out == null) {
            return;
        }
        try {
            byte[] myByteArray = drawView.getCanvas();
            int start = 0;

            int len = myByteArray.length;

            //System.out.println("this is len: "+ len);

            if (len < 0)
                throw new IllegalArgumentException("Negative length not allowed");
            if (start < 0 || start >= myByteArray.length)
                throw new IndexOutOfBoundsException("Out of bounds: " + start);

            DataOutputStream dos = new DataOutputStream(out);
            dos.writeInt(len);
            if (len > 0) {
                dos.write(myByteArray, start, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        drawView.clearCanvas();
    }

    private void updateImage(final Bitmap bmp) {
        handler.post(new Runnable() {

            @Override
            public void run() {

                imageView.setImageBitmap(bmp);

            }
        });
    }

    private void log(final String message) {
        handler.post(new Runnable() {

            @Override
            public void run() {

                text.setText(message);

            }
        });
    }

}