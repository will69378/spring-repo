package com.kh.spring.common;

import lombok.extern.slf4j.Slf4j;


// 스프링에서 로그를 사용하기 위한 어노테이션
@Slf4j
public class LogTest {
	
	public static void main(String[] args) {
	/*
		Logging level
			
			
			1. fatal
				- 치명적인 에러를 의미함
			
			2. error 
				- 요청 처리 중 발생하는 오류에서 사용하는 메서드
				- try ~ catch문의 catch에서 사용함
				
			3. warn
				- 경고성 메시지 작성시 사용하는 메서드.
				
			4. info
				- 요청 처리 중 발생하는 정보성 메시지 출력시 사용하는 메서드
				
			5. debug 
				- 개발 중에 필요한 정보성 메시지 출력시 사용
				
			6. trace
				- 가장 상세한 로깅 레벨로 디버그보다 많은 내부정보를 출력하고자 할때 사용
				
	 */	
		log.error("error - {}", "에러메세지");
		log.warn("warn - {}", "경고성메시지");
		log.warn("info - {}", "정보성메시지");
		log.debug("debug -{}", "디버그메시지");
		log.trace("trace - {}", "트레이스");
	}
}
