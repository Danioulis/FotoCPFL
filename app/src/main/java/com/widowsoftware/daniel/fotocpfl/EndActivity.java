package com.widowsoftware.daniel.fotocpfl;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

public class EndActivity extends AppCompatActivity {

    Intent intent;
    String filename;
    Button send, cancel;
    EditText email ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);
        intent = getIntent();
        filename = intent.getStringExtra(MainActivity.EXTRA_FOTO);
        File directory = Environment.getExternalStorageDirectory();
        File imgFile = new File(directory, filename);
        send = (Button)findViewById(R.id.button_send);
        cancel = (Button)findViewById(R.id.button_cancel);
        email = (EditText)findViewById(R.id.email);

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            ImageView myImage = (ImageView) findViewById(R.id.foto);

            myImage.setImageBitmap(myBitmap);

        }

        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                filename = intent.getStringExtra(MainActivity.EXTRA_FOTO);
                File directory = Environment.getExternalStorageDirectory();
                File imgFile = new File(directory, filename);
                String filename="contacts_sid.vcf";
                Uri path = Uri.fromFile(imgFile);
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent .setType("vnd.android.cursor.dir/email");
                String to[] = {email.getText().toString()};
                emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
                emailIntent .putExtra(Intent.EXTRA_STREAM, path);
                emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Foto CPFL");
                startActivity(Intent.createChooser(emailIntent , "Enviar email..."));
            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                finish();
            }
        });
    }


}
