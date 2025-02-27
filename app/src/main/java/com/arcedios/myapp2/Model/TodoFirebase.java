package com.arcedios.myapp2.Model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.arcedios.myapp2.ModelView.Usuarios;

public class TodoFirebase {
    public boolean aver;

    private static DatabaseReference databaseReference = null;

    public TodoFirebase() {
        // Initialize the database reference in the constructor
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public boolean isAver() {

        return aver;
    }

    public boolean setAver(boolean aver) {
        this.aver = aver;
        return aver;
    }

    public static boolean addUser(Usuarios p) {
        AtomicBoolean success = new AtomicBoolean(false);
        TodoFirebase firebase = new TodoFirebase();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        HashMap<String, Object> map = new HashMap<>();
        map.put("nombre", p.getNombre());
        map.put("cedula", p.getCedula());
        map.put("edad", p.getEdad());
        map.put("enfermedades", p.getEnfermedades());
        myRef.child("Usuarios").child(p.getCedula()).setValue(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                success.set(true);
            }


        });
        return success.get();
    }

    public static void removeItem(String cedula) {
        TodoFirebase firebase = new TodoFirebase();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        myRef.child("Usuarios").child(cedula).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                firebase.setAver(true);
            }
        });
    }

    public interface IdentificarCallback {
        void onIdentificarResult(String nombre);
    }

    public static void Identificar(String cedula, IdentificarCallback callback) {
        Log.d("TodoFirebase", "Identificar called with cedula: " + cedula);
        if (cedula == null || cedula.isEmpty()) {
            Log.d("TodoFirebase", "cedula is null or empty");
            callback.onIdentificarResult(null); // Indicate an error
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Usuarios");

        myRef.child(cedula).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TodoFirebase", "onDataChange called");
                if (snapshot.exists()) {
                    Log.d("TodoFirebase", "snapshot exists");
                    Usuarios p = snapshot.getValue(Usuarios.class);
                    if (p != null) {
                        Log.d("TodoFirebase", "User found: " + p.getNombre());
                        callback.onIdentificarResult(p.getNombre());
                    } else {
                        Log.d("TodoFirebase", "User is null");
                        callback.onIdentificarResult(null); // Handle the case where p is null
                    }
                } else {
                    Log.d("TodoFirebase", "snapshot does not exist");
                    callback.onIdentificarResult("No existe"); // Handle the case where the snapshot doesn't exist
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TodoFirebase", "onCancelled called: " + error.getMessage());
                callback.onIdentificarResult(null); // Handle the error case
            }
        });
    }



}