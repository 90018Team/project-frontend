package com.example.help.ui.alert;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.help.R;
import com.example.help.databinding.AlertFragmentBinding;

public class AlertFragment extends Fragment {

    private AlertViewModel mViewModel;
    private AlertFragmentBinding binding;
    private Button button;
    private long down,up;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(AlertViewModel.class);
        binding = AlertFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        button = (Button) root.findViewById(R.id.gaga);
        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean  onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN :
                            Toast.makeText( getActivity(), "Down", Toast.LENGTH_SHORT).show();
                        down=System.currentTimeMillis();
                        float x = (float) 1.25;
                        float y = (float) 1.25;

                        button.setScaleX(x);
                        button.setScaleY(y);
                        break;
                    case MotionEvent.ACTION_UP :
                        x = 1;
                        y = 1;
                        button.setScaleX(x);
                        button.setScaleY(y);
                        Toast.makeText(getActivity(), "Up", Toast.LENGTH_SHORT).show();
                        up=System.currentTimeMillis();
                        if(up-down>3000)
                            Toast.makeText(getActivity(), "More than 3", Toast.LENGTH_SHORT).show();
                        return true;
                }
                return false;
            }
        });

        return root;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AlertViewModel.class);
        // TODO: Use the ViewModel
    }


}