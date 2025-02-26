package com.arcedios.myapp2.View;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.arcedios.myapp2.Model.TodoFirebase;
import com.arcedios.myapp2.R;
import com.arcedios.myapp2.databinding.ActivityRegistrarseBinding;
import com.arcedios.myapp2.ModelView.Usuarios;
public class Registrarse extends AppCompatActivity {
    private ActivityRegistrarseBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrarseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Usuarios usuario = new Usuarios();
        binding.PreguntaSalud.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Encuesta enfermedades")
                    .setMessage("Tiene usted alguna de las siguientes enfermedades: \n" +
                            "1. Diabetes.\n" +
                            "2. Ansiedad.\n " +
                            "3. Depresión. \n" +
                            "4. Insomnio.")
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "aceptado", Toast.LENGTH_SHORT).show();
                            usuario.setEnfermedades(true);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "no aceptado", Toast.LENGTH_SHORT).show();

                            usuario.setEnfermedades(false);
                        }
                    })
                    .show();
        });
        binding.registro.setOnClickListener(view -> {
            String nombre = binding.nombre.getText().toString();
            String cedula = binding.cedula.getText().toString();
            int edad = Integer.parseInt(binding.edad.getText().toString());

            usuario.setNombre(nombre);
            usuario.setCedula(cedula);
            usuario.setEdad(edad);



            if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(cedula)) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean noC= TodoFirebase.addUser(usuario);

            if (noC) {
                Toast.makeText(this, "error al agregar el usuario", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Usuario agregado correctamente", Toast.LENGTH_SHORT).show();


            }
            Intent intent = new Intent(Registrarse.this, Home.class);
            intent.putExtra("NOMBRE", nombre); // Pass the user name
            intent.putExtra("USER_ID", cedula); // Pass the user ID
            startActivity(intent);
            finish();


        });

    }
}