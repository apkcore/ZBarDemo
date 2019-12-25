package com.apkcore.zbardemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.apkcore.zbardemo.util.ZXingUtil;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

/**
 * 使用zbar扫描，zxing生成二维码以及解码本地二维码
 * ZBar参考
 * 参考1：ZBar打包等 ，http://blog.csdn.net/skillcollege/article/details/38855023
 * 参考2：二维码本地用zbar读取（http://blog.csdn.net/b2259909/article/details/43273231），推荐用ZXing省事，因为生成二维码也用的它
 */
public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
//        tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(stringFromJNI());
        findViewById(R.id.bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CaptureActivity.class));
            }
        });
        ImageView img = (ImageView) findViewById(R.id.img);
        final Bitmap bitmap = ZXingUtil.createQRCode("|sdf奇趣叕 粤语", 500);
        img.setImageBitmap(bitmap);
        img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String text = getStr(bitmap);
                tv.setText(text);
                return true;
            }
        });
    }

    private String getStr(Bitmap bitmap) {
        // 在这里我用到的 getSmallerBitmap 非常重要，下面就要说到
        bitmap = getSmallerBitmap(bitmap);

        // 获取bitmap的宽高，像素矩阵
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        // 最新的库中，RGBLuminanceSource 的构造器参数不只是bitmap了
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        Result result = null;

        // 尝试解析此bitmap，！！注意！！ 这个部分一定写到外层的try之中，因为只有在bitmap获取到之后才能解析。写外部可能会有异步的问题。（开始解析时bitmap为空）
        try {
            result = reader.decode(binaryBitmap);
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 防止图片过大OOM
     */
    public static Bitmap getSmallerBitmap(Bitmap bitmap) {
        int size = bitmap.getWidth() * bitmap.getHeight() / 160000;
        if (size <= 1) return bitmap; // 如果小于
        else {
            Matrix matrix = new Matrix();
            matrix.postScale((float) (1 / Math.sqrt(size)), (float) (1 / Math.sqrt(size)));
            Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return resizeBitmap;
        }
    }
//
//    /**
//     * A native method that is implemented by the 'native-lib' native library,
//     * which is packaged with this application.
//     */
//    public native String stringFromJNI();
}
