package com.example.tesseractocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.biometrics.BiometricManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    ImageView imageView; // 이미지 뷰
    TextView OCRTextView; // OCR 결과뷰뷰
    private TessBaseAPI mTess; //Tess API reference
    Button btnImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tesseract 엔진 초기화
        mTess = new TessBaseAPI();
        String language = "eng"; // traineddata 파일의 언어 코드 (예: 영어 - eng, 한국어 - kor 등)
        String tessDataPath = getFilesDir() + "";

        copyTrainedDataFile();

        mTess.init(tessDataPath, language);

        imageView = findViewById(R.id.imageView);
        OCRTextView = findViewById(R.id.OCRTextView);

        btnImage = findViewById(R.id.btn_get_image);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
    }

    public void copyTrainedDataFile() {
        try {
            // traineddata 파일의 InputStream 가져오기
            InputStream inputStream = getAssets().open("eng.traineddata");

            // 내부 저장소의 "tessdata" 디렉토리에 대한 File 객체 생성
            File tessdataDir = new File(getFilesDir(), "tessdata");
            //Log.d("tessdataDir : ", tessdataDir.toString());

            if (!tessdataDir.exists()) {
                tessdataDir.mkdirs(); // 디렉토리가 없으면 생성
            }

            // traineddata 파일의 대상 경로 설정
            File targetFile = new File(tessdataDir, "eng.traineddata");

            // 파일 복사 수행
            if(!targetFile.exists()) {
                copyFile(inputStream, targetFile);
                Toast.makeText(this, "traineddata 파일이 복사되었습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "traineddata 파일 복사에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 파일 복사 메소드
    private void copyFile(InputStream inputStream, File targetFile) throws IOException {
        OutputStream outputStream = new FileOutputStream(targetFile);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    private String extractTextFromImage(Bitmap bitmap) {
        mTess.setImage(bitmap);
        String extractedText = mTess.getUTF8Text();
        return extractedText;
    }

    // 텍스트 인식 버튼 클릭 이벤트 처리
    public void recognizeText(View view) {
        // 이미지의 텍스트 추출
        //image = BitmapFactory.decodeResource(getResources(), R.drawable.mrz_01);
        Bitmap bitmap = imageToBitmap(imageView);
        String extractedText = extractTextFromImage(bitmap);
        OCRTextView.setText(extractedText);
    }

    private Bitmap imageToBitmap(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    imageView.setImageURI(uri);
                }
                break;
        }
    }

}