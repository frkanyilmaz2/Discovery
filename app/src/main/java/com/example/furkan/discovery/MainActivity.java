package com.example.furkan.discovery;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;

@RequiresApi(api = Build.VERSION_CODES.O)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private TextView tv4;
    Database myDb;


    //geriye basınca null pointer çıkıyor. textviewin içini doldurunca geriye çıkarken kod boşalıyor
    /*
    Java’nın en popüler hatası olan java.lang.NullPointerException Hatası’nın çözümü aslında kolay.
    Yazılımızda class yapısı kullanıyorsanız ve o class’dan bir nesne türettiğinizi daha sonra sildiğinizi varsayalım.
    Bu işlemden sonra tekrar kullanma girişiminde bu hatayı alırsınız. Yapısına göre farklılık göstersede mantık budur.
    Türetilmeyen bir nesneyi kullanmak, türetildikten sonra null’layıp tekrar kullanmaya çalışmak bize bu hatayı verir.
    */


    private TextView mText;
    private Button mAdvertiseButton;
    private Button mDiscoverButton;
    private BluetoothLeAdvertiser advertiser;
    private BluetoothLeScanner mBluetoothLeScanner;
    private Handler mHandler = new Handler();
    private BluetoothAdapter mBluetoothAdapter;
    DatabaseReference databaseReference;

    Date currentTime = Calendar.getInstance().getTime();

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if( result == null
                    || result.getDevice() == null
                    || TextUtils.isEmpty(result.getDevice().getName()) )
                return;

            StringBuilder builder = new StringBuilder( result.getDevice().getName() );

            builder.append("\n").append(new String(result.getScanRecord().getServiceData(result.getScanRecord().getServiceUuids().get(0)), Charset.forName("UTF-8")));

            mText.setText(builder.toString());
            mText.setClickable(true);

            mText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"Connecting to Database", LENGTH_SHORT).show();
                    AddData();

                }
            });
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e( "BLE", "Discovery onScanFailed: " + errorCode );
            super.onScanFailed(errorCode);
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        databaseReference = FirebaseDatabase.getInstance().getReference().child("c106");
        //myDb = new Database();
        mText = (TextView) findViewById( R.id.text );
        mDiscoverButton = (Button) findViewById( R.id.discover_btn );

        mDiscoverButton.setOnClickListener( this );

        mText.setOnClickListener(this);

        tv1 = findViewById(R.id.textView1);
        tv2 = findViewById(R.id.textView2);
        tv3 = findViewById(R.id.textView3);
        tv4 = findViewById(R.id.textView4);

        String st1 = getIntent().getExtras().getString("name");
        String st2 = getIntent().getExtras().getString("surname");
        String st3 = getIntent().getExtras().getString("no");



        tv1.setText(st1);
        tv2.setText(st2);
        tv3.setText(st3);
        tv4.setText(BluetoothAdapter.getDefaultAdapter().getAddress());

        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();


        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "The permission to get BLE location data is required", LENGTH_SHORT).show();
            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else{
            Toast.makeText(this, "Location permissions already granted", LENGTH_SHORT).show();
        }
        if( !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() ) {
            Toast.makeText( this, "Multiple advertisement not supported", LENGTH_SHORT ).show();

            mDiscoverButton.setEnabled( false );
        }


    }


    @Override
    protected void onPause() {
        super.onPause();
        stopAdvertising();
    }



    private void discover() {
        List<ScanFilter> filters = new ArrayList<>();

        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid( new ParcelUuid(UUID.fromString( getString(R.string.ble_uuid ) ) ) )
                .build();
        filters.add( filter );

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }, 10000);
    }

    private void advertise() {
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_HIGH )
                .setConnectable(false)
                .build();

        ParcelUuid pUuid = new ParcelUuid( UUID.fromString( getString( R.string.ble_uuid ) ) );

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName( true )
                .addServiceUuid( pUuid )
                .addServiceData( pUuid, "Allah".getBytes(Charset.forName("UTF-8") ) )
                .build();

        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e( "BLE", "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        advertiser.startAdvertising( settings, data, advertisingCallback );
    }

    private void stopAdvertising() {
        if (advertiser == null) return;

        advertiser.stopAdvertising(advertisingCallback);
    }

    private AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d("advertise","advertising started"+ settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.d("advertise", "advertisingfailed "+errorCode);
        }
    };





    @Override
    public void onClick(View v) {
        if( v.getId() == R.id.discover_btn ) {
            discover();
        }

    }


    public void AddData(){

        String studentName = tv1.getText().toString();
        String studentSurname = tv2.getText().toString();
        String studentNo = tv3.getText().toString();
        String btAddress = tv4.getText().toString();


        if(!TextUtils.isEmpty(studentName)&&!TextUtils.isEmpty(studentSurname)&&!TextUtils.isEmpty(studentNo)) {
            Toast.makeText(this, "testing database insertion", LENGTH_SHORT).show();
            /*myDb.getBtAddress(btAddress);
            myDb.getStuName(studentName);
            myDb.getStuSurname(studentSurname);
            myDb.getStuNo(studentNo);
            myDb.getTime(currentTime);

            databaseReference.child("student1").setValue(myDb);
            */

            String id = databaseReference.push().getKey();
            Database students = new Database(btAddress,studentName,studentSurname,studentNo,currentTime);

            databaseReference.child(id).setValue(students);

            tv1.setText("");
            tv2.setText("");
            tv3.setText("");
            tv4.setText("");

            Toast.makeText(this,"insertion complete",LENGTH_SHORT).show();



        }else
            Toast.makeText(this,"Name or surname or no is wrong",LENGTH_SHORT).show();


    }

}