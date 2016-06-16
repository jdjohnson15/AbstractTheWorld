package com.example.jessejohnson.abstracttheworld;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.content.Context;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;


public class DrawView extends SurfaceView {
    private Bitmap bitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.RGB_565 );
    private Bitmap sendBitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.RGB_565 );
    private Path path = new Path();
    private Paint paint = new Paint();
    Paint w = new Paint();
    public Canvas canvas = new Canvas(bitmap);
    public Canvas sendCanvas = new Canvas(sendBitmap);
    public boolean clear = false;
    public DrawView(Context context, AttributeSet attrs){
        super(context, attrs);

        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        w.setColor(getResources().getColor(R.color.drawviewbg));
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true); //necessary
        getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    @Override
    protected  void onDraw(Canvas c){
        if(clear){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            path.rewind();
            clear = false;
            invalidate();
        }
        c.drawPath(path, paint);
        canvas = c;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        float eventX = event.getX();
        float eventY = event.getY();
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                return true;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(eventX,eventY);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public byte[] getCanvas(){
        sendCanvas.drawColor(Color.WHITE);
        sendCanvas.drawPath(path, paint);
        ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
        sendBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output_stream);
        return output_stream.toByteArray();
    }

    public void clearCanvas(){
        clear = true;
        invalidate();
    }

}
