package de.tadris.fitness.ui.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

import de.tadris.fitness.Instance;
import de.tadris.fitness.R;
import de.tadris.fitness.data.ExportTargetConfiguration;
import de.tadris.fitness.ui.FitoTrackActivity;
import de.tadris.fitness.ui.adapter.ExportTargetConfigurationAdapter;
import de.tadris.fitness.ui.dialog.SelectExportTargetTypeDialog;
import de.tadris.fitness.util.autoexport.source.ExportSource;
import de.tadris.fitness.util.autoexport.target.DirectoryTarget;
import de.tadris.fitness.util.autoexport.target.ExportTarget;

public class ConfigureExportTargetsActivity extends FitoTrackActivity implements ExportTargetConfigurationAdapter.ExportTargetAdapterListener, SelectExportTargetTypeDialog.ExportTargetTypeSelectListener {

    public static final String EXTRA_SOURCE = "source";

    String exportSourceId = "";

    private RecyclerView recyclerView;
    private ExportTargetConfigurationAdapter adapter;
    private TextView hintText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_export_targets);

        if (getIntent().getExtras() != null) {
            exportSourceId = getIntent().getExtras().getString(EXTRA_SOURCE, "");
        }
        if (exportSourceId.isEmpty()) {
            finish();
            return;
        }
        setTitle(ExportSource.getTitle(exportSourceId));
        setupActionBar();

        recyclerView = findViewById(R.id.exportTargetsRecyclerView);
        hintText = findViewById(R.id.exportTargetsHint);
        findViewById(R.id.exportTargetsAdd).setOnClickListener(v -> showAddDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        ExportTargetConfiguration[] configurations = Instance.getInstance(this).db.exportTargetDao().findAllFor(exportSourceId);
        adapter = new ExportTargetConfigurationAdapter(new ArrayList<>(Arrays.asList(configurations)), this);
        recyclerView.setAdapter(adapter);
        hintText.setVisibility(configurations.length == 0 ? View.VISIBLE : View.GONE);
    }

    public void showAddDialog() {
        new SelectExportTargetTypeDialog(this, this).show();
    }

    @Override
    public void onTargetTypeSelect(ExportTarget target) {
        if (target instanceof DirectoryTarget) {

        }
    }

    @Override
    public void onDelete(ExportTargetConfiguration configuration) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.deleteExportTargetConfirmation)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> delete(configuration))
                .show();
    }

    private void delete(ExportTargetConfiguration configuration) {
        Instance.getInstance(this).db.exportTargetDao().delete(configuration.id);
    }
}