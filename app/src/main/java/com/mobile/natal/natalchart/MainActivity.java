package com.mobile.natal.natalchart;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.StackView;
import android.widget.TabHost;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TabHost mParentTab;
    private TableRow mTblRowMore;
    private String mUserName;
    private Button mBtnAspect;
    private Button mBtnChart;
    private Button mBtnProfile;
    private Button mBtnPurpose;
    private TabHost mProfileTab;
    private ScrollView mAspectsMoreScroll;
    private ToggleButton mBtnMore;
    static final int READ_BLOCK_SIZE= 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnAspect = (Button) findViewById(R.id.btnAspects);
        mBtnChart = (Button)findViewById(R.id.btnCharts);
        mBtnProfile = (Button)findViewById(R.id.btnProfile);
        mBtnPurpose = (Button)findViewById(R.id.btnPurpose);
        mAspectsMoreScroll = (ScrollView) findViewById(R.id.aspectsMoreScroll);
        mBtnMore = (ToggleButton) findViewById(R.id.btnMore);
        //////////////////////Parent tab init/////////////////////////////////
        mParentTab = (TabHost)findViewById(R.id.tabHostParent);
//        mTblRowMore = (TableRow)findViewById(R.id.tblRowMore);
        mParentTab.setup();
        TabHost.TabSpec spec = mParentTab.newTabSpec("tabCharts");
        spec.setIndicator("charts");
        spec.setContent(R.id.tabCharts);
        mParentTab.addTab(spec);
        mParentTab.addTab(mParentTab.newTabSpec("tabPurpose")
                .setIndicator("purpose")
                .setContent(R.id.tabPurpose));
        mParentTab.addTab(mParentTab.newTabSpec("tabAspects")
                .setIndicator("aspects")
                .setContent(R.id.tabAspects));
        mParentTab.addTab(mParentTab.newTabSpec("tabProfile")
                .setIndicator("profile")
                .setContent(R.id.tabProfile));


        ImageView chartImgView = (ImageView)findViewById(R.id.imageView);
        String result = getIntent().getStringExtra("result");
        try{
            JSONObject totalObject = new JSONObject(result);

            Typeface fontType = Typeface.createFromAsset(getAssets(),"fonts/Claregate Astrology 3.ttf");
            mUserName = totalObject.getString("name");
            setTitle(mUserName);

            JSONObject chartObject = new JSONObject(totalObject.getString("Chart"));
            String chartUrl = chartObject.getString("Url");
            String position = chartObject.getString("Position");
            JSONObject json_Positon = new JSONObject(position);
            int index_sun = Integer.parseInt(json_Positon.getString("Sun"));
            int index_moon = Integer.parseInt(json_Positon.getString("Moon"));
            int index_Raise = Integer.parseInt(json_Positon.getString("Raise"));

            new DownloadImageTask(chartImgView) .execute(chartUrl);
            chartImgView.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            JSONObject purposeObject = new JSONObject(totalObject.getString("Purpose"));
            String sign = purposeObject.getString("Rising");

            TextView txtSign = (TextView) findViewById(R.id.lblSignTab1);
            txtSign.setTypeface(fontType);
            txtSign.setText("Rising Sign " + sign);
//            TextView txtSign2 = (TextView) findViewById(R.id.txtSignTab2);
//            txtSign2.setTypeface(fontType);
//            txtSign2.setText(sign);

            String Symbols = purposeObject.getString("Symbols");
            TextView txtPurpose = (TextView) findViewById(R.id.txtPurpose);
            txtPurpose.setTypeface(fontType);
            txtPurpose.setText(sign + " ASC " + Symbols);

            TextView txtAspect = (TextView) findViewById(R.id.txtAspects);
            txtAspect.setTypeface(fontType);
            txtAspect.setText(Symbols);

            String Aspects = totalObject.getString("aspects");
            TextView txtPlainAspects = (TextView) findViewById(R.id.txtPlainAspects);
            txtPlainAspects.setTypeface(fontType);
            JSONArray aspectgsArray = new JSONArray(Aspects);
            for ( int i = 0; i < aspectgsArray.length(); i++) {
                txtPlainAspects.append(aspectgsArray.getString(i) +"\n" );
            }

            Resources myResources = getResources();
            InputStream myFile = myResources.openRawResource(R.raw.aspects);
            InputStreamReader tmp=new InputStreamReader(myFile);
            BufferedReader reader=new BufferedReader(tmp);
            String str;
            StringBuilder buf=new StringBuilder();
            while ((str = reader.readLine()) != null) {
                buf.append(str+"\n");
            }
            myFile.close();
            TextView txtAspectMore = (TextView)findViewById(R.id.txtAspectsMore);
            txtAspectMore.setText(buf.toString());

            //////////////////Profile Tab init////////////////////////////
            mProfileTab = (TabHost)findViewById(R.id.tabHostProfile);
            mProfileTab.setup();
            TabHost.TabSpec spec1 = mProfileTab.newTabSpec("tabSun");
            spec1.setIndicator("SUN");
            spec1.setContent(R.id.tabSun);
            mProfileTab.addTab(spec1);
            mProfileTab.addTab(mProfileTab.newTabSpec("tabMoon")
                    .setIndicator("MOON")
                    .setContent(R.id.tabMoon));
            mProfileTab.addTab(mProfileTab.newTabSpec("tabRising")
                    .setIndicator("RISING")
                    .setContent(R.id.tabRIsing));
            mProfileTab.setOnTabChangedListener( new TabHost.OnTabChangeListener(){
                @Override
                public void onTabChanged(String tabId) {
                    switch (tabId)
                    {
                        case "tabSun":
                            setTitle("SUN PROFILE");
                            break;
                        case "tabMoon":
                            setTitle("MOON PROFILE");
                            break;
                        case "tabRising":
                            setTitle("RISING PROFILE");
                            break;
                    }
                }
            });

            String [] template_Names = getResources().getStringArray(R.array.template_names);
            String[] template_Signs = getResources().getStringArray(R.array.template_sign);
            String[] lstSun = getResources().getStringArray(R.array.sun_templates);
            String[] lstMoon = getResources().getStringArray(R.array.moon_templates);
            String[] lstRising = getResources().getStringArray(R.array.rising_templates);

            TextView txtSunName = (TextView) findViewById(R.id.lblSunName);
            txtSunName.setText(template_Names[index_sun]);

            TextView txtSunSign = (TextView) findViewById(R.id.lblSunSign);
            txtSunSign.setTypeface(fontType);
            txtSunSign.setText(template_Signs[index_sun]);

            TextView txtSunTemp = (TextView) findViewById(R.id.txtSun);
            txtSunTemp.setText(lstSun[index_sun]+1);

            TextView txtMoonName = (TextView) findViewById(R.id.lblMoonName);
            txtMoonName.setText(template_Names[index_moon]);

            TextView txtMoonSign = (TextView) findViewById(R.id.lblMoonSign);
            txtMoonSign.setTypeface(fontType);
            txtMoonSign.setText(template_Signs[index_moon]);

            TextView txtMoonTemp = (TextView) findViewById(R.id.txtMoon);
            txtMoonTemp.setText(lstMoon[index_moon+1]);

            TextView txtRisingName = (TextView) findViewById(R.id.lblRisingName);
            txtRisingName.setText(template_Names[index_Raise]);

            TextView txtRisingSign = (TextView) findViewById(R.id.lblRisingSign);
            txtRisingSign.setTypeface(fontType);
            txtRisingSign.setText(template_Signs[index_Raise]);

            TextView txtRisingTemp = (TextView) findViewById(R.id.txtRising);
            txtRisingTemp.setText(lstRising[index_Raise+1]);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public void btnAspectsClick(View view)
    {
//        mTblRowMore.setVisibility(View.VISIBLE);
        mParentTab.setCurrentTab(2);
        setTitle("ASPECTS");
        mBtnAspect.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtnAspect.setTextColor(getResources().getColor(R.color.textColorPrimary));
        init_buttons(mBtnAspect);
//        getActionBar().setTitle(mUserName);

    }
    public void btnChartsClick(View view)
    {
//        mTblRowMore.setVisibility(View.GONE);
        mParentTab.setCurrentTab(0);
        mBtnChart.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtnChart.setTextColor(getResources().getColor(R.color.textColorPrimary));
        setTitle(mUserName);
        init_buttons(mBtnChart);
    }
    public void btnProfileClick(View view)
    {
//       mTblRowMore.setVisibility(View.GONE);
        mParentTab.setCurrentTab(3);
        mBtnProfile.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtnProfile.setTextColor(getResources().getColor(R.color.textColorPrimary));
        init_buttons(mBtnProfile);
        setTitle("SUN PROFILE");
    }
    public void btnPurposeClick(View view)
    {
//        mTblRowMore.setVisibility(View.GONE);
        mParentTab.setCurrentTab(1);
        mBtnPurpose.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        mBtnPurpose.setTextColor(getResources().getColor(R.color.textColorPrimary));
        setTitle("SOUL'S PURPOSE");
        init_buttons(mBtnPurpose);
    }
    public void btnMoreClick(View view)
    {
        if (mBtnMore.isChecked())
        {
            mAspectsMoreScroll.setVisibility(View.VISIBLE);
        }
        else
        {
            mAspectsMoreScroll.setVisibility(View.GONE);
        }
    }

    private void init_buttons(Button bt)
    {

            if(bt != mBtnChart)
            {
//                mBtnChart.setChecked(false);
                mBtnChart.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mBtnChart.setTextColor(getResources().getColor(R.color.colorAccent));
                mBtnChart.setBackgroundResource(R.drawable.button_border);
            }
            if(bt != mBtnAspect ){
//                mBtnAspect.setChecked(false);
                mBtnAspect.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mBtnAspect.setTextColor(getResources().getColor(R.color.colorAccent));
                mBtnAspect.setBackgroundResource(R.drawable.button_border);
            }
            if(bt != mBtnProfile) {
//                mBtnProfile.setChecked(false);
                mBtnProfile.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mBtnProfile.setTextColor(getResources().getColor(R.color.colorAccent));
                mBtnProfile.setBackgroundResource((R.drawable.button_border));
            }
            if(bt != mBtnPurpose ) {
//                mBtnPurpose.setChecked(false);
                mBtnPurpose.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mBtnPurpose.setTextColor(getResources().getColor(R.color.colorAccent));
                mBtnPurpose.setBackgroundResource((R.drawable.button_border));
            }
//            avoidRecursions = false;
//        }
    };
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
