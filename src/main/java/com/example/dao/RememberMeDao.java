package com.example.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import com.example.config.HibernateUtil;
import com.example.model.RememberMeToken;
import com.example.model.User;



@Repository
public class RememberMeDao {
	public RememberMeToken findByToken(String token) {
		RememberMeToken rememberMeToken = null;
		Session session = null;
		Transaction transaction = null;
		
		try {
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();
			
			Query<RememberMeToken> query = session.createQuery("From RememberMeToken Where token = :token",RememberMeToken.class);
			query.setParameter("token", token);
			rememberMeToken = query.getSingleResult();
			
			transaction.commit();
		}catch(Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
		}finally {
			if(session != null && session.isOpen()) {
				session.close();
			}
		}
		
		return rememberMeToken;
	}

	public void save(RememberMeToken rmt) {
		Session session = null;
		Transaction transaction = null;
		
		try {
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();
			
			session.persist(rmt);
			
			transaction.commit();
		}catch(Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
		}finally {
			if(session != null && session.isOpen()) {
				session.close();
			}
		}
	
		
	}

	public void removeToken(String token) {
		Session session = null;
		Transaction transaction = null;
		
		try {
			session = HibernateUtil.getSession();
			transaction = session.beginTransaction();
			
			MutationQuery query= session.createMutationQuery("Delete From RememberMeToken r Where r.token = :token");
			query.setParameter("token", token);
			query.executeUpdate();
			
			
			transaction.commit();
		}catch(Exception e) {
			if(transaction != null) {
				transaction.rollback();
			}
			e.printStackTrace();
		}finally {
			if(session != null && session.isOpen()) {
				session.close();
			}
		}		
	}
}