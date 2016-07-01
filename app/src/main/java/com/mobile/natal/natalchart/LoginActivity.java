package com.mobile.natal.natalchart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.content.Context;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private ReadCitiesTask mReadCitiesTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mUserNameEdit;
    private EditText mBirthDateEdit;
    private EditText mBirthTimeEdit;
    private EditText mLongitudeEdit;
    private EditText mLatitudeEdit;
    private EditText mTimezoneEdit;
    private CheckBox mBirthTimeNotCheck;
    private View mProgressView;
    private View mLoginFormView;
    private Spinner mCountrySpinner;
    private Spinner mCitySpinner;
    private ArrayList<String> countrieIds = new ArrayList<>();
    private DBAdapter db;
    private ArrayList<cityStruct> mCities = new ArrayList<>();
    private String mew, mns;
    public static String mServerUrl ;
    public static final String PREFS_NAME = "LoginInfo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.title_new_chart));
        setContentView(R.layout.activity_login);
//        getUserInputForServerIP();
//        mServerUrl = getString(R.string.url_local_server);
        mServerUrl = getString(R.string.url_server);

        // Set up the login form.
        mUserNameEdit = (EditText)findViewById(R.id.userName);
        mBirthDateEdit = (EditText)findViewById(R.id.birthdate);
        mBirthTimeEdit = (EditText)findViewById(R.id.birthtime);
        mCountrySpinner = (Spinner) findViewById(R.id.spinner_country);
        mCitySpinner = (Spinner) findViewById(R.id.spinner_city);
        mLongitudeEdit = (EditText)findViewById(R.id.longitude);
        mLatitudeEdit = (EditText)findViewById(R.id.latitude);
        mTimezoneEdit = (EditText)findViewById(R.id.timezone);
        mUserNameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String strName = mUserNameEdit.getText().toString();
                    if (!strName.isEmpty())
                    {
                        String ff = strName.substring(0,1).toUpperCase();
                        mUserNameEdit.setText(ff + strName.substring(1));
                    }
                }
            }
        });
        /////////////////natal.db copy/////////////////////////////////////
        try {

            String destPath = "/data/data/" + getPackageName() +
            "/databases";
            File f = new File(destPath, "natal");
            if (!f.exists()) {
                File f1 = new File(destPath);
                f1.mkdirs();
                f1.createNewFile();
                //---copy the db from the assets folder into
                // the databases folder---
                db = new DBAdapter(this);
                CopyDB(getBaseContext().getAssets().open("natal"),
                        new FileOutputStream(destPath + "/natal"));
            }
            else
            {
                db = new DBAdapter(this);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        populateSpinnerCountry();

        mBirthTimeNotCheck = (CheckBox)findViewById(R.id.birthtimenot);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();
        mBirthTimeNotCheck.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                if (((CheckBox)v).isChecked()) mBirthTimeEdit.setText("12:00");
                else mBirthTimeEdit.setText("");
            }
        });



        Button mSubmitButton = (Button) findViewById(R.id.submit_button);
        mSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        Button mAdvancedButton = (Button) findViewById(R.id.advanced_button);
        mAdvancedButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btnAdvancedClick();
            }
        });

        mBirthDateEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DatePickerDialog datePicker = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd");
                            Date date = new Date(year, monthOfYear, dayOfMonth);
//                            String[] ymd = mBirthDateEdit.getText().toString().split("-");
//                            Date bDate = new Date(Integer.parseInt(ymd[0]), Integer.parseInt(ymd[1]), Integer.parseInt(ymd[2]));
                            Date sDate = new Date(1901, 11, 31);
                            Date eDate = new Date(2020, 0, 1);
                            if (date.before(sDate) || date.after(eDate)) {
                                mBirthDateEdit.setError(getString(R.string.error_birthdate));
                                View focusView = mBirthDateEdit;
                                focusView.requestFocus();
                                return;
                            }
                            String strDate = timeFormat.format(date);
                            String yy = String.format("%d-", year);
                            strDate = yy + strDate;
                            mBirthDateEdit.setText(strDate);
                        }
                    }, 1950, 0, 1);
                    datePicker.show();
                }
            }
        });

        mBirthDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (hasFocus) {
                DatePickerDialog datePicker = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd");
                        Date date = new Date(year, monthOfYear, dayOfMonth);
                        Date sDate = new Date(1901, 11, 31);
                        Date eDate = new Date(2020, 0, 1);
                        if (date.before(sDate) || date.after(eDate)) {
                            mBirthDateEdit.setError(getString(R.string.error_birthdate));
                            View focusView = mBirthDateEdit;
                            focusView.requestFocus();
                            return;
                        }
                        String strDate = timeFormat.format(date);
                        String yy = String.format("%d-", year);
                        strDate = yy + strDate;
                        mBirthDateEdit.setText(strDate);
                    }
                }, 1950, 0, 1);
                datePicker.show();
