package com.example.obd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Score extends AppCompatActivity {

    TextView txtspeed;

    int color1 = 0, color2 = 0, color3 = 0, color4 = 0, color5 = 0;
    int des, ace, totalace, totaldes, totalspeed, totalturns, total;
    float taxaturn, taxaace, taxades, taxaspeed, totalscore;
    String resultace, resultdes, usuario;

    PieChart pieChartAce, pieChartDec, pieChartTurn, pieChartSpeed, pieChartTotal;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        pieChartAce = findViewById(R.id.chartace);
        pieChartDec = findViewById(R.id.chartdec);
        pieChartTurn = findViewById(R.id.chartturn);
        pieChartSpeed = findViewById(R.id.chartspeed);
        pieChartTotal = findViewById(R.id.charttotal);



        usuario = getIntent().getStringExtra("usuario");

        /*Trip tripPont = (Trip) getIntent().getSerializableExtra("Tripfinal");
        des = tripPont.getSlowdown();
        ace = tripPont.getAcceleration();
        dist = tripPont.getDistance();*/

        /*DataBaseHelper db = new DataBaseHelper(this);
        totaltrips = db.getTripCount();
        totalace = Integer.parseInt(db.getAceAmount());
        totaldes = Integer.parseInt(db.getSlowAmount());
        totalspeed = Integer.parseInt(db.getSpeedAmount());
        totalturns = Integer.parseInt(db.getTurnAmount());
        db.close();*/


        /*taxaace = (totalace / totaltrips) * 100;
        taxades = (totaldes / totaltrips) * 100;

        if(taxaace < 3){
            resultace = "Muito bom";
        } else if (taxaace > 3 && taxaace < 8){
            resultace = "Bom";
        } else if (taxaace > 8 && taxaace < 14){
            resultace = "Mediano";
        } else if (taxaace > 14 && taxaace < 25){
            resultace = "Ruim";
        } else if (taxaace > 25){
            resultace = "Muito ruim";
        }

        if(taxades < 3){
            resultdes = "Muito bom";
        } else if (taxades > 3 && taxades < 8){
            resultdes = "Bom";
        } else if (taxades > 8 && taxades < 14){
            resultdes = "Mediano";
        } else if (taxades > 14 && taxades < 25){
            resultdes = "Ruim";
        } else if (taxades > 25){
            resultdes = "Muito ruim";
        }

        taxaspeed = totalspeed / totaltrips;

        totalscore = ((4 * (taxaace + taxades)) + (4 * totalturns) + (2 * totalspeed)) / 10;

        txtace.setText(resultace);
        txtdes.setText(resultdes);
        txtspeed.setText(String.valueOf(taxaspeed));
        txtwheel.setText(String.valueOf(totalturns));
        txtscore.setText(String.valueOf(totalscore));*/

        new ScoreData().execute(usuario);

    }

    class ScoreData extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try{
                String link = Connection.API + "Scoreutils.php";

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
                JSONObject jsonObjectace = jsonArray.getJSONObject(0);
                String aceapi = jsonObjectace.getString("ace");
                JSONObject jsonObjectdec = jsonArray.getJSONObject(1);
                String decapi = jsonObjectdec.getString("decs");
                JSONObject jsonObjectturn = jsonArray.getJSONObject(2);
                String turnapi = jsonObjectturn.getString("turns");
                JSONObject jsonObjectspeed = jsonArray.getJSONObject(3);
                String speedapi = jsonObjectspeed.getString("speedmax");
                JSONObject jsonObjecttotal = jsonArray.getJSONObject(4);
                String totalapi = jsonObjecttotal.getString("total");

                ace = Integer.parseInt(aceapi);
                des = Integer.parseInt(decapi);
                totalturns = Integer.parseInt(turnapi);
                totalspeed = Integer.parseInt(speedapi);
                total = Integer.parseInt(totalapi);

                calcpont(ace, des, totalturns, totalspeed, total);


            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void calcpont(int ace, int des, int turns, int speed, int total){

        taxaace = ((float) ace / total);
        if (taxaace > 1){
            taxaace *= 10;
        } else {
            taxaace *= 100;
        }
        taxades = ((float) des / total);
        if (taxades > 1){
            taxades *= 10;
        } else {
            taxades *= 100;
        }

        taxaspeed = (float) speed / total;

        taxaturn = ((float) turns / total);
        if (taxaturn > 1){
            taxaturn *= 10;
        } else {
            taxaturn *= 100;
        }

        if (taxaace < 15){
            color1 = 0xFF70db70;
        } else if (taxaace > 15 && taxaace < 30){
            color1 = 0xFF5cd6d6;
        } else if (taxaace > 30 && taxaace < 50){
            color1 = 0xFFffff33;
        } else if (taxaace > 50){
            color1 = 0xFFe62e00;
        }

        if (taxades < 15){
            color2 = 0xFF70db70;
        } else if (taxades > 15 && taxades < 30){
            color2 = 0xFF5cd6d6;
        } else if (taxades > 30 && taxades < 50){
            color2 = 0xFFffff33;
        } else if (taxades > 50){
            color2 = 0xFFe62e00;
        }

        if (taxaturn < 10){
            color3 = 0xFF70db70;
        } else if (taxaturn > 10 && taxaturn < 20){
            color3 = 0xFF5cd6d6;
        } else if (taxaturn > 20 && taxaturn < 30){
            color3 = 0xFFffff33;
        } else if (taxaturn > 30){
            color3 = 0xFFe62e00;
        }

        if (taxaspeed < 40){
            color4 = 0xFF70db70;
        } else if (taxaspeed > 40 && taxaspeed < 60){
            color4 = 0xFF5cd6d6;
        } else if (taxaspeed > 60 && taxaspeed < 80){
            color4 = 0xFFffff33;
        } else if (taxaspeed > 80){
            color4 = 0xFFe62e00;
        }

        float acepi = 100 - taxaace;
        float decpi = 100 - taxades;
        float speedpi;
        if (taxaspeed > 100){
            speedpi = taxaspeed - 100;
        } else {
            speedpi = 100 - taxaspeed;
        }
        float turnpi = 100 - taxaturn;

        ArrayList<PieEntry> aces = new ArrayList<>();
        aces.add(new PieEntry(taxaace, ""));
        aces.add(new PieEntry(acepi, ""));

        PieDataSet pieDataSetace = new PieDataSet(aces, "Acelerações Bruscas");
        pieDataSetace.setColors(color1, Color.WHITE);
        pieDataSetace.setValueFormatter(new PercentFormatter());
        pieDataSetace.setValueTextColor(Color.BLACK);
        pieDataSetace.setValueTextSize(12f);

        PieData pieDataAce = new PieData(pieDataSetace);
        pieChartAce.setData(pieDataAce);
        pieChartAce.setCenterText("Acelerações Bruscas");
        pieChartAce.animateXY(1400, 1400);

        ArrayList<PieEntry> desc = new ArrayList<>();
        desc.add(new PieEntry(taxades, ""));
        desc.add(new PieEntry(decpi, ""));

        PieDataSet pieDataSetdesc = new PieDataSet(desc, "Freadas Bruscas");
        pieDataSetdesc.setColors(color2, Color.WHITE);
        pieDataSetdesc.setValueFormatter(new PercentFormatter());
        pieDataSetdesc.setValueTextColor(Color.BLACK);
        pieDataSetdesc.setValueTextSize(12f);

        PieData pieDataDesc = new PieData(pieDataSetdesc);
        pieChartDec.setData(pieDataDesc);
        pieChartDec.setCenterText("Freadas Bruscas");
        pieChartDec.animateXY(1400, 1400);

        ArrayList<PieEntry> turnss = new ArrayList<>();
        turnss.add(new PieEntry(taxaturn));
        turnss.add(new PieEntry(turnpi));

        PieDataSet pieDataSetturn = new PieDataSet(turnss, "Viradas Bruscas");
        pieDataSetturn.setColors(color3, Color.WHITE);
        pieDataSetturn.setValueFormatter(new PercentFormatter());
        pieDataSetturn.setValueTextColor(Color.BLACK);
        pieDataSetturn.setValueTextSize(12f);

        PieData pieDataTurn = new PieData(pieDataSetturn);
        pieChartTurn.setData(pieDataTurn);
        pieChartTurn.setCenterText("Viradas Bruscas");
        pieChartTurn.animateXY(1400, 1400);

        ArrayList<PieEntry> speeds = new ArrayList<>();
        speeds.add(new PieEntry(taxaspeed));
        speeds.add(new PieEntry(speedpi));

        PieDataSet pieDataSetspeed = new PieDataSet(speeds, "Velocidade máxima média");
        pieDataSetspeed.setColors(color4, Color.WHITE);
        pieDataSetspeed.setValueFormatter(new PercentFormatter());
        pieDataSetspeed.setValueTextColor(Color.BLACK);
        pieDataSetspeed.setValueTextSize(12f);

        PieData pieDataSpees = new PieData(pieDataSetspeed);
        pieChartSpeed.setData(pieDataSpees);
        pieChartSpeed.setCenterText("Velocidade máxima média");
        pieChartSpeed.animateXY(1400, 1400);


        totalscore = ((2 * taxaspeed) + (3 * (taxades + taxaace / 2)) + (5 * taxaturn)) / 10;

        if (totalscore > 90){
            color5 = 0xFF70db70;
        } else if (totalscore < 90 && totalscore > 75){
            color5 = 0xFF5cd6d6;
        } else if (totalscore < 75 && totalscore > 60){
            color5 = 0xFFffff33;
        } else if (totalscore < 60){
            color5 = 0xFFe62e00;
        }

        float scorewhite;
        if (totalscore < 100){
            scorewhite = 100 - totalscore;
        } else {
            scorewhite = totalscore - 100;
        }


        ArrayList<PieEntry> points = new ArrayList<>();
        points.add(new PieEntry(totalscore));
        points.add(new PieEntry(scorewhite));

        PieDataSet pieDataSettotal = new PieDataSet(points, "Pontuação");
        pieDataSettotal.setColors(color5, Color.WHITE);
        pieDataSettotal.setValueFormatter(new PercentFormatter());
        pieDataSettotal.setValueTextColor(Color.BLACK);
        pieDataSettotal.setValueTextSize(12f);

        PieData pieDataTotal = new PieData(pieDataSettotal);
        pieChartTotal.setData(pieDataTotal);
        pieChartTotal.setCenterText("Pontuação");
        pieChartTotal.animateXY(1400, 1400);



    }
}