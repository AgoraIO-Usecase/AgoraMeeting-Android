package io.agora.meeting.ui.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.meeting.ui.R;
import io.agora.meeting.ui.databinding.LayoutActionSheetBinding;
import io.agora.meeting.ui.databinding.LayoutActionSheetListItemBinding;
import io.agora.meeting.ui.util.AvatarUtil;


public class ActionSheetFragment extends BottomSheetDialogFragment {
    private LayoutActionSheetBinding binding;
    private Menu menu;
    private Map<Integer, Integer> menuTitle;
    private List<Integer> moveMenuIds;
    private List<Integer> showMenuIds;
    private ActionSheetAdapter adapter;
    private OnItemClickListener listener;
    private ActionSheetFragmentArgs args;

    public static ActionSheetFragment getInstance(@MenuRes int menuRes) {
        return getInstance("", "", menuRes);
    }

    public static ActionSheetFragment getInstance(String userName, String userId, @MenuRes int menuRes) {
        ActionSheetFragment fragment = new ActionSheetFragment();
        fragment.setArguments(new ActionSheetFragmentArgs(menuRes, userName, userId).toBundle());
        return fragment;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void resetMenuTitle(@NonNull Map<Integer, Integer> map) {
        menuTitle = map;
    }

    public void removeMenu(@NonNull List<Integer> list) {
        moveMenuIds = list;
    }

    public void showMenu(@NonNull List<Integer> list) {
        showMenuIds = list;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = ActionSheetFragmentArgs.fromBundle(requireArguments());
        PopupMenu popupMenu = new PopupMenu(requireContext(), null);
        popupMenu.inflate(args.getMenuRes());
        menu = popupMenu.getMenu();

        if (moveMenuIds != null) {
            for (int id : moveMenuIds) {
                menu.removeItem(id);
            }
        }

        if (showMenuIds != null) {
            List<Integer> menuIds = new ArrayList<>();
            for (int i = 0; i < menu.size(); i++) {
                int itemId = menu.getItem(i).getItemId();
                menuIds.add(itemId);
            }
            for (Integer menuId : menuIds) {
                if(!showMenuIds.contains(menuId)){
                    menu.removeItem(menuId);
                }
            }
        }

        if (menuTitle != null) {
            for (Map.Entry<Integer, Integer> entry : menuTitle.entrySet()) {
                MenuItem menuItem = menu.findItem(entry.getKey());
                if (menuItem != null) {
                    menuItem.setTitle(entry.getValue());
                }
            }
        }

        if (menu.size() == 0) {
            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LayoutActionSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.list.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.list.setNestedScrollingEnabled(false);

        adapter = new ActionSheetAdapter();
        binding.list.setAdapter(adapter);

        binding.setClickListener(v -> dismiss());

        if(TextUtils.isEmpty(args.getUserId()) && TextUtils.isEmpty(args.getUserId())){
            binding.layoutTitle.setVisibility(View.GONE);
        }else{
            binding.layoutTitle.setVisibility(View.VISIBLE);
            AvatarUtil.loadCircleAvatar(binding.getRoot(), binding.ivAvatar, args.getUserName());
            binding.tvName.setText(args.getUserName());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            FrameLayout view = dialog.findViewById(R.id.design_bottom_sheet);
            view.setBackgroundColor(Color.TRANSPARENT);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = binding.getRoot().getMeasuredWidth() - 100;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            view.setLayoutParams(layoutParams);
            fixListHeight();
        });
        BottomSheetBehavior<FrameLayout> behavior = dialog.getBehavior();
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
        return dialog;
    }

    private void fixListHeight() {
        if (binding == null) {
            return;
        }
        Rect screenRect = new Rect();
        binding.getRoot().getWindowVisibleDisplayFrame(screenRect);
        int rootHeight = screenRect.height();
        int listHeight = binding.list.getMeasuredHeight();
        int listMaxHeight = rootHeight * 2 / 3;
        if (listHeight > listMaxHeight) {
            ViewGroup.LayoutParams layoutParams = binding.list.getLayoutParams();
            layoutParams.height = listMaxHeight;
            binding.list.setLayoutParams(layoutParams);
        }
    }

    public MenuItem getItem(int position) {
        return menu.getItem(position);
    }

    public MenuItem findItem(@IdRes int idRes) {
        return menu.findItem(idRes);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutActionSheetListItemBinding binding;

        public ViewHolder(@NonNull LayoutActionSheetListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.setClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(v, getAdapterPosition(), getItemId());
                }
                dismiss();
            });
        }

        void bind(MenuItem item) {
            binding.setTitle(item.getTitle().toString());
        }
    }

    private class ActionSheetAdapter extends RecyclerView.Adapter<ViewHolder> {
        public ActionSheetAdapter() {
            setHasStableIds(true);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(
                    LayoutActionSheetListItemBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false
                    )
            );
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(getItem(position));
        }

        public MenuItem getItem(int position) {
            return menu.getItem(position);
        }

        @Override
        public int getItemCount() {
            return menu.size();
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getItemId();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, long id);
    }
}
