package com.example.annotationtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Random;

import zero.Zero;
import zero.annotation.BindView;
import zero.annotation.ContentView;
import zero.annotation.OnClick;

@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

  @BindView(R.id.text)
  TextView textView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Zero.bind(this);
  }

  @OnClick({R.id.text, R.id.button}) void changeText() {
    textView.setText(String.valueOf(new Random().nextInt()));
  }
}
