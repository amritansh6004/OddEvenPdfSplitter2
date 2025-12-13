package com.example.oddevensplitter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    File oddFile, evenFile;
    CheckBox reverseEven;

    ActivityResultLauncher<Intent> picker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            splitPdf(result.getData().getData());
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pick = findViewById(R.id.pickPdf);
        Button share = findViewById(R.id.sharePdf);
        reverseEven = findViewById(R.id.reverseEven);

        pick.setOnClickListener(v -> openPicker());
        share.setOnClickListener(v -> shareFiles());
    }

    void openPicker() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/pdf");
        picker.launch(i);
    }

    void splitPdf(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            PDDocument input = PDDocument.load(is);
            PDDocument odd = new PDDocument();
            PDDocument even = new PDDocument();

            for (int i = 0; i < input.getNumberOfPages(); i++) {
                PDPage p = input.getPage(i);
                if ((i + 1) % 2 == 1) odd.addPage(p);
                else even.addPage(p);
            }

            if (reverseEven.isChecked())
                Collections.reverse(even.getPages());

            oddFile = new File(getExternalFilesDir(null), "ODD.pdf");
            evenFile = new File(getExternalFilesDir(null), "EVEN.pdf");

            odd.save(oddFile);
            even.save(evenFile);

            input.close();
            odd.close();
            even.close();

            Toast.makeText(this, "PDF split done", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    void shareFiles() {
        if (oddFile == null || evenFile == null) return;

        Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
        i.setType("application/pdf");
        i.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
                new java.util.ArrayList<>(java.util.Arrays.asList(
                        Uri.fromFile(oddFile),
                        Uri.fromFile(evenFile)
                )));
        startActivity(Intent.createChooser(i, "Share PDFs"));
    }
}
