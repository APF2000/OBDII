package com.example.obd;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Logger;

public class Login extends AppCompatActivity {

    Logger log = Logger.getLogger("general");

    EditText email, senha;
    Button logar, cad;
    DataBaseHelper db;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_login);
//
//        email = findViewById(R.id.loginemail);
//        senha = findViewById(R.id.loginsenha);
//
//        logar = findViewById(R.id.btnlogin);
//        cad = findViewById(R.id.btnvoltarcadastro);
//
//        db = new DataBaseHelper(this);
//
//        logar.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String user = email.getText().toString();
//                String pass = senha.getText().toString();
//
//                InputMethodManager inputManager = (InputMethodManager)
//                        getSystemService(Context.INPUT_METHOD_SERVICE);
//                if (inputManager != null) {
//                    inputManager.hideSoftInputFromWindow(view.getWindowToken(),
//                            InputMethodManager.HIDE_NOT_ALWAYS);
//                }
//
//                log.info("onclick");
//                // ignore login, for testing purposes
////                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(pass)){
////                    Toast.makeText(Login.this, "Preencha todos os campos!", Toast.LENGTH_LONG).show();
////                } else {
//                    new LoginData().execute(user, pass);
////                }
//            }
//        });
//
//        cad.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(Login.this, Register.class);
//                startActivity(i);
//            }
//        });

        setBluetoothEnable(true);
    }

    public void setBluetoothEnable(Boolean enable) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null){
            try {
                if (enable) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.enable();
                    }
                } else {
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                }
            }
            catch (SecurityException se)
            {
                log.warning("Security exception");
            }
        }
    }

    class LoginData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                String link = Connection.API + "login.php";

                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8");
                data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8");

                URL url = new URL(link);

                // temporarily remove, because there is no external server for now
//                URLConnection connection = url.openConnection();
//                connection.setDoOutput(true);

//                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
//                writer.write(data);
//                writer.flush();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                return "nothing";//reader.readLine();

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
                    Toast.makeText(Login.this, "Login Realizado!", Toast.LENGTH_SHORT).show();
                    user = jsonObject.getString("user");
                    Intent i = new Intent(Login.this, MainActivity.class);
                    i.putExtra("usuario", user);
                    startActivity(i);
                } else {
                    Toast.makeText(Login.this, "Email ou senha incorretos!", Toast.LENGTH_SHORT).show();
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}