package com.example.horseraceapp;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegPassword, etRegConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;
    private ImageView ivRegHorseGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        ivRegHorseGif = findViewById(R.id.ivRegHorseGif);

        // Load GIF using Glide
        Glide.with(this).asGif().load(R.drawable.horse_brown).into(ivRegHorseGif);

        // Animation: Horse running across the screen (behind the card)
        ivRegHorseGif.post(() -> {
            float screenWidth = getResources().getDisplayMetrics().widthPixels;
            ObjectAnimator animator = ObjectAnimator.ofFloat(ivRegHorseGif, "translationX", -300f, screenWidth + 300f);
            animator.setDuration(6000); // 6 seconds to run across
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.start();
        });

        btnRegister.setOnClickListener(v -> {
            String user = etRegUsername.getText().toString().trim();
            String pass = etRegPassword.getText().toString().trim();
            String confirmPass = etRegConfirmPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        tvLoginLink.setOnClickListener(v -> finish());
    }
}