//                }
            }
        });
        mBirthTimeEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                TimePickerDialog timePicker = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        Date date = new Date(0, 0, 0, hourOfDay, minuteOfHour);
                        String strDate = timeFormat.format(date);
                        mBirthTimeEdit.setText(strDate);
                    }
                }, 0, 0, true);
                timePicker.show();
            }
            }
        });
        mBirthTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (hasFocus) {
                    TimePickerDialog timePicker = new TimePickerDialog(v.getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                            Date date = new Date(0, 0, 0, hourOfDay, minuteOfHour);
                            String strDate = timeFormat.format(date);
                            mBirthTimeEdit.setText(strDate);
                        }
                    }, 0, 0, true);
                    timePicker.show();
                }
//            }
        });
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }
    private void CopyDB(InputStream inputStream,
                       OutputStream outputStream) throws IOException {
        //---copy 1K bytes at a time---
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        inputStream.close();
        outputStream.close();
    }
    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        if (VERSION.SDK_INT >= 14) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, this);
        } else if (VERSION.SDK_INT >= 8) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }
    private void getUserInputForServerIP()
    {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String serverIP = settings.getString("ServerIP", getString(R.string.url_server_ip));
        mServerUrl = "http://" + serverIP + getString(R.string.url_server_path);
        //editor.commit();

        userInput.setText(serverIP);

        TextView msgTextView = (TextView) promptsView.findViewById(R.id.messageText);
        msgTextView.setText(getString(R.string.msg_input_server_ip));
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // get user input and set it to result
                                // edit text
                                mServerUrl = "http://" + userInput.getText() + getString(R.string.url_server_path);
                                SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("ServerIP", userInput.getText().toString());
                                editor.commit();

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }
    private void populateSpinnerCountry()
    {
//        XmlPullParser parser = getResources().getXml(R.xml.countries);
        //StringBuffer sb = new StringBuffer();
//        String text=null;
        ArrayList<String> countries = new ArrayList<>();
        countries.add("");
        try {
            db.open();
            Cursor c = db.getAllCountries();
//            ArrayList<String> strCities = new ArrayList<>();
//            strCities.add("");

            if (c.moveToFirst()) {
                countrieIds.clear();
                do {

                    countries.add(c.getString(0));
                    countrieIds.add(c.getString(1));

                } while (c.moveToNext());
            }
            db.close();
//            int event = parser.getEventType();
//            while (event != XmlPullParser.END_DOCUMENT) {
//                String name = parser.getName();
//                switch (event) {
//                    case XmlPullParser.START_TAG:
//                        break;
//
//                    case XmlPullParser.TEXT:
//                        text = parser.getText();
//                        break;
//
//                    case XmlPullParser.END_TAG:
//                        if (name.equals("name")) {
//                            countries.add(text);
//                        }
//                        if (name.equals("code")) {
//                            countrieIds.add(text);
//                        }
//                }
//                event = parser.next();


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, countries);

            mCountrySpinner.setAdapter(adapter);

            mCountrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    int index = arg0.getSelectedItemPosition();
                    if (index != 0)
                    {

                        String selectedCountry = (String) arg0.getSelectedItem();
                        String selectedId = countrieIds.get(index-1);
                        if (!mBirthDateEdit.getText().toString().isEmpty() && ! mBirthTimeEdit.getText().toString().isEmpty()) {
//                            showProgress(true);
//                            db.open();
//                            Cursor c = db.getCities(selectedId, mBirthDateEdit.getText().toString(), mBirthTimeEdit.getText().toString());
//                            ArrayList<String> strCities = new ArrayList<>();
//                            strCities.add("");
//
//                            if (c.moveToFirst()) {
//                                mCities.clear();
//                                do {
//                                    String cityName;
//                                    if (!c.getString(5).isEmpty())
//                                    {
//                                        cityName = c.getString(4) +"("+c.getString(5)+")";
//                                    }
//                                    else
//                                    {
//                                        cityName = c.getString(4);
//                                    }
//                                    strCities.add(cityName);
//                                    mCities.add(new cityStruct(cityName, c.getString(1), c.getString(2), c.getString(3)));
//
//                                } while (c.moveToNext());
//                            }
//                            db.close();
                            showProgress(true);
                            mReadCitiesTask = new ReadCitiesTask();
                            mReadCitiesTask.execute(selectedId);

//                            initCityCombo(readCities(selectedId));
                            initParameterEdit();



                        }
                        else
                        {
                            mCountrySpinner.setSelection(0);
                            if(mBirthDateEdit.getText().toString().isEmpty())
                            {
                                mBirthDateEdit.setError(getString(R.string.error_field_required));

                                View focusView = mBirthDateEdit;
                                focusView.requestFocus();
                            }
                            else
                            {
                                mBirthTimeEdit.setError(getString(R.string.error_field_required));

                                View focusView = mBirthTimeEdit;
                                focusView.requestFocus();
                            }
                        }

                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });

        }
        catch (Exception e) {
            Log.e("ReadXMLResourceFile", e.getMessage(), e);
        }

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private ArrayList<String> readCities(String selectedId)
    {
//        showProgress(true);
        db.open();
        ArrayList<String> strCities = new ArrayList<>();

//        Cursor c = db.getCities(selectedId, mBirthDateEdit.getText().toString(), mBirthTimeEdit.getText().toString());
//
//        if (c.moveToFirst()) {
//            mCities.clear();
//            strCities.add("");
//            do {
//                String cityName;
//                if (!c.getString(5).isEmpty())
//                {
//                    cityName = c.getString(4) +"("+c.getString(5)+")";
//                }
//                else
//                {
//                    cityName = c.getString(4);
//                }
//                strCities.add(cityName);
//                mCities.add(new cityStruct(cityName, c.getString(1), c.getString(2), c.getString(3)));
//
//            } while (c.moveToNext());
//        }
////////////////////////////////////////////////
        Cursor ct = db.getTimezone(selectedId, mBirthDateEdit.getText().toString(), mBirthTimeEdit.getText().toString());

        if (ct.moveToFirst()) {
            mCities.clear();
            strCities.add("");
            do {
                Cursor c = db.getCity(selectedId, ct.getString(0));
                if (c.moveToFirst()) {
                    do {
                        String cityName;
                        if (!c.getString(4).isEmpty()) {
                            cityName = c.getString(3) + "(" + c.getString(4) + ")";
                        } else {
                            cityName = c.getString(3);
                        }
                        strCities.add(cityName);
                        mCities.add(new cityStruct(cityName, c.getString(1), c.getString(2), ct.getString(1)));

                    } while (c.moveToNext());
                }
            }while (ct.moveToNext());
        }

        db.close();
//        showProgress(false);
        return strCities;
    }
    private void initParameterEdit()
    {
        mLongitudeEdit.setText("");
        mLatitudeEdit.setText("");
        mTimezoneEdit.setText("");
    }
    private  void initCityCombo(ArrayList<String> strCities)
    {
//        mCitySpinner();
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strCities);
        mCitySpinner.setAdapter(adapter1);
        mCitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0,
                                       View arg1, int arg2, long arg3) {
                int index1 = arg0.getSelectedItemPosition();
                if (index1 != 0) {
                    cityStruct selectedCity = mCities.get(index1 - 1);
                    mLatitudeEdit.setText(makeDegreeMMSS(selectedCity.getmLatitude(), true));
                    mLongitudeEdit.setText(makeDegreeMMSS(selectedCity.getmLongitude(), false));
                    mTimezoneEdit.setText(selectedCity.getmTz());
                } else {
                    initParameterEdit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }
    private String makeDegreeMMSS(String degree, boolean isLat)
    {
        try {
            Double deg = Double.parseDouble(degree);
            int int_deg = deg.intValue();
            String lat_deg = String.format("%d", Math.abs(int_deg));
            double min = (Math.abs(deg) - Math.abs(int_deg)) * 60;
//            String lat_min = String.format("%d", Math.round(min));
//            String mlat_min = String.format("%d", Math.round(min));
            String lat_min = String.format("%d", (int) min);
            String lat_sec = String.format("%d", (int)((min - ((int) min)) * 60));
            String ns;
            if (int_deg >0) {
                if (isLat)
                {
                    ns = "N";
                    mns = "1";
                }
                else
                {
                    ns = "E";
                    mew = "1";
                }
            }
            else
            {
                if (isLat)
                {
                    ns = "S";
                    mns = "-1";
                }
                else {
                    mew = "-1";
                    ns="W";
                }
            }
            return String.format("%s %s %s %s", lat_deg, lat_min,lat_sec,ns);
        }
        catch (Exception e) {
            return null;
        }
    }
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);

        // Store values at the time of the login attempt.
        String userName = mUserNameEdit.getText().toString();
        String birthDate = mBirthDateEdit.getText().toString();
        String birthTime = mBirthTimeEdit.getText().toString();
        String country = (String) mCountrySpinner.getSelectedItem();
