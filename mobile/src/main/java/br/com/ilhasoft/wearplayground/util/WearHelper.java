package br.com.ilhasoft.wearplayground.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by johndalton on 22/05/15.
 */
public class WearHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WearHelper";

    public static final String DEFAULT_MESSAGE_PATH = "/message";
    public static final String DEFAULT_DATA_PATH = "/shared";

    private GoogleApiClient client;
    private List<Node> nodes;

    private ResultCallback<NodeApi.GetConnectedNodesResult> nodesResultResultCallback;
    private MessageApi.MessageListener messageListener;
    private DataApi.DataListener dataListener;

    public WearHelper(Context context) {
        client = new GoogleApiClient.Builder(context)
                                    .addConnectionCallbacks(this)
                                    .addOnConnectionFailedListener(this)
                                    .addApi(Wearable.API)
                                    .build();
    }

    public void connect(ResultCallback<NodeApi.GetConnectedNodesResult> nodesResultResultCallback
            , MessageApi.MessageListener messageListener
            , DataApi.DataListener dataListener) {

        this.nodesResultResultCallback = nodesResultResultCallback;
        this.messageListener = messageListener;
        this.dataListener = dataListener;

        client.connect();
    }

    public void connect(){
        client.connect();
    }

    public void disconnect() {
        if(client.isConnected()) {
            client.disconnect();
        }
    }

    /** Message API
     *
     * @param message
     */
    public void sendMessage(String message) {
        if(nodes != null && !nodes.isEmpty()) {
            for (Node node : nodes) {
                Wearable.MessageApi.sendMessage(client, node.getId(), DEFAULT_MESSAGE_PATH, message.getBytes());
            }
        }
    }

    private void addMessageListener(MessageApi.MessageListener messageListener) {
        if(client.isConnected()) {
            Wearable.MessageApi.addListener(client, messageListener);
        }
    }

    /** Data API
     *
     * @param data
     */
    public void saveData(String data) {
        if(client.isConnected()) {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DEFAULT_DATA_PATH);
            DataMap dataMap = putDataMapRequest.getDataMap();
            dataMap.putString("data", data);

            Wearable.DataApi.putDataItem(client, putDataMapRequest.asPutDataRequest());
        }
    }

    private void addDataListener(DataApi.DataListener dataListener) {
        if(client.isConnected()) {
            Wearable.DataApi.addListener(client, dataListener);
        }
    }

    /** Node API
     *
     */
    private void searchConnectedNodes() {
        PendingResult<NodeApi.GetConnectedNodesResult> result = Wearable.NodeApi.getConnectedNodes(client);
        result.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                if (nodesResultResultCallback != null)
                    nodesResultResultCallback.onResult(getConnectedNodesResult);

                nodes = getConnectedNodesResult.getNodes();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        searchConnectedNodes();

        addMessageListener(messageListener);
        addDataListener(dataListener);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed ");
    }
}
