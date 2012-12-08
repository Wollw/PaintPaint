package com.alizarinarts.paintpaint;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;

import android.os.Bundle;
import android.os.Environment;

import android.view.View;

import android.widget.AdapterView;

import android.widget.AdapterView.OnItemClickListener;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Create a ListView of all the files that can be opened for editing.  When an
 * item is selected it will open that image in the CanvasActivity.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 */
public class OpenFileActivity extends SherlockActivity {

    String mSavePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        mSavePath = Environment.getExternalStorageDirectory() + "/" + PaintPaint.NAME + "/";
        File dir = new File(mSavePath);
        ArrayAdapter<String> aa;
        if (dir.exists() && dir.list().length > 0) {
            /*
             * Build the list of images that can be opened
             */
            ArrayList<String> imageArrayList = new ArrayList<String>();
            for (String fileName : dir.list()) {
                if (!fileName.startsWith(".") && fileName.endsWith(".png")) {
                    imageArrayList.add(fileName.substring(0,fileName.length()-4));
                }
            }
            String[] imageList = new String[imageArrayList.size()];
            imageArrayList.toArray(imageList);
            Arrays.sort(imageList);
            aa = new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, imageList);
        } else {
            aa = new ArrayAdapter<String>(this,
                 android.R.layout.simple_list_item_1, new String[]{"No files found."}); 
        }

        ListView lv = (ListView) findViewById(R.id.fileList);
        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent i = new Intent(OpenFileActivity.this, CanvasActivity.class);
                i.putExtra("openFile", ((TextView)v).getText()+".png");
                startActivity(i); // Launch the canvas and pass it a filename to open.
            }
        });
        lv.setAdapter(aa);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        switch (mi.getItemId()) {
        // Handle the Up action from ActionBar icon clicks.
        case android.R.id.home:
            Intent i = new Intent(this, MainMenuActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            return true;
        }
        return false;
    }

}
