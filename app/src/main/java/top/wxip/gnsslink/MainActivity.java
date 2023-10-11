package top.wxip.gnsslink;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.provider.ProviderProperties;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tvLog = null;

    private Context ctx = null;

    private ArrayList<String> logLines = new ArrayList<>();

    private void log(String text) {
        Log.i("gnss", text);
        if (null != tvLog && null != ctx) {
            logLines.add(text);
            if (logLines.size() > 5) {
                logLines.remove(0);
            }
            final String combine = String.join("\n", logLines);
            runOnUiThread(() -> tvLog.setText(combine));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = getApplicationContext();
        tvLog = findViewById(R.id.tv_log);

        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        final Button btnReceive = findViewById(R.id.btn_receive);
        final Button btnSend = findViewById(R.id.btn_send);
        final EditText etRemote = findViewById(R.id.et_remote);
        final int remotePort = 7007;
        final String providerStr = "gps";

        final SharedPreferences sharedPreferences = getSharedPreferences("gnsslink", MODE_PRIVATE);
        final String remoteAddr = sharedPreferences.getString("remoteAddr", "192.168.2.100");
        etRemote.setText(remoteAddr);

        log("系统初始化完毕");


        final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        btnReceive.setOnClickListener(v -> {
            // 设置mockgps
            try {
                locationManager.addTestProvider(providerStr, true, false, false, false, true, true, true, ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE);
            } catch (SecurityException e) {
                Toast.makeText(ctx, "请设置为模拟位置应用", Toast.LENGTH_SHORT).show();
                return;
            }
            locationManager.setTestProviderEnabled(providerStr, true);
            new Thread(() -> {
                log("开始接收数据");
                try (final DatagramSocket datagramSocket = new DatagramSocket(remotePort)) {
                    log("端口监听成功");
                    while (true) {
                        final byte[] buf = new byte[1024];
                        final DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                        try {
                            datagramSocket.receive(pkt);
                            final int dataLen = pkt.getLength();
                            final byte[] data = new byte[dataLen];
                            System.arraycopy(buf, 0, data, 0, dataLen);
                            log("收到数据:" + dataLen);
                            final GpsModel model = JSON.parseObject(data, GpsModel.class);
                            final Location location = model.toLoc();
                            log(JSON.toJSONString(location));
                            locationManager.setTestProviderLocation(providerStr, location);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (SocketException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        });

        btnSend.setOnClickListener(v -> {
            final String remote = etRemote.getText().toString();
            sharedPreferences.edit().putString("remoteAddr", remote).apply();

            log("开始获取定位数据");
            runOnUiThread(() -> {
                locationManager.requestLocationUpdates(providerStr, 1000, 0.0f, new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        log("位置更新 " + location.getLongitude() + " " + location.getLatitude());
                        new Thread(() -> {
                            try (final DatagramSocket datagramSocket = new DatagramSocket()) {
                                final InetAddress remoteAddr = InetAddress.getByName(remote);
                                final GpsModel model = new GpsModel();
                                model.fromLoc(location);
                                final String data = JSON.toJSONString(model);
                                final byte[] dataByte = data.getBytes();
                                log(data);
                                final DatagramPacket pkt = new DatagramPacket(dataByte, dataByte.length, remoteAddr, remotePort);
                                datagramSocket.send(pkt);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    }
                });
            });
        });

    }
}