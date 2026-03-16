-- Tabla de mensajes de chat privado entre empleados
CREATE TABLE IF NOT EXISTS mensajes_chat (
    id_mensaje     BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_emisor      INT NOT NULL,
    id_receptor    INT NOT NULL,
    contenido      TEXT NOT NULL,
    fecha_envio    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leido          BOOLEAN DEFAULT FALSE,
    fecha_lectura  DATETIME,
    FOREIGN KEY (id_emisor)   REFERENCES empleados(id_empleado),
    FOREIGN KEY (id_receptor) REFERENCES empleados(id_empleado),
    INDEX idx_emisor   (id_emisor),
    INDEX idx_receptor (id_receptor),
    INDEX idx_fecha    (fecha_envio)
);
