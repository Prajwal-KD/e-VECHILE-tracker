package com.v2v.smartsystem.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.v2v.smartsystem.R;
import com.v2v.smartsystem.communication.V2VMessageDispatcher;
import com.v2v.smartsystem.hardware.HardwareManager;
import com.v2v.smartsystem.hardware.HardwareStatusListener;
import com.v2v.smartsystem.model.HardwarePacket;
import com.v2v.smartsystem.model.V2VMessage;
import com.v2v.smartsystem.model.Vehicle;
import com.v2v.smartsystem.model.VehicleType;
import com.v2v.smartsystem.safety.SafetyActionController;
import com.v2v.smartsystem.safety.WarningEvent;
import com.v2v.smartsystem.safety.WarningListener;
import com.v2v.smartsystem.safety.WarningSystem;
import com.v2v.smartsystem.ui.adapter.MessageLogAdapter;
import com.v2v.smartsystem.ui.adapter.NearbyVehiclesAdapter;
import com.v2v.smartsystem.ui.view.RadarView;
import com.v2v.smartsystem.vehicle.VehicleManager;
import com.v2v.smartsystem.vehicle.VehicleStatusListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Main Cockpit Dashboard for the Smart V2V Communication System.
 */
public class MainActivity extends AppCompatActivity implements
        VehicleStatusListener,
        HardwareStatusListener,
        WarningListener,
        SafetyActionController.SafetyActionListener,
        V2VMessageDispatcher.DispatcherEventListener {

    private static final int PERMISSION_REQUEST_CODE = 101;

    // UI Elements
    private TextView tvHwStatusBadge;
    private ImageView ivHwStatusDot;
    private LinearLayout layoutHardwareHeaderBadge;

    private LinearLayout cardActiveWarning;
    private TextView tvWarningTitle;
    private TextView tvWarningDescription;
    private ImageButton btnDismissWarning;

    private ImageView ivVehicleTypeIcon;
    private TextView tvVehicleTypeName;
    private TextView tvVehicleId;
    private Button btnChangeVehicleType;

    private TextView tvVehicleSpeed;
    private TextView tvVehicleHeading;
    private TextView tvGpsStatus;
    private TextView tvGpsCoordinates;
    private SeekBar seekBarSpeed;

    private TextView tvHwBrakeState;
    private TextView tvHwTurnState;
    private TextView tvHwBuzzerState;
    private TextView tvHwDetails;
    private Button btnConfigureHardware;

    private Button btnEmergencyBrake;
    private Button btnTurnLeft;
    private Button btnHazardFlash;
    private Button btnTurnRight;
    private Button btnSimulateIncomingThreat;

    private RadarView radarView;
    private TextView tvEmptyVehicles;
    private RecyclerView rvNearbyVehicles;
    private NearbyVehiclesAdapter vehiclesAdapter;

    private RecyclerView rvMessageLogs;
    private MessageLogAdapter logAdapter;
    private Button btnClearLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkAndRequestPermissions();

        // Subscribe to subsystem listeners
        VehicleManager.getInstance().addListener(this);
        HardwareManager.getInstance().addListener(this);
        WarningSystem.getInstance().addListener(this);
        SafetyActionController.getInstance().addListener(this);
        V2VMessageDispatcher.getInstance().addListener(this);

        // Start V2V transmission loop
        V2VMessageDispatcher.getInstance().start();

        updateHostVehicleDisplay(VehicleManager.getInstance().getHostVehicle());
    }

    private void initViews() {
        tvHwStatusBadge = findViewById(R.id.tvHwStatusBadge);
        ivHwStatusDot = findViewById(R.id.ivHwStatusDot);
        layoutHardwareHeaderBadge = findViewById(R.id.layoutHardwareHeaderBadge);

        cardActiveWarning = findViewById(R.id.cardActiveWarning);
        tvWarningTitle = findViewById(R.id.tvWarningTitle);
        tvWarningDescription = findViewById(R.id.tvWarningDescription);
        btnDismissWarning = findViewById(R.id.btnDismissWarning);

        ivVehicleTypeIcon = findViewById(R.id.ivVehicleTypeIcon);
        tvVehicleTypeName = findViewById(R.id.tvVehicleTypeName);
        tvVehicleId = findViewById(R.id.tvVehicleId);
        btnChangeVehicleType = findViewById(R.id.btnChangeVehicleType);

        tvVehicleSpeed = findViewById(R.id.tvVehicleSpeed);
        tvVehicleHeading = findViewById(R.id.tvVehicleHeading);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);
        tvGpsCoordinates = findViewById(R.id.tvGpsCoordinates);
        seekBarSpeed = findViewById(R.id.seekBarSpeed);

        tvHwBrakeState = findViewById(R.id.tvHwBrakeState);
        tvHwTurnState = findViewById(R.id.tvHwTurnState);
        tvHwBuzzerState = findViewById(R.id.tvHwBuzzerState);
        tvHwDetails = findViewById(R.id.tvHwDetails);
        btnConfigureHardware = findViewById(R.id.btnConfigureHardware);

        btnEmergencyBrake = findViewById(R.id.btnEmergencyBrake);
        btnTurnLeft = findViewById(R.id.btnTurnLeft);
        btnHazardFlash = findViewById(R.id.btnHazardFlash);
        btnTurnRight = findViewById(R.id.btnTurnRight);
        btnSimulateIncomingThreat = findViewById(R.id.btnSimulateIncomingThreat);

        radarView = findViewById(R.id.radarView);
        tvEmptyVehicles = findViewById(R.id.tvEmptyVehicles);
        rvNearbyVehicles = findViewById(R.id.rvNearbyVehicles);

        rvMessageLogs = findViewById(R.id.rvMessageLogs);
        btnClearLogs = findViewById(R.id.btnClearLogs);

        // RecyclerView Setups
        vehiclesAdapter = new NearbyVehiclesAdapter();
        rvNearbyVehicles.setLayoutManager(new LinearLayoutManager(this));
        rvNearbyVehicles.setAdapter(vehiclesAdapter);

        logAdapter = new MessageLogAdapter();
        rvMessageLogs.setLayoutManager(new LinearLayoutManager(this));
        rvMessageLogs.setAdapter(logAdapter);

        // Interactive Click Listeners
        layoutHardwareHeaderBadge.setOnClickListener(v -> showHardwareConfigDialog());
        btnConfigureHardware.setOnClickListener(v -> showHardwareConfigDialog());
        btnChangeVehicleType.setOnClickListener(v -> showVehicleTypeDialog());

        btnDismissWarning.setOnClickListener(v -> {
            WarningSystem.getInstance().clearActiveWarning();
            cardActiveWarning.setVisibility(View.GONE);
        });

        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    VehicleManager.getInstance().setSpeed((float) progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Safety Controls
        btnEmergencyBrake.setOnClickListener(v -> SafetyActionController.getInstance().toggleEmergencyBrake());
        btnTurnLeft.setOnClickListener(v -> SafetyActionController.getInstance().setTurnSignalState("LEFT"));
        btnHazardFlash.setOnClickListener(v -> SafetyActionController.getInstance().setTurnSignalState("HAZARD"));
        btnTurnRight.setOnClickListener(v -> SafetyActionController.getInstance().setTurnSignalState("RIGHT"));

        // Demo Threat Button
        btnSimulateIncomingThreat.setOnClickListener(v -> {
            Toast.makeText(this, "Simulating vehicle ahead slamming brakes!", Toast.LENGTH_SHORT).show();
            V2VMessageDispatcher.getInstance().simulateNearbyHazardVehicle("Truck", 38.0, true);
        });

        btnClearLogs.setOnClickListener(v -> logAdapter.clear());
    }

    private void updateHostVehicleDisplay(Vehicle vehicle) {
        if (vehicle == null) return;
        tvVehicleId.setText("ID: " + vehicle.getVehicleId());
        tvVehicleTypeName.setText(vehicle.getVehicleType().getDisplayName());
        ivVehicleTypeIcon.setImageResource(vehicle.getVehicleType().getIconResId());
        tvVehicleSpeed.setText(String.format("%.0f", vehicle.getSpeedKmh()));
        tvVehicleHeading.setText(String.format("%.0f°", vehicle.getHeadingDegrees()));
        tvGpsCoordinates.setText(String.format("%.4f, %.4f", vehicle.getLatitude(), vehicle.getLongitude()));
    }

    // --- Subsystem Callbacks ---

    @Override
    public void onVehicleProfileChanged(Vehicle vehicle) {
        runOnUiThread(() -> updateHostVehicleDisplay(vehicle));
    }

    @Override
    public void onSpeedChanged(float newSpeedKmh) {
        runOnUiThread(() -> tvVehicleSpeed.setText(String.format("%.0f", newSpeedKmh)));
    }

    @Override
    public void onHeadingChanged(float newHeading) {
        runOnUiThread(() -> tvVehicleHeading.setText(String.format("%.0f°", newHeading)));
    }

    @Override
    public void onEmergencyBrakeStateChanged(boolean isActive, String source) {
        runOnUiThread(() -> {
            if (isActive) {
                btnEmergencyBrake.setBackgroundColor(Color.parseColor("#B71C1C"));
                btnEmergencyBrake.setText("BRAKE ACTIVE (" + source + ")");
            } else {
                btnEmergencyBrake.setBackgroundColor(Color.parseColor("#FF1744"));
                btnEmergencyBrake.setText(R.string.action_emergency_brake);
            }
        });
    }

    @Override
    public void onTurnSignalStateChanged(String turnState) {
        runOnUiThread(() -> {
            btnTurnLeft.setTextColor("LEFT".equals(turnState) ? Color.parseColor("#00E5FF") : Color.WHITE);
            btnHazardFlash.setTextColor("HAZARD".equals(turnState) ? Color.parseColor("#FFAB00") : Color.WHITE);
            btnTurnRight.setTextColor("RIGHT".equals(turnState) ? Color.parseColor("#00E5FF") : Color.WHITE);
        });
    }

    @Override
    public void onWarningTriggered(WarningEvent event) {
        runOnUiThread(() -> {
            cardActiveWarning.setVisibility(View.VISIBLE);
            tvWarningTitle.setText(event.getTitle());
            tvWarningDescription.setText(event.getDescription());
        });
    }

    @Override
    public void onWarningCleared(String warningId) {
        runOnUiThread(() -> cardActiveWarning.setVisibility(View.GONE));
    }

    @Override
    public void onHardwareConnected(String deviceName, String address) {
        runOnUiThread(() -> {
            tvHwStatusBadge.setText(deviceName);
            ivHwStatusDot.setColorFilter(Color.parseColor("#00E676"));
            tvHwDetails.setText("Hardware Connected: " + deviceName + " (" + address + ")");
        });
    }

    @Override
    public void onHardwareDisconnected(String reason) {
        runOnUiThread(() -> {
            tvHwStatusBadge.setText("HW Offline");
            ivHwStatusDot.setColorFilter(Color.parseColor("#FF5252"));
            tvHwDetails.setText("Hardware Disconnected: " + reason);
        });
    }

    @Override
    public void onHardwareError(String errorMessage) {
        runOnUiThread(() -> Toast.makeText(this, "Hardware: " + errorMessage, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onTelemetryPacketReceived(HardwarePacket packet) {
        runOnUiThread(() -> {
            if (packet != null) {
                tvHwBrakeState.setText(packet.isBrakePressed() ? "TRIPPED" : "IDLE");
                tvHwBrakeState.setTextColor(packet.isBrakePressed() ? Color.parseColor("#FF1744") : Color.parseColor("#90A4AE"));

                tvHwTurnState.setText(packet.getTurnSwitch());
                tvHwTurnState.setTextColor(!"OFF".equals(packet.getTurnSwitch()) ? Color.parseColor("#00E5FF") : Color.parseColor("#90A4AE"));

                tvHwBuzzerState.setText(HardwareManager.getInstance().getSimulator().isBuzzerActive() ? "ACTIVE" : "SILENT");
            }
        });
    }

    @Override
    public void onBrakeSensorTriggered(boolean isPressed) {
        // Automatically handled by SafetyActionController
    }

    @Override
    public void onTurnSwitchChanged(String switchState) {
        // Automatically handled by SafetyActionController
    }

    @Override
    public void onPacketDispatched(V2VMessage message, boolean isOutgoing) {
        runOnUiThread(() -> logAdapter.addEntry(message, isOutgoing));
    }

    @Override
    public void onNearbyVehiclesUpdated(List<Vehicle> vehicles) {
        runOnUiThread(() -> {
            radarView.updateVehicles(vehicles);
            vehiclesAdapter.updateList(vehicles);
            tvEmptyVehicles.setVisibility(vehicles.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    // --- Dialogs ---

    @SuppressLint("MissingPermission")
    private void showHardwareConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_hardware_config, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Spinner spDevices = dialogView.findViewById(R.id.spBluetoothDevices);
        Button btnConnect = dialogView.findViewById(R.id.btnConnectBtHardware);
        Button btnDisconnect = dialogView.findViewById(R.id.btnDisconnectBtHardware);
        Button btnTestBuzzer = dialogView.findViewById(R.id.btnTestHardwareActuator);
        Button btnTripBrake = dialogView.findViewById(R.id.btnSimulateHwBrakePress);
        Button btnClose = dialogView.findViewById(R.id.btnCloseHwDialog);

        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        final List<String> deviceLabels = new ArrayList<>();
        final List<String> deviceAddresses = new ArrayList<>();

        if (bt != null && bt.isEnabled()) {
            Set<BluetoothDevice> paired = bt.getBondedDevices();
            for (BluetoothDevice d : paired) {
                deviceLabels.add(d.getName() + " (" + d.getAddress() + ")");
                deviceAddresses.add(d.getAddress());
            }
        }
        if (deviceLabels.isEmpty()) {
            deviceLabels.add("No Paired Devices Found (Pair HC-05/ESP32 first)");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, deviceLabels);
        spDevices.setAdapter(adapter);

        btnConnect.setOnClickListener(v -> {
            int pos = spDevices.getSelectedItemPosition();
            if (!deviceAddresses.isEmpty() && pos < deviceAddresses.size()) {
                String mac = deviceAddresses.get(pos);
                HardwareManager.getInstance().connectPhysicalBluetooth(mac);
                Toast.makeText(this, "Connecting to " + mac + "...", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please pair hardware device in Android Bluetooth Settings first.", Toast.LENGTH_LONG).show();
            }
        });

        btnDisconnect.setOnClickListener(v -> {
            HardwareManager.getInstance().disconnect();
            dialog.dismiss();
        });

        btnTestBuzzer.setOnClickListener(v -> {
            HardwareManager.getInstance().triggerHardwareAlert(true, "RED");
            Toast.makeText(this, "Hardware Alert Sent (#ALERT,BUZZER=ON,LED=RED)", Toast.LENGTH_SHORT).show();
        });

        btnTripBrake.setOnClickListener(v -> {
            boolean current = HardwareManager.getInstance().getSimulator().isConnected();
            HardwareManager.getInstance().getSimulator().triggerBrakeSensor(true);
            Toast.makeText(this, "Physical Brake Sensor Tripped!", Toast.LENGTH_SHORT).show();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showVehicleTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_vehicle_type, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        RadioGroup rg = dialogView.findViewById(R.id.rgVehicleTypes);
        RadioButton rbCar = dialogView.findViewById(R.id.rbCar);
        RadioButton rbBike = dialogView.findViewById(R.id.rbBike);
        RadioButton rbTruck = dialogView.findViewById(R.id.rbTruck);
        RadioButton rbBus = dialogView.findViewById(R.id.rbBus);
        Button btnRegen = dialogView.findViewById(R.id.btnRegenerateVehicleId);
        Button btnSave = dialogView.findViewById(R.id.btnSaveVehicleConfig);

        Vehicle host = VehicleManager.getInstance().getHostVehicle();
        if (host != null) {
            switch (host.getVehicleType()) {
                case BIKE: rbBike.setChecked(true); break;
                case TRUCK: rbTruck.setChecked(true); break;
                case BUS: rbBus.setChecked(true); break;
                default: rbCar.setChecked(true); break;
            }
        }

        btnRegen.setOnClickListener(v -> {
            VehicleManager.getInstance().regenerateVehicleId();
            Toast.makeText(this, "Generated New Vehicle ID: " + VehicleManager.getInstance().getHostVehicle().getVehicleId(), Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> {
            VehicleType selected = VehicleType.CAR;
            if (rbBike.isChecked()) selected = VehicleType.BIKE;
            else if (rbTruck.isChecked()) selected = VehicleType.TRUCK;
            else if (rbBus.isChecked()) selected = VehicleType.BUS;

            VehicleManager.getInstance().setVehicleType(selected);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void checkAndRequestPermissions() {
        List<String> needed = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.BLUETOOTH_SCAN);
            }
        }

        if (!needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        V2VMessageDispatcher.getInstance().stop();
    }
}
