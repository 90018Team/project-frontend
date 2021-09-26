package com.example.help.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.help.ui.chatRoom.ChatActivity;
import com.example.help.R;
import com.example.help.databinding.HomeFragmentBinding;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private HomeFragmentBinding binding;

    private View frameLayout1;
    private ImageView imageView2;
    private Handler mHandler=new Handler();
    private ViewGroup.LayoutParams params;
    private int mHeight;
    private boolean isClick;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = HomeFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        imageView2 = root.findViewById(R.id.imageView2);
        frameLayout1 = root.findViewById(R.id.frameLayout1);
        params = frameLayout1.getLayoutParams();
        mHeight = params.height;
        imageView2.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isClick=true;
                        startplay();
                        break;
                    case MotionEvent.ACTION_UP:
                        isClick=false;
                        endplay();
                        break;

                    default:
                        break;
                }

                return false;
            }

        });
        ////


        return root;
    }

    private void endplay() {

        params.height=mHeight;
        frameLayout1.setLayoutParams(params);
    }

    private void startplay() {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                if (isClick) {
                    params.height+=10;
                    if (params.height>=imageView2.getHeight()) {
                        params.height=imageView2.getHeight();
                        Toast.makeText(getContext(), "start", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getContext(), ChatActivity.class));
                    }else{
                        mHandler.postDelayed(this, 5);
                    }
                    frameLayout1.setLayoutParams(params);
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}