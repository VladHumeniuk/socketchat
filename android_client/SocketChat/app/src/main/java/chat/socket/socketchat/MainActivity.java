package chat.socket.socketchat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.message) EditText mMessage;
    @Bind(R.id.messages) ListView mMessageList;

    private ArrayAdapter<String> mAdapter;
    private Connector mConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mMessageList.setAdapter(mAdapter);

        EventBus.getDefault().register(this);

        mConnector = new Connector();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mConnector.connect();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mConnector.close();
        super.onDestroy();
    }

    @OnClick(R.id.send)
    protected void onSendClick() {
        String message = mMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            mConnector.post(message);
            mMessage.setText("");
        }
    }

    public void onEventMainThread(String str) {
        mAdapter.add(str);
    }
}
