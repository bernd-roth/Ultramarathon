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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

import de.tadris.fitness.BuildConfig;
import de.tadris.fitness.R;
import de.tadris.fitness.export.BackupController;
import de.tadris.fitness.export.RestoreController;
import de.tadris.fitness.ui.ShareFileActivity;
import de.tadris.fitness.ui.dialog.ProgressDialogController;
import de.tadris.fitness.util.DataManager;

public class BackupSettingsFragment extends FitoTrackSettingFragment {

    private final Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_backup, rootKey);

        findPreference("import").setOnPreferenceClickListener(preference -> {
            showImportDialog();
            return true;
        });
        findPreference("export").setOnPreferenceClickListener(preference -> {
            showExportDialog();
            return true;
        });
    }

    private void showExportDialog() {
        if (!hasPermission()) {
            requestPermissions();
            return;
        }
        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.exportData)
                .setMessage(R.string.exportDataSummary)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.backup, (dialog, which) -> exportBackup()).create().show();
    }

    private void exportBackup() {
        ProgressDialogController dialogController = new ProgressDialogController(requireActivity(), getString(R.string.backup));
        dialogController.show();
        new Thread(() -> {
            try {
                String file = DataManager.getSharedDirectory(requireContext()) + "/backup" + System.currentTimeMillis() + ".ftb";
                File parent = new File(file).getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IOException("Cannot write");
                }
                Uri uri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider", new File(file));

                BackupController backupController = new BackupController(requireContext(), new File(file), (progress, action) -> mHandler.post(() -> dialogController.setProgress(progress, action)));
                backupController.exportData();

                mHandler.post(() -> {
                    dialogController.cancel();
                    Intent intent = new Intent(getContext(), ShareFileActivity.class);
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
        new AlertDialog.Builder(requireActivity())
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
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
        }
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private static final int FILE_REPLACE_SELECT_CODE = 21;
    private static final int FILE_MERGE_SELECT_CODE = 22;

    private void showMergeImport() {
        importBackup(FILE_MERGE_SELECT_CODE);
    }

    private void showReplaceImport() {
        new AlertDialog.Builder(requireActivity())
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
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
        ProgressDialogController dialogController = new ProgressDialogController(requireActivity(), getString(R.string.backup));
        dialogController.show();
        new Thread(() -> {
            try {
                RestoreController restoreController = new RestoreController(requireContext(), uri, replace,
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