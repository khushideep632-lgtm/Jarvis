package com.myagent.jarvis;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.provider.AlarmClock;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

/**
 * JARVIS - pehla test build. Sirf screen aur animation hai.
 * Bataan abhi Termux wale agent se jude nahi hain.
 */
public class MainActivity extends Activity {

    static final int BG = 0xFF05070D;
    static final int BLUE = 0xFF2BB5FF;
    static final int ORANGE = 0xFFFF7A1A;
    static final int GREEN = 0xFF22E07A;
    static final int TEXT = 0xFFE6F2FF;
    static final int DIM = 0xFF7F93A8;
    static final int CARD = 0xFF0A1220;
    static final int EDGE = 0xFF1B3A57;

    private static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    private static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    private RingsView rings;
    private IconView statusWave;
    private TextView status;
    private TextView tapText;
    private LinearLayout chat;
    private ScrollView chatScroll;
    private int state = RingsView.READY;

    private SpeechRecognizer recognizer;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean torchOn = false;
    private SharedPreferences prefs;
    private boolean conversationOn = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable delayedListen = () -> {
        if (state == RingsView.READY && conversationOn
                && prefs.getBoolean("continuous", true)) {
            startListening();
        }
    };
    private String partialText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("jarvis", MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        setContentView(buildUi());
        addCard("Hello, I'm JARVIS.\nHow can I help you today?", false);
        addCard("Tap the mic and speak. Start the bridge in Termux (jarvis-bridge), then paste the agent key in SETTINGS.", false);
        setState(RingsView.READY);
        initTts();
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int fill, int stroke, float radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        g.setStroke(Math.max(1, dp(1)), stroke);
        return g;
    }

    private GradientDrawable circle(int fill, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(fill);
        g.setStroke(Math.max(1, dp(1)), stroke);
        return g;
    }

    private TextView label(String text, float sp, float spacing, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setLetterSpacing(spacing);
        tv.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        return tv;
    }

    private void soon(String what) {
        Toast.makeText(this, what + ": not connected yet", Toast.LENGTH_SHORT).show();
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        root.addView(buildTopBar(), new LinearLayout.LayoutParams(MATCH, dp(80)));
        root.addView(buildHero(), new LinearLayout.LayoutParams(MATCH, WRAP));

        // status patti: chhoti wave + "LISTENING..."
        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER);
        statusWave = new IconView(this, IconView.WAVE);
        statusRow.addView(statusWave, new LinearLayout.LayoutParams(dp(30), dp(22)));
        status = label("READY", 13, 0.3f, BLUE);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(WRAP, WRAP);
        sp.leftMargin = dp(10);
        statusRow.addView(status, sp);
        LinearLayout.LayoutParams srp = new LinearLayout.LayoutParams(MATCH, WRAP);
        srp.topMargin = dp(4);
        srp.bottomMargin = dp(8);
        root.addView(statusRow, srp);

        // baat-cheet ke card
        chatScroll = new ScrollView(this);
        chatScroll.setVerticalScrollBarEnabled(false);
        chat = new LinearLayout(this);
        chat.setOrientation(LinearLayout.VERTICAL);
        chat.setPadding(dp(16), dp(4), dp(16), dp(8));
        chatScroll.addView(chat, new ViewGroup.LayoutParams(MATCH, WRAP));
        root.addView(chatScroll, new LinearLayout.LayoutParams(MATCH, 0, 1f));

