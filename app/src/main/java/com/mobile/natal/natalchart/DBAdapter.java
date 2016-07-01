package com.mobile.natal.natalchart;

/**
 * Created by Administrator on 3/11/2016.
 */
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
public class DBAdapter {
//    static final String KEY_ROWID = "_id" ;
//    static final String KEY_NAME = "name" ;
//    static final String KEY_EMAIL = "email" ;
    static final String TAG = "DBAdapter" ;
    static final String DATABASE_NAME = "natal";
    static final String DATABASE_TABLE = "cities2";
    static final int DATABASE_VERSION = 3;
    static final String DATABASE_CREATE =
    "create table contacts (_id integer primary key autoincrement, "
            + "name text not null, email text not null);";
    final Context context;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;
    public DBAdapter(Context ctx)
    {
        this. context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(DATABASE_CREATE);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log. w(TAG, "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }
    //---opens the database---
    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getReadableDatabase();
        return this;
    }
    //---closes the database---
    public void close()
    {
        DBHelper.close();
    }
    //---insert a contact into the database---
//    public long insertContact(String name, String email)
//    {
//        ContentValues initialValues = new ContentValues();
//        initialValues.put(KEY_NAME, name);
//        initialValues.put(KEY_EMAIL, email);
//        return db.insert(DATABASE_TABLE, null, initialValues);
//    }
    //---deletes a particular contact---
//    public boolean deleteContact(long rowId)
//    {
//        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
//    }
    //---retrieves all the contacts---
    public Cursor getCities(String countryId, String date, String time)
    {
        String query ="SELECT A.id, A.latitude, A.longitude, B.stz AS tz, A.name, A.state FROM cities A INNER JOIN timezone B ON(A.country = B.country AND lower(A.maincity) = lower(B.city)) WHERE datetime('" +
                date +" " + time +"') BETWEEN datetime(B.stime) AND datetime(B.etime) AND A.country = '" +
                countryId +"' ORDER BY A.name, A.state";
        return db.rawQuery(query, null);
    }
    public Cursor getTimezone(String countryId, String date, String time)
    {
        String query ="SELECT city, stz FROM timezone WHERE datetime('" +
                date +" " + time +"') BETWEEN datetime(stime) AND datetime(etime) AND country = '" + countryId +"'";
        return db.rawQuery(query, null);
    }
    public Cursor getCity(String countryId, String city)
    {
        String query ="SELECT id, latitude, longitude, name, state FROM cities WHERE country='" + countryId +"' AND lower(maincity)=lower('" + city + "') ORDER BY name, state";
        return db.rawQuery(query, null);
    }
    public Cursor getAllCountries()
    {
        String query ="SELECT name, code FROM countries WHERE active = '1' ORDER BY name";
        return db.rawQuery(query, null);
    }
//    public Cursor getResults(String tableName, String[] columns, String selection,String[] selectionArgs, String groupBy,
//                             String having, String orderBy)
//    {
//        return db.query(tableName, columns,
//                 selection,selectionArgs,groupBy, having, orderBy);
//    }
//    //---retrieves a particular contact---
//    public Cursor getContact(long rowId) throws SQLException
//    {
//
//        Cursor mCursor =
//                db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
//                        KEY_NAME, KEY_EMAIL}, KEY_ROWID + "=" + rowId, null,
//            null, null, null, null);
//        if (mCursor != null) {
//            mCursor.moveToFirst();
//        }
//        return mCursor;
//    }
//    //---updates a contact---
//    public boolean updateContact(long rowId, String name, String email)
//    {
//        ContentValues args = new ContentValues();
//        args.put(KEY_NAME, name);
//        args.put(KEY_EMAIL, email);
//        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
//    }
}
