package sid.cryptowatch;

/**
 * Created by siddhs on 28-12-2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SimpleDatabaseHelper {
    private static SQLiteOpenHelper _openHelper;
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "CryptoWatchSid2";
    private static final String TABLE_NAME_ALERTS = "activealerts";
    private static final String TABLE_NAME_PRICES = "currentprices";
    //private static final String KEY_CURRENCY = "currency";
    private static final String KEY_LAST_PRICE = "last_price";
    private static final String KEY_ID = "alert_id";
    private static final String KEY_USD = "check_usd";
    private static final String KEY_INR = "check_inr";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_CURRENCY = "currency";

    int mOpenCounter = 0;
    SQLiteDatabase mDatabase;
    SQLiteOpenHelper mDatabaseHelper;

    public SimpleDatabaseHelper(Context context) {
        _openHelper = new SimpleSQLiteOpenHelper(context);
        mDatabaseHelper = new SimpleSQLiteOpenHelper(context);
    }

    /* This is an internal class that handles the creation of all database tables */
    class SimpleSQLiteOpenHelper extends SQLiteOpenHelper {
        SimpleSQLiteOpenHelper(Context context) {
            super(context, DATABASE_NAME+".db", null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.beginTransaction();
            /*
            Stupid bloody Android isn't calling this function no matter what I do.
            Calling getWritableDatabase() doesn't work
            Changing version doesn't work
            Uninstalling doesn't work
            Makes me wonder how it's been working all this time (and when the f*$% it actually ever called this function)
            MIND = BLOWN. NOT.
             */
            String CREATE_TABLE_ALERTS = String.format("create table if not exists %s(%s text not null, %s text not null)",
                    TABLE_NAME_ALERTS, KEY_CURRENCY, KEY_AMOUNT);
            db.execSQL(CREATE_TABLE_ALERTS);
            String CREATE_TABLE_PRICES = String.format("create table if not exists %s(%s text not null, %s text not null)",
                    TABLE_NAME_PRICES, KEY_CURRENCY, KEY_LAST_PRICE);
            db.execSQL(CREATE_TABLE_PRICES);
            db.endTransaction();
            //db.close();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            this.onCreate(db);
        }
    }
    public synchronized SQLiteDatabase openDatabase() {
        mOpenCounter++;
        if(mOpenCounter == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        mOpenCounter--;
        if(mOpenCounter == 0) {
            // Closing database
            mDatabase.close();

        }
    }

    public synchronized void updateCurrent(String amt, String coin){
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        //SQLiteDatabase db = openDatabase();
        //db.beginTransaction();
        db.execSQL(String.format("delete from %s where %s='%s'", TABLE_NAME_PRICES, KEY_CURRENCY, coin));
        db.execSQL(String.format("insert into %s values('%s', '%s')", TABLE_NAME_PRICES, coin, amt));
        //db.endTransaction();
        //db.close();
    }
    public synchronized float getCurrent(String coin){

        Float lp;
        String lp_;

        //SQLiteDatabase db = _openHelper.getWritableDatabase();
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        //Cursor c1 = db.rawQuery("PRAGMA journal_mode=OFF", null); c1.close(); // DOES NOT WORK, NOTHING WORKS
        //db.beginTransaction();

        //The most ridiculous hack ever. Android won't call onCreate no matter what, so here:-
        String CREATE_TABLE_ALERTS = String.format("create table if not exists %s(%s text not null, %s text not null)",
                TABLE_NAME_ALERTS, KEY_CURRENCY, KEY_AMOUNT);
        db.execSQL(CREATE_TABLE_ALERTS);
        String CREATE_TABLE_PRICES = String.format("create table if not exists %s(%s text not null, %s text not null)",
                TABLE_NAME_PRICES, KEY_CURRENCY, KEY_LAST_PRICE);
        db.execSQL(CREATE_TABLE_PRICES);
        Cursor cursor = db.rawQuery(String.format("select distinct %s, %s from %s where %s='%s'", KEY_LAST_PRICE, KEY_CURRENCY, TABLE_NAME_PRICES, KEY_CURRENCY, coin), null);
        if (cursor.getCount() == 0) {
            updateCurrent("0", coin);
            return 0;
        }
        cursor.moveToFirst();
        lp_ = cursor.getString(cursor.getColumnIndex(KEY_LAST_PRICE));
        cursor.close();
        //db.endTransaction();
        //db.close();
        System.out.println(lp_);
        lp = Float.parseFloat(lp_);
        return lp;

    }
    public synchronized void addAlert(String amt, String currency){
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        //db.beginTransaction();
        String token;
        switch(currency){
            case "Bitcoin": token = "BTC"; break;
            case "Ripple": token = "XRP"; break;
            case "Ether": token = "ETH"; break;
            case "Bitcoin Cash": token = "BCH"; break;
            case "Litecoin": token = "LTC"; break;
            default: token = "BTC";
        }
        String sql = String.format("insert into %s values('%s', '%s')", TABLE_NAME_ALERTS, token, amt);
        db.execSQL(sql);
        //db.endTransaction();
        //db.close();
    }
    public synchronized void deleteAlert(String amt, String token){
        SQLiteDatabase db = _openHelper.getWritableDatabase();
        String amt_;
        try{
            amt_ = "" + Integer.parseInt(amt);
        }catch(Exception e){
            amt_ = "" + Float.parseFloat(amt); //convert back to int since that's how it's stored in db; not cool - should store as string but "float"
        }
        //db.beginTransaction();
        //db.execSQL(String.format("delete from %s where %s='%s' and %s='%s'", TABLE_NAME_ALERTS, KEY_AMOUNT, amt, KEY_CURRENCY, token));
        if(db.delete(TABLE_NAME_ALERTS, String.format("%s='%s' and %s='%s';", KEY_AMOUNT, amt_, KEY_CURRENCY, token), null) > 0){
            System.out.println("It's deleting stuff.");
        }else{
            System.out.println("It's NOT deleting stuff.");
        }
        //db.endTransaction();
        db.close();
    }
    public synchronized String[] getAllAlerts() {
        SQLiteDatabase db = _openHelper.getReadableDatabase();
        //db.beginTransaction();
        Cursor cursor = db.rawQuery(String.format("select distinct %s, %s from %s", KEY_AMOUNT, KEY_CURRENCY, TABLE_NAME_ALERTS), null);
        if (cursor.getCount() == 0) return new String[0];

        String[] alarms = new String[cursor.getCount()];

        // cursor = db.rawQuery("select distinct " + KEY_TIME + " from " + TABLE_NAME_ALARMS, null);
        cursor.moveToFirst();
        int i = 0;
        while (!cursor.isAfterLast()) {
            alarms[i++] = cursor.getString(cursor.getColumnIndex(KEY_CURRENCY)) + ":" + cursor.getString(cursor.getColumnIndex(KEY_AMOUNT));
            cursor.moveToNext();
        } /* will return "XRP:100" */
        cursor.close();
        //db.endTransaction();
        //db.close();
        return alarms;
    }
}