        root.addView(buildBottomBar());
        return root;
    }

    private View buildTopBar() {
        FrameLayout bar = new FrameLayout(this);

        FrameLayout menu = new FrameLayout(this);
        menu.setBackground(circle(0xFF08111F, EDGE));
        IconView menuIcon = new IconView(this, IconView.MENU);
        menu.addView(menuIcon, new FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER));
        menu.setOnClickListener(v -> soon("MENU"));
        FrameLayout.LayoutParams mp = new FrameLayout.LayoutParams(dp(46), dp(46),
                Gravity.START | Gravity.CENTER_VERTICAL);
        mp.leftMargin = dp(16);
        bar.addView(menu, mp);

        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER_HORIZONTAL);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        nameRow.addView(label("JARVIS", 28, 0.3f, TEXT));
        View dot = new View(this);
        dot.setBackground(circle(GREEN, GREEN));
        LinearLayout.LayoutParams dp1 = new LinearLayout.LayoutParams(dp(10), dp(10));
        dp1.leftMargin = dp(10);
        nameRow.addView(dot, dp1);
        center.addView(nameRow);
        center.addView(label("AI VOICE ASSISTANT", 10, 0.4f, DIM));

        bar.addView(center, new FrameLayout.LayoutParams(WRAP, WRAP, Gravity.CENTER));
        return bar;
    }

    private View buildHero() {
        int screenW = getResources().getDisplayMetrics().widthPixels;
        int ringSize = Math.min(screenW - dp(80), dp(340));

        FrameLayout hero = new FrameLayout(this);

        rings = new RingsView(this);
        rings.setOnClickListener(v -> onMicTap());
        hero.addView(rings, new FrameLayout.LayoutParams(ringSize, ringSize, Gravity.CENTER));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        left.addView(sideButton(IconView.TORCH, "TORCH"));
        left.addView(sideButton(IconView.CALL, "CALL"));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(WRAP, WRAP,
                Gravity.START | Gravity.CENTER_VERTICAL);
        lp.leftMargin = dp(10);
        hero.addView(left, lp);

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.addView(sideButton(IconView.ALARM, "ALARM"));
        right.addView(sideButton(IconView.SETTINGS, "SETTINGS"));
        FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(WRAP, WRAP,
                Gravity.END | Gravity.CENTER_VERTICAL);
        rp.rightMargin = dp(10);
        hero.addView(right, rp);

        hero.setMinimumHeight(ringSize + dp(8));
        return hero;
    }

    private View sideButton(int icon, String name) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER_HORIZONTAL);
        col.setPadding(dp(6), dp(6), dp(6), dp(6));

        FrameLayout disc = new FrameLayout(this);
        disc.setBackground(circle(0xFF08111F, EDGE));
        disc.addView(new IconView(this, icon), new FrameLayout.LayoutParams(dp(26), dp(26), Gravity.CENTER));
        col.addView(disc, new LinearLayout.LayoutParams(dp(52), dp(52)));

        TextView t = label(name, 9, 0.2f, DIM);
        t.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(WRAP, WRAP);
        tp.topMargin = dp(4);
        col.addView(t, tp);

        col.setOnClickListener(v -> onSide(name));
        return col;
    }

    private View buildBottomBar() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackground(shape(0xFF070D18, EDGE, 34));
        bar.setPadding(dp(10), 0, dp(10), 0);

        FrameLayout kb = new FrameLayout(this);
        kb.addView(new IconView(this, IconView.KEYBOARD), new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER));
        kb.setOnClickListener(v -> soon("TYPE"));
        bar.addView(kb, new LinearLayout.LayoutParams(dp(48), dp(48)));

        tapText = label("TAP TO SPEAK", 14, 0.3f, DIM);
        tapText.setGravity(Gravity.CENTER);
        tapText.setBackground(shape(0xFF0B1728, 0xFF1D5A8A, 26));
        tapText.setOnClickListener(v -> onMicTap());
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        tp.leftMargin = dp(6);
        tp.rightMargin = dp(6);
        bar.addView(tapText, tp);

        FrameLayout wave = new FrameLayout(this);
        wave.addView(new IconView(this, IconView.WAVE), new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.CENTER));
        wave.setOnClickListener(v -> soon("VOICE MODE"));
        bar.addView(wave, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(MATCH, dp(68));
        bp.setMargins(dp(16), dp(4), dp(16), dp(14));
        bar.setLayoutParams(bp);
        return bar;
    }

    // ---------------- buttons ----------------

    private void onSide(String name) {
        if (name.equals("TORCH")) {
            toggleTorch();
        } else if (name.equals("CALL")) {
            openDialer();
        } else if (name.equals("ALARM")) {
            openAlarms();
        } else if (name.equals("SETTINGS")) {
            showSettings();
        } else {
            soon(name);
        }
    }

    private void toggleTorch() {
        try {
            CameraManager cm = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            String[] ids = cm.getCameraIdList();
            for (String id : ids) {
                Boolean flash = cm.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (flash != null && flash) {
                    torchOn = !torchOn;
                    cm.setTorchMode(id, torchOn);
                    Toast.makeText(this, torchOn ? "Torch ON" : "Torch OFF", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Toast.makeText(this, "No flashlight found", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Torch error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openDialer() {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:")));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open dialer", Toast.LENGTH_SHORT).show();
        }
    }

    private void openAlarms() {
        try {
            startActivity(new Intent(AlarmClock.ACTION_SHOW_ALARMS));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open alarms", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSettings() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundColor(CARD);
        box.setPadding(dp(20), dp(16), dp(20), dp(8));

        TextView title = label("SETTINGS", 16, 0.3f, TEXT);
        box.addView(title);

        TextView kl = label("Agent key (shown in Termux by jarvis-bridge)", 12, 0.05f, DIM);
        kl.setPadding(0, dp(14), 0, dp(4));
        box.addView(kl);
        final EditText key = new EditText(this);
        key.setText(prefs.getString("token", ""));
        key.setTextColor(TEXT);
        key.setSingleLine(true);
        box.addView(key);

        Switch voice = new Switch(this);
        voice.setText("Speak replies aloud");
        voice.setTextColor(TEXT);
        voice.setChecked(prefs.getBoolean("speak", true));
        voice.setPadding(0, dp(16), 0, dp(8));
        voice.setOnCheckedChangeListener((b, on) -> prefs.edit().putBoolean("speak", on).apply());
        box.addView(voice);

        Switch continuous = new Switch(this);
        continuous.setText("Continuous conversation");
        continuous.setTextColor(TEXT);
        continuous.setChecked(prefs.getBoolean("continuous", true));
        continuous.setPadding(0, dp(8), 0, dp(8));
        continuous.setOnCheckedChangeListener((b, on) -> {
            prefs.edit().putBoolean("continuous", on).apply();
            if (!on) {
                conversationOn = false;
                cancelDelayedListen();
            }
        });
        box.addView(continuous);

        TextView hint = label("Microphone permission is managed in Android settings.", 12, 0.02f, DIM);
        hint.setPadding(0, dp(8), 0, dp(8));
        box.addView(hint);

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setView(box);
        b.setPositiveButton("SAVE", (d, w) -> prefs.edit().putString("token", key.getText().toString().trim()).apply());
        b.setNeutralButton("APP PERMISSIONS", (d, w) -> {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(i);
        });
        b.show();
    }

    // ---------------- voice ----------------

    private void initTts() {
        tts = new TextToSpeech(this, st -> {
            if (st == TextToSpeech.SUCCESS) {
                int r = tts.setLanguage(new Locale("hi", "IN"));
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.getDefault());
                }
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override public void onStart(String id) { }
                    @Override public void onDone(String id) {
                        runOnUiThread(() -> {
                            if (state == RingsView.SPEAKING) {
                                setState(RingsView.READY);
                                scheduleNextListening();
                            }
                        });
                    }
                    @Override public void onError(String id) {
                        runOnUiThread(() -> {
                            if (state == RingsView.SPEAKING) setState(RingsView.READY);
                        });
                    }
                    @Override public void onStop(String id, boolean interrupted) {
                        runOnUiThread(() -> {
                            if (state == RingsView.SPEAKING) setState(RingsView.READY);
                        });
                    }
                });
                ttsReady = true;
            }
        });
    }

    private void onMicTap() {
        if (state == RingsView.LISTENING || state == RingsView.SPEAKING) {
            conversationOn = false;
            cancelDelayedListen();
            if (state == RingsView.LISTENING && recognizer != null) recognizer.cancel();
            if (state == RingsView.SPEAKING && tts != null) tts.stop();
            setState(RingsView.READY);
            return;
        }
        cancelDelayedListen();
        conversationOn = prefs.getBoolean("continuous", true);
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            return;
        }
        startListening();
    }

    private void cancelDelayedListen() {
        mainHandler.removeCallbacks(delayedListen);
    }

    private void scheduleNextListening() {
        cancelDelayedListen();
        if (conversationOn && prefs.getBoolean("continuous", true)) {
            mainHandler.postDelayed(delayedListen, 600);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == 1 && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        } else if (code == 1) {
            conversationOn = false;
            Toast.makeText(this, "Microphone permission needed", Toast.LENGTH_SHORT).show();
        }
    }

    private void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            conversationOn = false;
            cancelDelayedListen();
            setState(RingsView.READY);
            Toast.makeText(this, "Speech recognition not available on this phone", Toast.LENGTH_LONG).show();
            return;
        }
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle p) { }
                @Override public void onBeginningOfSpeech() { }
                @Override public void onBufferReceived(byte[] b) { }
                @Override public void onEvent(int t, Bundle p) { }
                @Override public void onEndOfSpeech() { rings.setLevel(0f); }
                @Override public void onRmsChanged(float rms) {
                    rings.setLevel(Math.max(0f, Math.min(1f, (rms + 2f) / 12f)));
                }
                @Override public void onPartialResults(Bundle p) {
                    ArrayList<String> l = p.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (l != null && !l.isEmpty()) {
                        partialText = l.get(0);
                        status.setText(partialText.length() > 28
                                ? "..." + partialText.substring(partialText.length() - 28) : partialText);
                    }
                }
                @Override public void onResults(Bundle r) {
                    ArrayList<String> l = r.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String t = (l != null && !l.isEmpty()) ? l.get(0) : partialText;
                    heard(t);
                }
                @Override public void onError(int e) {
                    rings.setLevel(0f);
                    conversationOn = false;
                    cancelDelayedListen();
                    setState(RingsView.READY);
                    if (e == SpeechRecognizer.ERROR_NO_MATCH || e == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                        status.setText("DID NOT HEAR ANYTHING");
                    } else {
                        Toast.makeText(MainActivity.this, "Mic error " + e, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        partialText = "";
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L);
        i.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L);
        setState(RingsView.LISTENING);
        recognizer.startListening(i);
    }

    private void heard(String text) {
        rings.setLevel(0f);
        if (text == null || text.trim().isEmpty()) {
            conversationOn = false;
            cancelDelayedListen();
            setState(RingsView.READY);
            return;
        }
        String stopText = text.trim().toLowerCase(Locale.ROOT);
        if (stopText.equals("bas") || stopText.equals("ruko") || stopText.equals("stop")
                || stopText.equals("बस") || stopText.equals("रुको") || stopText.equals("बस करो")
                || stopText.equals("रुक जाओ")) {
            conversationOn = false;
            cancelDelayedListen();
            addCard("Okay.", false);
            setState(RingsView.READY);
            return;
        }
        addCard(text, true);
        setState(RingsView.READY);
        status.setText("THINKING...");
        final String q = text;
        new Thread(() -> {
            String reply = askAgent(q);
            runOnUiThread(() -> answer(reply));
        }).start();
    }

    /** Termux wale bridge ko sawal bhejta hai. Galti par "!..." code lautata hai. */
    private String askAgent(String text) {
        String token = prefs.getString("token", "");
        if (token.isEmpty()) return "!NOTOKEN";
        try {
            URL u = new URL("http://127.0.0.1:8765/ask");
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setConnectTimeout(3000);
            c.setReadTimeout(90000);
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            c.setRequestProperty("X-Token", token);
            JSONObject j = new JSONObject();
            j.put("text", text);
            OutputStream os = c.getOutputStream();
            os.write(j.toString().getBytes("UTF-8"));
            os.close();
            int code = c.getResponseCode();
            if (code == 401) return "!BADTOKEN";
            InputStream is = code < 400 ? c.getInputStream() : c.getErrorStream();
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) bo.write(buf, 0, n);
            if (code >= 400) return "!ERROR " + code;
            return new JSONObject(bo.toString("UTF-8")).getString("reply");
        } catch (Exception e) {
            return "!OFFLINE";
        }
    }

    private void answer(String r) {
        String msg = r;
        if (r.equals("!NOTOKEN")) {
            msg = "Agent key is not set. Open SETTINGS and paste the key shown in Termux.";
        } else if (r.equals("!BADTOKEN")) {
            msg = "Agent key is wrong. Copy it again from Termux into SETTINGS.";
        } else if (r.equals("!OFFLINE")) {
            msg = "Agent is offline. In Termux run: jarvis-bridge";
        } else if (r.equals("EXIT")) {
            msg = "Okay.";
        }
        addCard(msg, false);
        if (r.startsWith("!")) {
            conversationOn = false;
            cancelDelayedListen();
            setState(RingsView.READY);
            return;
        }
        if (state == RingsView.LISTENING) return;
        if (prefs.getBoolean("speak", true) && ttsReady) {
            setState(RingsView.SPEAKING);
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "u1");
        } else {
            setState(RingsView.READY);
            scheduleNextListening();
        }
    }

    @Override
    protected void onDestroy() {
        conversationOn = false;
        cancelDelayedListen();
        if (recognizer != null) recognizer.destroy();
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    private void setState(int s) {
        state = s;
        rings.setState(s);
        int color = (s == RingsView.SPEAKING) ? ORANGE : BLUE;
        if (s == RingsView.READY) {
            status.setText("READY");
            tapText.setText("TAP TO SPEAK");
        } else if (s == RingsView.LISTENING) {
            status.setText("LISTENING...");
            tapText.setText("TAP TO STOP");
        } else {
            status.setText("SPEAKING...");
            tapText.setText("TAP TO STOP");
        }
        status.setTextColor(color);
        statusWave.setColor(color);
    }

    private void addCard(String text, boolean fromUser) {
        TextView tv = label(text, 15, 0.02f, TEXT);
        tv.setPadding(dp(16), dp(12), dp(16), dp(12));
        tv.setBackground(shape(CARD, fromUser ? ORANGE : EDGE, 16));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP, WRAP);
        lp.topMargin = dp(6);
        lp.bottomMargin = dp(6);
        if (fromUser) {
            lp.gravity = Gravity.END;
            lp.leftMargin = dp(48);
        } else {
            lp.gravity = Gravity.START;
            lp.rightMargin = dp(48);
        }
        chat.addView(tv, lp);
        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
    }
}
