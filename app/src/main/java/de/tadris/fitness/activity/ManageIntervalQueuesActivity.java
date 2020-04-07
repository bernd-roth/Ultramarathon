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
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.IntervalQueue;
import de.tadris.fitness.view.IntervalQueueAdapter;

public class ManageIntervalQueuesActivity extends FitoTrackActivity implements IntervalQueueAdapter.IntervalQueueAdapterListener {

    private RecyclerView recyclerView;
    private TextView hint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_interval_queues);

        setTitle(R.string.manageIntervals);
        setupActionBar();

        recyclerView= findViewById(R.id.intervalQueuesList);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        findViewById(R.id.intervalQueuesAdd).setOnClickListener(v -> showCreateDialog());
        hint = findViewById(R.id.intervalQueuesHint);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        IntervalQueue[] queues = Instance.getInstance(this).db.intervalDao().getVisibleQueues();
        RecyclerView.Adapter adapter = new IntervalQueueAdapter(queues, this);
        recyclerView.setAdapter(adapter);
        hint.setVisibility(queues.length == 0 ? View.VISIBLE : View.INVISIBLE);
    }

    void showCreateDialog() {
        EditText text = new EditText(this);
        text.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        AlertDialog.Builder db = new AlertDialog.Builder(this);
        db.setTitle(R.string.createIntervalSet);
        db.setView(text);
        db.setPositiveButton(R.string.create, (dialog, which) -> createIntervalQueue(text.getText().toString()));
        db.create().show();

        requestKeyboard(text);
    }

    void createIntervalQueue(String name) {
        IntervalQueue queue = new IntervalQueue();
        queue.id = System.currentTimeMillis();
        queue.name = name;
        queue.state = IntervalQueue.STATE_VISIBLE;
        Instance.getInstance(this).db.intervalDao().insertIntervalQueue(queue);
        startEditQueueActivity(queue);
    }

    public void startEditQueueActivity(IntervalQueue queue) {
        Intent intent = new Intent(this, EditIntervalQueueActivity.class);
        intent.putExtra("queueId", queue.id);
        startActivity(intent);
    }

    @Override
    public void onItemSelect(int pos, IntervalQueue queue) {
        startEditQueueActivity(queue);
    }

    @Override
    public void onItemDelete(int pos, IntervalQueue queue) {

    }
}
