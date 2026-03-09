package com.kh.spring.border.model.service;

import org.springframework.stereotype.Service;

import com.kh.spring.border.model.vo.BorderDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BorderServiceImpl implements BorderService{
	private final BorderDao borderDao;
}
