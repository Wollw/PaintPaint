package com.alizarinarts.paintpaint;

import com.actionbarsherlock.app.SherlockActivity;

import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Create a ListView of all the files that can be opened for editing.  When an
 * item is selected it will open that image in the CanvasActivity.
 *
 * @author <a href="mailto:david.e.shere@gmail.com">David Shere</a>
 */
public class OpenFileActivity extends SherlockActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_file);

        /*
         * Placeholder for now.  This should populate the ListView with
         * the names of the files that can be opened.
         */
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, new String[] {""});
        ListView lv = (ListView) findViewById(R.id.fileList);
        lv.setAdapter(aa);
    }

}
