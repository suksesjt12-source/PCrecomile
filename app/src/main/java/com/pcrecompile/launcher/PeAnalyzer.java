
package com.pcrecompile.launcher;

import android.content.ContentResolver;
import android.net.Uri;
import java.io.InputStream;

public final class PeAnalyzer {

    public static String inspect(
        ContentResolver resolver,
        Uri uri
    ) {
        try (InputStream in =
                 resolver.openInputStream(uri)) {

            if (in == null) {
                return "File tidak bisa dibaca.";
            }

            byte[] data = new byte[4096];
            int count = in.read(data);

            if (count < 64 ||
                data[0] != 'M' ||
                data[1] != 'Z') {
                return "Bukan file Windows EXE.";
            }

            int offset =
                (data[0x3c] & 255) |
                ((data[0x3d] & 255) << 8) |
                ((data[0x3e] & 255) << 16) |
                ((data[0x3f] & 255) << 24);

            if (offset < 0 || offset + 6 > count) {
                return "Header PE tidak valid.";
            }

            if (data[offset] != 'P' ||
                data[offset + 1] != 'E') {
                return "Header PE tidak ditemukan.";
            }

            int machine =
                (data[offset + 4] & 255) |
                ((data[offset + 5] & 255) << 8);

            if (machine == 0x014c) {
                return "Windows x86 32-bit";
            }

            if (machine == 0x8664) {
                return "Windows x86-64";
            }

            if (machine == 0xaa64) {
                return "Windows ARM64";
            }

            return "Arsitektur tidak dikenal.";

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
