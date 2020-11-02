package com.example.myapplication_java;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;

//////////////////////////////


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ClusterManager<MyItem> clusterManager;
    private GoogleMap mMap;
    private GpsTracker gpsTracker;
    Context context = this;
    ArrayList<Shelter> shelters;
    //ArrayList<Shelter> shelters2;
    final String TAG = "LogMainActivity";

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        shelters = (ArrayList<Shelter>)getIntent().getSerializableExtra("shelter");//대피소 정보 받기
        //shelters2 = (ArrayList<Shelter>)getIntent().getSerializableExtra("shelter2");

        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        }else {

            checkRunTimePermission();
        }
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        clusterManager = new ClusterManager<>(this, mMap);

        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        gpsTracker = new GpsTracker(MainActivity.this);


        googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.d(TAG, "Load");
                double latitude = gpsTracker.getLatitude(); //Variable ' is accessed from within inner class, needs to be declared final
                double longitude = gpsTracker.getLongitude(); //익멸 클래스 외부에 있는 변수를 접근못함 이유는 모르지만 그래서 대신 안에 선언
                LatLng NowLocation = new LatLng(latitude, longitude);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(NowLocation);
                markerOptions.title("현재 위치");
                markerOptions.snippet("현재 있는 장소");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mMap.addMarker(markerOptions);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NowLocation,10));//현재 위치로 카메라 이동시키기

                for(int i = 0 ; i < shelters.size(); i++) {
                    MyItem clinicItem = new MyItem(shelters.get(i).getY(), shelters.get(i).getX(),
                            shelters.get(i).getName());
                    clusterManager.addItem(clinicItem);
                } // 대피소 개수만큼 마커 추가

            /*    for(int i = 0 ; i < shelters2.size(); i++) {
                    MyItem clinicItem = new MyItem(shelters2.get(i).getY(), shelters2.get(i).getX(),
                            shelters2.get(i).getName());
                    clusterManager.addItem(clinicItem);
                } // 대피소 개수만큼 마커 추가2*/

            }
        });

        clusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                LatLng latLng = new LatLng(cluster.getPosition().latitude, cluster.getPosition().longitude);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                mMap.moveCamera(cameraUpdate);
                return false;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                int marker_number=-1;

                for (int i = 0; i < shelters.size(); i++) {// marker title로 shelter를 검색하여 number 반환받아옴
                    if (shelters.get(i).findIndex(marker.getTitle()) != -1){
                        marker_number = shelters.get(i).findIndex(marker.getTitle()); //뽑아옴
                        Log.d(TAG, "marker_number " + marker_number);}
                }
                final int marker_ID_number = marker_number;
                Log.d(TAG, "marker number = " + String.valueOf(marker_ID_number));
                Log.d(TAG, "marker shelter name = " + shelters.get(marker_ID_number).getName());
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("지진 옥외 대피소 정보");
                builder.setMessage(
                        "이름 : " + shelters.get(marker_ID_number).getName() +
                                "\n주소 : " + shelters.get(marker_ID_number).getAddress() +
                                "\n시설 면적 : " + shelters.get(marker_ID_number).getArea() +
                        "\n관리 부서 : " + shelters.get(marker_ID_number).getPerson() +
                                "\n담당자 연락처 : " + shelters.get(marker_ID_number).getPhone()
                );
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setNegativeButton("전화걸기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + shelters.get(marker_ID_number).getPhone())));
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });// 마커 클릭 시 Alert Dialog가 나오도록 설정

    }
    ///



    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    ///
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    ///

    void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }
    ///
}
