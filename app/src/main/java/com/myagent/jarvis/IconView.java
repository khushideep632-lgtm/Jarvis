package com.myagent.jarvis;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

/** Chhote line-icon, bina kisi image file ke, seedha code se bane. */
public class IconView extends View {

    public static final int MENU = 0;
    public static final int TORCH = 1;
    public static final int CALL = 2;
    public static final int ALARM = 3;
    public static final int SETTINGS = 4;
    public static final int KEYBOARD = 5;
    public static final int WAVE = 6;

    private final int type;
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF box = new RectF();
    private final Path path = new Path();
    private int color = 0xFFD7ECFF;

    public IconView(Context context, int type) {
        super(context);
        this.type = type;
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
    }

    public void setColor(int c) {
        color = c;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth();
        float h = getHeight();
        float s = Math.min(w, h);
        float cx = w / 2f;
        float cy = h / 2f;
        float u = s * 0.30f;

        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(Math.max(2f, s * 0.07f));

        switch (type) {
            case MENU:
                c.drawLine(cx - u, cy - u * 0.7f, cx + u, cy - u * 0.7f, p);
                c.drawLine(cx - u, cy, cx + u, cy, p);
                c.drawLine(cx - u, cy + u * 0.7f, cx + u, cy + u * 0.7f, p);
                break;

            case TORCH:
                path.reset();
                path.moveTo(cx - u * 0.55f, cy - u * 0.45f);
                path.lineTo(cx + u * 0.55f, cy - u * 0.45f);
                path.lineTo(cx + u * 0.32f, cy - u * 0.05f);
                path.lineTo(cx - u * 0.32f, cy - u * 0.05f);
                path.close();
                c.drawPath(path, p);
                box.set(cx - u * 0.32f, cy - u * 0.05f, cx + u * 0.32f, cy + u * 0.95f);
                c.drawRoundRect(box, u * 0.15f, u * 0.15f, p);
                c.drawLine(cx, cy - u * 0.75f, cx, cy - u * 1.05f, p);
                c.drawLine(cx - u * 0.65f, cy - u * 0.65f, cx - u * 0.9f, cy - u * 0.9f, p);
                c.drawLine(cx + u * 0.65f, cy - u * 0.65f, cx + u * 0.9f, cy - u * 0.9f, p);
                break;

            case CALL:
                path.reset();
                path.moveTo(cx - u * 0.8f, cy - u * 0.55f);
                path.quadTo(cx - u * 0.8f, cy + u * 0.8f, cx + u * 0.55f, cy + u * 0.8f);
                p.setStrokeWidth(Math.max(2f, s * 0.11f));
                c.drawPath(path, p);
                p.setStyle(Paint.Style.FILL);
                c.drawCircle(cx - u * 0.8f, cy - u * 0.6f, u * 0.28f, p);
                c.drawCircle(cx + u * 0.6f, cy + u * 0.8f, u * 0.28f, p);
                break;

            case ALARM:
                c.drawCircle(cx, cy + u * 0.1f, u * 0.8f, p);
                c.drawLine(cx, cy + u * 0.1f, cx, cy - u * 0.4f, p);
                c.drawLine(cx, cy + u * 0.1f, cx + u * 0.35f, cy + u * 0.3f, p);
                c.drawLine(cx - u * 0.95f, cy - u * 0.5f, cx - u * 0.55f, cy - u * 0.95f, p);
                c.drawLine(cx + u * 0.95f, cy - u * 0.5f, cx + u * 0.55f, cy - u * 0.95f, p);
                break;

            case SETTINGS:
                c.drawCircle(cx, cy, u * 0.35f, p);
                c.drawCircle(cx, cy, u * 0.72f, p);
                c.save();
                for (int i = 0; i < 8; i++) {
                    c.drawLine(cx, cy - u * 0.72f, cx, cy - u * 1.02f, p);
                    c.rotate(45f, cx, cy);
                }
                c.restore();
                break;

            case KEYBOARD:
                box.set(cx - u * 1.1f, cy - u * 0.65f, cx + u * 1.1f, cy + u * 0.65f);
                c.drawRoundRect(box, u * 0.2f, u * 0.2f, p);
                p.setStrokeWidth(Math.max(3f, s * 0.09f));
                for (int i = 0; i < 4; i++) {
                    float x = cx - u * 0.66f + i * u * 0.44f;
                    c.drawPoint(x, cy - u * 0.3f, p);
                    c.drawPoint(x, cy, p);
                }
                p.setStrokeWidth(Math.max(2f, s * 0.07f));
                c.drawLine(cx - u * 0.6f, cy + u * 0.38f, cx + u * 0.6f, cy + u * 0.38f, p);
                break;

            case WAVE:
                float[] hs = {0.3f, 0.7f, 1.0f, 0.6f, 0.35f};
                for (int i = 0; i < hs.length; i++) {
                    float x = cx + (i - 2) * u * 0.55f;
                    c.drawLine(x, cy - u * hs[i], x, cy + u * hs[i], p);
                }
                break;

            default:
                break;
        }
    }
}
