package com.example.myapplication.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DonutProgressView extends View {

    private int progress = 17; // percentage
    private Paint trackPaint, progressPaint, percentTextPaint, usedTextPaint;
    private final RectF rectF = new RectF();

    public DonutProgressView(Context context) {
        super(context);
        init();
    }

    public DonutProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DonutProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        float density = getResources().getDisplayMetrics().density;
        float strokeWidth = 10 * density;

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(strokeWidth);
        trackPaint.setColor(Color.parseColor("#EFE9DA"));

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(Color.parseColor("#C59A27"));

        percentTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        percentTextPaint.setTextSize(20 * density);
        percentTextPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        percentTextPaint.setColor(Color.parseColor("#121926"));
        percentTextPaint.setTextAlign(Paint.Align.CENTER);

        usedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        usedTextPaint.setTextSize(11 * density);
        usedTextPaint.setTypeface(Typeface.SANS_SERIF);
        usedTextPaint.setColor(Color.parseColor("#64748B"));
        usedTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setProgress(int pct) {
        this.progress = Math.min(100, Math.max(0, pct));
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float density = getResources().getDisplayMetrics().density;
        float strokeWidth = 10 * density;
        float pad = strokeWidth / 2f + 4 * density;

        float width = getWidth();
        float height = getHeight();

        if (width <= 0 || height <= 0) return;

        rectF.set(pad, pad, width - pad, height - pad);

        // Draw background track ring
        canvas.drawOval(rectF, trackPaint);

        // Draw progress arc
        float sweepAngle = (progress / 100f) * 360f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);

        // Draw text in center
        float centerX = width / 2f;
        float centerY = height / 2f;

        String pctText = progress + "%";
        canvas.drawText(pctText, centerX, centerY, percentTextPaint);
        canvas.drawText("used", centerX, centerY + 16 * density, usedTextPaint);
    }
}
