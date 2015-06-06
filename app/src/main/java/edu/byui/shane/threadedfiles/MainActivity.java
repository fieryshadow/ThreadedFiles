package edu.byui.shane.threadedfiles;

import android.content.Context;
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


public class MainActivity extends ActionBarActivity {
    private ProgressBar mProgress;
    private List<String> loadedLines;
    private ArrayAdapter<String> linesView;
    private Handler mHandler = new Handler();
    private final String filename = "numbers.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        loadedLines = new ArrayList<>();
        linesView = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, loadedLines);
        ((ListView) findViewById(R.id.listView)).setAdapter(linesView);
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

    /**
     * Allows you to easily display a toast from any thread.
     * @param text
     */
    public void showToast(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Allows you to easily refresh the ArrayAdapter connected to R.id.listView from any thread.
     */
    public void updateListView() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                linesView.notifyDataSetChanged();
            }
        });
    }

    /**
     * Spawns a new thread which creates a "large" file.
     * @param view
     */
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

                    // write data to the file and update the progress bar along the way
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

    /**
     * Loads a "large" file using a separate thread.
     * @param view
     */
    public void loadFile(View view) {
        final Context context = getApplicationContext();
        mProgress.setProgress(0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File f = new File(context.getFilesDir(), filename);
                    BufferedReader file = new BufferedReader(new java.io.FileReader(f));

                    // load data into list and update progress bar to correspond
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

    /**
     * Clears the list on the main screen, and sets the progress bar to 0.
     * @param view
     */
    public void clearList(View view) {
        mProgress.setProgress(0);
        linesView.clear();
    }
}