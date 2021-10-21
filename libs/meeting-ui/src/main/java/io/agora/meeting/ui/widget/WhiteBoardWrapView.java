package io.agora.meeting.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.herewhite.sdk.domain.Appliance;

import java.util.HashMap;
import java.util.Map;

import io.agora.meeting.ui.R;
import io.agora.whiteboard.netless.widget.WhiteBoardView;

public class WhiteBoardWrapView extends FrameLayout {
    private LinearLayout toolsLayout;
    private FrameLayout boardContainer;

    private OnSelectListener onSelectListener;
    private WhiteBoardView whiteBoardView;

    public WhiteBoardWrapView(Context context) {
        this(context, null);
    }

    public WhiteBoardWrapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View inflate = View.inflate(getContext(), R.layout.widget_whiteboard, this);
        boardContainer = inflate.findViewById(R.id.whiteboard_container);
        RadioGroup toolsRadioGroup = inflate.findViewById(R.id.rg_tools);
        ImageView colorImageButton = inflate.findViewById(R.id.btn_color);
        toolsLayout = inflate.findViewById(R.id.tools_layout);

        Rect visibleRect = new Rect();
        getWindowVisibleDisplayFrame(visibleRect);
        boolean isLand = visibleRect.width() > visibleRect.height();
        if(isLand){
            // 设为横条
            toolsRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
            toolsLayout.setOrientation(LinearLayout.HORIZONTAL);

            LayoutParams layoutParams = (LayoutParams) toolsLayout.getLayoutParams();
            layoutParams.gravity = Gravity.BOTTOM;
            toolsLayout.setLayoutParams(layoutParams);
        }else{
            // 设为竖条
            toolsRadioGroup.setOrientation(LinearLayout.VERTICAL);
            toolsLayout.setOrientation(LinearLayout.VERTICAL);
        }

        colorImageButton.setOnClickListener(v -> {
            int[] strokeColor = null;
            if (onSelectListener != null) {
                strokeColor = onSelectListener.onStrokeColorSelectedBefore();
            }

            ColorPickerDialogBuilder builder = ColorPickerDialogBuilder.with(getContext());
            if (strokeColor != null && strokeColor.length == 3) {
                builder.initialColor(Color.argb(255, strokeColor[0], strokeColor[1], strokeColor[2]));
            }

            AlertDialog dialog = builder.showAlphaSlider(false)
                    .setPositiveButton(R.string.cmm_continue, (d, lastSelectedColor, allColors) ->
                            {
                                int[] nColor = {Color.red(lastSelectedColor), Color.green(lastSelectedColor), Color.blue(lastSelectedColor)};
                                if (onSelectListener != null) {
                                    onSelectListener.onStrokeColorSelectedAfter(nColor);
                                }
                            }
                    )
                    .setNegativeButton(R.string.cmm_cancel, null)
                    .build();

            dialog.setOnShowListener(dialog1 -> {
                if(isLand){
                    // 确保横屏下显示完整
                    View customView = dialog.getWindow().getDecorView().findViewById(R.id.custom);
                    ViewGroup.LayoutParams layoutParams = customView.getLayoutParams();
                    layoutParams.height = (int) (visibleRect.height() / 1.6);
                    customView.setLayoutParams(layoutParams);
                }
            });

            dialog.show();
        });

        Map<Integer, OnClickListener> clickMap = new HashMap<>();
        clickMap.put(R.id.rb_selector, v -> callOnApplianceSelected(Appliance.SELECTOR));
        clickMap.put(R.id.rb_pencil, v -> callOnApplianceSelected(Appliance.PENCIL));
        clickMap.put(R.id.rb_rectangle, v -> callOnApplianceSelected(Appliance.RECTANGLE));
        clickMap.put(R.id.rb_ellipse, v -> callOnApplianceSelected(Appliance.ELLIPSE));
        clickMap.put(R.id.rb_text, v -> callOnApplianceSelected(Appliance.TEXT));
        clickMap.put(R.id.rb_eraser, v -> callOnApplianceSelected(Appliance.ERASER));

        toolsRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            OnClickListener onClickListener = clickMap.get(checkedId);
            if (onClickListener != null) {
                onClickListener.onClick(group);
            }
        });
        toolsLayout.findViewById(R.id.btn_clean).setOnClickListener(v -> {
            if (onSelectListener != null) {
                onSelectListener.onCleanSelected();
            }
        });
    }


    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
        if (whiteBoardView != null && whiteBoardView.getParent() == boardContainer) {
            whiteBoardView.setOnClickListener(v -> performClick());
        }
    }

    private void callOnApplianceSelected(String appliance) {
        if (onSelectListener != null) {
            onSelectListener.onApplianceSelected(appliance);
        }
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
    }

    public void startup(WhiteBoardView boardView) {
        this.whiteBoardView = boardView;
        if (boardView == null) {
            return;
        }
        if (hasOnClickListeners()) {
            boardView.setOnClickListener(v -> performClick());
        } else {
            boardView.setOnClickListener(null);
        }
        if (boardView.getParent() != boardContainer && boardView.getParent() instanceof ViewGroup) {
            ((ViewGroup) boardView.getParent()).removeView(boardView);
        }
        boardContainer.addView(boardView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public boolean hasBoardView() {
        return boardContainer.getChildCount() > 0;
    }

    public void setInterceptTouchEvent(boolean interceptTouchEvent) {
        if (whiteBoardView != null) {
            whiteBoardView.setInterceptTouchEvent(interceptTouchEvent);
        }
    }

    public void setToolsVisible(boolean visible) {
        toolsLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            callOnApplianceSelected(Appliance.PENCIL);
        }
    }

    public interface OnSelectListener {

        int[] onStrokeColorSelectedBefore();

        void onStrokeColorSelectedAfter(int[] color);

        void onCleanSelected();

        void onApplianceSelected(String appliance);
    }
}
