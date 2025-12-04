package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dao.RememberMeDao;
import com.example.model.RememberMeToken;

@Service
public class RememberMeService {
	@Autowired
	private RememberMeDao rememberMeDao;

	public RememberMeToken findByToken(String token) {
		return rememberMeDao.findByToken(token);
	}

	public void save(RememberMeToken rmt) {
		rememberMeDao.save(rmt);
	}

	public void removeToken(String token) {
		rememberMeDao.removeToken(token);
	}
}
