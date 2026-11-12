
package com.pcrecompile.launcher;

import android.content.Context;
import android.net.Uri;

public final class RuntimeManager {

    public RuntimeManager(Context context) {
    }

    public boolean isInstalled() {
        // Wine belum diintegrasikan.
        return false;
    }

    public String launch(Uri exe) {
        if (exe == null) {
            return "Pilih EXE terlebih dahulu.";
        }

        if (!isInstalled()) {
            return "Runtime Windows belum terpasang.";
        }

        return "Runtime belum memiliki konfigurasi peluncuran.";
    }
}
