package com.winlator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.winlator.box86_64.Box86_64EditPresetDialog;
import com.winlator.box86_64.Box86_64Preset;
import com.winlator.box86_64.Box86_64PresetManager;
import com.winlator.contentdialog.ContentDialog;
import com.winlator.core.AppUtils;
import com.winlator.core.ArrayUtils;
import com.winlator.core.Callback;
import com.winlator.core.FileUtils;
import com.winlator.midi.MidiManager;
import com.winlator.xenvironment.RootFSInstaller;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsFragment extends Fragment {
    public static final String DEFAULT_WINE_DEBUG_CHANNELS = "warn,err,fixme";
    private SharedPreferences preferences;
    private Callback<Uri> installSoundFontCallback;
    private static final int REQUEST_CODE_INSTALL_SOUNDFONT = 1001;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.settings);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        final Context context = getContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        // --- Theme (RadioGroup) ---
        final RadioGroup rgAppTheme = view.findViewById(R.id.RGAppTheme);
        if (rgAppTheme != null) {
            rgAppTheme.check(isDarkMode ? R.id.RBDark : R.id.RBLight);
        }

        // --- Cursor speed ---
        final com.winlator.widget.SeekBar sbCursorSpeed = view.findViewById(R.id.SBCursorSpeed);
        if (sbCursorSpeed != null) {
            sbCursorSpeed.setValue(preferences.getFloat("cursor_speed", 1.0f) * 100f);
        }

        // --- Cursor size ---
        final com.winlator.widget.SeekBar sbCursorSize = view.findViewById(R.id.SBCursorSize);
        if (sbCursorSize != null) {
            sbCursorSize.setValue(preferences.getFloat("cursor_size", 1.0f) * 100f);
        }

        // --- Move cursor to touchpoint ---
        final CheckBox cbMoveCursorToTouchpoint = view.findViewById(R.id.CBMoveCursorToTouchpoint);
        if (cbMoveCursorToTouchpoint != null) {
            cbMoveCursorToTouchpoint.setChecked(preferences.getBoolean("move_cursor_to_touchpoint", false));
        }

        // --- Capture pointer on external mouse ---
        final CheckBox cbCapturePointerOnExternalMouse = view.findViewById(R.id.CBCapturePointerOnExternalMouse);
        if (cbCapturePointerOnExternalMouse != null) {
            cbCapturePointerOnExternalMouse.setChecked(preferences.getBoolean("capture_pointer_on_external_mouse", false));
        }

        // --- Preferred input API ---
        final Spinner sPreferredInputApi = view.findViewById(R.id.SPreferredInputApi);
        if (sPreferredInputApi != null) {
            AppUtils.setSpinnerSelectionFromValue(sPreferredInputApi, preferences.getString("preferred_input_api", "Auto"));
        }

        // --- Sound font ---
        final Spinner sSoundFont = view.findViewById(R.id.SSoundFont);
        if (sSoundFont != null) {
            MidiManager.loadSFSpinner(sSoundFont);
            AppUtils.setSpinnerSelectionFromValue(sSoundFont, preferences.getString("sound_font", ""));
        }

        // Sound font toolbox buttons (by tag)
        View soundFontToolbox = view.findViewById(R.id.SoundFontToolbox);
        if (soundFontToolbox instanceof ViewGroup) {
            ViewGroup toolbox = (ViewGroup) soundFontToolbox;
            for (int i = 0; i < toolbox.getChildCount(); i++) {
                View child = toolbox.getChildAt(i);
                Object tag = child.getTag();
                if ("install".equals(tag)) {
                    child.setOnClickListener(v -> {
                        installSoundFontCallback = uri -> {
                            MidiManager.installSF2File(context, uri, new MidiManager.OnSoundFontInstalledCallback() {
                                @Override
                                public void onSuccess() {
                                    requireActivity().runOnUiThread(() -> {
                                        ContentDialog.alert(context, R.string.sound_font_installed_success, null);
                                        if (sSoundFont != null) MidiManager.loadSFSpinner(sSoundFont);
                                    });
                                }
                                @Override
                                public void onFailed(int reason) {
                                    int resId = reason == MidiManager.ERROR_BADFORMAT
                                            ? R.string.sound_font_bad_format
                                            : (reason == MidiManager.ERROR_EXIST
                                            ? R.string.sound_font_already_exist
                                            : R.string.sound_font_installed_failed);
                                    requireActivity().runOnUiThread(() -> ContentDialog.alert(context, resId, null));
                                }
                            });
                        };
                        openFile(REQUEST_CODE_INSTALL_SOUNDFONT);
                    });
                } else if ("remove".equals(tag)) {
                    child.setOnClickListener(v -> {
                        if (sSoundFont != null && sSoundFont.getSelectedItemPosition() != 0) {
                            ContentDialog.confirm(context, R.string.do_you_want_to_remove_this_sound_font, () -> {
                                if (MidiManager.removeSF2File(context, sSoundFont.getSelectedItem().toString())) {
                                    AppUtils.showToast(context, R.string.sound_font_removed_success);
                                    MidiManager.loadSFSpinner(sSoundFont);
                                } else {
                                    AppUtils.showToast(context, R.string.sound_font_removed_failed);
                                }
                            });
                        } else {
                            AppUtils.showToast(context, R.string.cannot_remove_default_sound_font);
                        }
                    });
                }
            }
        }

        // --- Language ---
        final Spinner sLanguage = view.findViewById(R.id.SLanguage);
        if (sLanguage != null) {
            AppUtils.setSpinnerSelectionFromValue(sLanguage, preferences.getString("language", "English"));
        }

        // --- Open Android browser from Wine ---
        final CheckBox cbOpenAndroidBrowser = view.findViewById(R.id.CBOpenAndroidBrowserFromWine);
        if (cbOpenAndroidBrowser != null) {
            cbOpenAndroidBrowser.setChecked(preferences.getBoolean("open_with_android_browser", false));
        }

        // --- Use Android clipboard on Wine ---
        final CheckBox cbUseAndroidClipboard = view.findViewById(R.id.CBUseAndroidClipboardOnWine);
        if (cbUseAndroidClipboard != null) {
            cbUseAndroidClipboard.setChecked(preferences.getBoolean("share_android_clipboard", false));
        }

        // --- Box64 preset ---
        final Spinner sBox64Preset = view.findViewById(R.id.SBox64Preset);
        loadBox64PresetSpinners(view, sBox64Preset);

        // --- Wine debug ---
        final CheckBox cbEnableWineDebug = view.findViewById(R.id.CBEnableWineDebug);
        if (cbEnableWineDebug != null) {
            cbEnableWineDebug.setChecked(preferences.getBoolean("enable_wine_debug", false));
        }

        final ArrayList<String> wineDebugChannels = new ArrayList<>(
                Arrays.asList(preferences.getString("wine_debug_channels", DEFAULT_WINE_DEBUG_CHANNELS).split(",")));
        loadWineDebugChannels(view, wineDebugChannels);

        // --- Box64 logs ---
        final Spinner sBox64Logs = view.findViewById(R.id.SBox64Logs);
        if (sBox64Logs != null) {
            AppUtils.setSpinnerSelectionFromValue(sBox64Logs, preferences.getString("box64_logs", "Disable"));
        }

        // --- Save logs to file ---
        final CheckBox cbSaveLogsToFile = view.findViewById(R.id.CBSaveLogsToFile);
        final EditText etLogFile = view.findViewById(R.id.ETLogFile);
        if (cbSaveLogsToFile != null) {
            boolean saveLogs = preferences.getBoolean("save_logs_to_file", false);
            cbSaveLogsToFile.setChecked(saveLogs);
            if (etLogFile != null) {
                etLogFile.setVisibility(saveLogs ? View.VISIBLE : View.GONE);
                etLogFile.setText(preferences.getString("log_file", ""));
                cbSaveLogsToFile.setOnCheckedChangeListener((btn, checked) ->
                        etLogFile.setVisibility(checked ? View.VISIBLE : View.GONE));
            }
        }

        // --- Reinstall system files ---
        View btnReinstall = view.findViewById(R.id.BTReinstallSystemFiles);
        if (btnReinstall != null) {
            btnReinstall.setOnClickListener(v ->
                    ContentDialog.confirm(context, R.string.do_you_want_to_reinstall_imagefs,
                            () -> RootFSInstaller.install((MainActivity) getActivity())));
        }

        // --- Confirm / Save ---
        view.findViewById(R.id.BTConfirm).setOnClickListener(v -> {
            SharedPreferences.Editor editor = preferences.edit();

            // Theme
            if (rgAppTheme != null) {
                editor.putBoolean("dark_mode", rgAppTheme.getCheckedRadioButtonId() == R.id.RBDark);
            }
            // Cursor
            if (sbCursorSpeed != null) editor.putFloat("cursor_speed", sbCursorSpeed.getValue() / 100f);
            if (sbCursorSize != null) editor.putFloat("cursor_size", sbCursorSize.getValue() / 100f);
            if (cbMoveCursorToTouchpoint != null)
                editor.putBoolean("move_cursor_to_touchpoint", cbMoveCursorToTouchpoint.isChecked());
            if (cbCapturePointerOnExternalMouse != null)
                editor.putBoolean("capture_pointer_on_external_mouse", cbCapturePointerOnExternalMouse.isChecked());
            // Input
            if (sPreferredInputApi != null)
                editor.putString("preferred_input_api", sPreferredInputApi.getSelectedItem().toString());
            // Sound
            if (sSoundFont != null)
                editor.putString("sound_font", sSoundFont.getSelectedItem().toString());
            // Language
            if (sLanguage != null)
                editor.putString("language", sLanguage.getSelectedItem().toString());
            // Browser / Clipboard
            if (cbOpenAndroidBrowser != null)
                editor.putBoolean("open_with_android_browser", cbOpenAndroidBrowser.isChecked());
            if (cbUseAndroidClipboard != null)
                editor.putBoolean("share_android_clipboard", cbUseAndroidClipboard.isChecked());
            // Box64 preset
            if (sBox64Preset != null)
                editor.putString("box64_preset", Box86_64PresetManager.getSpinnerSelectedId(sBox64Preset));
            // Wine debug
            if (cbEnableWineDebug != null)
                editor.putBoolean("enable_wine_debug", cbEnableWineDebug.isChecked());
            if (!wineDebugChannels.isEmpty()) {
                editor.putString("wine_debug_channels", String.join(",", wineDebugChannels));
            } else {
                editor.remove("wine_debug_channels");
            }
            // Logs
            if (sBox64Logs != null)
                editor.putString("box64_logs", sBox64Logs.getSelectedItem().toString());
            if (cbSaveLogsToFile != null)
                editor.putBoolean("save_logs_to_file", cbSaveLogsToFile.isChecked());
            if (etLogFile != null)
                editor.putString("log_file", etLogFile.getText().toString());

            if (editor.commit()) {
                NavigationView navigationView = getActivity().findViewById(R.id.NavigationView);
                if (navigationView != null)
                    navigationView.setCheckedItem(R.id.main_menu_containers);
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.FLFragmentContainer, new ContainersFragment())
                        .commit();
            }
        });

        return view;
    }

    private void loadBox64PresetSpinners(View view, final Spinner sBox64Preset) {
        if (sBox64Preset == null) return;
        final Context context = getContext();

        Callback<String> updateSpinner = prefix ->
                Box86_64PresetManager.loadSpinner(prefix, sBox64Preset,
                        preferences.getString(prefix + "_preset", Box86_64Preset.COMPATIBILITY));

        Callback<String> onAddPreset = prefix -> {
            Box86_64EditPresetDialog dialog = new Box86_64EditPresetDialog(context, prefix, null);
            dialog.setOnConfirmCallback(() -> updateSpinner.call(prefix));
            dialog.show();
        };

        Callback<String> onEditPreset = prefix -> {
            Box86_64EditPresetDialog dialog = new Box86_64EditPresetDialog(context, prefix,
                    Box86_64PresetManager.getSpinnerSelectedId(sBox64Preset));
            dialog.setOnConfirmCallback(() -> updateSpinner.call(prefix));
            dialog.show();
        };

        Callback<String> onDuplicatePreset = prefix ->
                ContentDialog.confirm(context, R.string.do_you_want_to_duplicate_this_preset, () -> {
                    Box86_64PresetManager.duplicatePreset(prefix, context,
                            Box86_64PresetManager.getSpinnerSelectedId(sBox64Preset));
                    updateSpinner.call(prefix);
                    sBox64Preset.setSelection(sBox64Preset.getCount() - 1);
                });

        Callback<String> onRemovePreset = prefix -> {
            final String presetId = Box86_64PresetManager.getSpinnerSelectedId(sBox64Preset);
            if (!presetId.startsWith(Box86_64Preset.CUSTOM)) {
                AppUtils.showToast(context, R.string.you_cannot_remove_this_preset);
                return;
            }
            ContentDialog.confirm(context, R.string.do_you_want_to_remove_this_preset, () -> {
                Box86_64PresetManager.removePreset(prefix, context, presetId);
                updateSpinner.call(prefix);
            });
        };

        updateSpinner.call("box64");

        View btnAdd = view.findViewById(R.id.BTAddBox64Preset);
        View btnEdit = view.findViewById(R.id.BTEditBox64Preset);
        View btnDuplicate = view.findViewById(R.id.BTDuplicateBox64Preset);
        View btnRemove = view.findViewById(R.id.BTRemoveBox64Preset);

        if (btnAdd != null) btnAdd.setOnClickListener(v2 -> onAddPreset.call("box64"));
        if (btnEdit != null) btnEdit.setOnClickListener(v2 -> onEditPreset.call("box64"));
        if (btnDuplicate != null) btnDuplicate.setOnClickListener(v2 -> onDuplicatePreset.call("box64"));
        if (btnRemove != null) btnRemove.setOnClickListener(v2 -> onRemovePreset.call("box64"));
    }

    private void loadWineDebugChannels(final View view, final ArrayList<String> debugChannels) {
        final Context context = getContext();
        LinearLayout container = view.findViewById(R.id.LLWineDebugChannels);
        if (container == null) return;
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.wine_debug_channel_list_item, container, false);
        itemView.findViewById(R.id.TextView).setVisibility(View.GONE);
        itemView.findViewById(R.id.BTRemove).setVisibility(View.GONE);

        View addButton = itemView.findViewById(R.id.BTAdd);
        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener(v -> {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(FileUtils.readString(context, "wine_debug_channels.json"));
            } catch (JSONException e) {
                // ignored
            }
            final String[] items = ArrayUtils.toStringArray(jsonArray);
            ContentDialog.showMultipleChoiceList(context, R.string.wine_debug_channel, items, selectedPositions -> {
                for (int pos : selectedPositions)
                    if (!debugChannels.contains(items[pos])) debugChannels.add(items[pos]);
                loadWineDebugChannels(view, debugChannels);
            });
        });

        View resetButton = itemView.findViewById(R.id.BTReset);
        resetButton.setVisibility(View.VISIBLE);
        resetButton.setOnClickListener(v -> {
            debugChannels.clear();
            debugChannels.addAll(Arrays.asList(DEFAULT_WINE_DEBUG_CHANNELS.split(",")));
            loadWineDebugChannels(view, debugChannels);
        });
        container.addView(itemView);

        for (int i = 0; i < debugChannels.size(); i++) {
            itemView = inflater.inflate(R.layout.wine_debug_channel_list_item, container, false);
            TextView textView = itemView.findViewById(R.id.TextView);
            textView.setText(debugChannels.get(i));
            final int index = i;
            itemView.findViewById(R.id.BTRemove).setOnClickListener(v -> {
                debugChannels.remove(index);
                loadWineDebugChannels(view, debugChannels);
            });
            container.addView(itemView);
        }
    }

    private void openFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        getActivity().startActivityFromFragment(this, intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == REQUEST_CODE_INSTALL_SOUNDFONT && installSoundFontCallback != null) {
                try {
                    installSoundFontCallback.call(data.getData());
                } catch (Exception e) {
                    AppUtils.showToast(getContext(), R.string.unable_to_install_soundfont);
                } finally {
                    installSoundFontCallback = null;
                }
            }
        }
    }

    // Static helpers called from other classes
    public static void resetEmulatorsVersion(AppCompatActivity activity) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        prefs.edit()
                .remove("current_box64_version")
                .remove("current_wowbox64_version")
                .remove("current_fexcore_version")
                .apply();
    }

    public static void resetBox64Version(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .remove("current_box64_version")
                .apply();
    }
}
