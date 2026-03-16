package com.hosteleria.chat;

import com.hosteleria.model.Empleado;
import com.hosteleria.model.MensajeChat;
import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * (EN DESARROLLO - SIN COMPROBAR LA APP MOVIL AUN SIN DESARROLLAR)
 * Controlador de persistencia para los mensajes de chat.
 * Usado desde ChatEndpoint para guardar y recuperar mensajes.
 */
public class ChatController {

    /**
     * Persiste un mensaje nuevo y devuelve el ID asignado por la BBDD.
     * Devuelve -1 si hay error.
     */
    public long guardarMensaje(int idEmisor, int idReceptor, String contenido) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Empleado emisor   = session.get(Empleado.class, idEmisor);
            Empleado receptor = session.get(Empleado.class, idReceptor);

            if (emisor == null || receptor == null) {
                tx.rollback();
                return -1L;
            }

            MensajeChat msg = new MensajeChat();
            msg.setEmisor(emisor);
            msg.setReceptor(receptor);
            msg.setContenido(contenido);
            msg.setFechaEnvio(LocalDateTime.now());
            msg.setLeido(false);

            session.persist(msg);
            tx.commit();
            return msg.getIdMensaje();

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error guardando mensaje: " + e.getMessage());
            return -1L;
        }
    }

    /**
     * Devuelve los últimos N mensajes entre dos empleados, ordenados del más
     * antiguo al más reciente (listos para mostrar en pantalla).
     */
    public List<MensajeChat> getHistorial(int idEmpleado1, int idEmpleado2, int limite) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            List<MensajeChat> mensajes = session.createQuery(
                "SELECT m FROM MensajeChat m " +
                "JOIN FETCH m.emisor " +
                "JOIN FETCH m.receptor " +
                "WHERE (m.emisor.idEmpleado = :a AND m.receptor.idEmpleado = :b) " +
                "   OR (m.emisor.idEmpleado = :b AND m.receptor.idEmpleado = :a) " +
                "ORDER BY m.fechaEnvio DESC",
                MensajeChat.class
            ).setParameter("a", idEmpleado1)
             .setParameter("b", idEmpleado2)
             .setMaxResults(limite)
             .list();

            tx.commit();

            // Invertir para mostrar del más antiguo al más reciente
            Collections.reverse(mensajes);
            return mensajes;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error recuperando historial: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Marca como leídos todos los mensajes que el receptor aún no ha leído
     * en la conversación con el emisor.
     */
    public void marcarComoLeidos(int idEmisor, int idReceptor) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery(
                "UPDATE MensajeChat m SET m.leido = true, m.fechaLectura = :ahora " +
                "WHERE m.emisor.idEmpleado = :emisor " +
                "AND m.receptor.idEmpleado = :receptor " +
                "AND m.leido = false"
            ).setParameter("ahora", LocalDateTime.now())
             .setParameter("emisor", idEmisor)
             .setParameter("receptor", idReceptor)
             .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error marcando mensajes como leídos: " + e.getMessage());
        }
    }

    /**
     * Cuenta los mensajes no leídos que tiene un empleado en total.
     * Útil para el badge de notificaciones en la app.
     */
    public long contarNoLeidos(int idReceptor) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Long count = session.createQuery(
                "SELECT COUNT(m) FROM MensajeChat m " +
                "WHERE m.receptor.idEmpleado = :id AND m.leido = false",
                Long.class
            ).setParameter("id", idReceptor).uniqueResult();
            tx.commit();
            return count != null ? count : 0L;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error contando no leídos: " + e.getMessage());
            return 0L;
        }
    }
}
