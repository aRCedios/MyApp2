package com.arcedios.myapp2.View;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.arcedios.myapp2.databinding.ActivitySplashScreenBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {

    private ActivitySplashScreenBinding binding;
    private DatabaseReference databaseReference;
    public static int tiempo = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");
                //Verificar si hay un usuario guardado
                checkUserLoggedIn();
            }
        }, tiempo);
    }
    private void checkUserLoggedIn() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                   Intent intent = new Intent(SplashScreen.this, Login.class);//aquí
                    startActivity(intent);
                    finish();
                }
                    else {
                        Toast.makeText(SplashScreen.this, "No hay usuarios registrados", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SplashScreen.this, Registrarse.class);
                        startActivity(intent);
                        finish();
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "Error al verificar usuario", databaseError.toException());
                Toast.makeText(SplashScreen.this, "Error al verificar usuario", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
