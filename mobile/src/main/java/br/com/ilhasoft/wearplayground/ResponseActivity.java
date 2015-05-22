package br.com.ilhasoft.wearplayground;

import android.support.v4.app.RemoteInput;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class ResponseActivity extends ActionBarActivity {

    public static final String EXTRA_NOTIFICATION_CONTENT = "notification_content";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        TextView tvResponse = (TextView) findViewById(R.id.tvResponse);

        Bundle remoteInput = RemoteInput.getResultsFromIntent(getIntent());
        if (remoteInput != null) {
            tvResponse.setText("Resposta: " + remoteInput.getCharSequence(EXTRA_NOTIFICATION_CONTENT));
        } else {
            String contentNotification = getIntent().getStringExtra(EXTRA_NOTIFICATION_CONTENT);
            tvResponse.setText("Resposta: " + contentNotification);
        }
    }
}
