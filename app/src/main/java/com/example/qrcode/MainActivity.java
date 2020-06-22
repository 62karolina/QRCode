package com.example.qrcode;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.yanzhenjie.zbar.ImageScanner;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    private ImageView imageView;
    private Button button;
    private static final int PICK_CONTACT = 0;
    private ImageScanner scanner;

    private TextView statusMessage;
    private TextView barcodeValue;
    static String email = "";
    static String seciliEmail = "";
    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView2);
        button = findViewById(R.id.openContact);
        findViewById(R.id.readBarcode).setOnClickListener(this);

        statusMessage = findViewById(R.id.status_message);
//        barcodeValue = findViewById(R.id.barcode_value);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    ContentResolver contect_resolver = getContentResolver();
                    Cursor c = getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {
                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String number = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        String id = c
                                .getString(c
                                        .getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                        Cursor emailCur = contect_resolver.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                        + " = ?", new String[] { id }, null);

                        while (emailCur.moveToNext()) {
                            // This would allow you get several email addresses
                            // if the email addresses were stored in an array
                            email = emailCur
                                    .getString(emailCur
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                            if (email != null)
                            {
                                seciliEmail = email;
                            } else {

                            }
                        }


                        String phoneNumber = name + " " + number + " " + seciliEmail;
                            try {
                                byte[] bytes = phoneNumber.getBytes();
                                String Result = new String(bytes);
                                String latin1Result = new String(bytes, "ISO-8859-1");
                                String utf8Result = new String(bytes, "UTF-8");
                                Bitmap barcode_bitmap = createBarCode(latin1Result, BarcodeFormat.QR_CODE, 200, 200);
                                imageView.setImageBitmap(barcode_bitmap);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                    }
                }
                break;
        }
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                    statusMessage.setText(barcode.displayValue);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static Bitmap createBarCode(String codeData, BarcodeFormat barcodeFormat, int codeHeight, int codeWidth) {

        try {
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            Writer codeWriter;
            if (barcodeFormat == BarcodeFormat.QR_CODE) {
                codeWriter = new QRCodeWriter();
            } else if (barcodeFormat == BarcodeFormat.CODE_128) {
                codeWriter = new Code128Writer();
            } else {
                throw new RuntimeException("Format Not supported.");
            }

            BitMatrix byteMatrix = codeWriter.encode(
                    codeData,
                    barcodeFormat,
                    codeWidth,
                    codeHeight,
                    hintMap
            );

            int width = byteMatrix.getWidth();
            int height = byteMatrix.getHeight();

            Bitmap imageBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    imageBitmap.setPixel(i, j, byteMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
                }
            }

            return imageBitmap;

        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.readBarcode) {
            // launch barcode activity.
            Intent intent = new Intent(this, BarcodeCaptureActivity.class);

            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        }
    }


}