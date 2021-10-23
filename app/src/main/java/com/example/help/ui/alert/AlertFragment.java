package com.example.help.ui.alert;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.help.R;
import com.example.help.databinding.AlertFragmentBinding;
import com.google.android.material.tabs.TabLayout;

public class AlertFragment extends Fragment {

    private AlertViewModel mViewModel;
    private AlertFragmentBinding binding;
    TabLayout tabLayout;
    ViewPager viewPager;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(AlertViewModel.class);
        binding = AlertFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        tabLayout=root.findViewById(R.id.tablayout);
        viewPager=root.findViewById(R.id.viewPager);
        tabLayout.addTab(tabLayout.newTab().setText("Map"));
        tabLayout.addTab(tabLayout.newTab().setText("List"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        TabLayoutAdapter adapter=new TabLayoutAdapter(this.getContext(), getFragmentManager(),tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){

            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return root;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AlertViewModel.class);
        // TODO: Use the ViewModel
        getActivity().getSupportFragmentManager();
    }


}