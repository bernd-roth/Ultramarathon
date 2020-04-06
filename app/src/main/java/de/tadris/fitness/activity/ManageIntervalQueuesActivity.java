package de.tadris.fitness.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.IntervalQueue;
import de.tadris.fitness.view.IntervalQueueAdapter;

public class ManageIntervalQueuesActivity extends Activity implements IntervalQueueAdapter.IntervalQueueAdapterListener {

    private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_interval_queues);

        recyclerView= findViewById(R.id.intervalQueuesList);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

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
    }

    @Override
    public void onItemSelect(int pos, IntervalQueue queue) {
        
    }

    @Override
    public void onItemDelete(int pos, IntervalQueue queue) {

    }
}
