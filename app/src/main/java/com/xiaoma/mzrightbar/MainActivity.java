package com.xiaoma.mzrightbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import com.xiaoma.mzrightbar.rightbar.utils.RightWindow;
import com.xiaoma.mzrightbar.rightbar.utils.Utils;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0x11;
    private RightWindow rightWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btn=findViewById(R.id.btn);
        Button bt2=findViewById(R.id.btn2);
        final SeekBar seekBar=findViewById(R.id.seek);
        CheckBox checkBox=findViewById(R.id.check);
        RadioGroup group=findViewById(R.id.radio);


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rightWindow.setShowFromRight(isChecked);
            }
        });
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
               RadioButton rd= findViewById(checkedId);
              rightWindow.setShowCount(Integer.valueOf( rd.getText().toString()));
            }
        });

        rightWindow = new RightWindow(MainActivity.this);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightWindow.show();
                rightWindow.setAlpha(seekBar.getProgress());
            }
        });

        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rightWindow.setAlpha(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_CODE:
                Bitmap bm=null;
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    if(uri != null){
                        try {
                             bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }else {
                        Bundle bundleExtras = data.getExtras();
                        if(bundleExtras != null){
                             bm = bundleExtras.getParcelable("data");
                        }
                    }
                    rightWindow.setImageBackground(bm);
                }

                break;
        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
