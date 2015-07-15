package com.o3dr.android.client.apis;

import android.os.Bundle;

import com.o3dr.android.client.Drone;
import com.o3dr.services.android.lib.drone.companion.solo.SoloControllerMode;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloButtonSettingSetter;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproRecord;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.SoloGoproSetRequest;
import com.o3dr.services.android.lib.drone.companion.solo.tlv.TLVPacket;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.model.action.Action;

import java.util.concurrent.ConcurrentHashMap;

import static com.o3dr.services.android.lib.drone.companion.solo.action.SoloLinkActions.*;

/**
 * Provides access to the sololink specific functionality
 * Created by Fredia Huya-Kouadio on 7/12/15.
 */
public class SoloLinkApi implements Api {

    private static final ConcurrentHashMap<Drone, SoloLinkApi> soloLinkApiCache = new ConcurrentHashMap<>();

    /**
     * Retrieves a sololink api instance.
     * @param drone target vehicle
     * @return a SoloLinkApi instance.
     */
    public static SoloLinkApi getApi(final Drone drone){
        return ApiUtils.getApi(drone, soloLinkApiCache, new Builder<SoloLinkApi>() {
            @Override
            public SoloLinkApi build() {
                return new SoloLinkApi(drone);
            }
        });
    }

    private final Drone drone;

    private SoloLinkApi(Drone drone){
        this.drone = drone;
    }

    /**
     * Updates the wifi settings for the solo vehicle.
     * @param wifiSsid Updated wifi ssid
     * @param wifiPassword Updated wifi password
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void updateWifiSettings(String wifiSsid, String wifiPassword, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putString(EXTRA_WIFI_SSID, wifiSsid);
        params.putString(EXTRA_WIFI_PASSWORD, wifiPassword);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_WIFI_SETTINGS, params), listener);
    }

    /**
     * Updates the button settings for the solo vehicle.
     * @param buttonSettings Updated button settings.
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void updateButtonSettings(SoloButtonSettingSetter buttonSettings, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_BUTTON_SETTINGS, buttonSettings);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_BUTTON_SETTINGS, params), listener);
    }

    /**
     * Sends a message to the solo vehicle.
     * @param messagePacket TLV message data.
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void sendMessage(TLVPacket messagePacket, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putParcelable(EXTRA_MESSAGE_DATA, messagePacket);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_SEND_MESSAGE, params), listener);
    }

    /**
     * Updates the controller mode (joystick mapping)
     *
     * @param controllerMode Controller mode. @see {@link SoloControllerMode}
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void updateControllerMode(@SoloControllerMode.ControllerMode int controllerMode, AbstractCommandListener listener){
        Bundle params = new Bundle();
        params.putInt(EXTRA_CONTROLLER_MODE, controllerMode);
        drone.performAsyncActionOnDroneThread(new Action(ACTION_UPDATE_CONTROLLER_MODE, params), listener);
    }

    /**
     * Take a photo with the connected gopro.
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void takePhoto(final AbstractCommandListener listener){
        //Set the gopro to photo mode
        final SoloGoproSetRequest photoModeRequest = new SoloGoproSetRequest(SoloGoproSetRequest.CAPTURE_MODE,
                SoloGoproSetRequest.CAPTURE_MODE_PHOTO);

        sendMessage(photoModeRequest, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                //Send the command to take a picture.
                final SoloGoproRecord photoRecord = new SoloGoproRecord(SoloGoproRecord.START_RECORDING);
                sendMessage(photoRecord, listener);
            }

            @Override
            public void onError(int executionError) {
                if (listener != null) {
                    listener.onError(executionError);
                }
            }

            @Override
            public void onTimeout() {
                if (listener != null) {
                    listener.onTimeout();
                }
            }
        });
    }

    /**
     * Toggle video recording on the connected gopro.
     * @param listener Register a callback to receive update of the command execution status.
     */
    public void toggleVideoRecording(final AbstractCommandListener listener){
        //Set the gopro to video mode
        final SoloGoproSetRequest videoModeRequest = new SoloGoproSetRequest(SoloGoproSetRequest.CAPTURE_MODE,
                SoloGoproSetRequest.CAPTURE_MODE_VIDEO);

        sendMessage(videoModeRequest, new AbstractCommandListener() {
            @Override
            public void onSuccess() {
                //Send the command to toggle video recording
                final SoloGoproRecord videoToggle = new SoloGoproRecord(SoloGoproRecord.TOGGLE_RECORDING);
                sendMessage(videoToggle, listener);
            }

            @Override
            public void onError(int executionError) {
                if(listener != null){
                    listener.onError(executionError);
                }
            }

            @Override
            public void onTimeout() {
                if(listener != null){
                    listener.onTimeout();
                }
            }
        });
    }
}
