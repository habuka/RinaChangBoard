package com.example.rinachangboard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;

public class SimpleModeActivity extends AppCompatActivity {

    private static final int SR_STATE_STOP = 0;
    private static final int SR_STATE_WAIT_RINACHANBOARD = 1;
    private static final int SR_STATE_WAIT_FACE = 2;

    private static final int FACE_CHANGING = 0;
    private static final int FACE_NORMAL = 1;
    private static final int FACE_NIKKORIN = 2;
    private static final int FACE_MUN = 3;
    private static final int FACE_SYONBORI = 4;
    private static final int FACE_MUU = 5;
    private static final int FACE_SURPRISE = 6;
    private static final int FACE_WINK = 7;
    private static final int FACE_RELAX = 8;
    private static final int FACE_YAJU = 9;

    private Intent intent;
    private SpeechRecognizer sr;
    private TextView textView;

    private int sr_stare = SR_STATE_STOP;
    private int face_state = FACE_NORMAL;
    private boolean is_debug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_mode);

        Intent i = getIntent();
        is_debug = i.getBooleanExtra("debug", false);

        textView = findViewById(R.id.textView);

        if (is_debug == false) {
            // 通常モードではデバッグ用の認識単語表示を非表示にする
            textView.setVisibility(View.GONE);
        }

        // 全画面表示にする
        View decor = this.getWindow().getDecorView();
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        // 「璃奈ちゃんボード」待ち
        sr_stare = SR_STATE_WAIT_RINACHANBOARD;

        // 表情セット
        setFace(face_state);

        // permission チェック
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, RECORD_AUDIO)) {
                // 拒否した場合
            } else {
                // 許可した場合
                int MY_PERMISSIONS_RECORD_AUDIO = 1;
                ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        startListening();
    }

    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Log.d("log:: ", "準備できてます");
        }

        @Override
        public void onBeginningOfSpeech() {
            Log.d("log:: ", "始め！");
        }

        @Override
        public void onRmsChanged(float v) {
            Log.d("log:: ", "音声が変わった");
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            Log.d("log:: ", "新しい音声");
        }

        @Override
        public void onEndOfSpeech() {
            Log.d("log:: ", "終わりました");
        }

        @Override
        public void onError(int i) {
            switch (i) {
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    textView.setText("ネットワークタイムエラー");
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    textView.setText("その外ネットワークエラー");
                    break;
                case SpeechRecognizer.ERROR_AUDIO:
                    textView.setText("Audio エラー");
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    textView.setText("サーバーエラー");
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    textView.setText("クライアントエラー");
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    textView.setText("何も聞こえてないエラー");
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    textView.setText("適当な結果を見つけてませんエラー");
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    textView.setText("RecognitionServiceが忙しいエラー");
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    textView.setText("RECORD AUDIOがないエラー");
                    break;
            }
            restartListening();
        }

        @Override
        public void onResults(Bundle bundle) {
            ArrayList<String> speech_list = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            // 文字列を結合(for debug)
            String resultString = "";
            for (int i = 0; i < speech_list.size(); i++) {
                resultString += speech_list.get(i) + ";";
            }
            // 結果を表示(for debug)
            textView.setText(resultString);

            // 表情制御用ステートマシン
            switch (sr_stare) {
            case SR_STATE_STOP:
                break;
            case SR_STATE_WAIT_RINACHANBOARD:
                if (searchRinaChanBoard(speech_list)) {
                    sr_stare = SR_STATE_WAIT_FACE;
                    setFace(FACE_CHANGING);
                }
                break;
            case SR_STATE_WAIT_FACE:
                if(searchFace(speech_list)) {
                    setFace(face_state);
                    sr_stare = SR_STATE_WAIT_RINACHANBOARD;
                }
                break;
            }
            restartListening();
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }
    };

    void startListening() {
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.JAPAN.toString());
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000);

        sr = SpeechRecognizer.createSpeechRecognizer(SimpleModeActivity.this);
        sr.setRecognitionListener(recognitionListener);

        sr.startListening(intent);
    }

    // 音声認識を終了する
    protected void stopListening() {
        if (sr != null) sr.destroy();
        sr = null;
    }

    // 音声認識を再開する
    public void restartListening() {
        stopListening();
        startListening();
    }

    // 認識した音声に表情ワードが含まれるか検索する
    boolean searchFace(ArrayList<String> speech_list) {
        if (searchNormal(speech_list)) {
            face_state = FACE_NORMAL;
            return true;
        } else if (searchNikkorin(speech_list)) {
            face_state = FACE_NIKKORIN;
            return true;
        } else if (searchMun(speech_list)) {
            face_state = FACE_MUN;
            return true;
        } else if (searchSyonbori(speech_list)) {
            face_state = FACE_SYONBORI;
            return true;
        } else if (searchMuu(speech_list)) {
            face_state = FACE_MUU;
            return true;
        } else if (searchSurprise(speech_list)) {
            face_state = FACE_SURPRISE;
            return true;
        } else if (searchWink(speech_list)) {
            face_state = FACE_WINK;
            return true;
        } else if (searchRelax(speech_list)) {
            face_state = FACE_RELAX;
            return true;
        } else if (searchYaju(speech_list)) {
            face_state = FACE_YAJU;
            return true;
        } else {
            return false;
        }
    }

    // 認識した音声に「璃奈ちゃんボード」が含まれるか検索する(判定甘め)
    boolean searchRinaChanBoard(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("りなちゃんボード")) {
                return true;
            } else if (speech_list.get(i).contains("りなちゃんぼーど")) {
                return true;
            } else if (speech_list.get(i).contains("りなちゃんモード")) {
                return true;
            } else if (speech_list.get(i).contains("りなちゃんもーど")) {
                return true;
            } else if (speech_list.get(i).contains("ひなちゃんボード")) {
                return true;
            } else if (speech_list.get(i).contains("いなちゃんボード")) {
                return true;
            } else if (speech_list.get(i).contains("璃奈ちゃんボード")) {
                return true;
            }
        }
        return false;
    }

    // 1: 認識した音声に「ノーマル」が含まれるか検索する
    boolean searchNormal(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("ノーマル")) {
                return true;
            }
        }
        return false;
    }

    // 2: 認識した音声に「にっこりん」が含まれるか検索する
    boolean searchNikkorin(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("にっこりん")) {
                return true;
            } else if (speech_list.get(i).contains("にっこり")) {
                return true;
            } else if (speech_list.get(i).contains("ニッコリン")) {
                return true;
            } else if (speech_list.get(i).contains("にこりん")) {
                return true;
            }
        }
        return false;
    }

    // 3: 認識した音声に「むん」が含まれるか検索する(単語が短くて認識しにくい…)
    boolean searchMun(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("むん")) {
                return true;
            } else if (speech_list.get(i).contains("ムーン")) {
                return true;
            } else if (speech_list.get(i).contains("moon")) {
                return true;
            } else if (speech_list.get(i).contains("何")) {
                return true;
            } else if (speech_list.get(i).contains("うん")) {
                return true;
            }
        }
        return false;
    }

    // 4: 認識した音声に「しょんぼり」が含まれるか検索する
    boolean searchSyonbori(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("しょんぼり")) {
                return true;
            } else if (speech_list.get(i).contains("ションボリ")) {
                return true;
            }
        }
        return false;
    }

    // 5: 認識した音声に「ぷんぷん」が含まれるか検索する
    boolean searchMuu(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("ぷんぷん")) {
                return true;
            } else if (speech_list.get(i).contains("プンプン")) {
                return true;
            } else if (speech_list.get(i).contains("ぶんぶん")) {
                return true;
            } else if (speech_list.get(i).contains("ブンブン")) {
                return true;
            }
        }
        return false;
    }

    // 6: 認識した音声に「びっくり」が含まれるか検索する
    boolean searchSurprise(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("びっくり")) {
                return true;
            } else if (speech_list.get(i).contains("ビックリ")) {
                return true;
            } else if (speech_list.get(i).contains("吃驚")) {
                return true;
            }
        }
        return false;
    }

    // 7: 認識した音声に「ウインク」が含まれるか検索する
    boolean searchWink(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("ウインク")) {
                return true;
            } else if (speech_list.get(i).contains("Wink")) {
                return true;
            } else if (speech_list.get(i).contains("ウィンク")) {
                return true;
            }
        }
        return false;
    }

    // 8: 認識した音声に「リラックス」が含まれるか検索する
    boolean searchRelax(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("リラックス")) {
                return true;
            } else if (speech_list.get(i).contains("りらっくす")) {
                return true;
            } else if (speech_list.get(i).contains("Relax")) {
                return true;
            } else if (speech_list.get(i).contains("Linux")) {
                return true;
            }
        }
        return false;
    }

    // 9: 認識した音声に「野獣先輩」が含まれるか検索する
    boolean searchYaju(ArrayList<String> speech_list) {
        // 候補リストを全て検索
        for (int i = 0; i < speech_list.size(); i++) {
            if (speech_list.get(i).contains("野獣先輩")) {
                return true;
            }
        }
        return false;
    }

    public void setFace(int face) {
        switch (face) {
            case FACE_CHANGING:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.changing);
                break;
            case FACE_NORMAL:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.normal);
                break;
            case FACE_NIKKORIN:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.nikkorin);
                break;
            case FACE_MUN:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.mun);
                break;
            case FACE_SYONBORI:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.syonbori);
                break;
            case FACE_MUU:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.angry);
                break;
            case FACE_SURPRISE:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.surprise);
                break;
            case FACE_WINK:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.wink);
                break;
            case FACE_RELAX:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.relax);
                break;
            case FACE_YAJU:
                ((ImageView) findViewById(R.id.imageView)).setImageResource(R.drawable.yaju);
                break;
        }
    }

    @Override
    public void onDestroy() {
        stopListening();
        super.onDestroy();
    }

}