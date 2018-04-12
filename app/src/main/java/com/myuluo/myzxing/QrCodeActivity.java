package com.myuluo.myzxing;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.myuluo.zxing.CaptureActivity;
import com.myuluo.zxing.util.QRCodeUtil;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

/**
 * Created by Myuluo on 2016-12-30.
 */

public class QrCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText context;// 要生成二维码的信息
    private Button generate;// 生成二维码
    private ImageView imageView;// 显示生成的二维码
    private String filePath;// 保存所生成的二维码的存储路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qt_code);
        initView();
    }

    // 初始化控件
    private void initView() {
        context = (EditText) findViewById(R.id.context);
        generate = (Button) findViewById(R.id.generate);
        imageView = (ImageView) findViewById(R.id.imageView);
        generate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        AndPermission.with(QrCodeActivity.this)
                .requestCode(100)// 请求码
                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)// 要申请的权限
                .rationale(rationaleListener)// 用户拒绝权限后，再次发起申请时解释权限的作用
                .send();// 提交申请
    }

    /**
     * 第一次申请权限拒绝后，后续申请会先调用下面的提示
     */
    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            new AlertDialog.Builder(QrCodeActivity.this)
                    .setTitle("提示")
                    .setMessage("是否允许App使用存储功能来保存生成的二维码？")
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
            public void onSucceed(int requestCode) {// 生成二维码
                final String details = context.getText().toString();
                if (details.length() > 0) {
                    //二维码图片较大时，生成图片、保存文件的时间可能较长，因此放在新线程中
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            /**
                             * QRCodeUtil.createQRImage()：自定义工具类中，生成二维码的方法，该方法返回所生成的二维码的存储路径
                             *      参数一：二维码中记录的信息，一般来是url，或者文字内容
                             *      参数二：生成的二维码的宽度
                             *      参数三：生成的二维码的高度
                             *      参数四：当值为null时，默认不添加logo；不为空时，将传递的bitmap添加到二维码中
                             *      参数五：生成的二维码的保存路径。当值为null时，使用默认的路径
                             */
                            filePath = QRCodeUtil.createQRImage(details, 300, 300, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), null);
                            if (filePath != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 将图片路径所对应的图片展示在 ImageView 中
                                        imageView.setImageBitmap(BitmapFactory.decodeFile(filePath));
                                    }
                                });
                            }
                        }
                    }).start();
                } else {
                    Toast.makeText(QrCodeActivity.this, "请输入内容后再生成二维码！", Toast.LENGTH_SHORT).show();
                }
            }

            @Override// 申请权限失败
            public void onFailed(int requestCode) {
                Toast.makeText(QrCodeActivity.this, "二维码生成失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}