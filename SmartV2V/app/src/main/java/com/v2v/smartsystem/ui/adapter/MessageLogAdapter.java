package com.v2v.smartsystem.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.v2v.smartsystem.R;
import com.v2v.smartsystem.model.V2VMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for real-time packet stream logs.
 */
public class MessageLogAdapter extends RecyclerView.Adapter<MessageLogAdapter.LogViewHolder> {

    public static class LogEntry {
        public final V2VMessage message;
        public final boolean isOutgoing;
        public final long timestamp;

        public LogEntry(V2VMessage message, boolean isOutgoing) {
            this.message = message;
            this.isOutgoing = isOutgoing;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final List<LogEntry> entries = new ArrayList<>();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    public void addEntry(V2VMessage msg, boolean isOutgoing) {
        entries.add(0, new LogEntry(msg, isOutgoing));
        if (entries.size() > 50) {
            entries.remove(entries.size() - 1);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        entries.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_log, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        LogEntry entry = entries.get(position);
        holder.bind(entry, timeFormat);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class LogViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDirection;
        private final TextView tvTime;
        private final TextView tvType;
        private final TextView tvContent;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDirection = itemView.findViewById(R.id.tvLogDirection);
            tvTime = itemView.findViewById(R.id.tvLogTime);
            tvType = itemView.findViewById(R.id.tvLogType);
            tvContent = itemView.findViewById(R.id.tvLogContent);
        }

        public void bind(LogEntry entry, SimpleDateFormat format) {
            if (entry == null || entry.message == null) return;

            tvTime.setText(format.format(new Date(entry.timestamp)));
            tvType.setText(entry.message.getMessageType().getCode());

            if (entry.isOutgoing) {
                tvDirection.setText("TX");
                tvDirection.setTextColor(Color.parseColor("#00E676"));
            } else {
                tvDirection.setText("RX");
                tvDirection.setTextColor(Color.parseColor("#00E5FF"));
            }

            V2VMessage m = entry.message;
            tvContent.setText(String.format("%s | %.0fkm/h | %s",
                    m.getSenderVehicleId(), m.getSpeedKmh(),
                    m.getPayloadText().isEmpty() ? m.getMessageType().name() : m.getPayloadText()));
        }
    }
}
