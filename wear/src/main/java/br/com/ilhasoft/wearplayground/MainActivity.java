package br.com.ilhasoft.wearplayground;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.NodeApi;

import java.util.Date;

import br.com.ilhasoft.wearplayground.util.WearHelper;

public class MainActivity extends Activity {

    private TextView tvNodes;
    private TextView tvLastMessage;
    private TextView tvSharedData;

    private WearHelper wearHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wearHelper = new WearHelper(MainActivity.this);

        setupViews();
        connect();
    }

    private void setupViews() {
        tvNodes = (TextView) findViewById(R.id.tvNodes);
        tvLastMessage = (TextView) findViewById(R.id.tvLastMessage);
        tvSharedData = (TextView) findViewById(R.id.tvSharedData);

        Button btSendMessage = (Button) findViewById(R.id.btSendMessage);
        btSendMessage.setOnClickListener(onSendMessageClickListener);

        Button btSaveData = (Button) findViewById(R.id.btSaveData);
        btSaveData.setOnClickListener(onSaveDataClickListener);
    }

    private void connect() {
        wearHelper.connect(nodesResultResultCallback, messageListener, dataListener);
    }

    private void setSharedDataInUIThread(final String sharedData) {
        tvSharedData.post(new Runnable() {
            @Override
            public void run() {
                tvSharedData.setText(sharedData);
            }
        });
    }

    private void setMessageInUIThread(final MessageEvent messageEvent) {
        tvLastMessage.post(new Runnable() {
            @Override
            public void run() {
                tvLastMessage.setText(new String(messageEvent.getData()));
            }
        });
    }

    private ResultCallback<NodeApi.GetConnectedNodesResult> nodesResultResultCallback =
            new ResultCallback<NodeApi.GetConnectedNodesResult>() {
        @Override
        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
            tvNodes.setText(String.valueOf(getConnectedNodesResult.getNodes().size()));
        }
    };

    private DataApi.DataListener dataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            for (DataEvent dataEvent : dataEventBuffer) {
                Uri uri = dataEvent.getDataItem().getUri();

                if (uri.getPath().equals(WearHelper.DEFAULT_DATA_PATH)) {
                    DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();

                    String sharedData = dataMap.getString("data");
                    setSharedDataInUIThread(sharedData);
                }
            }
        }
    };

    private MessageApi.MessageListener messageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(final MessageEvent messageEvent) {
            if (messageEvent.getPath().equals(WearHelper.DEFAULT_MESSAGE_PATH)) {
                setMessageInUIThread(messageEvent);
            }
        }
    };

    private View.OnClickListener onSendMessageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            wearHelper.sendMessage(new Date().toString());
        }
    };

    private View.OnClickListener onSaveDataClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String sharedData = tvSharedData.getText().toString();
            sharedData = sharedData.isEmpty() ? "1" : sharedData + "1";

            wearHelper.saveData(sharedData);
        }
    };
}
