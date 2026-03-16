package com.hosteleria.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_chat",
       indexes = {
           @Index(name = "idx_emisor",   columnList = "id_emisor"),
           @Index(name = "idx_receptor", columnList = "id_receptor"),
           @Index(name = "idx_fecha",    columnList = "fecha_envio")
       })
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long idMensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emisor", nullable = false)
    private Empleado emisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receptor", nullable = false)
    private Empleado receptor;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(name = "leido")
    private Boolean leido = false;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    public MensajeChat() {}

    public Long getIdMensaje() { return idMensaje; }
    public void setIdMensaje(Long idMensaje) { this.idMensaje = idMensaje; }

    public Empleado getEmisor() { return emisor; }
    public void setEmisor(Empleado emisor) { this.emisor = emisor; }

    public Empleado getReceptor() { return receptor; }
    public void setReceptor(Empleado receptor) { this.receptor = receptor; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public Boolean getLeido() { return leido; }
    public void setLeido(Boolean leido) { this.leido = leido; }

    public LocalDateTime getFechaLectura() { return fechaLectura; }
    public void setFechaLectura(LocalDateTime fechaLectura) { this.fechaLectura = fechaLectura; }
}
