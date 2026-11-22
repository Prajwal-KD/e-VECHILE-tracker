package com.v2v.smartsystem.model;

import com.v2v.smartsystem.R;

/**
 * Supported vehicle categories in the Smart V2V Communication System.
 * Defines operational parameters such as braking deceleration and icon assets.
 */
public enum VehicleType {
    CAR("Car", R.drawable.ic_car, 6.0f),
    BIKE("Bike", R.drawable.ic_bike, 7.5f),
    TRUCK("Truck", R.drawable.ic_truck, 4.0f),
    BUS("Bus", R.drawable.ic_bus, 4.5f);

    private final String displayName;
    private final int iconResId;
    private final float maxDecelMps2;

    VehicleType(String displayName, int iconResId, float maxDecelMps2) {
        this.displayName = displayName;
        this.iconResId = iconResId;
        this.maxDecelMps2 = maxDecelMps2;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getIconResId() {
        return iconResId;
    }

    public float getMaxDecelMps2() {
        return maxDecelMps2;
    }

    public static VehicleType fromString(String name) {
        if (name == null) return CAR;
        for (VehicleType type : values()) {
            if (type.name().equalsIgnoreCase(name) || type.displayName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return CAR;
    }
}
