package com.example.BTControl;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.UUID;

public class Main extends Activity implements SeekBar.OnSeekBarChangeListener{

    String DeviceMAC = "20:13:07:18:51:14";

    boolean Connection = false;
    BluetoothAdapter BTadapter;
    BluetoothDevice device;
    BluetoothSocket btSocket;
    SeekBar Rudder, Throttle;
    TextView ConnectStatus;
    Button ConnectButton;
    ProgressBar Busy;
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        BTadapter = BluetoothAdapter.getDefaultAdapter();

        Rudder = (SeekBar) findViewById(R.id.seekBarRudd);
        Throttle = (SeekBar) findViewById(R.id.seekBarThrot);
        ConnectStatus = (TextView) findViewById(R.id.textView1);
        ConnectButton = (Button) findViewById(R.id.button);
        Busy = (ProgressBar) findViewById(R.id.progressBar);

        Rudder.setOnSeekBarChangeListener(this);
        Throttle.setOnSeekBarChangeListener(this);

        Rudder.setEnabled(false);
        Throttle.setEnabled(false);

    }

    public int TryConnect() throws InterruptedException {

        if (BTadapter == null)
        {
            Toast.makeText(this, "Bluetooth не поддерживается устройством", Toast.LENGTH_LONG).show();
            return 1;
        }

        //Intent enableBtIntent = new Intent(BTadapter.ACTION_REQUEST_ENABLE);

         BTadapter.enable();

           Thread.sleep(500, 0);

            if(!BTadapter.isEnabled())
            {
                Toast.makeText(this, "Bluetooth не включается", Toast.LENGTH_LONG).show();
                return 2;
            }

        device = BTadapter.getRemoteDevice(DeviceMAC);

        if(device == null)
        {
            Toast.makeText(this, "Удаленное устройство не подключено!", Toast.LENGTH_LONG).show();
            return 3;
        }

        try {
            btSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e)
        {
            Toast.makeText(this, "Socket не создан!", Toast.LENGTH_LONG).show();
            return 4;
        }

        try
        {
            btSocket.connect();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Socket не подключается!", Toast.LENGTH_LONG).show();
            return 4;
        }



        ConnectStatus.setText("Подключено");
        ConnectStatus.setTextColor(Color.GREEN);
        ConnectButton.setText("Отключить");
        Connection = true;
        Rudder.setEnabled(true);
        Throttle.setEnabled(true);
        return 0;


    }

    public int TryDisconnect()
    {

        BTadapter.disable();

        if(BTadapter.isEnabled())
        {
            Toast.makeText(this, "Невозможно отключить", Toast.LENGTH_LONG).show();
            return 1;
        }

        ConnectStatus.setText("Отключено");
        ConnectStatus.setTextColor(Color.RED);
        ConnectButton.setText("Подключить");
        Connection = false;
        Rudder.setEnabled(false);
        Throttle.setEnabled(false);
        return 0;
    }

    public void ConnectButtonHandler(View view)
    {
        try
        {
        Busy.setVisibility(View.VISIBLE);
        if(Connection)
        {
            TryDisconnect();
        }
        else
        {
            TryConnect();
        }
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Ошибка"+ex.toString(), Toast.LENGTH_LONG).show();
        }
        finally
        {
            Busy.setVisibility(View.INVISIBLE);
        }
    }

    public void ThrottleClick(View view)
    {

        try
        {
            int val = Throttle.getProgress();
            val |= 128;
            btSocket.getOutputStream().write(val);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Ошибка"+ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void RudderClick(View view)
    {
        try
        {
            int val = Rudder.getProgress();
            btSocket.getOutputStream().write(val);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Ошибка"+ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        /*
        if(Busy.getVisibility()==View.VISIBLE)
        {
            Busy.setVisibility(View.INVISIBLE);
        }
        else
        {
            Busy.setVisibility(View.VISIBLE);
        }
        */
        try
        {
            int val = Throttle.getProgress();
            val |= 128;
            btSocket.getOutputStream().write(val);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Ошибка"+ex.toString(), Toast.LENGTH_LONG).show();
        }

        try
        {
            int val = Rudder.getProgress();
            btSocket.getOutputStream().write(val);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Ошибка"+ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
