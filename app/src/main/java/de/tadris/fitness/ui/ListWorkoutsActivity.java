/*
 * Copyright (c) 2020 Jannis Scheibe <jannis@tadris.de>
 *
 * This file is part of FitoTrack
 *
 * FitoTrack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     FitoTrack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tadris.fitness.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.InputStream;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.Workout;
import de.tadris.fitness.data.WorkoutType;
import de.tadris.fitness.ui.adapter.WorkoutAdapter;
import de.tadris.fitness.ui.dialog.ProgressDialogController;
import de.tadris.fitness.ui.dialog.SelectWorkoutTypeDialog;
import de.tadris.fitness.ui.record.RecordWorkoutActivity;
import de.tadris.fitness.ui.settings.MainSettingsActivity;
import de.tadris.fitness.ui.workout.AggregatedWorkoutStatisticsActivity;
import de.tadris.fitness.ui.workout.EnterWorkoutActivity;
import de.tadris.fitness.ui.workout.ShowWorkoutActivity;
import de.tadris.fitness.util.DialogUtils;
import de.tadris.fitness.util.Icon;
import de.tadris.fitness.util.io.general.IOHelper;

public class ListWorkoutsActivity extends FitoTrackActivity implements WorkoutAdapter.WorkoutAdapterListener {

    private RecyclerView listView;
    private WorkoutAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private FloatingActionMenu menu;
    private Workout[] workouts;
    private TextView hintText;
    private int listSize;
    private int lastClickedIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_workouts);

        listView = findViewById(R.id.workoutList);
        listView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        listView.setLayoutManager(layoutManager);
        adapter = new WorkoutAdapter(workouts, this);
        listView.setAdapter(adapter);

        menu = findViewById(R.id.workoutListMenu);
        menu.setOnMenuButtonLongClickListener(v -> {
            if (workouts.length > 0) {
                startRecording(workouts[0].getWorkoutType(this));
                return true;
            } else {
                return false;
            }
        });

        hintText = findViewById(R.id.hintAddWorkout);

        findViewById(R.id.workoutListRecord).setOnClickListener(v -> showWorkoutSelection());
        findViewById(R.id.workoutListEnter).setOnClickListener(v -> startEnterWorkoutActivity());
        findViewById(R.id.workoutListImport).setOnClickListener(v -> showImportDialog());

        checkFirstStart();

        refresh();
    }

    private final Handler mHandler = new Handler();

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
        }
    }

    private void showImportDialog() {
        if(!hasPermission()){
            requestPermissions();
            return;
        }
        importWorkout();
        refresh();
        menu.close(true);
    }

    private static final int FILE_SELECT_CODE= 21;
    private void importWorkout(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.importWorkout)), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ignored) { }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                importFile(data.getData());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void importFile(Uri uri){
        ProgressDialogController dialogController= new ProgressDialogController(this, getString(R.string.importWorkout));
        dialogController.show();

        new Thread(() -> {
            try {
                InputStream stream = getContentResolver().openInputStream(uri);
                IOHelper.GpxImporter.importWorkout(getApplicationContext(), stream);
                mHandler.post(() -> {
                    Toast.makeText(this, R.string.workoutImported, Toast.LENGTH_LONG).show();
                    dialogController.cancel();
                    refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.post(() -> {
                    dialogController.cancel();
                    showErrorDialog(e, R.string.error, R.string.errorImportFailed);
                });
            }
        }).start();
    }

    private void checkFirstStart() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("firstStart", true)) {
            preferences.edit().putBoolean("firstStart", false).apply();
            new AlertDialog.Builder(this)
                    .setTitle(R.string.setPreferencesTitle)
                    .setMessage(R.string.setPreferencesMessage)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.settings, (dialog, which) -> startActivity(new Intent(ListWorkoutsActivity.this, MainSettingsActivity.class)))
                    .create().show();
        }
    }

    private void startEnterWorkoutActivity() {
        menu.close(true);
        final Intent intent = new Intent(this, EnterWorkoutActivity.class);
        new Handler().postDelayed(() -> startActivity(intent), 300);
    }

    private void showWorkoutSelection() {
        menu.close(true);
        new SelectWorkoutTypeDialog(this, this::startRecording).show();
    }

    private void startRecording(WorkoutType activity) {
        menu.close(true);
        final Intent intent = new Intent(this, RecordWorkoutActivity.class);
        intent.setAction(RecordWorkoutActivity.LAUNCH_ACTION);
        intent.putExtra(RecordWorkoutActivity.WORKOUT_TYPE_EXTRA, activity);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
        menu.close(true);
    }

    @Override
    public void onItemClick(int pos, Workout workout) {
        final Intent intent = new Intent(this, ShowWorkoutActivity.class);
        intent.putExtra(ShowWorkoutActivity.WORKOUT_ID_EXTRA, workout.id);
        startActivity(intent);
        lastClickedIndex = pos;
    }

    @Override
    public void onItemLongClick(int pos, Workout workout) {
        DialogUtils.showDeleteWorkoutDialog(this, () -> {
            Instance.getInstance(ListWorkoutsActivity.this).db.workoutDao().deleteWorkout(workout);
            refresh();
        });
    }

    private void refresh() {
        loadData();
        if (workouts.length > 0) {
            adapter.notifyItemChanged(lastClickedIndex, workouts[lastClickedIndex]);
        }
        if (listSize != workouts.length) {
            adapter.notifyDataSetChanged();
        }
        listSize = workouts.length;
        refreshFABMenu();
    }

    private void loadData() {
        workouts = Instance.getInstance(this).db.workoutDao().getWorkouts();
        hintText.setVisibility(workouts.length == 0 ? View.VISIBLE : View.INVISIBLE);
        adapter.setWorkouts(workouts);
    }

    private void refreshFABMenu() {
        FloatingActionButton lastFab = findViewById(R.id.workoutListRecordLast);
        if (workouts.length > 0) {
            WorkoutType lastType = workouts[0].getWorkoutType(this);
            lastFab.setLabelText(lastType.title);
            lastFab.setImageResource(Icon.getIcon(lastType.icon));
            lastFab.setColorNormal(getResources().getColor(lastType.color));
            lastFab.setColorPressed(lastFab.getColorNormal());
            lastFab.setOnClickListener(v -> {
                menu.close(true);
                new Handler().postDelayed(() -> startRecording(lastType), 300);
            });
        } else {
            lastFab.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_workout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.actionOpenSettings) {
            startActivity(new Intent(this, MainSettingsActivity.class));
            return true;
        }

        if (id == R.id.actionOpenStatisticss) {
            startActivity(new Intent(this, AggregatedWorkoutStatisticsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
