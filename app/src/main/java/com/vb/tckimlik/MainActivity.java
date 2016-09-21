package com.vb.tckimlik;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String NameSpace="http://tckimlik.nvi.gov.tr/WS";
    private static final String URL="https://tckimlik.nvi.gov.tr/Service/KPSPublic.asmx?WSDL";
    private static final String Soap_Action="http://tckimlik.nvi.gov.tr/WS/TCKimlikNoDogrula";
    private static final String Method_Name="TCKimlikNoDogrula";

    private ProgressDialog progressDialog ;
    private AlertDialog.Builder builder;
    private  EditText et_Ad,et_Soyad,et_yas,et_TC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bagla();
    }

    private void bagla() {
        et_TC= (EditText) findViewById(R.id.et_TC);
        et_Ad= (EditText) findViewById(R.id.et_AD);
        et_Soyad= (EditText) findViewById(R.id.et_Soyad);
        et_yas= (EditText) findViewById(R.id.et_YAS);
        progressDialog = new ProgressDialog(MainActivity.this);
        builder =new AlertDialog.Builder(MainActivity.this);
    }


    public void sorgula(View view) {
        final String tckn= et_TC.getText().toString();
        final String ad=et_Ad.getText().toString().toUpperCase(new Locale("tr_TR"));;
        final String soyad=et_Soyad.getText().toString().toUpperCase(new Locale("tr_TR"));;
        final String yas=et_yas.getText().toString();

        et_Ad.setText(ad);
        et_Soyad.setText(soyad);

        if(checkInternet()){
            new TckimlikNo().execute(tckn,ad,soyad,yas);
        }else Toast.makeText(MainActivity.this, "Hata net", Toast.LENGTH_SHORT).show();

    }



    private boolean checkInternet() {
        ConnectivityManager cm= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean result=false;
        if(cm.getActiveNetworkInfo()!=null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) result=true;
        else result=false;
        return result;
    }


    private class TckimlikNo extends AsyncTask<String,Void,Void> {
        private String durumText;
        private Boolean durum;

        AlertDialog alert;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Checking..");
            progressDialog.show();
        }



        @Override
        protected Void doInBackground(String... params) {
            SoapObject Request_ = new SoapObject(NameSpace,Method_Name);

            Request_.addProperty("TCKimlikNo",params[0]);
            Request_.addProperty("Ad",params[1]);
            Request_.addProperty("Soyad",params[2]);
            Request_.addProperty("DogumYili",params[3]);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet=true;
            envelope.encodingStyle = SoapEnvelope.ENC;
            envelope.setAddAdornments(false);
            envelope.implicitTypes=false;
            envelope.setOutputSoapObject(Request_);

            HttpTransportSE androidHttpTransport= new HttpTransportSE (URL);
            try {

                androidHttpTransport.call(Soap_Action,envelope);
                SoapObject response= (SoapObject) envelope.bodyIn;
                durum =Boolean.parseBoolean(response.getProperty(0).toString());
                if (durum) durumText ="OKEY";
                else durumText ="False";

            }catch(ClassCastException e){
                durum = false;
                durumText = "TC Kimlik Numarası Geçersiz";
            }
            catch(ConnectException e){
                durum = false;
                durumText = "İnternet bağlantınız kesilmiş ya da TCKN Servisi devre dışı kalmış olabilir.";
            }
            catch (UnknownHostException e) {
                durum = false;
                durumText = "İnternet bağlantınız kesilmiş ya da TCKN Servisi devre dışı kalmış olabilir.";
            }
            catch (UnknownServiceException e) {
                durum = false;
                durumText = "İnternet bağlantınız kesilmiş ya da TCKN Servisi devre dışı kalmış olabilir.";
            }
            catch(Exception e){
                durum = false;
                durumText = "İnternet bağlantınız kesilmiş ya da TCKN Servisi devre dışı kalmış olabilir.";
            }


            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
                progressDialog.dismiss();

            alert = builder.setMessage(durumText)
                    .setCancelable(true)
                    .setTitle("Sonuc")
                    .setPositiveButton("Tamamdır kanks", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).create();
            alert.show();
        }

    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (progressDialog!=null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }




}
