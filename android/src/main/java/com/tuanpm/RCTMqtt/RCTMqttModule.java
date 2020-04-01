/**
 * Created by TuanPM (tuanpm@live.com) on 1/4/16.
 */

package com.tuanpm.RCTMqtt;

import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;
import java.util.UUID;

public class RCTMqttModule extends ReactContextBaseJavaModule
{

    private static final String TAG = "RCTMqttModule";
    private final ReactApplicationContext reactContext;
    private HashMap<String, RCTMqtt> clients;

    public RCTMqttModule(ReactApplicationContext reactContext)
    {
        super(reactContext);
        this.reactContext = reactContext;
        clients = new HashMap<>();
    }

    @Override
    public @NonNull String getName()
    {
        return "Mqtt";
    }

    @ReactMethod
    public void createClient(final ReadableMap options,
                             Promise promise)
    {
        final String clientRef = createClientRef();
        RCTMqtt client = new RCTMqtt(clientRef, reactContext, options);
        client.setCallback();
        clients.put(clientRef, client);
        promise.resolve(clientRef);
        log(new StringBuilder("ClientRef:").append(clientRef).toString());
    }

    @ReactMethod
    public void connect(@NonNull final String clientRef)
    {
        clients.get(clientRef).connect();
    }

    @ReactMethod
    public void disconnect(@NonNull final String clientRef)
    {
        clients.get(clientRef).disconnect();
    }

    @ReactMethod
    public void disconnectAll()
    {
        if (clients != null && clients.size() > 0) {
            for (RCTMqtt aClient : clients.values()) {
                aClient.disconnect();
            }
        }
    }

    @ReactMethod
    public void subscribe(@NonNull final String clientRef,
                          @NonNull final String topic,
                          final int qos)
    {
        clients.get(clientRef).subscribe(topic, qos);
    }

    @ReactMethod
    public void unsubscribe(@NonNull final String clientRef,
                            final @NonNull String topic)
    {
        clients.get(clientRef).unsubscribe(topic);
    }

    @ReactMethod
    public int publish(@NonNull final String clientRef,
                        @NonNull final String topic,
                        @NonNull final String payload,
                        final int qos,
                        final boolean retain)
    {
        return clients.get(clientRef).publish(topic, payload, qos, retain);
    }

    @ReactMethod
    public int publishUInt8(@NonNull final String clientRef,
                        @NonNull final String topic,
                        @NonNull final ReadableArray payload,
                        final int qos,
                        final boolean retain)
    {
        byte[] bytesArr = new byte[payload.size()];
        for (int i = 0; i < payload.size(); i++) {
            //String str = payload.getString(i);
            int in = payload.getInt(i);
            //bytesArr[i] = in & 0xFF;
            bytesArr[i] = (byte)in;
        }
        return clients.get(clientRef).publishUInt8(topic, bytesArr, qos, retain);
    }

    @ReactMethod
    public void removeClient(@NonNull final String clientRef) {
        if (!clients.containsKey(clientRef))
        {
            return;
        }
        log(new StringBuilder("Remove client ").append(clientRef).toString());
        clients.remove(clientRef);
    }

    @ReactMethod
    public void reconnect(@NonNull final String clientRef)
    {
        clients.get(clientRef).reconnect();
    }

    @ReactMethod
    public void isConnected(@NonNull final String clientRef, Promise promise)
    {
        promise.resolve(clients.get(clientRef).isConnected());
    }

    private String createClientRef()
    {
        return UUID.randomUUID().toString();
    }

    private void log(@NonNull final String message)
    {
        if (!BuildConfig.DEBUG)
        {
            return;
        }
        Log.d(TAG, message);
    }

    @Override
    public void onCatalystInstanceDestroy() {
        disconnectAll();
    }

}