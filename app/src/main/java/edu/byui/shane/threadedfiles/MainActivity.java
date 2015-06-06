package edu.byui.shane.threadedfiles;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;


public class MainActivity extends ActionBarActivity {
    private ProgressBar mProgress;
    private List<String> loadedLines;
    private ArrayAdapter<String> viewLines;
    private Handler mHandler = new Handler();
    private final String filename = "numbers.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        loadedLines = new ArrayList<>();
        viewLines = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, loadedLines);
        ((ListView) findViewById(R.id.listView)).setAdapter(viewLines);
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

    public void showToast(final String toast) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateListView() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                viewLines.notifyDataSetChanged();
            }
        });
    }

    public void createFile(View view) {
        final Context context = getApplicationContext();
        mProgress.setProgress(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = new File(context.getFilesDir(), filename);
                    if (!f.exists()) {
                        f.createNewFile();
                    }
                    BufferedWriter file = new BufferedWriter(new java.io.FileWriter(f));

                    for (int i = 1; i <= 10; i++) {
                        file.write(i + "\n");
                        mHandler.post(new Runnable() {
                            public void run() {
                                mProgress.incrementProgressBy(10);
                            }
                        });
                        Thread.sleep(250);
                    }
                    file.close();
                    showToast("File creation successful.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast("Error creating file.");
                }
            }
        }).start();
    }

    public void loadFile(View view) {
        final Context context = getApplicationContext();
        mProgress.setProgress(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = new File(context.getFilesDir(), filename);
                    if (!f.exists()) {
                        throw new IOException();
                    }
                    BufferedReader file = new BufferedReader(new java.io.FileReader(f));

                    for (int i = 1; i <= 10; i++) {
                        loadedLines.add(file.readLine());
                        mHandler.post(new Runnable() {
                            public void run() {
                                mProgress.incrementProgressBy(10);
                            }
                        });
                        Thread.sleep(250);
                    }
                    file.close();
                    updateListView();
                    showToast("File loaded.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    showToast("Error loading file.");
                }
            }
        }).start();
    }

    public void clearList(View view) {
        mProgress.setProgress(0);
        viewLines.clear();
    }
}