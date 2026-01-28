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
import android.widget.ImageView;
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
    private MediaPlayer bgMusic, raceSound, betSound, startSound;

    // Bet state (only 1 horse)
    // horse: 2..4, 0 = not selected
    private int selectedHorse = 0;
    private int betAmount = 0;

    // Lưu trữ các đối tượng GIF để điều khiển trực tiếp
    private GifDrawable[] horseGifs = new GifDrawable[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadAllHorses();
        updateBalanceUI();
        updateBetSummaryUI();
        setupAudio();

        btnBet.setOnClickListener(v -> {
            if (isRacing) return;
            showBetDialog();
        });

        btnStart.setOnClickListener(v -> {
            if (isRacing) return;
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
        // Chỉ còn 3 ngựa: Đen, Nâu, Trắng
        setHorseImage(sbHorse2, "horse_black", 0);
        setHorseImage(sbHorse3, "horse_brown", 1);
        setHorseImage(sbHorse4, "house_white", 2);
    }

    private void setHorseImage(SeekBar seekBar, String imageName, int index) {
        int resId = getResources().getIdentifier(imageName, "drawable", getPackageName());
        if (resId != 0) {
            int sizeInPx = (int) (500 * getResources().getDisplayMetrics().density);

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

    private void updateBetSummaryUI() {
        if (selectedHorse == 0 || betAmount <= 0) {
            tvBetSummary.setText("Chưa đặt cược");
            return;
        }
        String name = selectedHorse == 2 ? "Đen" : (selectedHorse == 3 ? "Nâu" : "Trắng");
        tvBetSummary.setText("Cược: Ngựa " + name + " - " + betAmount + "$");
    }

    private void showDepositDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_deposit, null, false);
        EditText etAmount = view.findViewById(R.id.etDepositAmount);
        Button btnConfirm = view.findViewById(R.id.btnConfirmDeposit);
        Button btnCancel = view.findViewById(R.id.btnCancelDeposit);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setView(view)
                .create();

        btnConfirm.setOnClickListener(v -> {
            String val = etAmount.getText().toString().trim();
            if (!val.isEmpty()) {
                int amount = Integer.parseInt(val);
                if (amount > 0) {
                    balance += amount;
                    updateBalanceUI();
                    Toast.makeText(this, "✅ Nạp thành công " + amount + "$!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "⚠️ Số tiền phải lớn hơn 0!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "⚠️ Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showBetDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_bet, null, false);
        RadioGroup rg = view.findViewById(R.id.rgHorses);
        EditText etAmount = view.findViewById(R.id.etBetAmount);
        Button btnConfirm = view.findViewById(R.id.btnConfirmBet);
        Button btnCancel = view.findViewById(R.id.btnCancelBet);

        // Pre-fill current bet
        if (selectedHorse == 2) rg.check(R.id.rbHorse2);
        else if (selectedHorse == 3) rg.check(R.id.rbHorse3);
        else if (selectedHorse == 4) rg.check(R.id.rbHorse4);

        if (betAmount > 0) {
            etAmount.setText(String.valueOf(betAmount));
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setView(view)
                .setCancelable(false)
                .create();

        btnConfirm.setOnClickListener(v -> {
            int checkedId = rg.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(this, "Vui lòng chọn ngựa!", Toast.LENGTH_SHORT).show();
                return;
            }

            int horse = checkedId == R.id.rbHorse2 ? 2
                    : checkedId == R.id.rbHorse3 ? 3
                    : 4;

            String val = etAmount.getText().toString().trim();
            int amount = val.isEmpty() ? 0 : Integer.parseInt(val);
            if (amount <= 0) {
                Toast.makeText(this, "Vui lòng nhập số tiền cược hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedHorse = horse;
            betAmount = amount;
            updateBetSummaryUI();

            try{
                if (betSound != null) betSound.release();
                betSound = MediaPlayer.create(this, R.raw.cash_register);
                betSound.start();
            } catch (Exception e){
                e.printStackTrace();
            }

            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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

        try {
            if (raceSound != null){
                raceSound.release();
            }

            raceSound = MediaPlayer.create(this, R.raw.horse_neigh);

            if(raceSound != null){
                raceSound.start();
            }
        } catch (Exception e) {}

        Random random = new Random();
        new CountDownTimer(60000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                sbHorse2.setProgress(sbHorse2.getProgress() + random.nextInt(4));
                sbHorse3.setProgress(sbHorse3.getProgress() + random.nextInt(4));
                sbHorse4.setProgress(sbHorse4.getProgress() + random.nextInt(4));

                if (sbHorse2.getProgress() >= 100 || sbHorse3.getProgress() >= 100 || sbHorse4.getProgress() >= 100) {
                    this.cancel();
                    onFinish();
                }
            }

            @Override
            public void onFinish() {
                isRacing = false;
                toggleHorseGifs(false);

                if (raceSound != null) {
                    raceSound.stop();
                    raceSound.release();
                    raceSound = null;
                }

                int win;
                if (sbHorse2.getProgress() >= 100) win = 2;
                else if (sbHorse3.getProgress() >= 100) win = 3;
                else win = 4;

                showResult(win);
            }
        }.start();
    }

    private void showResult(int win) {
        int prize = (win == selectedHorse) ? betAmount * 2 : 0;
        balance += prize;
        updateBalanceUI();

        // Inflate custom dialog
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_result, null, false);
        TextView tvTitle = view.findViewById(R.id.tvResultTitle);
        ImageView ivHorse = view.findViewById(R.id.ivResultHorse);
        TextView tvMessage = view.findViewById(R.id.tvResultMessage);
        Button btnPlayAgain = view.findViewById(R.id.btnPlayAgain);

        // Set data
        String name = win == 2 ? "Đen" : (win == 3 ? "Nâu" : "Trắng");
        int horseDrawable = win == 2 ? R.drawable.horse_black
                          : (win == 3 ? R.drawable.horse_brown : R.drawable.house_white);

        String title = prize > 0 ? "🎉 CHIẾN THẮNG!" : "😢 THUA CUỘC";
        String message = prize > 0
            ? "🐴 Ngựa " + name + " về nhất!\n\n💰 Bạn thắng cược: +" + prize + "$\n🎊 Chúc mừng bạn!"
            : "🐴 Ngựa " + name + " về nhất!\n\n😔 Bạn đã thua cược: -" + betAmount + "$\n💪 Cố gắng lần sau nhé!";

        tvTitle.setText(title);
        tvTitle.setTextColor(prize > 0 ? 0xFFD4AF37 : 0xFFE74C3C);
        tvMessage.setText(message);

        // Load horse GIF
        Glide.with(this).load(horseDrawable).into(ivHorse);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setView(view)
                .setCancelable(false)
                .create();

        btnPlayAgain.setOnClickListener(v -> {
            resetRace();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void resetRace() {
        sbHorse2.setProgress(0);
        sbHorse3.setProgress(0);
        sbHorse4.setProgress(0);
        disableActions(false);
    }

    private void disableActions(boolean disable) {
        btnBet.setEnabled(!disable);
        btnStart.setEnabled(!disable);
        btnDeposit.setEnabled(!disable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.release();
        }
        if (raceSound != null) {
            raceSound.release();
        }
        if (betSound != null) {
            betSound.release();
        }
    }
}
