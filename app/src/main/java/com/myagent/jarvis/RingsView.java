package com.myagent.jarvis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/**
 * Jarvis ke ghoomte hue chhalle. Teen halat:
 * READY (shaant), LISTENING (neela, sun raha hai), SPEAKING (naarangi, bol raha hai).
 * Baad mein setLevel() se asli awaaz ki taakat di jaayegi.
 */
public class RingsView extends View {

    public static final int READY = 0;
    public static final int LISTENING = 1;
    public static final int SPEAKING = 2;

    private static final int BLUE = 0xFF2BB5FF;
    private static final int ORANGE = 0xFFFF7A1A;
    private static final int DISC = 0xFF060B16;

    private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF();
    private final float density;

    private int state = READY;
    private float level = 0f;
    private float smooth = 0f;
    private boolean running = false;
    private final long startTime = System.nanoTime();

    public RingsView(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;
        ring.setStyle(Paint.Style.STROKE);
        ring.setStrokeCap(Paint.Cap.ROUND);
        fill.setStyle(Paint.Style.FILL);
    }

    public void setState(int s) {
        state = s;
        invalidate();
    }

    /** 0..1. Abhi 0 rakhein to demo hilna chalta hai. */
    public void setLevel(float l) {
        level = Math.max(0f, Math.min(1f, l));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        running = true;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        running = false;
        super.onDetachedFromWindow();
    }

    private int alpha(int color, float a) {
        int al = (int) (255f * Math.max(0f, Math.min(1f, a)));
        return (al << 24) | (color & 0x00FFFFFF);
    }

    private void circle(Canvas c, float cx, float cy, float rad, float widthDp, int color, float strength) {
        // naqli chamak: ek hi gola kai baar, mote aur halke
        for (int i = 3; i >= 1; i--) {
            ring.setStrokeWidth(widthDp * density + i * 3f * density);
            ring.setColor(alpha(color, 0.10f * strength));
            c.drawCircle(cx, cy, rad, ring);
        }
        ring.setStrokeWidth(widthDp * density);
        ring.setColor(alpha(color, 0.95f * strength));
        c.drawCircle(cx, cy, rad, ring);
    }

    private void arc(Canvas c, float cx, float cy, float rad, float widthDp, float start, float sweep,
                     int color, float strength) {
        box.set(cx - rad, cy - rad, cx + rad, cy + rad);
        for (int i = 3; i >= 1; i--) {
            ring.setStrokeWidth(widthDp * density + i * 3f * density);
            ring.setColor(alpha(color, 0.10f * strength));
            c.drawArc(box, start, sweep, false, ring);
        }
        ring.setStrokeWidth(widthDp * density);
        ring.setColor(alpha(color, 0.95f * strength));
        c.drawArc(box, start, sweep, false, ring);
    }

    private void drawMic(Canvas c, float cx, float cy, float s, int color) {
        ring.setStrokeWidth(Math.max(2f * density, s * 0.09f));
        ring.setColor(color);
        box.set(cx - s * 0.28f, cy - s * 0.62f, cx + s * 0.28f, cy + s * 0.14f);
        c.drawRoundRect(box, s * 0.28f, s * 0.28f, ring);
        box.set(cx - s * 0.5f, cy - s * 0.34f, cx + s * 0.5f, cy + s * 0.44f);
        c.drawArc(box, 0f, 180f, false, ring);
        c.drawLine(cx, cy + s * 0.44f, cx, cy + s * 0.74f, ring);
        c.drawLine(cx - s * 0.28f, cy + s * 0.74f, cx + s * 0.28f, cy + s * 0.74f, ring);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        float w = getWidth();
        float h = getHeight();
        float cx = w / 2f;
        float cy = h / 2f;
        float r = Math.min(w, h) / 2f;
        float t = (System.nanoTime() - startTime) / 1.0e9f;

        int main = (state == SPEAKING) ? ORANGE : BLUE;
        int second = (state == SPEAKING) ? BLUE : ORANGE;

        float target;
        if (state == READY) {
            target = 0.08f + 0.04f * (float) Math.sin(t * 1.2f);
        } else if (level > 0f) {
            target = level;
        } else {
            target = 0.5f + 0.35f * (float) Math.sin(t * 4.0f) * (float) Math.cos(t * 1.7f);
        }
        smooth += (target - smooth) * 0.15f;
        float speed = (state == READY) ? 0.5f : (state == LISTENING ? 1.0f : 1.6f);
        float strength = (state == READY) ? 0.55f : 1f;

        // 1) bahar ki tick wali patti, dheere ghoomti
        c.save();
        c.rotate(t * 6f * speed, cx, cy);
        ring.setStrokeWidth(1f * density);
        for (int i = 0; i < 72; i++) {
            boolean longTick = (i % 6 == 0);
            ring.setColor(alpha(main, longTick ? 0.55f * strength : 0.25f * strength));
            float inner = r * (longTick ? 0.90f : 0.93f);
            c.drawLine(cx, cy - r * 0.97f, cx, cy - inner, ring);
            c.rotate(5f, cx, cy);
        }
        c.restore();

        // 2) patli bareek ring
        ring.setStrokeWidth(1f * density);
        ring.setColor(alpha(main, 0.30f * strength));
        c.drawCircle(cx, cy, r * 0.85f, ring);

        // 3) mote chaap, ulti disha mein ghoomte
        float a = -t * 40f * speed;
        arc(c, cx, cy, r * 0.76f, 4f, a, 80f, main, strength);
        arc(c, cx, cy, r * 0.76f, 4f, a + 120f, 60f, main, strength);
        arc(c, cx, cy, r * 0.76f, 4f, a + 230f, 100f, main, strength);
        arc(c, cx, cy, r * 0.76f, 4f, a + 95f, 14f, second, strength);

        // 4) awaaz ke saath phoolti-sikudti pooree ring
        float pulse = 1f + 0.06f * smooth;
        circle(c, cx, cy, r * 0.58f * pulse, 3.5f, main, strength);
        ring.setStrokeWidth(1f * density);
        ring.setColor(alpha(main, 0.35f * strength));
        c.drawCircle(cx, cy, r * 0.66f * (1f + 0.03f * smooth), ring);

        // 5) beech ki kaali thali aur mic
        fill.setColor(DISC);
        c.drawCircle(cx, cy, r * 0.46f, fill);
        ring.setStrokeWidth(1f * density);
        ring.setColor(alpha(main, 0.45f * strength));
        c.drawCircle(cx, cy, r * 0.46f, ring);
        drawMic(c, cx, cy, r * 0.26f, alpha(main, 0.45f + 0.55f * strength));

        if (running) {
            postInvalidateOnAnimation();
        }
    }
}
