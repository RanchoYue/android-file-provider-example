package com.idg.fileproviderexample;

import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static android.provider.ContactsContract.Directory.PACKAGE_NAME;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pdfFileName = "FileProviderExample.pdf";
                String pdfFolderInAssets = "pdf";

                //1st step, copy from assets to external files folder
                copyAssets(pdfFolderInAssets);

                //2nd step, sent intent with our content file provider uri to open file with external pdf app
                displayFile(pdfFileName);
            }
        });
    }


    private void copyAssets(String folderInsideAssets) {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list(folderInsideAssets);
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(folderInsideAssets + "/" + filename);
                File outFile = new File(getExternalFilesDir(null), filename);
                copyFile(in, outFile);

            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, File dst) throws IOException {
        FileOutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;

        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        in.close();
        out.close();
    }

    private void displayFile(String filename) {
        try {
            //get the file from external files
            File newFile = new File(getExternalFilesDir(null).getAbsolutePath() + "/" + filename);
            //create the URI for our file provider
            Uri fileUri = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID + ".provider", newFile);

            getApplicationContext().grantUriPermission(PACKAGE_NAME,
                    fileUri, FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/pdf");
            intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("tag", "Failed to open file: " + filename, e);
            Toast.makeText(this, "df " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
