package com.example.horseraceapp;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView tvBalance;
    private CheckBox cbHorse1, cbHorse2, cbHorse3, cbHorse4;
    private SeekBar sbHorse1, sbHorse2, sbHorse3, sbHorse4;
    private EditText etBet1, etBet2, etBet3, etBet4;
    private Button btnStart, btnReset, btnDeposit;

    private int balance = 100;
    private boolean isRacing = false;
    private MediaPlayer bgMusic, raceSound;
    
    // Lưu trữ các đối tượng GIF để điều khiển trực tiếp
    private GifDrawable[] horseGifs = new GifDrawable[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadAllHorses();
        updateBalanceUI();
        setupAudio();

        btnStart.setOnClickListener(v -> {
            if (isRacing) return;
            startRace();
        });

        btnReset.setOnClickListener(v -> resetRace());
        btnDeposit.setOnClickListener(v -> showDepositDialog());
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tvBalance);
        btnStart = findViewById(R.id.btnStart);
        btnReset = findViewById(R.id.btnReset);
        btnDeposit = findViewById(R.id.btnDeposit);

        cbHorse1 = findViewById(R.id.cbHorse1);
        cbHorse2 = findViewById(R.id.cbHorse2);
        cbHorse3 = findViewById(R.id.cbHorse3);
        cbHorse4 = findViewById(R.id.cbHorse4);

        sbHorse1 = findViewById(R.id.sbHorse1);
        sbHorse2 = findViewById(R.id.sbHorse2);
        sbHorse3 = findViewById(R.id.sbHorse3);
        sbHorse4 = findViewById(R.id.sbHorse4);

        etBet1 = findViewById(R.id.etBet1);
        etBet2 = findViewById(R.id.etBet2);
        etBet3 = findViewById(R.id.etBet3);
        etBet4 = findViewById(R.id.etBet4);
    }

    private void loadAllHorses() {
        setHorseImage(sbHorse1, "horse_tan", 0);
        setHorseImage(sbHorse2, "horse_black", 1);
        setHorseImage(sbHorse3, "horse_brown", 2);
        setHorseImage(sbHorse4, "house_white", 3);
    }

    private void setHorseImage(SeekBar seekBar, String imageName, int index) {
        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resId != 0) {
            int sizeInPx = (int) (100 * getResources().getDisplayMetrics().density);
            
            Glide.with(this).asDrawable().load(resId).into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    resource.setBounds(0, 0, sizeInPx, sizeInPx);
                    seekBar.setThumb(resource);
                    
                    if (resource instanceof GifDrawable) {
                        GifDrawable gif = (GifDrawable) resource;
                        horseGifs[index] = gif; // Lưu vào mảng
                        
                        gif.setCallback(new Drawable.Callback() {
                            @Override
                            public void invalidateDrawable(@NonNull Drawable who) { seekBar.invalidate(); }
                            @Override
                            public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) { seekBar.postDelayed(what, when); }
                            @Override
                            public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) { seekBar.removeCallbacks(what); }
                        });
                        gif.stop(); // Dừng ban đầu
                    }
                }
                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
        }
    }

    private void toggleHorseGifs(boolean start) {
        for (GifDrawable gif : horseGifs) {
            if (gif != null) {
                if (start) gif.start();
                else gif.stop();
            }
        }
    }

    private void setupAudio() {
        try {
            int bgMusicRes = getResources().getIdentifier("background_music", "raw", getPackageName());
            if (bgMusicRes != 0) {
                bgMusic = MediaPlayer.create(this, bgMusicRes);
                bgMusic.setLooping(true);
                bgMusic.start();
            }
        } catch (Exception e) {}
    }

    private void updateBalanceUI() {
        tvBalance.setText("Vốn: " + balance + "$");
    }

    private void showDepositDialog() {
        EditText etAmount = new EditText(this);
        etAmount.setHint("Nhập số tiền");
        etAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this).setTitle("Nạp tiền").setView(etAmount)
                .setPositiveButton("Nạp", (dialog, which) -> {
                    String val = etAmount.getText().toString();
                    if (!val.isEmpty()) { balance += Integer.parseInt(val); updateBalanceUI(); }
                }).setNegativeButton("Hủy", null).show();
    }

    private void startRace() {
        int b1 = getBetValue(etBet1, cbHorse1);
        int b2 = getBetValue(etBet2, cbHorse2);
        int b3 = getBetValue(etBet3, cbHorse3);
        int b4 = getBetValue(etBet4, cbHorse4);
        int total = b1 + b2 + b3 + b4;

        if (total == 0) {
            Toast.makeText(this, "Vui lòng đặt cược!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (total > balance) {
            Toast.makeText(this, "Số dư không đủ!", Toast.LENGTH_SHORT).show();
            return;
        }

        balance -= total;
        updateBalanceUI();
        isRacing = true;
        disableBets(true);
        
        // Bắt đầu chạy GIF ngay lập tức
        toggleHorseGifs(true);

        try {
            int raceSoundRes = getResources().getIdentifier("race_sound", "raw", getPackageName());
            if (raceSoundRes != 0) {
                raceSound = MediaPlayer.create(this, raceSoundRes);
                raceSound.start();
            }
        } catch (Exception e) {}

        Random random = new Random();
        new CountDownTimer(60000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                sbHorse1.setProgress(sbHorse1.getProgress() + random.nextInt(4));
                sbHorse2.setProgress(sbHorse2.getProgress() + random.nextInt(4));
                sbHorse3.setProgress(sbHorse3.getProgress() + random.nextInt(4));
                sbHorse4.setProgress(sbHorse4.getProgress() + random.nextInt(4));

                if (sbHorse1.getProgress() >= 100 || sbHorse2.getProgress() >= 100 || 
                    sbHorse3.getProgress() >= 100 || sbHorse4.getProgress() >= 100) {
                    this.cancel();
                    onFinish();
                }
            }
            @Override
            public void onFinish() {
                isRacing = false;
                toggleHorseGifs(false); // Dừng GIF
                
                if (raceSound != null) { raceSound.stop(); raceSound.release(); raceSound = null; }
                
                int win = 0;
                if (sbHorse1.getProgress() >= 100) win = 1;
                else if (sbHorse2.getProgress() >= 100) win = 2;
                else if (sbHorse3.getProgress() >= 100) win = 3;
                else win = 4;

                showResult(win, b1, b2, b3, b4);
            }
        }.start();
    }

    private void showResult(int win, int b1, int b2, int b3, int b4) {
        int prize = (win == 1 ? b1 : (win == 2 ? b2 : (win == 3 ? b3 : b4))) * 2;
        balance += prize;
        updateBalanceUI();
        
        String[] colors = {"Vàng", "Đen", "Nâu", "Trắng"};
        new AlertDialog.Builder(this).setTitle("KẾT QUẢ")
                .setMessage("Ngựa " + colors[win-1] + " thắng!\nBạn nhận được: " + prize + "$")
                .setPositiveButton("Chơi tiếp", (dialog, which) -> disableBets(false))
                .setCancelable(false).show();
    }

    private int getBetValue(EditText et, CheckBox cb) {
        if (!cb.isChecked()) return 0;
        String val = et.getText().toString();
        return val.isEmpty() ? 0 : Integer.parseInt(val);
    }

    private void disableBets(boolean disable) {
        cbHorse1.setEnabled(!disable); cbHorse2.setEnabled(!disable);
        cbHorse3.setEnabled(!disable); cbHorse4.setEnabled(!disable);
        etBet1.setEnabled(!disable); etBet2.setEnabled(!disable);
        etBet3.setEnabled(!disable); etBet4.setEnabled(!disable);
        btnStart.setEnabled(!disable); btnDeposit.setEnabled(!disable);
    }

    private void resetRace() {
        if (isRacing) return;
        sbHorse1.setProgress(0); sbHorse2.setProgress(0);
        sbHorse3.setProgress(0); sbHorse4.setProgress(0);
        toggleHorseGifs(false);
        disableBets(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bgMusic != null) { bgMusic.release(); bgMusic = null; }
        if (raceSound != null) { raceSound.release(); raceSound = null; }
    }
}