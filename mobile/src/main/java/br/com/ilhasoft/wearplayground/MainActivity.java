package br.com.ilhasoft.wearplayground;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    private static final int SIMPLE_NOTIFICATION_ID = 1;
    private static final int ACTION_NOTIFICATION_ID = 2;
    private static final int REPLY_NOTIFICATION_ID = 3;

    public static final String ACTION_ACCEPT = "br.com.ilhasoft.wearplayground.ACCEPT_OFFER";
    public static final String ACTION_REJECT = "br.com.ilhasoft.wearplayground.REJECT_OFFER";

    private TextView tvNodes;
    private TextView tvLastMessage;
    private TextView tvSharedData;

    private WearHelper wearHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupObjects();
        registerReceivers();
        setupView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wearHelper.disconnect();
        unregisterReceivers();
    }

    private void setupObjects() {
        wearHelper = new WearHelper(this);
        wearHelper.connect(nodesResultResultCallback, messageListener, dataListener);
    }

    private void setupView() {
        tvNodes = (TextView) findViewById(R.id.tvNodes);
        tvLastMessage = (TextView) findViewById(R.id.tvLastMessage);
        tvSharedData = (TextView) findViewById(R.id.tvSharedData);

        Button btSendMessage = (Button) findViewById(R.id.btSendMessage);
        btSendMessage.setOnClickListener(onSendMessageClickListener);

        Button btSaveData = (Button) findViewById(R.id.btSaveData);
        btSaveData.setOnClickListener(onSaveDataClickListener);

        Button btSendNotification = (Button) findViewById(R.id.btSendNotification);
        btSendNotification.setOnClickListener(onSimpleNotificationClickListener);

        Button btSendActionNotification = (Button) findViewById(R.id.btSendActionNotification);
        btSendActionNotification.setOnClickListener(onActionNotificationClickListener);

        Button btSendResponseNotification = (Button) findViewById(R.id.btSendResponseNotification);
        btSendResponseNotification.setOnClickListener(onResponseNotificationClickListener);
    }

    private void registerReceivers() {
        registerReceiver(actionsReceiver, new IntentFilter(ACTION_ACCEPT));
        registerReceiver(actionsReceiver, new IntentFilter(ACTION_REJECT));
    }

    private void unregisterReceivers() {
        unregisterReceiver(actionsReceiver);
    }

    private PendingIntent getBroadcastPendingIntent(String action) {
        return PendingIntent.getBroadcast(this, 0, new Intent(action), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getResponsePendingIntent(String text) {
        Intent intent = new Intent(getApplicationContext(), ResponseActivity.class);
        intent.putExtra(ResponseActivity.EXTRA_NOTIFICATION_CONTENT, text);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(getApplicationContext());
        taskStackBuilder.addParentStack(ResponseActivity.class);
        taskStackBuilder.addNextIntent(intent);

        return taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private BroadcastReceiver actionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "Action escolhida: " + intent.getAction(), Toast.LENGTH_LONG).show();
        }
    };

    private View.OnClickListener onSendMessageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            wearHelper.sendMessage(new Date().toString());
        }
    };

    private View.OnClickListener onSaveDataClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String sharedData = tvSharedData.getText().toString();
            sharedData = sharedData.isEmpty() ? "0" : sharedData + "0";

            wearHelper.saveData(sharedData);
        }
    };

    private View.OnClickListener onSimpleNotificationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PendingIntent pendingIntent = getResponsePendingIntent("clicou em uma notificação simples");

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Título")
                    .setContentText("Conteúdo")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainActivity.this);
            notificationManagerCompat.notify(SIMPLE_NOTIFICATION_ID, notificationBuilder.build());
        }
    };

    private View.OnClickListener onActionNotificationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            PendingIntent pendingIntent = getResponsePendingIntent("clicou em uma notificação com ações");

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Título")
                    .setContentText("Notificação com ações")
                    .setContentIntent(pendingIntent)
                    .addAction(R.drawable.ic_check_white_24dp, "Aceitar", getBroadcastPendingIntent(ACTION_ACCEPT))
                    .addAction(R.drawable.ic_close_white_24dp, "Rejeitar", getBroadcastPendingIntent(ACTION_REJECT))
                    .setAutoCancel(true);

            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            manager.notify(ACTION_NOTIFICATION_ID, notificationBuilder.build());
        }
    };

    private View.OnClickListener onResponseNotificationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RemoteInput remoteInput = new RemoteInput.Builder(ResponseActivity.EXTRA_NOTIFICATION_CONTENT)
                    .setLabel("Diga a resposta")
                    .setChoices(new String[]{"Sim", "Não", "Falo já"})
                    .build();

            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(
                    R.drawable.ic_reply_white_24dp
                    , "Responder"
                    , getResponsePendingIntent("Resposta"))
                    .addRemoteInput(remoteInput)
                    .build();

            Bitmap gdg = BitmapFactory.decodeResource(getResources(), R.drawable.gdg);

            NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
            extender.setBackground(gdg);
            extender.addAction(replyAction);

            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Notificação com resposta")
                    .setContentText("Diga como se sente hoje")
                    .setAutoCancel(true)
                    .extend(extender)
                    .build();

            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            manager.notify(REPLY_NOTIFICATION_ID, notification);
        }
    };

    private ResultCallback<NodeApi.GetConnectedNodesResult> nodesResultResultCallback
            = new ResultCallback<NodeApi.GetConnectedNodesResult>() {
        @Override
        public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
            tvNodes.setText(String.valueOf(getConnectedNodesResult.getNodes().size()));
        }
    };

    private MessageApi.MessageListener messageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {
            if(messageEvent.getPath().equals(WearHelper.DEFAULT_MESSAGE_PATH)) {
                final String message = new String(messageEvent.getData());
                tvLastMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        tvLastMessage.setText(message);
                    }
                });
            }
        }
    };

    private DataApi.DataListener dataListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEventBuffer) {
            for (DataEvent dataEvent : dataEventBuffer) {
                Uri uri = dataEvent.getDataItem().getUri();

                if(uri.getPath().equals(WearHelper.DEFAULT_DATA_PATH)) {
                    DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();

                    final String sharedData = dataMap.getString("data");
                    tvSharedData.post(new Runnable() {
                        @Override
                        public void run() {
                            tvSharedData.setText(sharedData);
                        }
                    });
                }
            }
        }
    };
}
