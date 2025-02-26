package com.arcedios.myapp2.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arcedios.myapp2.Model.TodoFirebase;
import com.arcedios.myapp2.R;
import com.arcedios.myapp2.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.login.setOnClickListener(view -> {
            String nombre = binding.nombre.getText().toString();
            String cedula = binding.cedula.getText().toString();
            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(cedula)) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }


            TodoFirebase.Identificar(cedula, new TodoFirebase.IdentificarCallback() {
                @Override
                public void onIdentificarResult(String nombreFirebase) {
                    if (nombreFirebase == null) {
                        Toast.makeText(Login.this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
                    } else if (nombreFirebase.equals("No existe")) {
                        Toast.makeText(Login.this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Login.this, Registrarse.class);
                        startActivity(intent);
                    } else if (nombreFirebase.equals(nombre)) {
                        Intent intent = new Intent(Login.this, Home.class);
                        intent.putExtra("USER_ID", cedula); // Pass the user ID
                        intent.putExtra("NOMBRE", nombre); // Pass the user name
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Error al verificar usuario, ya hay un usuario con esa cedula", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}