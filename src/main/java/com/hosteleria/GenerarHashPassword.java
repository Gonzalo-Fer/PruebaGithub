package com.hosteleria;

import com.hosteleria.util.PasswordUtil;

/**
 * Utilidad para generar un hash de contraseña y poder actualizar manualmente
 * el usuario admin en la base de datos (por ejemplo tras cargar data.sql).
 *
 * Uso: ejecutar con argumentos [usuario] [contraseña]
 * Ejemplo: java -cp ... com.hosteleria.GenerarHashPassword admin admin123
 * Luego en MySQL: UPDATE usuarios SET password_hash = '...' WHERE username = 'admin';
 */
public class GenerarHashPassword {

    public static void main(String[] args) {
        String password = args.length >= 2 ? args[1] : "admin123";
        String hash = PasswordUtil.hash(password);
        System.out.println("Hash para contraseña '" + password + "':");
        System.out.println(hash);
        System.out.println("\nEjecuta en MySQL:");
        System.out.println("UPDATE usuarios SET password_hash = '" + hash + "' WHERE username = '" + (args.length >= 1 ? args[0] : "admin") + "';");
    }
}
