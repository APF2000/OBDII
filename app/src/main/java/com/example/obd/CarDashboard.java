package com.example.obd;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.obd.databinding.ActivityCarDashboardBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CarDashboard extends AppCompatActivity {

    private ActivityCarDashboardBinding binding;

    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothSocket socket = null;
    BluetoothSocket mBTSocket = null;
    MainActivity.ReadInput mReadThread = null;

    String deviceAddress = null;
    private int speed;
    private int rpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCarDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle(getTitle());

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void listBluetoothDevicesAndConnectToOBD(View v) {
        listBluetoothDevices(v);

        createConnectionToOBDReader(v);
    }

    public void listBluetoothDevices(View v) {
        /*Aqui ele lista os dispositivos pareados e que estão disponiveis*/
        try {
            List<String> deviceStrs = new ArrayList<String>();
            List<String> devices = new ArrayList<String>();

            /*Pega o nome e endereço de cada dispositivo*/
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    deviceStrs.add(device.getName() + "\n" + device.getAddress());
                    devices.add(device.getAddress());
                }
            }else{
                String msg = "No bluetooth device detected";
                Toast.makeText(this, msg, Toast.LENGTH_LONG);
                Log.w("dashobard_warning", msg);
                return;
            }

            /*Constrói uma caixa pro usuário selecionar o obd*/
            // show list
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice,
                    deviceStrs.toArray(new String[deviceStrs.size()]));

            alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String deviceAddress = devices.get(position);
                    setDeviceAdress(deviceAddress);
                    //conecobd(deviceAddress);
                }
            });

            alertDialog.setTitle("Choose Bluetooth device");
            alertDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setDeviceAdress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void createConnectionToOBDReader(View v)
    {
        if(deviceAddress == null)
        {
            String msg = "No valid bluetooth address set";
            Toast.makeText(this, msg, Toast.LENGTH_LONG);
            Log.w("dashobard_warning", msg);
            return;
        }

        /*Aqui ele conecta no obd*/
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(deviceAddress);

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        if(socket == null) {
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SecurityException se) {
                Log.w("CarDashboard", "Security exception");
                se.printStackTrace();
            }
        }
    }

    public void retrieveDataFromOBDReader(View v)
    {
        if(socket == null)
        {
            String msg = "Socket connection not set";
            Toast.makeText(this, msg, Toast.LENGTH_LONG);
            Log.w("dashobard_warning", msg);
            return;
        }

        try {

            /** Mais metodos da biblioteca **/
            RPMCommand engineRpmCommand = new RPMCommand();
            //Thread.sleep(200);
            SpeedCommand speedCommand = new SpeedCommand();
            //speedmax = speed;
            //Thread.sleep(200);
            engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
            //Thread.sleep(200);
            speedCommand.run(socket.getInputStream(), socket.getOutputStream());
            speed = Integer.parseInt(speedCommand.getCalculatedResult());
            rpm = Integer.parseInt(engineRpmCommand.getCalculatedResult());

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Log.d("info", "Setting speed text!!");
//                    TextView mSpeedTextView = findViewById(R.);
//                    mSpeedTextView.setText("321 Km/h");
                    //mSpeedTextView.setText(speedCommand.getCalculatedResult() + " Km/h");
//                    mRpmTextView.setText(engineRpmCommand.getCalculatedResult() + " RPM");
//                    //mObdInfoTextView.setText(String.valueOf(dec));
//                    gaugespeed.setSpeed(speed,false);
//                    gaugerpm.setValue(rpm);

                }
            });
            //Thread.sleep(400);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public void onClickUpdateButton(View v)
    {
        Button b = (Button) v;
        b.setText("Teste 123");
        Log.d("alouuu", "alooou");

        TextView layoutBigText = findViewById(R.id.id_big_text);

        Date currentTime = Calendar.getInstance().getTime();
        layoutBigText.setText(currentTime.toString() + "\n" + deviceAddress);
    }
}