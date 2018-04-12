package com.myuluo.myzxing;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myuluo.zxing.CaptureActivity;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

/**
 * Created by Myuluo on 2016-12-30.
 */

public class ScanningActivity extends AppCompatActivity {

    private Button btn;
    private ImageView image;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sweep);
        initView();
    }

    // 初始化控件
    private void initView(){
        btn = (Button)findViewById(R.id.btn);
        image = (ImageView)findViewById(R.id.image);
        text = (TextView)findViewById(R.id.text);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AndPermission.with(ScanningActivity.this)
                        .requestCode(100)// 请求码
                        .permission(Manifest.permission.CAMERA)// 要申请的权限
                        .rationale(rationaleListener)// 用户拒绝权限后，再次发起申请时解释权限的作用
                        .send();// 提交申请
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 10){// 处理扫描的结果
            Bundle bundle = data.getExtras();
            //显示扫描到的内容
            text.setText(bundle.getString("result"));
            //显示扫描到的二维码图片
            image.setImageBitmap((Bitmap) data.getParcelableExtra("bitmap"));
        }else if(resultCode == 20){// 处理从相片选择二维码的结果
            // 显示扫描到的内容
            text.setText(data.getStringExtra("photo"));
        }
    }

    /**
     * 第一次申请权限拒绝后，后续申请会先调用下面的提示
     */
    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            new AlertDialog.Builder(ScanningActivity.this)
                    .setTitle("提示")
                    .setMessage("是否允许App启动相机来扫描二维码？")
                    .setPositiveButton("同意", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rationale.resume();
                        }
                    })
                    .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rationale.cancel();
                        }
                    }).show();
        }
    };

    /**
     * 申请权限授权的回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults, new PermissionListener() {
            @Override// 申请权限成功
            public void onSucceed(int requestCode) {// 跳转到扫码界面
                Intent intent = new Intent(ScanningActivity.this, CaptureActivity.class);
                startActivityForResult(intent,1);
            }

            @Override// 申请权限失败
            public void onFailed(int requestCode) {
                Toast.makeText(ScanningActivity.this, "相机功能打开失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
