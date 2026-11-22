package com.v2v.smartsystem.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.v2v.smartsystem.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom circular spatial radar view visualizing nearby vehicles relative to host position and heading.
 */
public class RadarView extends View {

    private static final float MAX_RANGE_METERS = 150.0f;

    private Paint gridPaint;
    private Paint sweepPaint;
    private Paint textPaint;
    private Paint hostVehiclePaint;
    private Paint remoteVehiclePaint;
    private Paint dangerVehiclePaint;

    private float sweepAngle = 0.0f;
    private final List<Vehicle> vehicles = new ArrayList<>();

    public RadarView(Context context) {
        super(context);
        init();
    }

    public RadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#16324A"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(2.0f);

        sweepPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sweepPaint.setColor(Color.parseColor("#1A4060"));
        sweepPaint.setStyle(Paint.Style.STROKE);
        sweepPaint.setStrokeWidth(3.0f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#546E7A"));
        textPaint.setTextSize(22.0f);

        hostVehiclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hostVehiclePaint.setColor(Color.parseColor("#00E5FF"));
        hostVehiclePaint.setStyle(Paint.Style.FILL);

        remoteVehiclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        remoteVehiclePaint.setColor(Color.parseColor("#FFAB00"));
        remoteVehiclePaint.setStyle(Paint.Style.FILL);

        dangerVehiclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dangerVehiclePaint.setColor(Color.parseColor("#FF1744"));
        dangerVehiclePaint.setStyle(Paint.Style.FILL);
    }

    public void updateVehicles(List<Vehicle> newVehicles) {
        vehicles.clear();
        if (newVehicles != null) {
            vehicles.addAll(newVehicles);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2.0f;
        float cy = getHeight() / 2.0f;
        float radius = Math.min(cx, cy) - 20.0f;

        if (radius <= 0) return;

        // Draw concentric distance circles (50m, 100m, 150m)
        canvas.drawCircle(cx, cy, radius * 0.33f, gridPaint);
        canvas.drawCircle(cx, cy, radius * 0.66f, gridPaint);
        canvas.drawCircle(cx, cy, radius, gridPaint);

        // Draw crosshairs
        canvas.drawLine(cx - radius, cy, cx + radius, cy, gridPaint);
        canvas.drawLine(cx, cy - radius, cx, cy + radius, gridPaint);

        // Distance text labels
        canvas.drawText("50m", cx + 6, cy - radius * 0.33f + 18, textPaint);
        canvas.drawText("100m", cx + 6, cy - radius * 0.66f + 18, textPaint);
        canvas.drawText("150m", cx + 6, cy - radius + 18, textPaint);

        // Draw rotating sweep line
        sweepAngle = (sweepAngle + 2.0f) % 360.0f;
        float sweepRad = (float) Math.toRadians(sweepAngle);
        float sx = cx + radius * (float) Math.cos(sweepRad);
        float sy = cy + radius * (float) Math.sin(sweepRad);
        canvas.drawLine(cx, cy, sx, sy, sweepPaint);

        // Draw host vehicle at center (Cyan arrow pointing forward / Up)
        Path hostPath = new Path();
        hostPath.moveTo(cx, cy - 14.0f);
        hostPath.lineTo(cx - 9.0f, cy + 10.0f);
        hostPath.lineTo(cx, cy + 5.0f);
        hostPath.lineTo(cx + 9.0f, cy + 10.0f);
        hostPath.close();
        canvas.drawPath(hostPath, hostVehiclePaint);

        // Draw nearby vehicles
        for (Vehicle v : vehicles) {
            float dist = (float) v.getDistanceMeters();
            if (dist > MAX_RANGE_METERS) continue;

            float rRatio = (dist / MAX_RANGE_METERS) * radius;
            // Relative bearing: 0 is up (-90° in standard math coords)
            double angleRad = Math.toRadians(v.getRelativeBearing() - 90.0);

            float vx = cx + rRatio * (float) Math.cos(angleRad);
            float vy = cy + rRatio * (float) Math.sin(angleRad);

            if (v.isEmergencyBrakeActive()) {
                // Hazard pulsing ring
                Paint ringPaint = new Paint(dangerVehiclePaint);
                ringPaint.setStyle(Paint.Style.STROKE);
                ringPaint.setStrokeWidth(3.0f);
                canvas.drawCircle(vx, vy, 16.0f, ringPaint);
                canvas.drawCircle(vx, vy, 9.0f, dangerVehiclePaint);
            } else {
                canvas.drawCircle(vx, vy, 8.0f, remoteVehiclePaint);
            }

            // Draw vehicle ID label
            canvas.drawText(v.getVehicleId(), vx + 12, vy + 6, textPaint);
        }

        // Continually animate radar sweep
        postInvalidateOnAnimation();
    }
}
