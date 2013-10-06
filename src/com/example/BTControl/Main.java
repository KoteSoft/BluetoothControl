package com.example.BTControl;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
    TextView CalibrateHint;
    Button ConnectButton;
    Button CalibrateButton;
    ProgressBar Busy;
    UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    SharedPreferences sPref;

    int CalibrateStage;
    int Left;

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
        CalibrateHint = (TextView)findViewById(R.id.textView2);
        ConnectButton = (Button) findViewById(R.id.button);
        CalibrateButton = (Button)findViewById(R.id.button1);
        Busy = (ProgressBar) findViewById(R.id.progressBar);

        Rudder.setOnSeekBarChangeListener(this);
        Throttle.setOnSeekBarChangeListener(this);

        Rudder.setEnabled(false);
        Throttle.setEnabled(false);
        CalibrateButton.setEnabled(false);

        CalibrateStage = 0;
        sPref = getPreferences(MODE_PRIVATE);

        Rudder.setProgress((sPref.getInt("RightLimit", 63) - sPref.getInt("LeftLimit", 0)) / 2);
        Rudder.setMax(sPref.getInt("RightLimit", 63)-sPref.getInt("LeftLimit", 0));
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
        CalibrateButton.setEnabled(true);
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
        CalibrateButton.setEnabled(false);
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
            Toast.makeText(this, "Ошибка 10: "+ex.toString(), Toast.LENGTH_LONG).show();
        }
        finally
        {
            Busy.setVisibility(View.INVISIBLE);
        }
    }

    public void CalibrateButtonHandler(View view)
    {
        SharedPreferences.Editor sEdit = sPref.edit();

        switch (CalibrateStage)
        {
            case 0:
                Rudder.setMax(63);
                sEdit.putInt("LeftLimit", 0);
                sEdit.commit();
                CalibrateHint.setVisibility(View.VISIBLE);
                CalibrateHint.setText("Усановите крайнее левое положение и нажмите 'Далее'");
                CalibrateButton.setText("Далее");
                CalibrateStage = 1;
                break;
            case 1:
                Left = Rudder.getProgress();
                CalibrateHint.setText("Усановите крайнее правое положение и нажмите 'Далее'");
                CalibrateStage = 2;
                break;
            case 2:
                sEdit.putInt("LeftLimit", Left);
                sEdit.commit();
                sEdit.putInt("RightLimit", Rudder.getProgress());
                sEdit.commit();
                Rudder.setProgress((sPref.getInt("RightLimit", 63) - sPref.getInt("LeftLimit", 0)) / 2);
                Rudder.setMax(sPref.getInt("RightLimit", 63)-sPref.getInt("LeftLimit", 0));
                CalibrateHint.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Калибровка завершена", Toast.LENGTH_LONG).show();
                CalibrateButton.setText("Калибровка");
                CalibrateStage = 0;
                break;
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
            if(Connection)
                btSocket.getOutputStream().write(val);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Ошибка 11: "+ex.toString(), Toast.LENGTH_LONG).show();
        }

        try
        {
            int val = Rudder.getProgress()+sPref.getInt("LeftLimit", 0);
            //Log.v("My", Integer.toString(val));
            if(Connection)
                btSocket.getOutputStream().write(val);
        }
        catch (Exception ex)
        {
            Toast.makeText(this, "Ошибка 12: "+ex.toString(), Toast.LENGTH_LONG).show();
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