//        String countryId = countrieIds.get(mCountrySpinner.getSelectedItemPosition());
        String city = (String) mCitySpinner.getSelectedItem();
//        String city = countrieIds.get(mCountrySpinner.getSelectedItemPosition());
        String longitude = mLongitudeEdit.getText().toString();
        String latitude = mLatitudeEdit.getText().toString();
        String timeZone = mTimezoneEdit.getText().toString();
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
//        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
//            mPasswordView.setError(getString(R.string.error_invalid_password));
//            focusView = mPasswordView;
//            cancel = true;
//        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(userName)) {
            mUserNameEdit.setError(getString(R.string.error_field_required));
            focusView = mUserNameEdit;
            cancel = true;
        }
        else if (TextUtils.isEmpty(birthDate)) {
            mBirthDateEdit.setError(getString(R.string.error_field_required));
            focusView = mBirthDateEdit;
            cancel = true;
        }
        else if (TextUtils.isEmpty(birthTime)) {
            mBirthTimeEdit.setError(getString(R.string.error_field_required));
            focusView = mBirthTimeEdit;
            cancel = true;
        } else if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, getString(R.string.country_err), Toast.LENGTH_LONG).show();
            focusView = mCountrySpinner;
            cancel = true;
        } else if (TextUtils.isEmpty(city)) {
            Toast.makeText(this, getString(R.string.city_err), Toast.LENGTH_LONG).show();
            focusView = mCitySpinner;
            cancel = true;
        } else if (TextUtils.isEmpty(longitude)) {
            mLongitudeEdit.setError(getString(R.string.error_field_required));
            focusView = mLongitudeEdit;
            cancel = true;
        } else if (TextUtils.isEmpty(latitude)) {
            mLatitudeEdit.setError(getString(R.string.error_field_required));
            focusView = mLatitudeEdit;
            cancel = true;
        } else if (TextUtils.isEmpty(timeZone)) {
            mTimezoneEdit.setError(getString(R.string.error_field_required));
            focusView = mTimezoneEdit;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        String birthCheck = "0";
        if (mBirthTimeNotCheck.isChecked()) birthCheck = "1";
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, userName, birthDate, birthTime, country, city, longitude, latitude, timeZone, birthCheck);
            mAuthTask.execute((Void) null);
        }
    }

    private void btnAdvancedClick()
    {
        Intent advancedIntent = new Intent(this, MainActivity.class);
        startActivity(advancedIntent);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setEnabled(show);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class cityStruct
    {
        private String mCityName;
        private String mLatitude;
        private String mLongitude;
        private String mTz;
        cityStruct(String cityName, String latitude, String longitude, String tz)
        {
            setmCityName(cityName);
            setmLatitude(latitude);
            setmLongitude(longitude);
            setmTz(tz);
        }

        public String getmCityName() {
            return mCityName;
        }

        public void setmCityName(String mCityName) {
            this.mCityName = mCityName;
        }

        public String getmLatitude() {
            return mLatitude;
        }

        public void setmLatitude(String mLatitude) {
            this.mLatitude = mLatitude;
        }

        public String getmLongitude() {
            return mLongitude;
        }

        public void setmLongitude(String mLongitude) {
            this.mLongitude = mLongitude;
        }

        public String getmTz() {
            return mTz;
        }

        public void setmTz(String mTz) {
            this.mTz = mTz;
        }
    }
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mUserName;
        private final String mBirthDate;
        private final String mBirthTime;
        private final String mCountry;
        private final String mCity;
        private final String mLongitude;
        private final String mLatitude;
        private final String mTimezone;
        private final String mBirthCheck;
        UserLoginTask(String email, String userName, String birthDate, String birthTime,
                      String country, String city, String longitude, String latitude, String timezone, String birthCheck ) {
            mEmail = email;
            mUserName = userName;
            mBirthDate = birthDate;
            mBirthTime = birthTime;
            mCountry = country;
            mCity = city;
            mLongitude = longitude;
            mLatitude = latitude;
            mTimezone = timezone;
            mBirthCheck = birthCheck;
        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String lat_mm = this.mLatitude.split(" ")[1];
            if(Integer.parseInt(this.mLatitude.split(" ")[2]) > 29) lat_mm = String.format("%d",Integer.parseInt(lat_mm)+1);
            String lon_mm = this.mLongitude.split(" ")[1];
            if(Integer.parseInt(this.mLongitude.split(" ")[2]) > 29) lon_mm = String.format("%d",Integer.parseInt(lon_mm)+1);

            StringBuffer psBuffer = new StringBuffer();
            psBuffer.append("page=natal&action=mobile&name=");
            psBuffer.append(this.mUserName);
            psBuffer.append("&fname=" + this.mUserName);
            psBuffer.append("&lname=" + this.mUserName);
            psBuffer.append("&year=" + this.mBirthDate.split("-")[0]);
            psBuffer.append("&month=" + this.mBirthDate.split("-")[1]);
            psBuffer.append("&day=" + this.mBirthDate.split("-")[2]);
            psBuffer.append("&hour=" + this.mBirthTime.split(":")[0]) ;
            psBuffer.append("&minute=" + this.mBirthTime.split(":")[1]);
            psBuffer.append("&check=" + this.mBirthCheck);
            psBuffer.append("&timezone=" + this.mTimezone);
            psBuffer.append("&timezone1=UTC" + this.mTimezone +"h");
            psBuffer.append("&long_deg=" + this.mLongitude.split(" ")[0]);
            psBuffer.append("&long_min=" + lon_mm);
            psBuffer.append("&ew=" + mew);
            psBuffer.append("&lat_deg=" + this.mLatitude.split(" ")[0]);
            psBuffer.append("&lat_min=" + lat_mm);
            psBuffer.append("&ns=" + mns);
            psBuffer.append("&country=" + this.mCountry);
            psBuffer.append("&counval=" + this.mCountry);
            psBuffer.append("&city=" + this.mCity);
            psBuffer.append("&cityval=" + this.mCity);
            psBuffer.append("&birthday=" + this.mBirthDate +" " + this.mBirthTime);
            psBuffer.append("&h_sys=P");
            String strResult = "";
            //showProgress(true);
            try {
                    strResult = NetworkUtilities.sendPost(getBaseContext(), mServerUrl, psBuffer.toString(), null, null, true);
//                    strResult = NetworkUtilities.sendPost(getBaseContext(), getString(R.string.url_server), psBuffer.toString(), null, null, true);
//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }
            } catch (Exception e) {

                Toast.makeText(getBaseContext(), getString(R.string.server_connect_failed), Toast.LENGTH_LONG).show();
                return null;
            }

            // TODO: register the new account here.
            return strResult;
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            showProgress(false);

            if (!result.isEmpty()) {

                try {

                    JSONObject jsonObject = new JSONObject(result);
                    String strresult = jsonObject.getString("message");
//                    result.contains("success")
                    if (strresult != null && strresult.equals("success")) {
                        Intent mapsIntent = new Intent(getBaseContext(), MainActivity.class);
                        mapsIntent.putExtra("result", result);
                        setResult(Activity.RESULT_OK, mapsIntent);
//                        startActivity(mapsIntent);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(), getString(R.string.server_failed), Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), getString(R.string.server_failed), Toast.LENGTH_LONG).show();
                }
            } else {
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
//                mPasswordView.requestFocus();
                Toast.makeText(getBaseContext(), getString(R.string.server_failed), Toast.LENGTH_LONG).show();

            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private class ReadCitiesTask extends AsyncTask<String, Void, ArrayList<String>> {

        public ReadCitiesTask() {
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            return readCities(params[0]);
        }


        protected void onPostExecute(ArrayList<String> result) {
            initCityCombo(result);
            showProgress(false);

        }
    }

}

