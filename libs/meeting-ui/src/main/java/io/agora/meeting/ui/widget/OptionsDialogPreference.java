package io.agora.meeting.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import com.contrarywind.adapter.WheelAdapter;
import com.contrarywind.view.WheelView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.agora.meeting.ui.R;

/**
 * Description:
 *
 * @author xcz
 * @since 3/4/21
 */
public class OptionsDialogPreference extends DialogPreference {
    private final List<String> options = new ArrayList<>();
    private final List<Integer> optionsValue = new ArrayList<>();
    private int mMinValue;
    private BottomSheetDialog mDialog;

    private OnItemSelectedListener onItemSelectedListener;

    public OptionsDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public OptionsDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OptionsDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OptionsDialogPreference(Context context) {
        super(context);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        if (defaultValue instanceof Integer) {
            setValue((Integer) defaultValue);
        } else {
            setValue(getValue());
        }
    }

    public OptionsDialogPreference addOptions(int min, int step, int count, String format) {
        mMinValue = min;
        int currentValue = getValue();
        for (int i = 0; i < count; i++) {
            int value = min + (i * step);
            String description = String.format(Locale.US, format, value);
            options.add(description);
            optionsValue.add(value);
            if (currentValue == value) {
                setSummary(description);
            }
        }
        return this;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public OptionsDialogPreference addOption(String description, int value) {
        int currentValue = getValue();
        options.add(description);
        optionsValue.add(value);
        if (currentValue == value) {
            setSummary(description);
        }
        return this;
    }

    public void setValue(int value) {
        if (optionsValue.contains(value)) {
            setSummary(options.get(optionsValue.indexOf(value)));
        }
        persistInt(value);
    }

    public int getValue() {
        return getPersistedInt(0);
    }

    @Override
    protected void onClick() {
        if (options == null || options.size() == 0) {
            return;
        }
        if (mDialog == null) {
            mDialog = new BottomSheetDialog(getContext());
            mDialog.setContentView(R.layout.layout_alert_options_single);
            BottomSheetBehavior<FrameLayout> behavior = mDialog.getBehavior();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    //禁止拖拽，
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        //设置为收缩状态
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });
        }
        WheelView wheelView = mDialog.findViewById(R.id.wheelview);
        wheelView.setCyclic(false);

        wheelView.setAdapter(new WheelAdapter<String>() {
            @Override
            public int getItemsCount() {
                return options.size();
            }

            @Override
            public String getItem(int index) {
                return options.get(index);
            }

            @Override
            public int indexOf(String o) {
                return options.indexOf(o);
            }
        });
        wheelView.setCurrentItem(optionsValue.indexOf(getValue()));
        mDialog.findViewById(R.id.submit).setOnClickListener(
                v -> {
                    int position = wheelView.getCurrentItem();
                    Integer value = optionsValue.get(position);
                    setValue(value);
                    mDialog.dismiss();
                    if(onItemSelectedListener != null){
                        onItemSelectedListener.onItemSelected(position, value);
                    }
                });
        mDialog.findViewById(R.id.cancel).setOnClickListener(v -> mDialog.dismiss());
        mDialog.show();
    }

    public interface OnItemSelectedListener{
        void onItemSelected(int position, int value);
    }
}
