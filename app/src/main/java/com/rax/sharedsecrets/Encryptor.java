package com.rax.sharedsecrets;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import org.joda.time.Instant;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

public class Encryptor extends AppCompatActivity {

    private static final long MILLISECONDS_PER_5_MINUTES = 300_000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryptor);

        final Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText secretEditText = (EditText) findViewById(R.id.secretField);
                final TextView timeSalt = (TextView) findViewById(R.id.saltTextView);
                final TextView hashView = (TextView) findViewById(R.id.hashView);
                final ImageView qrCodeImage = (ImageView) findViewById(R.id.qrCode);
                final Button checkButton = (Button) findViewById(R.id.checkButton);
                final String secretText = secretEditText.getText().toString();
                String salt;
                String hash;

                salt = getTimeSalt();
                timeSalt.setText(getString(R.string.salt) + salt);
                timeSalt.setVisibility(View.VISIBLE);

                hash = generateHash(secretText, salt);

                hashView.setText(hash);
                hashView.setVisibility(View.VISIBLE);

                qrCodeImage.setImageBitmap(generateQRCode(hash));
                qrCodeImage.setVisibility(View.VISIBLE);

                checkButton.setVisibility(View.VISIBLE);
            }
        });

        final Button checkButton = (Button) findViewById(R.id.checkButton);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readQRCode();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.credits) {
            Intent intent = new Intent(this, CreditsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts an instance of ZXing QR code reader framework, which uses the camera to automatically
     * detect and read a QR code and sends the contained text back to the activity.
     */
    public void readQRCode() {
        //https://github.com/zxing/zxing/wiki/Scanning-Via-Intent
        new IntentIntegrator(this).initiateScan();
    }

    /**
     * Handles a read QR code. This is a callback method called by ZXing framework.
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        final TextView hashView = (TextView) findViewById(R.id.hashView);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        String peerHash, ownHash;
        String displayText;

        if (scanResult != null) {
            //Retrieve scanned result
            peerHash = scanResult.getContents();

            ownHash = hashView.getText().toString();

            if(ownHash.equals(peerHash)){
                displayText = "Trust established. Your secrets are equal.";
            }else{
                displayText = "Authentication failed. Your secrets differ.";
            }
            Toast.makeText(getApplicationContext(), displayText, Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap generateQRCode(String text) {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            // generate a QR code the size of the screen width
            int width = (int) (0.75 * Resources.getSystem().getDisplayMetrics().widthPixels);
            int height = width;

            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, height, width);

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_8888);
    }

    private String generateHash(String secret, String salt) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(secret.getBytes());

            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getTimeSalt(){
        Instant instant = Instant.now();
        long millTime = instant.getMillis();
        long roundedMillTime = millTime - (millTime % MILLISECONDS_PER_5_MINUTES);

        return String.valueOf(roundedMillTime);
    }
}
