package com.alizarinarts.paintpaint;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;

public class MainMenuActivity extends SherlockActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
    }

    /**
     * Launch the canvas activity.  This is the click listener for the New
     * button.
     *
     * @param view The view the click originated from.
     */
    public void launchNewCanvas(View view) {
        //TextView tv = (TextView) view;
        Intent i = new Intent(MainMenuActivity.this, CanvasActivity.class);
        startActivity(i);
    }

    /**
     * Launch the open image activity.  This is the click listener for the
     * Open button.
     *
     * @param view The view the click originated from.
     */
    public void launchOpenFile(View view) {
        //TextView tv = (TextView) view;
        Intent i = new Intent(MainMenuActivity.this, OpenFileActivity.class);
        startActivity(i);
    }

}
