package com.v2v.smartsystem.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.v2v.smartsystem.R;
import com.v2v.smartsystem.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter presenting detected nearby vehicles.
 */
public class NearbyVehiclesAdapter extends RecyclerView.Adapter<NearbyVehiclesAdapter.VehicleViewHolder> {

    private final List<Vehicle> items = new ArrayList<>();

    public void updateList(List<Vehicle> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nearby_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = items.get(position);
        holder.bind(vehicle);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvId;
        private final TextView tvTelemetry;
        private final TextView tvDistance;
        private final TextView tvStatusBadge;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivItemVehicleType);
            tvId = itemView.findViewById(R.id.tvItemVehicleId);
            tvTelemetry = itemView.findViewById(R.id.tvItemTelemetry);
            tvDistance = itemView.findViewById(R.id.tvItemDistance);
            tvStatusBadge = itemView.findViewById(R.id.tvItemStatusBadge);
        }

        public void bind(Vehicle vehicle) {
            if (vehicle == null) return;

            tvId.setText(vehicle.getVehicleId());
            ivIcon.setImageResource(vehicle.getVehicleType().getIconResId());

            tvTelemetry.setText(String.format("Speed: %.0f km/h • Heading: %.0f°",
                    vehicle.getSpeedKmh(), vehicle.getHeadingDegrees()));

            tvDistance.setText(String.format("%.0f m", vehicle.getDistanceMeters()));

            if (vehicle.isEmergencyBrakeActive()) {
                tvStatusBadge.setText("BRAKING");
                tvStatusBadge.setTextColor(Color.parseColor("#FF1744"));
            } else if (vehicle.isHazardActive()) {
                tvStatusBadge.setText("HAZARD");
                tvStatusBadge.setTextColor(Color.parseColor("#FFAB00"));
            } else if (!"NONE".equals(vehicle.getTurnSignal()) && !"OFF".equals(vehicle.getTurnSignal())) {
                tvStatusBadge.setText("TURN " + vehicle.getTurnSignal());
                tvStatusBadge.setTextColor(Color.parseColor("#00E5FF"));
            } else {
                tvStatusBadge.setText("SAFE");
                tvStatusBadge.setTextColor(Color.parseColor("#00E676"));
            }
        }
    }
}
