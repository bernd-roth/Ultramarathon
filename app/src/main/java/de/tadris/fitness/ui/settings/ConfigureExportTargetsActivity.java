/*
 * Copyright (c) 2021 Jannis Scheibe <jannis@tadris.de>
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

public class ConfigureExportTargetsActivity extends FitoTrackActivity
        implements ExportTargetConfigurationAdapter.ExportTargetAdapterListener,
        SelectExportTargetTypeDialog.ExportTargetTypeSelectListener {

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
        Instance.getInstance(this).db.exportTargetDao().delete(configuration);
    }
}