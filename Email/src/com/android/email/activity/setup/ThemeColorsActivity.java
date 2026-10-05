package com.android.email.activity.setup;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.email.R;
import com.android.email.theme.ThemeColors;

/** Tela simples: editor hex para a cor de acento (laranja) e para o fundo (branco). */
public class ThemeColorsActivity extends Activity {
    private EditText mAccent;
    private EditText mBg;
    private View mAccentSwatch;
    private View mBgSwatch;

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.xl_theme_colors_title);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(16), dp(20), dp(16));

        final TextView note = new TextView(this);
        note.setText(R.string.xl_theme_note);
        note.setPadding(0, 0, 0, dp(16));
        root.addView(note);

        mAccent = addEditor(root, R.string.xl_theme_accent_label, ThemeColors.accent(this), true);
        mBg = addEditor(root, R.string.xl_theme_bg_label, ThemeColors.background(this), false);

        final LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(20), 0, 0);
        final Button save = new Button(this);
        save.setText(R.string.xl_theme_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onSave(); }
        });
        final Button reset = new Button(this);
        reset.setText(R.string.xl_theme_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ThemeColors.reset(ThemeColorsActivity.this);
                Toast.makeText(ThemeColorsActivity.this, R.string.xl_theme_saved,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        buttons.addView(save);
        buttons.addView(reset);
        root.addView(buttons);

        final ScrollView sv = new ScrollView(this);
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);
    }

    private EditText addEditor(LinearLayout root, int labelRes, int color, final boolean accent) {
        final TextView label = new TextView(this);
        label.setText(labelRes);
        root.addView(label);

        final LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(4), 0, dp(14));

        final EditText et = new EditText(this);
        et.setSingleLine(true);
        et.setHint(R.string.xl_theme_hex_hint);
        et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(7)});
        et.setText(ThemeColors.toHex(color));
        row.addView(et, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final View swatch = new View(this);
        swatch.setBackgroundColor(color);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48), dp(48));
        lp.leftMargin = dp(12);
        row.addView(swatch, lp);
        root.addView(row);

        if (accent) mAccentSwatch = swatch; else mBgSwatch = swatch;
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable e) {
                final Integer c = ThemeColors.parseHex(e.toString());
                if (c != null) {
                    (accent ? mAccentSwatch : mBgSwatch).setBackgroundColor(c);
                }
            }
        });
        return et;
    }

    private void onSave() {
        final Integer a = ThemeColors.parseHex(mAccent.getText().toString());
        final Integer b = ThemeColors.parseHex(mBg.getText().toString());
        boolean ok = true;
        if (a == null) { mAccent.setError(getString(R.string.xl_theme_invalid)); ok = false; }
        if (b == null) { mBg.setError(getString(R.string.xl_theme_invalid)); ok = false; }
        if (!ok) return;
        ThemeColors.save(this, a, b);
        Toast.makeText(this, R.string.xl_theme_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
