package com.arcedios.myapp2.ModelView;

public class Usuarios {
    private   String nombre;
    private  String cedula;
    private  int edad;
    private  boolean enfermedades;
    public Usuarios() {
    }
    public  String getNombre() {

        return nombre;
    }

    public void setNombre(String nombre) {


        this.nombre = nombre;
    }

    public String getCedula() {

        return cedula;
    }

    public void setCedula(String cedula) {

        this.cedula = cedula;
    }

    public int getEdad() {

        return edad;
    }
    public boolean getEnfermedades() {

        return enfermedades;
    }
    public void setEnfermedades(boolean enfermedades) {

        this.enfermedades = enfermedades;
    }

    public void setEdad(int edad) {

        this.edad = edad;
    }
}
