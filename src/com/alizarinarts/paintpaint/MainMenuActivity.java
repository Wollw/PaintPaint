package com.alizarinarts.paintpaint;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;

/**
 * The main activity and entry point into the program.  This presents the user
 * with the choice between opening an existing image to edit or creating a new
 * image with a blank canvas.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 */
public class MainMenuActivity extends SherlockActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);
    }

    /**
     * Launches the CanvasActivity activity with a blank canvas.
     * This is the onClick method for the New button.
     * @param view The view the click originated from.
     */
    public void launchNewCanvas(View view) {
        Intent i = new Intent(MainMenuActivity.this, CanvasActivity.class);
        startActivity(i);
    }

    /**
     * Launches the OpenFileActivity for the user to
     * choose an existing image to edit.
     * This is the onClick method for the Open button.
     *
     * @param view The view the click originated from.
     */
    public void launchOpenFile(View view) {
        Intent i = new Intent(MainMenuActivity.this, OpenFileActivity.class);
        startActivity(i);
    }

}
