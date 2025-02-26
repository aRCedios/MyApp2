package com.arcedios.myapp2.View;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.arcedios.myapp2.databinding.ActivityEliminarCuentaBinding;
import com.arcedios.myapp2.Model.TodoFirebase;

public class EliminarCuenta extends AppCompatActivity {
    private ActivityEliminarCuentaBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityEliminarCuentaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.Eliminar.setOnClickListener(View->{
           TodoFirebase.removeItem(binding.TextoEliminar.getText().toString());
           Intent intent = new Intent(EliminarCuenta.this, Login.class);
           startActivity(intent);
        });


    }
}