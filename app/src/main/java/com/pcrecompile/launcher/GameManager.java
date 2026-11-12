
package com.pcrecompile.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public final class GameManager {

    private final SharedPreferences prefs;

    public GameManager(Context context) {
        prefs = context.getSharedPreferences(
            "pc_recompile_games",
            Context.MODE_PRIVATE
        );
    }

    public void saveFolder(Uri uri) {
        prefs.edit()
            .putString("folder_uri", uri.toString())
            .apply();
    }

    public Uri getFolder() {
        String value = prefs.getString("folder_uri", null);
        return value == null ? null : Uri.parse(value);
    }

    public void saveExe(Uri uri, String name) {
        prefs.edit()
            .putString("exe_uri", uri.toString())
            .putString("exe_name", name)
            .apply();
    }

    public Uri getExe() {
        String value = prefs.getString("exe_uri", null);
        return value == null ? null : Uri.parse(value);
    }

    public String getExeName() {
        return prefs.getString("exe_name", "");
    }

    public void clearExe() {
        prefs.edit()
            .remove("exe_uri")
            .remove("exe_name")
            .apply();
    }
}
