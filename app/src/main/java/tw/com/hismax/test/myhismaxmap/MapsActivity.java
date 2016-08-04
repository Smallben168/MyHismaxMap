package tw.com.hismax.test.myhismaxmap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // 寫入外部儲存設備授權請求代碼
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 100;
    // 錄音設備授權請求代碼
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 101;
    // 定位設備授權請求代碼
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;

    private GoogleMap mMap;
    // Google API用戶端物件
    private GoogleApiClient googleApiClient;
    // Location請求物件
    private LocationRequest locationRequest;
    // 記錄目前最新的位置
    private Location currentLocation;
    private LatLng currentLatLng;

    // 記錄 zoom 數值
    private float mapZoom = 15.0F;

    // 顯示目前與儲存位置的標記物件
    private Marker currentMarker;

    
    //Ben : Float Menu
    FloatingActionMenu materialDesignFAM;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;

    //元件宣告
    private Switch swRealPosit;
    private ImageButton imgbut_change_map_Type;


    //變數宣告
    private Boolean mRealPosit = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initProc();

        //Ben: 建立Google API用戶端物件
        configGoogleApiClient();

        // 建立Location請求物件
        configLocationRequest();

    }

    private void initProc(){
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //=======- Map ========
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        
        //====== 建立Menu =========
        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);

        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);
        floatingActionButton1.setOnClickListener(new ClickMenuButton());
        floatingActionButton2.setOnClickListener(new ClickMenuButton());
        floatingActionButton3.setOnClickListener(new ClickMenuButton());

        //Ben ------- 元件宣告
        swRealPosit = (Switch) findViewById(R.id.switch_real_posit);
        swRealPosit.setChecked(mRealPosit);
        swRealPosit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    mRealPosit = true;
                } else {
                    mRealPosit = false;
                }
            }
        });
        imgbut_change_map_Type = (ImageButton) findViewById(R.id.imgbut_change_map_Type);
        imgbut_change_map_Type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mMap.getMapType()) {
                    case GoogleMap.MAP_TYPE_NORMAL:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case GoogleMap.MAP_TYPE_HYBRID:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case GoogleMap.MAP_TYPE_SATELLITE:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case GoogleMap.MAP_TYPE_TERRAIN:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    default:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }

            }
        });



    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        LatLng place = new LatLng(25.033408, 121.564099);       //台北 101
        //Ben: 移動地圖
        moveMap(place);

        //Ben: 加入地圖標記
        //addMarker(place, "台北 101", "你在這裡");


        // 連線到Google API用戶端
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }

        //Ben : 在onMapReady(GoogleMap googleMap)中 Add Event 加入 mouse click
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addMarker(latLng, "Title", "is here !!");
            }
        });

        //Ben :在onMapReady(GoogleMap googleMap)中 加入 zoom 之偵測
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener(){

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mapZoom = cameraPosition.zoom;
            }
        });

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    //Ben: 移動地圖到參數指定的位置
    private void moveMap(LatLng place) {
        // 建立地圖攝影機的位置物件
        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(place)
                        .zoom(mapZoom)
                        .build();
        // 使用動畫的效果移動地圖
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //Ben: 在地圖加入指定位置與標題的標記
    private void addMarker(LatLng place, String title, String context) {
        //R.mipmap.ic_launcher
        BitmapDescriptor icon =
                BitmapDescriptorFactory.fromResource(R.mipmap.ic_person_pin_black_48dp);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place)
                .title(title)
                .snippet(context)
                .icon(icon);
        mMap.addMarker(markerOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 連線到Google API用戶端
        if (!googleApiClient.isConnected() && currentMarker != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 移除位置請求服務
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 移除位置請求服務
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Ben : 連線至 Google Services
        // 啟動位置更新服務
        // 位置資訊更新的時候，應用程式會自動呼叫LocationListener.onLocationChanged
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Ben: 檢查定位權限 -> 己搬至前面
            requestLocationPermission();
        }
        //Ben : 未授權前執行此行會當掉
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, MapsActivity.this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION) {

        }
        else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {

        }
        // 如果是定位設備授權請求
        else if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            // 如果在授權請求選擇「允許」
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 啟動地圖與定位元件
                //processLocation();

            }
            // 如果在授權請求選擇「拒絕」
            else {
                // 顯示沒有授權的訊息
                Toast.makeText(this, R.string.write_external_storage_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Google Services連線中斷
        // int參數是連線中斷的代號
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Google Services連線失敗
        // ConnectionResult參數是連線失敗的資訊

        int errorCode = connectionResult.getErrorCode();
        // 裝置沒有安裝Google Play服務
        if (errorCode == ConnectionResult.SERVICE_MISSING) {
            Toast.makeText(this, R.string.google_play_service_missing,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // 位置改變
        // Location參數是目前的位置
        currentLocation = location;
        currentLatLng = new LatLng(
                location.getLatitude(), location.getLongitude());
        // 設定目前位置的標記
        if (currentMarker == null) {
            currentMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng));
        }
        else {
            currentMarker.setPosition(currentLatLng);
        }

        if (mRealPosit)
            // 移動地圖到目前的位置
            moveMap(currentLatLng);
    }

    private synchronized void configGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    // 建立Location請求物件
    private void configLocationRequest() {
        locationRequest = new LocationRequest();
        // 設定讀取位置資訊的間隔時間為一秒（1000ms）
        locationRequest.setInterval(1000);
        // 設定讀取位置資訊最快的間隔時間為一秒（1000ms）
        locationRequest.setFastestInterval(1000);
        // 設定優先讀取高精確度的位置資訊（GPS）
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Ben: 讀取與處理定位設備授權請求
    private void requestLocationPermission() {

        // 如果裝置版本是6.0（包含）以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 取得授權狀態，參數是請求授權的名稱
            int hasPermission = checkSelfPermission(
                    android.Manifest.permission.ACCESS_FINE_LOCATION);

            // 如果未授權
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                // 請求授權
                //     第一個參數是請求授權的名稱
                //     第二個參數是請求代碼
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION_PERMISSION);
                return;
            }
        }

        // 如果裝置版本是6.0以下，
        // 或是裝置版本是6.0（包含）以上，使用者已經授權，
        // 啟動地圖與定位元件
        //processLocation();
        return;
    }

    class ClickMenuButton implements View.OnClickListener {
        @Override
        public void onClick(View view) {

            //Toast.makeText(Menu.this, "onClick-"+view.getId(), Toast.LENGTH_SHORT).show();
            switch (view.getId()) {
                case R.id.material_design_floating_action_menu_item1: {
                    Toast.makeText(MapsActivity.this, R.string.menu_send_msg, Toast.LENGTH_SHORT).show();
                    //Intent it = new Intent();
                    //it.setClass(Menu.this, Logout.class);
                    //startActivity(it);
                    //Menu.this.finish();
                    break;
                }
                case R.id.material_design_floating_action_menu_item2: {
                    Toast.makeText(MapsActivity.this, R.string.menu_find_address, Toast.LENGTH_SHORT).show();
                    //Intent it = new Intent();
                    //it.setClass(Menu.this, Web_list.class);
                    //startActivity(it);
                    //Menu.this.finish();
                    break;

                }
                case R.id.material_design_floating_action_menu_item3: {
                    Toast.makeText(MapsActivity.this, R.string.menu_current_posit, Toast.LENGTH_SHORT).show();
                    moveMap(MapsActivity.this.currentLatLng);
                    break;

                }

            }
        }
    }        // class ClickMenuButton end
}
