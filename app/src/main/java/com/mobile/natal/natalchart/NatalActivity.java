package com.mobile.natal.natalchart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

/**
 * Created by Administrator on 3/30/2016.
 */
public class NatalActivity extends AppCompatActivity {
    public static final int PICK_LOGIN_RESULT = 12;
    public static final int LOGIN_SUCCEED = 28;
    public static String SERVER_URL;
//    public static final boolean IS_LOCAL = true;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);

        //SERVER_URL = getString(R.string.url_local_server);
//        SERVER_URL = getString(R.string.url_server);
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginIntent, PICK_LOGIN_RESULT);
//        startActivity(loginIntent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the request went well (OK) and the request was PICK_LOGIN_RESULT
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_LOGIN_RESULT) {
            // Perform a query to the contact's content provider for the contact's name
            startActivity(data);
//            try {
//                int nLoginResult = data.getIntExtra(getString(R.string.login_result), 0);
//                if (nLoginResult == GUDCActivity.LOGIN_SUCCEED)
//                {
//                    Intent mapsIntent = new Intent(this, MapsActivity.class);
//                    String userName = data.getStringExtra(getString(R.string.user_name));
//                    String teamName = data.getStringExtra(getString(R.string.param_teamname));
//                    String taskLocation = data.getStringExtra(getString(R.string.param_tasklocation));
//                    mapsIntent.putExtra(getString(R.string.user_name), userName);
//                    mapsIntent.putExtra(getString(R.string.param_teamname), teamName);
//                    mapsIntent.putExtra(getString(R.string.param_tasklocation), taskLocation);
//
//                    startActivity(mapsIntent);
//                }
//                else
//                {
//                    Utilities.messageDialog(this, getString(R.string.msg_exit), null);
//                    finish();
//                }
//            } catch (Exception e) {
//                Utilities.errorDialog(this, e, null);
//                finish();
//            }
        }
    }

    public void login(View view) throws IOException {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivityForResult(loginIntent, PICK_LOGIN_RESULT);
//        startActivity(loginIntent);
    }
    public void close(View view) throws IOException {
        finish();
    }
}
