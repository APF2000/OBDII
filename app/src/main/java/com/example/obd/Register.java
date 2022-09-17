package com.example.obd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class Register extends AppCompatActivity {

    EditText nome, email, senha, confsenha;
    Button cadastrar, voltar;
    DataBaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nome = findViewById(R.id.username);
        email = findViewById(R.id.useremail);
        senha = findViewById(R.id.userpassword);
        confsenha = findViewById(R.id.userconfirmpassword);

        db = new DataBaseHelper(this);

        cadastrar = findViewById(R.id.btncadastro);
        voltar = findViewById(R.id.btnvoltarlogin);

        cadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nome.getText().toString();
                String em = email.getText().toString().trim();
                String pass = senha.getText().toString();
                String confpass = confsenha.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(em) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(confpass)){
                    Toast.makeText(Register.this, "Preencha todos os campos!", Toast.LENGTH_LONG).show();
                } else {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
                        if (pass.equals(confpass)){

                            new InsertData().execute(name, em, pass);
                            //Boolean checkemail = db.checkEmail(em);
                            //if (!checkemail){
                                /*Boolean insertuser = db.insertUser(name, em, pass);
                                if (insertuser){
                                    Toast.makeText(Register.this, "O usuario " + name + " foi cadastrado!", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(Register.this, MainActivity.class);
                                    startActivity(i);
                                } else {
                                    Toast.makeText(Register.this, "Ops! Cadastro inválido", Toast.LENGTH_SHORT).show();
                                }
                            /*} else {
                                Toast.makeText(Register.this, "Ops! email já cadastrado!", Toast.LENGTH_SHORT).show();
                            }*/
                        } else {
                            Toast.makeText(Register.this, "Senhas diferentes!", Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(Register.this, "Digite um email válido!", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });

        voltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Register.this, Login.class);
                startActivity(i);
            }
        });
    }

    class InsertData extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                String link = Connection.API + "insert.php";

                String data = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(strings[0], "UTF-8");
                data += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(strings[1], "UTF-8");
                data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(strings[2], "UTF-8");

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
                    Toast.makeText(Register.this, "Cadastro Realizado!", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(Register.this, Login.class);
                    startActivity(i);
                } else {
                    Toast.makeText(Register.this, "Erro no cadastro!", Toast.LENGTH_SHORT).show();
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}