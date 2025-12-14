package com.example.service;

import com.example.dao.RememberMeDao;
import com.example.model.RememberMeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RememberMeService {

	@Autowired
	private RememberMeDao rememberMeDao;

	public RememberMeToken findByToken(String token) {
		return rememberMeDao.findByToken(token);
	}

	@Transactional
	public void save(RememberMeToken rmt, Long userId) {
		rememberMeDao.save(rmt, userId);
	}

	@Transactional
	public void removeToken(String token) {
		rememberMeDao.delete(token);
	}
}