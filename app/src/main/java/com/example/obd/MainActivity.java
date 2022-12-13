package com.example.obd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;


import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.temperature.AmbientAirTemperatureCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.jignesh13.speedometer.SpeedoMeterView;
import com.sohrab.obd.reader.application.Preferences;
import com.sohrab.obd.reader.obdCommand.ObdConfiguration;
import com.sohrab.obd.reader.service.ObdReaderService;
import com.sohrab.obd.reader.trip.TripRecord;
import com.sohrab.obd.reader.utils.L;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

import pl.pawelkleczkowski.customgauge.CustomGauge;

import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_CONNECTION_STATUS_MSG;
import static com.sohrab.obd.reader.constants.DefineObdReader.ACTION_READ_OBD_REAL_TIME_DATA;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    Logger log = Logger.getLogger("general.main");

    private static final int BLUETOOTH_PERMISSION_CODE = 1;

    DataBaseHelper db = new DataBaseHelper(this);

    private TextView mObdInfoTextView;
    private TextView mRpmTextView;
    private TextView mSpeedTextView;
    private ImageView mWheelTextView;
    CardView cardPont, cardHist;

    String currenttrip, triptime, usuario;
    int speed = 0, speedf = 0, rpm, ace = 0, dec = 0, speedmax, turns, speedamount = 0, speedcount = 0;
    float time, tripdistance, drivingDuration;
    long tripStartTime, currentTime;

    private SensorManager sensorManager;
    private Sensor acelera;


    /* Inicializando os métodos pra trabalhar com o bluetooth*/
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothSocket socket = null;
    BluetoothSocket mBTSocket = null;
    ReadInput mReadThread = null;

    Button btnsensor;
    LinearLayout linearLayout;


    boolean ligado;

    SpeedoMeterView gaugespeed;
    CustomGauge gaugerpm;

    List<Trip> trips = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        TextClock textClock = (TextClock) findViewById(R.id.textClock);
        textClock.setFormat12Hour(null);
        //textClock.setFormat24Hour("dd/MM/yyyy hh:mm:ss a");
        textClock.setFormat24Hour("hh:mm:ss a  EEE MMM d");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mObdInfoTextView = findViewById(R.id.tv_obd_info);
        mRpmTextView = findViewById(R.id.txtrpm);
        mSpeedTextView = findViewById(R.id.speed);
        mWheelTextView = findViewById(R.id.wheel);
        cardPont = findViewById(R.id.cardPontuacao);
        cardHist = findViewById(R.id.cardHistorico);

        btnsensor = findViewById(R.id.btnsensor);

        gaugespeed = findViewById(R.id.gaugespeed);
        gaugerpm = findViewById(R.id.gaugerpm);

        linearLayout = findViewById(R.id.LayoutArroz);

        usuario = getIntent().getStringExtra("usuario");

        ligado = false;


        turns = 0;

        if (Build.VERSION.SDK_INT >= 31) {
            checkPermission(Manifest.permission.BLUETOOTH_CONNECT, BLUETOOTH_PERMISSION_CODE);
        } else {
            checkPermission(Manifest.permission.BLUETOOTH, BLUETOOTH_PERMISSION_CODE);
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        acelera = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        btnsensor.setOnClickListener(v -> {
            lersensor();
        });

        cardPont.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, Score.class);
            i.putExtra("usuario", usuario);
            startActivity(i);
        });
        cardHist.setOnClickListener(v -> {
            new ListTrip().execute(usuario);
        });

        /*Aqui ele lista os dispositivos pareados e que estão disponiveis*/
        try {
            List<String> deviceStrs = new ArrayList<String>();
            List<String> devices = new ArrayList<String>();

            /*Pega o nome e endereço de cada dispositivo*/
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    deviceStrs.add(device.getName() + "\n" + device.getAddress());
                    devices.add(device.getAddress());
                }
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
                    conecobd(deviceAddress);
                }
            });

            alertDialog.setTitle("Choose Bluetooth device");
            alertDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
    public void lersensor(){
        log.info("lendo sensor");

        try {
            List<String> deviceStrs = new ArrayList<String>();
            List<String> devices = new ArrayList<String>();

            try{
                Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        deviceStrs.add(device.getName() + "\n" + device.getAddress());
                        devices.add(device.getAddress());
                    }
                }else{
                    log.warning("No bluetooth devices available!!");
                    return;
                }
            }catch(SecurityException se){
                log.warning("Security exception");
            }


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
                    conecsensor(deviceAddress);
                }
            });

            alertDialog.setTitle("Choose Bluetooth device");
            alertDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    class ReadInput implements Runnable {

        private boolean bStop = false;
        private Thread t;

        public ReadInput() {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning() {
            return t.isAlive();
        }

        @Override
        public void run() {
            log.info("Running ReadInput runnable");
            InputStream inputStream;

            try {
                inputStream = mBTSocket.getInputStream();
                while (!bStop) {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0) {
                        inputStream.read(buffer);
                        int i = 0;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++) {
                        }
                        final String strInput = new String(buffer, 0, i);

                        /*
                         * If checked then receive text, better design would probably be to stop thread if unchecked and free resources, but this is a quick fix
                         */

                        float verify = Float.parseFloat(strInput);

                        if (verify < 5){
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    log.info("Mudando cor de fundo");
                                    linearLayout.setBackgroundColor(Color.RED);
                                    Toast.makeText(MainActivity.this, "Cuidado! " + "\n" + "Objeto próximo ao carro", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {

                                    log.info("Setting background color black");
                                    linearLayout.setBackgroundColor(Color.BLACK);
                                }
                            });

                        }

                    }
                    Thread.sleep(500);
                }
            } catch (IOException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop() {
            bStop = true;
        }

    }

    /*Nesse metodo ele conecta no arduino*/
    public void conecsensor(String adress){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(adress);

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            mBTSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            mBTSocket.connect();
            mReadThread = new ReadInput();
        } catch (IOException e) {
            e.printStackTrace();
        } catch(SecurityException se){
            log.warning("Security exception");
            se.printStackTrace();
        }
    }
    class ListTrip extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                String link = Connection.API + "listviagem.php";

                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8");

                URL url = new URL(link);

                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data);
                writer.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                return reader.readLine();

            }catch (Exception e){
                e.printStackTrace();
                return "Erro! " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try{

                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Trip cli = new Trip();
                        cli.setAcceleration(jsonObject.getInt("aces"));
                        cli.setSlowdown(jsonObject.getInt("decs"));
                        cli.setSpeedMax(jsonObject.getInt("speedmax"));
                        cli.setTurn(jsonObject.getInt("turns"));
                        cli.setTime(jsonObject.getString("time"));
                        cli.setUser(jsonObject.getString("email"));
                        trips.add(cli);
                        Log.d("VIAGENS: ", String.valueOf(trips.size()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                Intent i = new Intent(MainActivity.this, DisplayTrip.class);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST",(Serializable)trips);
                i.putExtra("BUNDLE",args);
                startActivity(i);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    class GetOBDParameters extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings){


            return "";
        }
    }



    public void conecobd(String deviceAddress){
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
                log.warning("Security exception");
                se.printStackTrace();
            }
        }
        /* Se o bluetooth estiver ligado, ele inicia um timer que funciona infinitamente

         */

        if (btAdapter.isEnabled()) {
            //tripStartTime = System.currentTimeMillis();
            Toast.makeText(this, "Viagem iniciada!", Toast.LENGTH_LONG).show();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        /******** Aqui são os metodos da biblioteca ********/
                        //Toast.makeText(getApplicationContext(), "Dispositivo OBD" + "\n" + "Puede proceder con obtener datos", Toast.LENGTH_LONG).show();
                        new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                        //Thread.sleep(400);
                        new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                        //Thread.sleep(400);
                        new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                        //Thread.sleep(400);
                        new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());
                        //Thread.sleep(400);
                    } catch (IOException | InterruptedException e) {
                        log.warning("Exception: " + e.toString());
                        //ligado = false;
                        Thread.currentThread().interrupt();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            log.info("OBD desconectado");
                            Toast.makeText(getApplicationContext(), "OBD desconectado!" + "\n" + "Conexão Encerrada!", Toast.LENGTH_SHORT).show();
                            //concluirViagem();
                            finish();
                        });


                    }


                    try {
                        if (socket == null){
                            timer.cancel();
                        }
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

                        if (speed > speedmax) {
                            speedmax = speed;
                        }
                        if ((speed - speedf > 4) && (speedf - speed < 0)) {
                            ace = ace + 1;
                        }
                        if ((speedf - speed > 4) && (speed - speedf < 0)) {
                            dec = dec + 1;
                        }

                        speedf = speed;


                        if (speed != 0){
                            speedamount = speedamount + speed;
                            speedcount = speedcount + 1;
                        }


                        //Thread.sleep(200);
                        //if (ace > 0) {

                        //}

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                log.info("Setting speed text!!");
                                mSpeedTextView.setText("321 Km/h");
                                //mSpeedTextView.setText(speedCommand.getCalculatedResult() + " Km/h");
                                mRpmTextView.setText(engineRpmCommand.getCalculatedResult() + " RPM");
                                //mObdInfoTextView.setText(String.valueOf(dec));
                                gaugespeed.setSpeed(speed,false);
                                gaugerpm.setValue(rpm);

                            }
                        });
                        //Thread.sleep(400);
                    } catch (IOException | InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
            }, 1000, 200);


        }

    }

    public void concluirViagem(){

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    log.info("Concluindo viagem");
                    Toast.makeText(MainActivity.this, "Viagem encerrada!", Toast.LENGTH_SHORT).show();
                    //btAdapter.disable();
                    finish();

                }
            });


        if (ace != 0 && dec != 0 && turns != 0 || speedmax != 0){
            Trip trip = new Trip();
            trip.setSpeedMax(speedmax);
            trip.setAcceleration(ace);
            trip.setSlowdown(dec);
            trip.setTurn(turns);
            trip.setUser(usuario);
            /*db.addTrip(trip);
            db.close();*/
            new InsertViagem().execute(String.valueOf(speedmax), String.valueOf(ace), String.valueOf(dec),
                    String.valueOf(turns), usuario);

        }
    }

    class InsertViagem extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                String link = Connection.API + "insertviagem.php";

                int speedapi = Integer.parseInt(strings[0]);
                int aceapi = Integer.parseInt(strings[1]);
                int decapi = Integer.parseInt(strings[2]);
                int turnapi = Integer.parseInt(strings[3]);
                String userapi = strings[4];

                String timeapi = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US).format(new java.util.Date());

                String data = URLEncoder.encode("speedmax", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(speedapi), "UTF-8");
                data += "&" + URLEncoder.encode("aces", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(aceapi), "UTF-8");
                data += "&" + URLEncoder.encode("decs", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(decapi), "UTF-8");
                data += "&" + URLEncoder.encode("turns", "UTF-8") + "=" + URLEncoder.encode(String.valueOf(turnapi), "UTF-8");
                data += "&" + URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(timeapi, "UTF-8");
                data += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(userapi, "UTF-8");

                URL url = new URL(link);

                URLConnection connection = url.openConnection();
                connection.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(data);
                writer.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                return reader.readLine();

            }catch (Exception e){
                e.printStackTrace();
                return "Erro! " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                JSONObject jsonObject = new JSONObject(s);

                if (jsonObject.getString("response").equals("success")){
                    String email = jsonObject.getString("user");
                    Toast.makeText(MainActivity.this, "Viagem Salva no usuario: " + email, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao salvar viagem!", Toast.LENGTH_SHORT).show();
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }



    /*public void conectarObd(){
        /*BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice deviceone = btAdapter.getRemoteDevice(obds);

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        BluetoothSocket socket = null;
        try {
            socket = deviceone.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "F demais", Toast.LENGTH_SHORT).show();
        }

        try {
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Deu ruim aqui oh", Toast.LENGTH_SHORT).show();

        }

        ObdConfiguration.setmObdCommands(this, null);


        // set gas price per litre so that gas cost can calculated. Default is 7 $/l
        float gasPrice = 7; // per litre, you should initialize according to your requirement.
        Preferences.get(this).setGasPrice(gasPrice);
        /*
         * Register receiver with some action related to OBD connection status

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_READ_OBD_REAL_TIME_DATA);
        intentFilter.addAction(ACTION_CONNECTION_STATUS_MSG);
        registerReceiver(mObdReaderReceiver, intentFilter);

        //start service which will execute in background for connecting and execute command until you stop
        startService(new Intent(this, ObdReaderService.class));
    }*/

    public void checkPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Bluetooth Permitido!", Toast.LENGTH_SHORT).show();
                //conectarObd();
            } else {
                Toast.makeText(this, "Bluetooth Negado!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*public final BroadcastReceiver mObdReaderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //findViewById(R.id.progress_bar).setVisibility(View.GONE);
            mObdInfoTextView.setVisibility(View.VISIBLE);
            String action = intent.getAction();

            if (action.equals(ACTION_CONNECTION_STATUS_MSG)) {

                String connectionStatusMsg = intent.getStringExtra(ObdReaderService.INTENT_EXTRA_DATA);
                mObdInfoTextView.setText(connectionStatusMsg);
                //Toast.makeText(MainActivity.this, connectionStatusMsg, Toast.LENGTH_SHORT).show();

                if (connectionStatusMsg.equals(getString(R.string.connected_ok))) {
                    Toast.makeText(MainActivity.this, "OBD2 conectado!", Toast.LENGTH_SHORT).show();
                    mObdInfoTextView.setVisibility(View.GONE);
                } else if (connectionStatusMsg.equals(getString(R.string.obd2_adapter_not_responding))) {
                    Toast.makeText(MainActivity.this, "OBD2 desconectado!", Toast.LENGTH_SHORT).show();
                    mObdInfoTextView.setVisibility(View.GONE);
                } else {
                    // here you could check OBD connection and pairing status
                }

            } else if (action.equals(ACTION_READ_OBD_REAL_TIME_DATA)) {

                TripRecord tripRecord = TripRecord.getTripRecode(MainActivity.this);
                //mObdInfoTextView.setText(tripRecord.toString());
                currenttrip = tripRecord.getmTripIdentifier();
                rpm = tripRecord.getEngineRpm();
                speed = tripRecord.getSpeed();

                mRpmTextView.setText(rpm);
                mSpeedTextView.setText(speed);

                throttlepos = Integer.parseInt(tripRecord.getmThrottlePos());
                if (throttlepos > throttleposmax) {
                    throttleposmax = throttlepos;
                }

                time = tripRecord.getDrivingDuration();
                Duration d = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    d = Duration.ofMinutes((long) time);
                }
                LocalTime hackUseOfClockAsDuration = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    hackUseOfClockAsDuration = LocalTime.MIN.plus(d);
                }
                String triptime = hackUseOfClockAsDuration.toString();
                tripdistance = tripRecord.getmDistanceTravel();
                speedmax = tripRecord.getSpeedMax();
                ace = tripRecord.getmRapidAccTimes();
                dec = tripRecord.getmRapidDeclTimes();

                Trip trip = new Trip();
                trip.setId(currenttrip.trim());
                trip.setAcceleration(ace);
                trip.setDistance(tripdistance);
                trip.setSlowdown(dec);
                trip.setSpeedMax(speedmax);
                trip.setThrottleposmax(throttleposmax);
                trip.setTime(triptime);
                trip.setTurn(turns);
                db.addTrip(trip);

            }

        }


    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister receiver
        unregisterReceiver(mObdReaderReceiver);
        //stop service
        stopService(new Intent(this, ObdReaderService.class));
        // This will stop background thread if any running immediately.
        Preferences.get(this).setServiceRunningStatus(false);
    }*/



    @Override
    public void onSensorChanged(SensorEvent event) {
        float ax = event.values[0];

        if (ax > 0.5 && ax < 5){
            mWheelTextView.setImageResource(R.drawable.ic_esquerda_verde);
        } else if (ax < -0.5 && ax > -5){
            mWheelTextView.setImageResource(R.drawable.ic_direita_verde);
        } else if (ax > 5){
            mWheelTextView.setImageResource(R.drawable.ic_esquerda_vermelho);
            turns++;
        } else if (ax < -5){
            mWheelTextView.setImageResource(R.drawable.ic_direita_vermelho);
            turns++;
        } else {
            mWheelTextView.setImageResource(R.drawable.ic_frente);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        if (mBTSocket == null || !btAdapter.isEnabled()) {
            lersensor();
        }
        super.onResume();
        sensorManager.registerListener(this, acelera, SensorManager.SENSOR_DELAY_UI);

        log.info("Resuming main activity");


//        Date currentTime = Calendar.getInstance().getTime();
//        mSpeedTextView.setText(currentTime.toString() + " Km/h");
        mSpeedTextView.setText("444 Km/h");
    }

    @Override
    protected void onPause() {
        if (mBTSocket != null && btAdapter.isEnabled()) {
            mReadThread.stop();
        }
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    protected void onStop() {
        Log.d("TALVEZ", "Stopped");
        super.onStop();
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        L.i("service onDestroy");
        //mNotificationManager.cancel(NOTIFICATION_ID);
        closeSocket();
        Preferences.get(getApplicationContext()).setServiceRunningStatus(false);
        Preferences.get(getApplicationContext()).setIsOBDconnected(false);
        TripRecord.getTripRecode(this).clear();
    }

    private void closeSocket() {
        //L.i("socket closed :: ");
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                L.i("socket closing failed :: ");
            }
        }
    }*/
}