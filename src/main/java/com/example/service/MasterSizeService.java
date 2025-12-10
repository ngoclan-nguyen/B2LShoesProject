package com.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dao.MasterSizeDao;
import com.example.model.MasterSize;

import java.util.List;

@Service 
public class MasterSizeService {
	@Autowired
	private MasterSizeDao masterSizeDao;
	
	public List<MasterSize> getAllSize() {
		return masterSizeDao.getAllSize();
	}
}

