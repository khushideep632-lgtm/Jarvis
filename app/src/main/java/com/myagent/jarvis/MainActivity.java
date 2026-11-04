package com.myagent.jarvis;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        setContentView(buildUi());
        addCard("Hello, I'm JARVIS.\nHow can I help you today?", false);
        addCard("First test build. The buttons are not connected to the agent yet.", false);
        setState(RingsView.READY);
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
        rings.setOnClickListener(v -> cycle());
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

        col.setOnClickListener(v -> soon(name));
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
        tapText.setOnClickListener(v -> cycle());
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

    /** Demo: READY -> LISTENING -> SPEAKING -> READY. Asli mic baad mein judega. */
    private void cycle() {
        state = (state + 1) % 3;
        setState(state);
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
