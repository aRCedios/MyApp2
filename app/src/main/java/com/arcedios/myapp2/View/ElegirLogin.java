package com.arcedios.myapp2.View;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arcedios.myapp2.R;
import com.arcedios.myapp2.databinding.ActivityElegirLoginBinding;

public class ElegirLogin extends AppCompatActivity {
    private ActivityElegirLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityElegirLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.Login.setOnClickListener(view -> {
           Intent intent = new Intent(ElegirLogin.this, Login.class);
           startActivity(intent);
        });
        binding.Register.setOnClickListener(view -> {
            Intent intent = new Intent(ElegirLogin.this, Registrarse.class);
            startActivity(intent);
        });
    }
}