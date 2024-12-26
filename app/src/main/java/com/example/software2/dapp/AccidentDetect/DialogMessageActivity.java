package com.example.software2.dapp.AccidentDetect;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.software2.dapp.R;

import java.util.Locale;
import java.util.Objects;

public class DialogMessageActivity extends AppCompatActivity {

    static TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVisible(false);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
                textToSpeech.setSpeechRate(1f);
                textToSpeech.speak("opening airbags", TextToSpeech.QUEUE_FLUSH, null, null);
                textToSpeech.speak("opening airbags", TextToSpeech.QUEUE_ADD, null, null);
            }
        });

        final Dialog dialog = new Dialog(DialogMessageActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_dialog_message);
        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        dialog.getWindow().setAttributes(lp);
        final VideoView videoview = dialog.findViewById(R.id.videoview);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.accident);
        videoview.setVideoURI(uri);
        videoview.start();
        videoview.setOnCompletionListener(mediaPlayer -> runOnUiThread(() -> {
            setVisible(true);
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "completed", Toast.LENGTH_SHORT).show();
        }));
    }
}
