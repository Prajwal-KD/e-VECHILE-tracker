package com.v2v.smartsystem.safety;

/**
 * Interface for UI and hardware callbacks when safety warnings are generated or cleared.
 */
public interface WarningListener {
    void onWarningTriggered(WarningEvent event);
    void onWarningCleared(String warningId);
}
