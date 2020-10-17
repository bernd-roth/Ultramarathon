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

package de.tadris.fitness.ui.settings;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.IOException;

import de.tadris.fitness.BuildConfig;
import de.tadris.fitness.R;
import de.tadris.fitness.export.BackupController;
import de.tadris.fitness.export.RestoreController;
import de.tadris.fitness.ui.ShareFileActivity;
import de.tadris.fitness.ui.dialog.ProgressDialogController;
import de.tadris.fitness.ui.dialog.ThreadSafeProgressDialogController;
import de.tadris.fitness.util.io.general.IOHelper;

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
        findPreference("importMultiple").setOnPreferenceClickListener(preference -> {
            showMassImportGpx();
            return true;
        });

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
                String file = getFilesDir().getAbsolutePath() + "/shared/backup" + System.currentTimeMillis() + ".ftb";
                File parent = new File(file).getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IOException("Cannot write");
                }
                Uri uri = FileProvider.getUriForFile(getBaseContext(), BuildConfig.APPLICATION_ID + ".fileprovider", new File(file));

                BackupController backupController = new BackupController(getBaseContext(), new File(file), (progress, action) -> mHandler.post(() -> dialogController.setProgress(progress, action)));
                backupController.exportData();

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
    private static final int FOLDER_IMPORT_SELECT_CODE = 23;

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
                case FOLDER_IMPORT_SELECT_CODE:
                    massImportGpx(data.getData());
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

    private void showMassImportGpx() {
        if (!hasPermission()) {
            requestPermissions();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.importMultipleGpxFiles)
                .setMessage(R.string.importMultipleMessageSelectFolder)
                .setPositiveButton(R.string.okay, (dialog, which) -> openMassImportFolderSelector())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openMassImportFolderSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, FOLDER_IMPORT_SELECT_CODE);
    }

    private void massImportGpx(Uri dirUri) {
        Log.d("MassImport", dirUri.toString());
        ThreadSafeProgressDialogController dialog = new ThreadSafeProgressDialogController(this, getString(R.string.importingFiles));
        dialog.show();
        new Thread(() -> {
            try {
                int imported = 0;
                DocumentFile documentFile = DocumentFile.fromTreeUri(this, dirUri);
                DocumentFile[] files = documentFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    dialog.setProgress(100 * i / files.length);
                    DocumentFile file = files[i];
                    if (file.isFile() && file.canRead()) {
                        try {
                            Uri fileUri = file.getUri();
                            Log.d("MassImport", "Importing " + fileUri.toString());
                            IOHelper.GpxImporter.importWorkout(this, getContentResolver().openInputStream(fileUri));
                            imported++;
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (imported == 0 && i == files.length - 1) {
                                // If all workouts failed throw exception so it is shown to the user
                                throw e;
                            }
                        }
                    }
                }
                dialog.setProgress(100);
                final int tmpImported = imported; // Needs to be a final variable to use in the handler lambda
                mHandler.post(() -> {
                    dialog.cancel();
                    Toast.makeText(this, String.format(getString(R.string.importedWorkouts), tmpImported), Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                mHandler.post(() -> {
                    dialog.cancel();
                    Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

}
