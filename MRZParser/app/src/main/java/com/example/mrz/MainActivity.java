package com.example.mrz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.kurzdigital.mrz.MrzInfo;
import com.kurzdigital.mrz.MrzParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private TessBaseAPI mTess; // Tess API reference
    Button btnImage;           // 이미지 가져오기 버튼
    ImageView imageView;       // 인식할 이미지뷰
    TextView OCRTextView;     // OCR 결과뷰
    String ocrResult;         // OCR 결과값
    TextView NormalTextView;  // 정규화 결과뷰
    String normalResult;      // 정규화 결과
    TextView MRZTextView;     // MRZ 결과뷰

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Tesseract 모델 가져오기
        checkTrainedDataFile();

        // Tesseract 엔진 초기화
        mTess = new TessBaseAPI();
        String language = "eng"; // traineddata 파일의 언어 코드 (예: 영어 - eng, 한국어 - kor 등)
        String tessDataPath = getFilesDir() + "";
        mTess.init(tessDataPath, language);

        imageView = findViewById(R.id.imageView);
        OCRTextView = findViewById(R.id.OCRTextView);
        MRZTextView = findViewById(R.id.MRZTextView);
        NormalTextView = findViewById(R.id.NormalTextView);

//        imageId = R.drawable.mrz_01;
//        imageView.setImageResource(imageId);

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

    // 이미지 가져오기 onActivityResult
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

    public void checkTrainedDataFile() {
        try {
//            String[] files = assetManager.list("");
//
//            // 파일 이름 출력
//            for (String file : files) {
//                // 파일 이름을 로그에 출력하거나 원하는 작업을 수행할 수 있습니다.
//                // 예를 들어, Toast 메시지로 파일 이름을 표시하는 것도 가능합니다.
//                // 예: Toast.makeText(this, file, Toast.LENGTH_SHORT).show();
//                System.out.println("File: " + file);
//            }

            // traineddata 파일의 InputStream 가져오기
            InputStream inputStream = getAssets().open("eng.traineddata");

            // 내부 저장소의 "tessdata" 디렉토리에 대한 File 객체 생성
            File tessdataDir = new File(getFilesDir(), "tessdata");
            if (!tessdataDir.exists()) {
                tessdataDir.mkdirs(); // 디렉토리가 없으면 생성
            }

            // traineddata 파일의 대상 경로 설정
            File targetFile = new File(tessdataDir, "eng.traineddata");

            // 파일 복사 수행
            copyFile(inputStream, targetFile);

            Toast.makeText(this, "traineddata 파일이 복사되었습니다.", Toast.LENGTH_SHORT).show();
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

    // 텍스트 인식
    public void recognizeText(View view) {
//        // 이미지의 텍스트 추출
//        Bitmap image = BitmapFactory.decodeResource(getResources(), imageId);
//        ocrResult = extractTextFromImage(image);
//        OCRTextView.setText(ocrResult);
//        System.out.println("ocrResult: " + ocrResult);

        Bitmap bitmap = imageToBitmap(imageView);
        ocrResult = extractTextFromImage(bitmap);
        OCRTextView.setText(ocrResult);
    }

    private Bitmap imageToBitmap(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        return bitmap;
    }

//    private String normalization(String result) {
//        result = result.replaceAll("[^A-Z0-9<]", "");
//        System.out.println("ocrResult: " + ocrResult);
//        return result;
//    }

    public void normalization(View view) {
        //normalResult = MrzParser.purify(ocrResult); // 불순한 문자필터링 (문제: 소문자 삭제해버림)
        normalResult = ocrResult.toUpperCase().replaceAll("[^A-Z0-9<]", "");
        NormalTextView.setText(normalResult);
        //System.out.println("normalResult: " + normalResult);
        //System.out.println("normalResult length: " + normalResult.length());
    }


    public void mrzParser(View view) {
        //String test = "I<UTOERIKSSON<<ANNA<MARIA<<<<<<<<<<<D231458907UTO7408122F1204159<<<<<<<6";
        //MrzInfo mrzInfo = MrzParser.parse(test);

        System.out.println("normalResult:" + normalResult);
        MrzInfo mrzInfo = MrzParser.parse(normalResult);

        String documentCode = mrzInfo.documentCode;
        String issuingState = mrzInfo.issuingState;
        String primaryIdentifier = mrzInfo.primaryIdentifier;
        String secondaryIdentifier = mrzInfo.secondaryIdentifier;
        String nationality = mrzInfo.nationality;
        String documentNumber = mrzInfo.documentNumber;
        String dateOfBirth = mrzInfo.dateOfBirth;
        String sex = mrzInfo.sex;
        String dateOfExpiry = mrzInfo.dateOfExpiry;

        HashMap<String, String> mrzInfoMap = new HashMap<>();
        mrzInfoMap.put("Document Code", documentCode);
        mrzInfoMap.put("Issuing State", issuingState);
        mrzInfoMap.put("Primary Identifier", primaryIdentifier);
        mrzInfoMap.put("Secondary Identifier", secondaryIdentifier);
        mrzInfoMap.put("Nationality", nationality);
        mrzInfoMap.put("Document Number", documentNumber);
        mrzInfoMap.put("Date of Birth", dateOfBirth);
        mrzInfoMap.put("Sex", sex);
        mrzInfoMap.put("Date of Expiry", dateOfExpiry);

        // 키-값 형태로 저장된 데이터를 하나의 문자열로 합치기
        StringBuilder resultString = new StringBuilder();
        for (String key : mrzInfoMap.keySet()) {
            String value = mrzInfoMap.get(key);
            resultString.append(key).append(": ").append(value).append("\n");
        }

        // 마지막 줄바꿈 문자 삭제 (불필요한 줄바꿈 제거)
        if (resultString.length() > 0) {
            resultString.deleteCharAt(resultString.length() - 1);
        }

        MRZTextView.setText(resultString);
    }

}