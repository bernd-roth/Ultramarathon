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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;

import de.tadris.fitness.BuildConfig;
import de.tadris.fitness.R;
import de.tadris.fitness.export.RestoreController;
import de.tadris.fitness.ui.ShareFileActivity;
import de.tadris.fitness.ui.dialog.ProgressDialogController;
import de.tadris.fitness.util.autoexport.source.BackupExportSource;
import de.tadris.fitness.util.autoexport.source.ExportSource;

public class BackupSettingsActivity extends FitoTrackSettingsActivity {

    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setTitle(R.string.preferencesBackupTitle);

        addPreferencesFromResource(R.xml.preferences_backup);

        findPreference("import").setOnPreferenceClickListener(preference -> {
            showImportDialog();
            return true;
        });
        findPreference("export").setOnPreferenceClickListener(preference -> {
            showExportDialog();
            return true;
        });
        findPreference("autoExportWorkouts").setOnPreferenceClickListener(preference -> {
            startExportTargetActivity(ExportSource.EXPORT_SOURCE_WORKOUT_GPX);
            return true;
        });
        findPreference("autoExportBackup").setOnPreferenceClickListener(preference -> {
            startExportTargetActivity(ExportSource.EXPORT_SOURCE_BACKUP);
            return true;
        });

    }

    private void startExportTargetActivity(String exportSource) {
        Intent intent = new Intent(this, ConfigureExportTargetsActivity.class);
        intent.putExtra(ConfigureExportTargetsActivity.EXTRA_SOURCE, exportSource);
        startActivity(intent);
    }

    private void showExportDialog() {
        if (!hasPermission()) {
            requestPermissions();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.exportData)
                .setMessage(R.string.exportDataSummary)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.backup, (dialog, which) -> exportBackup()).create().show();
    }

    private void exportBackup() {
        ProgressDialogController dialogController = new ProgressDialogController(this, getString(R.string.backup));
        dialogController.show();
        new Thread(() -> {
            try {
                File file = new BackupExportSource().provideFile(this, (progress, action) -> mHandler.post(() -> dialogController.setProgress(progress, action)));
                Uri uri = FileProvider.getUriForFile(getBaseContext(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
                mHandler.post(() -> {
                    dialogController.cancel();
                    Intent intent = new Intent(this, ShareFileActivity.class);
                    intent.putExtra(ShareFileActivity.EXTRA_FILE_URI, uri.toString());
                    startActivity(intent);
                });
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.post(() -> {
                    dialogController.cancel();
                    showErrorDialog(e, R.string.error, R.string.errorExportFailed);
                });
            }
        }).start();
    }

    private void showImportDialog() {
        if (!hasPermission()) {
            requestPermissions();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.importBackup)
                .setMessage(R.string.replaceOrMergeMessage)
                .setPositiveButton(R.string.replace, ((dialog, which) -> {
                    showReplaceImport();
                }))
                .setNegativeButton(R.string.merge, ((dialog, which) -> {
                    showMergeImport();
                }))
                .show();

    }

    private void requestPermissions() {
        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        }
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private static final int FILE_REPLACE_SELECT_CODE = 21;
    private static final int FILE_MERGE_SELECT_CODE = 22;

    private void showMergeImport() {
        importBackup(FILE_MERGE_SELECT_CODE);
    }

    private void showReplaceImport() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.importBackup)
                .setMessage(R.string.importBackupMessage)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.restore, (dialog, which) -> importBackup(FILE_REPLACE_SELECT_CODE)).create().show();
    }

    private void importBackup(final int selectCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.chooseBackupFile)), selectCode);
        } catch (android.content.ActivityNotFoundException ignored) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case FILE_REPLACE_SELECT_CODE:
                    importBackup(data.getData(), true);
                    break;
                case FILE_MERGE_SELECT_CODE:
                    importBackup(data.getData(), false);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void importBackup(Uri uri, boolean replace) {
        ProgressDialogController dialogController = new ProgressDialogController(this, getString(R.string.backup));
        dialogController.show();
        new Thread(() -> {
            try {
                RestoreController restoreController = new RestoreController(getBaseContext(), uri, replace,
                        (progress, action) -> mHandler.post(() -> dialogController.setProgress(progress, action)));
                restoreController.restoreData();

                mHandler.post(dialogController::cancel);
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.post(() -> {
                    dialogController.cancel();
                    showErrorDialog(e, R.string.error, R.string.errorImportFailed);
                });
            }
        }).start();
    }

}
