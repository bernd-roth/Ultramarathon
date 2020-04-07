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

package de.tadris.fitness.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.Interval;
import de.tadris.fitness.data.IntervalQueue;
import de.tadris.fitness.view.IntervalAdapter;

public class EditIntervalQueueActivity extends FitoTrackActivity implements IntervalAdapter.IntervalAdapterListener {

    private RecyclerView recyclerView;
    private IntervalAdapter adapter;
    private IntervalQueue queue;
    private long queueId;
    private TextView intervalQueueHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_interval_queue);

        queueId = getIntent().getExtras().getLong("queueId", -1);
        if (queueId == -1) {
            finish();
            return;
        }

        queue = Instance.getInstance(this).db.intervalDao().getQueue(queueId);

        setTitle(queue.name);
        setupActionBar();

        recyclerView = findViewById(R.id.intervalsList);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        findViewById(R.id.intervalAdd).setOnClickListener(v -> showAddDialog());
        intervalQueueHint = findViewById(R.id.intervalQueueHint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_interval_queue_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.actionDeleteIntervalQueue) {
            showDeleteQueueDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        Interval[] intervals = Instance.getInstance(this).db.intervalDao().getAllIntervalsOfQueue(queueId);
        adapter = new IntervalAdapter(new ArrayList<>(Arrays.asList(intervals)), this);
        recyclerView.setAdapter(adapter);
        intervalQueueHint.setVisibility(intervals.length == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onItemDelete(int pos, Interval interval) {
        Instance.getInstance(this).db.intervalDao().deleteInterval(interval);
        adapter.intervals.remove(interval);
        adapter.notifyItemRemoved(pos);
        if (adapter.intervals.size() == 0) {
            intervalQueueHint.setVisibility(View.VISIBLE);
        }
    }

    private void showAddDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_interval)
                .setView(R.layout.dialog_add_interval)
                .setPositiveButton(R.string.add, null) // Listener added later so that we can control if the dialog is dismissed on click
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            requestKeyboard(dialog.findViewById(R.id.intervalName));
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                EditText nameEditText = dialog.findViewById(R.id.intervalName);
                EditText lengthText = dialog.findViewById(R.id.intervalLengthInMinutes);
                String name = nameEditText.getText().toString();
                if (name.length() <= 2) {
                    nameEditText.setError(getString(R.string.enterName));
                    nameEditText.requestFocus();
                    return;
                }
                int lengthInMinutes;
                try {
                    lengthInMinutes = Integer.parseInt(lengthText.getText().toString());
                } catch (NumberFormatException e) {
                    lengthText.setError(getString(R.string.errorEnterValidNumber));
                    lengthText.requestFocus();
                    return;
                }
                if (lengthInMinutes < 1 || lengthInMinutes > 300) {
                    lengthText.setError(getString(R.string.errorEnterValidDuration));
                    lengthText.requestFocus();
                    return;
                }

                Interval interval = new Interval();
                interval.id = System.currentTimeMillis();
                interval.name = name;
                interval.delayMillis = TimeUnit.MINUTES.toMillis(1) * lengthInMinutes;
                interval.queueId = queueId;
                addInterval(interval);

                dialog.dismiss();
            });
        });
        dialog.show();

    }

    private void addInterval(Interval interval) {
        Instance.getInstance(this).db.intervalDao().insertInterval(interval);
        adapter.intervals.add(interval);
        adapter.notifyItemInserted(adapter.intervals.size() - 1);
        intervalQueueHint.setVisibility(View.INVISIBLE);
    }

    private void showDeleteQueueDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.deleteIntervalQueue)
                .setMessage(R.string.deleteIntervalQueueMessage)
                .setPositiveButton(R.string.delete, (dialogInterface, which) -> deleteQueue())
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }

    private void deleteQueue() {
        Instance.getInstance(this).db.intervalDao().deleteIntervalQueue(queue);
        finish();
    }

}
