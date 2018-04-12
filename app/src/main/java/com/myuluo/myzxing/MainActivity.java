package com.myuluo.myzxing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button scanning;
    private Button qrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    // 初始化控件
    private void initView(){
        scanning = (Button) findViewById(R.id.scanning);
        qrCode = (Button) findViewById(R.id.qrCode);
        scanning.setOnClickListener(this);
        qrCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()){
            case R.id.scanning:// 扫描二维码
                intent = new Intent(this,ScanningActivity.class);
                break;
            case R.id.qrCode:// 生成二维码
                intent = new Intent(this,QrCodeActivity.class);
                break;
        }
        startActivity(intent);
    }
}
