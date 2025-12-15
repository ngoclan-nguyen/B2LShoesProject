package com.example.dao;

import com.example.model.RememberMeToken;
import com.example.model.User; // Import User model
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RememberMeDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional(readOnly = true)
	public RememberMeToken findByToken(String token) {
		try {
			String hql = "FROM RememberMeToken WHERE token = :token";
			return entityManager.createQuery(hql, RememberMeToken.class)
					.setParameter("token", token)
					.getResultStream()
					.findFirst()
					.orElse(null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Transactional
	public void save(RememberMeToken token, Long userId) {
		try {
			User userRef = entityManager.getReference(User.class, userId);

			token.setUser(userRef);

			entityManager.persist(token);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Transactional
	public void delete(String token) {
		try {
			String hql = "DELETE FROM RememberMeToken WHERE token = :token";
			entityManager.createQuery(hql)
					.setParameter("token", token)
					.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}