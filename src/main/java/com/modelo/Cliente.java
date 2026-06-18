package com.modelo;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String apellido;
    private String razonSocial;
    private String correo;
    private String numeroDocumento; // Para DNI
    private String numeroRuc;       // Para RUC
    private String telefono;
    private String direccion;
    private String ubigeo;
    private String tipoDocumento;   // '1' = DNI, '6' = RUC (Códigos SUNAT)
    private String observacion;

    // Constructor vacío
    public Cliente() {}

    // Constructor completo para guardar desde el Controlador
    public Cliente(String nombre, String apellido, String razonSocial, String correo,
                   String numeroDocumento, String numeroRuc, String telefono,
                   String direccion, String ubigeo, String tipoDocumento, String observacion) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.razonSocial = razonSocial;
        this.correo = correo;
        this.numeroDocumento = numeroDocumento;
        this.numeroRuc = numeroRuc;
        this.telefono = telefono;
        this.direccion = direccion;
        this.ubigeo = ubigeo;
        this.tipoDocumento = tipoDocumento;
        this.observacion = observacion;
    }

    // --- GETTERS ---
    public int getIdCliente() { return idCliente; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getRazonSocial() { return razonSocial; }
    public String getCorreo() { return correo; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getNumeroRuc() { return numeroRuc; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getUbigeo() { return ubigeo; }
    public String getTipoDocumento() { return tipoDocumento; }
    public String getObservacion() { return observacion; }

    // --- SETTERS ---
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public void setNumeroRuc(String numeroRuc) { this.numeroRuc = numeroRuc; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public void setUbigeo(String ubigeo) { this.ubigeo = ubigeo; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}