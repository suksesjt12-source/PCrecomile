
package com.pcrecompile.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int PICK_FOLDER = 101;

    private GameManager games;
    private RuntimeManager runtime;

    private TextView status;
    private TextView selected;

    private final List<DocumentFile> executables =
        new ArrayList<>();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        games = new GameManager(this);
        runtime = new RuntimeManager(this);

        createInterface();
        refresh();
    }

    private void createInterface() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(1);
        layout.setPadding(25, 25, 25, 25);
        layout.setBackgroundColor(
            Color.rgb(17, 21, 30)
        );

        TextView title = new TextView(this);
        title.setText("PC RECOMPILE");
        title.setTextSize(27);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        selected = new TextView(this);
        selected.setTextColor(Color.WHITE);
        selected.setPadding(0, 25, 0, 20);

        Button folder = new Button(this);
        folder.setText("IMPORT GAME FOLDER");
        folder.setOnClickListener(v -> chooseFolder());

        Button scan = new Button(this);
        scan.setText("SCAN EXE");
        scan.setOnClickListener(v -> scanGame());

        Button play = new Button(this);
        play.setText("PLAY GAME");
        play.setOnClickListener(v -> playGame());

        Button clear = new Button(this);
        clear.setText("CLEAR EXE");
        clear.setOnClickListener(v -> {
            games.clearExe();
            refresh();
        });

        status = new TextView(this);
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(14);
        status.setPadding(0, 25, 0, 0);

        layout.addView(title);
        layout.addView(selected);
        layout.addView(folder);
        layout.addView(scan);
        layout.addView(play);
        layout.addView(clear);
        layout.addView(status);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);

        setContentView(scroll);
    }

    private void chooseFolder() {

        Intent intent =
            new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION |
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );

        startActivityForResult(intent, PICK_FOLDER);
    }

    @Override
    protected void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
    ) {
        super.onActivityResult(
            requestCode, resultCode, data
        );

        if (requestCode == PICK_FOLDER &&
            resultCode == RESULT_OK &&
            data != null) {

            Uri uri = data.getData();

            if (uri == null) return;

            int flags = data.getFlags() &
                Intent.FLAG_GRANT_READ_URI_PERMISSION;

            getContentResolver()
                .takePersistableUriPermission(uri, flags);

            games.saveFolder(uri);
            games.clearExe();

            refresh();
        }
    }

    private void scanGame() {

        Uri uri = games.getFolder();

        if (uri == null) {
            status.setText("Pilih folder game dahulu.");
            return;
        }

        DocumentFile folder =
            DocumentFile.fromTreeUri(this, uri);

        if (folder == null) {
            status.setText("Folder tidak dapat dibaca.");
            return;
        }

        executables.clear();
        findExe(folder, 0);

        if (executables.isEmpty()) {
            status.setText("EXE tidak ditemukan.");
            return;
        }

        StringBuilder result =
            new StringBuilder("EXE ditemukan:\\n");

        for (int i = 0; i < executables.size(); i++) {
            result.append(i + 1)
                .append(". ")
                .append(executables.get(i).getName())
                .append("\\n");
        }

        DocumentFile first = executables.get(0);

        games.saveExe(
            first.getUri(),
            first.getName()
        );

        result.append("\\nDipilih: ")
            .append(first.getName());

        result.append("\\nArsitektur: ")
            .append(PeAnalyzer.inspect(
                getContentResolver(),
                first.getUri()
            ));

        status.setText(result.toString());
        refresh();
    }

    private void findExe(
        DocumentFile folder,
        int depth
    ) {
        if (depth > 10 || executables.size() >= 200) {
            return;
        }

        for (DocumentFile file : folder.listFiles()) {

            String name = file.getName();

            if (file.isFile() &&
                name != null &&
                name.toLowerCase().endsWith(".exe")) {

                executables.add(file);

            } else if (file.isDirectory()) {

                findExe(file, depth + 1);
            }
        }
    }

    private void playGame() {
        status.setText(
            runtime.launch(games.getExe())
        );
    }

    private void refresh() {

        Uri folder = games.getFolder();

        selected.setText(
            "Folder: " +
            (folder == null ? "Belum dipilih" : folder) +
            "\\nEXE: " +
            (games.getExeName().isEmpty()
                ? "Belum dipilih"
                : games.getExeName())
        );

        status.setText(
            "Runtime: " +
            (runtime.isInstalled()
                ? "Terpasang"
                : "Belum terpasang")
        );
    }
}
