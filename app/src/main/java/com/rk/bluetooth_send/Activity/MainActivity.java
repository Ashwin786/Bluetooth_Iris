package com.rk.bluetooth_send.Activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rk.bluetooth_send.BlueToothPrint_new;
import com.rk.bluetooth_send.Connectivity;
import com.rk.bluetooth_send.R;

public class MainActivity extends AppCompatActivity implements Connectivity, View.OnClickListener {

    private TextView tv_connect_status;
    private TextView tv_message;
    private Button mSend;
    private Button mReceive;
    private Button mIrisConnect;
    private EditText mEdSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        tv_connect_status = (TextView) findViewById(R.id.device_status);
        tv_message = (TextView) findViewById(R.id.message);
        BlueToothPrint_new.getinstance(this).setCallBack(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        BlueToothPrint_new.getinstance(this).resume();
    }

    @Override
    public void connected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_connect_status.setText("Connected");
            }
        });
    }

    @Override
    public void disconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_connect_status.setText("DisConnected");
            }
        });
    }

    @Override
    public void message(final String readMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tv_message.setText(tv_message.getText().toString() + "\n" + readMessage);
            }
        });
    }

    private void initView() {

        mIrisConnect = (Button) findViewById(R.id.mIrisConnect);
        mReceive = (Button) findViewById(R.id.mReceive);

        mEdSend = (EditText) findViewById(R.id.mEdSend);
        mSend = (Button) findViewById(R.id.mSend);

        mSend.setOnClickListener(this);
        mReceive.setOnClickListener(this);
        mIrisConnect.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mIrisConnect:
                BlueToothPrint_new.getinstance(this).opendialog();
//                startActivity(new Intent(this,SendActivity.class));

                break;
            case R.id.mReceive:
                BlueToothPrint_new.getinstance(this).start();

//                startActivity(new Intent(this, ReceiveActivity.class));
                break;
            case R.id.mSend:
                String edtext = mEdSend.getText().toString();
                if (!edtext.isEmpty()) {
                    mEdSend.setText("");
                    BlueToothPrint_new.getinstance(this).sendMessage(edtext);
                } else
                    Toast.makeText(MainActivity.this, "Enter the text", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(this, IrisActivity.class));
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        BlueToothPrint_new.getinstance(this).start();
        BlueToothPrint_new.getinstance(this).opendialog();
    }
}
