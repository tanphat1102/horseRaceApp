package com.example.horseraceapp;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private TextView tvBetSummary;
    private SeekBar sbHorse2, sbHorse3, sbHorse4;
    private Button btnBet, btnStart, btnDeposit;

    private int balance = 100;
    private boolean isRacing = false;

    // MediaPlayer management - sử dụng WeakReference pattern
    private MediaPlayer bgMusic, raceSound, winSound;

    // Bet state
    private int selectedHorse = 0;
    private int betAmount = 0;

    // Lưu GIF drawables
    private GifDrawable[] horseGifs = new GifDrawable[3];

    // Timer reference để cancel khi cần
    private CountDownTimer raceTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadAllHorses();
        updateBalanceUI();
        updateBetSummaryUI();
        setupAudio();

        setupClickListeners();
    }

    private void setupClickListeners() {
        btnBet.setOnClickListener(v -> {
            if (isRacing) {
                Toast.makeText(this, "Đang chạy đua, vui lòng chờ!", Toast.LENGTH_SHORT).show();
                return;
            }
            showBetDialog();
        });

        btnStart.setOnClickListener(v -> {
            if (isRacing) {
                Toast.makeText(this, "Đang chạy đua, vui lòng chờ!", Toast.LENGTH_SHORT).show();
                return;
            }
            startRace();
        });

        btnDeposit.setOnClickListener(v -> showDepositDialog());
    }

    private void initViews() {
        tvBalance = findViewById(R.id.tvBalance);
        tvBetSummary = findViewById(R.id.tvBetSummary);
        btnBet = findViewById(R.id.btnBet);
        btnStart = findViewById(R.id.btnStart);
        btnDeposit = findViewById(R.id.btnDeposit);
        sbHorse2 = findViewById(R.id.sbHorse2);
        sbHorse3 = findViewById(R.id.sbHorse3);
        sbHorse4 = findViewById(R.id.sbHorse4);
    }

    private void loadAllHorses() {
        setHorseImage(sbHorse2, "horse_black", 0);
        setHorseImage(sbHorse3, "horse_brown", 1);
        setHorseImage(sbHorse4, "horse_white", 2); // Fixed typo: "house_white" -> "horse_white"
    }

    private void setHorseImage(SeekBar seekBar, String imageName, int index) {
        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resId == 0) {
            return; // Resource not found
        }

        int sizeInPx = (int) (100 * getResources().getDisplayMetrics().density);

        Glide.with(this)
                .asDrawable()
                .load(resId)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        resource.setBounds(0, 0, sizeInPx, sizeInPx);
                        seekBar.setThumb(resource);

                        if (resource instanceof GifDrawable) {
                            GifDrawable gif = (GifDrawable) resource;
                            horseGifs[index] = gif;

                            gif.setCallback(new Drawable.Callback() {
                                @Override
                                public void invalidateDrawable(@NonNull Drawable who) {
                                    seekBar.invalidate();
                                }

                                @Override
                                public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
                                    seekBar.postDelayed(what, when);
                                }

                                @Override
                                public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
                                    seekBar.removeCallbacks(what);
                                }
                            });

                            gif.stop(); // Dừng ban đầu
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Cleanup if needed
                    }
                });
    }

    private void toggleHorseGifs(boolean start) {
        for (GifDrawable gif : horseGifs) {
            if (gif != null) {
                try {
                    if (start && !gif.isRunning()) {
                        gif.start();
                    } else if (!start && gif.isRunning()) {
                        gif.stop();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setupAudio() {
        try {
            int bgMusicRes = getResources().getIdentifier("background_music", "raw", getPackageName());
            if (bgMusicRes != 0) {
                bgMusic = MediaPlayer.create(this, bgMusicRes);
                if (bgMusic != null) {
                    bgMusic.setLooping(true);
                    bgMusic.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBalanceUI() {
        tvBalance.setText("Vốn: " + balance + "$");
    }

    private void updateBetSummaryUI() {
        if (selectedHorse == 0 || betAmount <= 0) {
            tvBetSummary.setText("Chưa đặt cược");
            return;
        }

        String name = getHorseName(selectedHorse);
        tvBetSummary.setText("Cược: Ngựa " + name + " - " + betAmount + "$");
    }

    private String getHorseName(int horse) {
        switch (horse) {
            case 2:
                return "Đen";
            case 3:
                return "Nâu";
            case 4:
                return "Trắng";
            default:
                return "Không xác định";
        }
    }

    private void showDepositDialog() {
        EditText etAmount = new EditText(this);
        etAmount.setHint("Nhập số tiền");
        etAmount.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle("Nạp tiền")
                .setView(etAmount)
                .setPositiveButton("Nạp", (dialog, which) -> {
                    String val = etAmount.getText().toString().trim();
                    if (!val.isEmpty()) {
                        try {
                            int amount = Integer.parseInt(val);
                            if (amount > 0) {
                                balance += amount;
                                updateBalanceUI();
                                Toast.makeText(MainActivity.this, "Nạp tiền thành công!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "Số tiền phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(MainActivity.this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showBetDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_bet, null, false);
        RadioGroup rg = view.findViewById(R.id.rgHorses);
        EditText etAmount = view.findViewById(R.id.etBetAmount);

        // Pre-fill current bet
        if (selectedHorse == 2) {
            rg.check(R.id.rbHorse2);
        } else if (selectedHorse == 3) {
            rg.check(R.id.rbHorse3);
        } else if (selectedHorse == 4) {
            rg.check(R.id.rbHorse4);
        }

        if (betAmount > 0) {
            etAmount.setText(String.valueOf(betAmount));
        }

        new AlertDialog.Builder(this)
                .setTitle("Đặt cược")
                .setView(view)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    int checkedId = rg.getCheckedRadioButtonId();
                    if (checkedId == -1) {
                        Toast.makeText(MainActivity.this, "Vui lòng chọn ngựa!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int horse = getHorseFromRadioId(checkedId);
                    String val = etAmount.getText().toString().trim();

                    try {
                        int amount = val.isEmpty() ? 0 : Integer.parseInt(val);

                        if (amount <= 0) {
                            Toast.makeText(MainActivity.this, "Vui lòng nhập số tiền cược hợp lệ!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (amount > balance) {
                            Toast.makeText(MainActivity.this, "Số dư không đủ!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        selectedHorse = horse;
                        betAmount = amount;
                        updateBetSummaryUI();
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "Số tiền không hợp lệ!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private int getHorseFromRadioId(int radioId) {
        if (radioId == R.id.rbHorse2) {
            return 2;
        } else if (radioId == R.id.rbHorse3) {
            return 3;
        } else if (radioId == R.id.rbHorse4) {
            return 4;
        }
        return 0;
    }

    private void startRace() {
        if (selectedHorse == 0 || betAmount <= 0) {
            Toast.makeText(this, "Vui lòng đặt cược!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (betAmount > balance) {
            Toast.makeText(this, "Số dư không đủ!", Toast.LENGTH_SHORT).show();
            return;
        }

        balance -= betAmount;
        updateBalanceUI();
        isRacing = true;
        disableActions(true);
        toggleHorseGifs(true);

        playRaceSound();
        startRaceTimer();
    }

    private void playRaceSound() {
        try {
            // Release old sound nếu còn
            releaseMediaPlayer(raceSound);

            int raceSoundRes = getResources().getIdentifier("race_sound", "raw", getPackageName());
            if (raceSoundRes != 0) {
                raceSound = MediaPlayer.create(this, raceSoundRes);
                if (raceSound != null) {
                    raceSound.setLooping(true);
                    raceSound.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startRaceTimer() {
        // Cancel timer cũ nếu còn chạy
        if (raceTimer != null) {
            raceTimer.cancel();
            raceTimer = null;
        }

        raceTimer = new CountDownTimer(60000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                Random random = new Random();
                sbHorse2.setProgress(Math.min(sbHorse2.getProgress() + random.nextInt(4), 100));
                sbHorse3.setProgress(Math.min(sbHorse3.getProgress() + random.nextInt(4), 100));
                sbHorse4.setProgress(Math.min(sbHorse4.getProgress() + random.nextInt(4), 100));

                if (sbHorse2.getProgress() >= 100 || sbHorse3.getProgress() >= 100 || sbHorse4.getProgress() >= 100) {
                    cancel();
                    onFinish();
                }
            }

            @Override
            public void onFinish() {
                finishRace();
            }
        }.start();
    }

    private void finishRace() {
        isRacing = false;
        toggleHorseGifs(false);

        // Stop race sound
        if (raceSound != null && raceSound.isPlaying()) {
            raceSound.stop();
        }
        releaseMediaPlayer(raceSound);
        raceSound = null;

        // Play win sound
        try {
            int winSoundRes = getResources().getIdentifier("win_sound", "raw", getPackageName());
            if (winSoundRes != 0) {
                releaseMediaPlayer(winSound);
                winSound = MediaPlayer.create(this, winSoundRes);
                if (winSound != null) {
                    winSound.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Xác định chiến thắng
        int win = determineWinner();
        showResult(win);
    }

    private int determineWinner() {
        if (sbHorse2.getProgress() >= 100) {
            return 2;
        } else if (sbHorse3.getProgress() >= 100) {
            return 3;
        } else {
            return 4;
        }
    }

    private void showResult(int win) {
        int prize = (win == selectedHorse) ? betAmount * 2 : 0;
        balance += prize;
        updateBalanceUI();

        String name = getHorseName(win);
        String message = String.format("Ngựa %s thắng!\nBạn nhận được: %d$", name, prize);

        new AlertDialog.Builder(this)
                .setTitle("KẾT QUẢ")
                .setMessage(message)
                .setPositiveButton("Chơi tiếp", (dialog, which) -> resetRace())
                .setCancelable(false)
                .show();
    }

    private void disableActions(boolean disable) {
        btnBet.setEnabled(!disable);
        btnStart.setEnabled(!disable);
        btnDeposit.setEnabled(!disable);
    }

    private void resetRace() {
        if (isRacing) {
            return;
        }

        sbHorse2.setProgress(0);
        sbHorse3.setProgress(0);
        sbHorse4.setProgress(0);

        toggleHorseGifs(false);
        disableActions(false);

        // Stop win sound
        if (winSound != null && winSound.isPlaying()) {
            winSound.stop();
        }

        // Reset bet state
        selectedHorse = 0;
        betAmount = 0;
        updateBetSummaryUI();
    }

    private void releaseMediaPlayer(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause background music khi activity pause
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume background music khi activity resume
        if (bgMusic != null && !bgMusic.isPlaying()) {
            bgMusic.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel timer nếu còn chạy
        if (raceTimer != null) {
            raceTimer.cancel();
            raceTimer = null;
        }

        // Release all media players
        releaseMediaPlayer(bgMusic);
        releaseMediaPlayer(raceSound);
        releaseMediaPlayer(winSound);

        bgMusic = null;
        raceSound = null;
        winSound = null;

        // Clear GIF references
        for (int i = 0; i < horseGifs.length; i++) {
            horseGifs[i] = null;
        }
    }
}