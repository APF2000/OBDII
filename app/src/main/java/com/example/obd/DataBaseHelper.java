package com.example.obd;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private Context context;

    public DataBaseHelper(@Nullable Context context) {
        super(context, "dbObd", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS tbl_user (useremail TEXT primary key, username TEXT, " +
                    "password TEXT)");
        }catch (Exception e){
            Toast.makeText(context, "Sem tabela", Toast.LENGTH_SHORT).show();
        }

        db.execSQL("CREATE TABLE IF NOT EXISTS tbl_trip (tripid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "accelaration INTEGER, slowdown INTEGER, speedmax INTEGER, " +
                "turn INTEGER, user TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tbl_trip");
        db.execSQL("DROP TABLE IF EXISTS tbl_user");
        onCreate(db);
    }

    public Boolean insertUser(String name, String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("useremail", email);
        cv.put("username", name);
        cv.put("password", password);

        long result = db.insert("tbl_user", null, cv);
        if(result == -1){
            return false;
        }else {
            return true;
        }
    }

    public Boolean checkEmail(String email){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from tbl_user where useremail=?", new String[] {email});

        if(cursor.getCount() > 0){
            return true;
        } else {
            return false;
        }
    }

    public Boolean checkLogin(String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from tbl_user where useremail=? and password=?", new String[] {email, password});

        if(cursor.getCount() > 0){
            return true;

        } else {
            return false;
        }
    }

    public void addTrip(Trip weather){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("accelaration", weather.getAcceleration());
        cv.put("slowdown", weather.getSlowdown());
        cv.put("speedmax", weather.getSpeedMax());
        cv.put("turn", weather.getTurn());
        cv.put("user", weather.getUser());
        long result = db.insert("tbl_trip", null, cv);
        if(result == -1){
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Ops! Algum erro ocorreu, tente novamente!", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "A viagem de" + weather.getUser() + " foi salva!", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public List<Trip> returnTrip(String email){
        List<Trip> weathers = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("tbl_trip", new String[]{"accelaration", "slowdown", "speedmax",
                        "turn", "user"}, "user =?", new String[] {email}, null, null, null );
        while(cursor.moveToNext()){
            Trip cli = new Trip();
            cli.setAcceleration(cursor.getInt(0));
            cli.setSlowdown(cursor.getInt(1));
            cli.setSpeedMax(cursor.getInt(2));
            cli.setTurn(cursor.getInt(3));
            cli.setUser(cursor.getString(4));
            weathers.add(cli);
        }
        return weathers;

    }

    public long getTripCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, "tbl_trip");
        db.close();
        return count;
    }

    public String getAceAmount() {
        SQLiteDatabase db = this.getReadableDatabase();

        String total;
        String mQuery = "select sum(accelaration) from tbl_trip";

        Cursor cursor = db.rawQuery(mQuery, null);

        if (cursor.moveToFirst()){
            total = String.valueOf(cursor.getInt(0));
        } else {
            total = "0";
        }

        cursor.close();
        db.close();

        return total;

    }

    public String getSlowAmount() {
        SQLiteDatabase db = this.getReadableDatabase();

        String total;
        String mQuery = "select sum(slowdown) from tbl_trip";

        Cursor cursor = db.rawQuery(mQuery, null);

        if (cursor.moveToFirst()){
            total = String.valueOf(cursor.getInt(0));
        } else {
            total = "0";
        }

        cursor.close();
        db.close();

        return total;

    }

    public String getSpeedAmount() {
        SQLiteDatabase db = this.getReadableDatabase();

        String total;
        String mQuery = "select sum(speedmax) from tbl_trip";

        Cursor cursor = db.rawQuery(mQuery, null);

        if (cursor.moveToFirst()){
            total = String.valueOf(cursor.getInt(0));
        } else {
            total = "0";
        }

        cursor.close();
        db.close();

        return total;

    }

    public String getTurnAmount() {
        SQLiteDatabase db = this.getReadableDatabase();

        String total;
        String mQuery = "select sum(turn) from tbl_trip";

        Cursor cursor = db.rawQuery(mQuery, null);

        if (cursor.moveToFirst()){
            total = String.valueOf(cursor.getInt(0));
        } else {
            total = "0";
        }

        cursor.close();
        db.close();

        return total;

    }
}
