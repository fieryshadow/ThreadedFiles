package edu.byui.shane.threadedfiles;

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
     * @param text    The message you want toasted.
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
     * Allows easy setting of the progress bar from any thread.
     * @param value    The number to directly set the progress bar to.
     */
    public void setProgressBarTo(final int value) {
        mHandler.post(new Runnable() {
            public void run() {
                mProgress.setProgress(value);
            }
        });
    }

    /**
     * Allows easy incrementing of the progress bar from any thread.
     * @param amount    The amount you want to increment the progress bar by.
     */
    public void incrementProgressBarBy(final int amount) {
        mHandler.post(new Runnable() {
            public void run() {
                mProgress.incrementProgressBy(amount);
            }
        });
    }

    /**
     * Makes getting a file for reading from internal storage a one liner.
     * @param filename    The name of the file you want to open.
     * @return BufferedReader
     * @throws IOException
     */
    public BufferedReader getReadableFile(String filename) throws IOException {
        File f = new File(MainActivity.this.getFilesDir(), filename);
        return new BufferedReader(new java.io.FileReader(f));
    }

    /**
     * Makes getting a file for writing to internal storage a one liner.
     * @param filename    The name of the file you want to open.
     * @return BufferedWriter
     * @throws IOException
     */
    public BufferedWriter getWritableFile(String filename) throws IOException {
        File f = new File(MainActivity.this.getFilesDir(), filename);
        if (!f.exists()) {
            f.createNewFile();
        }
        return new BufferedWriter(new java.io.FileWriter(f));
    }

    /**
     * Spawns a new thread which creates a "large" file.
     * @param view    A view from the main activity.
     */
    public void createFile(View view) {
        new Thread(new SafeRunner() {
            @Override
            public void tryRun() throws IOException, InterruptedException {
                setProgressBarTo(0);
                BufferedWriter file = getWritableFile(filename);
                for (int i = 1; i <= 10; i++) {
                    file.write(i + "\n");
                    incrementProgressBarBy(10);
                    Thread.sleep(250);
                }
                file.close();
                showToast("File creation successful.");
            }

            @Override
            public void catchRun(Exception ex) {
                ex.printStackTrace();
                showToast("Error creating file.");
            }
        }).start();
    }

    /**
     * Loads a "large" file using a separate thread.
     * @param view    A view from the main activity.
     */
    public void loadFile(View view) {
        new Thread(new SafeRunner() {
            @Override
            public void tryRun() throws IOException, InterruptedException {
                setProgressBarTo(0);
                BufferedReader file = getReadableFile(filename);
                for (int i = 1; i <= 10; i++) {
                    loadedLines.add(file.readLine());
                    incrementProgressBarBy(10);
                    Thread.sleep(250);
                }
                file.close();
                updateListView();
                showToast("File loaded.");
            }

            @Override
            public void catchRun(Exception ex) {
                ex.printStackTrace();
                showToast("Error loading file.");
            }
        }).start();
    }

    /**
     * Clears the list on the main screen, and sets the progress bar to 0.
     * @param view    A view from the main activity.
     */
    public void clearList(View view) {
        mProgress.setProgress(0);
        linesView.clear();
    }
}

/**
 * Makes it so you don't need to put a try catch block explicitly in your code for
 * handling "run of the mill" exceptions (file IO, I'm looking at you).
 */
abstract class SafeRunner implements Runnable {
    public final void run() {
        try {
            tryRun();
        } catch (Exception ex) {
            catchRun(ex);
        }
    }

    /**
     * Override this with the code you don't want to put in a try catch block.
     * @throws Exception
     */
    public abstract void tryRun() throws Exception;

    /**
     * Override this with the code you want to execute if an exception occurs.
     * @param ex    The exception thrown in the try block method.
     */
    public abstract void catchRun(Exception ex);
}