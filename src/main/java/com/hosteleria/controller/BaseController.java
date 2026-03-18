package com.hosteleria.controller;

import com.hosteleria.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Clase base con métodos auxiliares reutilizables para todos los controladores.
 * Centraliza el manejo de sesión, transacciones y errores.
 */
public abstract class BaseController {

    /**
     * Ejecuta una HQL sin parámetros y devuelve la lista resultante.
     */
    protected <T> List<T> executeQuery(String hql, Class<T> clazz, String contexto) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<T> resultado = session.createQuery(hql, clazz).list();
            tx.commit();
            return resultado;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener " + contexto + ": " + e.getMessage());
            return Collections.emptyList();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /**
     * Ejecuta una HQL con un único parámetro y devuelve un Optional.
     * Útil para búsquedas por ID con JOIN FETCH.
     */
    protected <T> Optional<T> executeQuerySingle(String hql, Class<T> clazz,
                                                  String paramName, Object paramValue,
                                                  String contexto) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            T resultado = session.createQuery(hql, clazz)
                                 .setParameter(paramName, paramValue)
                                 .uniqueResult();
            tx.commit();
            return Optional.ofNullable(resultado);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al obtener " + contexto + ": " + e.getMessage());
            return Optional.empty();
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /**
     * Persiste una entidad nueva (INSERT).
     */
    protected boolean persistir(Object entidad, String contexto) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(entidad);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al guardar " + contexto + ": " + e.getMessage());
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    /**
     * Actualiza una entidad existente (UPDATE).
     */
    protected boolean fusionar(Object entidad, String contexto) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.merge(entidad);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception rb) { /* ignorar */ }
            }
            System.err.println("Error al actualizar " + contexto + ": " + e.getMessage());
            return false;
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}
