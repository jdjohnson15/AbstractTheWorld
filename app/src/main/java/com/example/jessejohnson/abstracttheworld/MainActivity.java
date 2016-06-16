package com.example.jessejohnson.abstracttheworld;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.jessejohnson.abstracttheworld.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View decorView = getWindow().getDecorView();
    // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    // Remember that you should never show the action bar if the
    // status bar is hidden, so hide that too if necessary.
        //ActionBar actionBar = getActionBar();
        //actionBar.hide();
    }

    public void runServer (View view) {
        Intent intent = new Intent(this, ServerActivity.class);
        EditText editText = (EditText)findViewById(R.id.ipinput);
        startActivity(intent);
    }
    public void runClient (View view) {
        Intent intent = new Intent(this, ClientActivity.class);
        EditText editText = (EditText)findViewById(R.id.ipinput);
        intent.putExtra(EXTRA_MESSAGE, editText.getText().toString());
        startActivity(intent);
    }



}
