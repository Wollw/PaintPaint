package com.alizarinarts.paintpaint;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.EditText;

public class CanvasActivity extends Activity {
    private Canvas mCanvas;

    private String mSavePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCanvas = new Canvas(this); 
        mSavePath = Environment.getExternalStorageDirectory() + "/" + getString(R.string.app_name) + "/";
        Log.d("",mSavePath);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCanvas.getSurfaceView().onResume();
        //Do additional stuff to unpause program
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCanvas.getSurfaceView().onPause();
        //Do additional stuff to pause program
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_canvas, menu);
        return true;
    }

    /**
     * Share the canvas image via the Android Share ability.
     *
     * Following example from:
     * http://twigstechtips.blogspot.com/2011/10/android-share-image-to-other-apps.html
     */
    public void onClickShare(MenuItem mi) {
        mCanvas.getSurfaceView().queueEvent(new Runnable() {public void run() {
            mCanvas.saveCanvasPNG(mSavePath,".tmp.png");
            File file = new File(mSavePath+".tmp.png");
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(file));
            startActivity(Intent.createChooser(intent, "Share image"));
        }});
    }

    /**
     *  Prompt user for a file name and save the canvas to disk.
     *  Alert prompt example from:
     *  http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog
     */
    public void onClickSave(MenuItem mi) {
        // Setup the EditText view for our save dialog
        final EditText input = new EditText(this);
        input.setSingleLine();

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Save Image");
        alert.setMessage("Name");

        alert.setView(input);
        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    final String fileName = input.getText().toString();
                    Log.d("","saving: "+fileName);
                    mCanvas.getSurfaceView().queueEvent(new Runnable() {public void run() {
                        mCanvas.saveCanvasPNG(mSavePath, fileName+".png");
                    }});
                }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled
            }
        });

        alert.show();

    }

}
