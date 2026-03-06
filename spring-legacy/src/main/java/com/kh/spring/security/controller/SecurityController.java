package com.kh.spring.security.controller;

import java.security.Principal;
import java.sql.Date;
import java.text.SimpleDateFormat;

import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.spring.member.model.service.MemberService;
import com.kh.spring.member.model.validator.MemberValidator;
import com.kh.spring.member.model.vo.Member;
import com.kh.spring.security.model.vo.MemberExt;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/security")
@Slf4j
public class SecurityController {
	
	
	//필드 방식의 의존성 주입방법
	//@Autowired
	private MemberService mService;
	
	
	//@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	
	// 생성자 방식 의존성 주입
	// 단, 생성자가 여러개면 @Autowired 어노테이션 필요.
	public SecurityController(MemberService mService,
			BCryptPasswordEncoder passwordEncoder) {
		this.mService = mService;
		this.passwordEncoder = passwordEncoder;
	}
	
	@RequestMapping("/accessDenied")
	public String accessDenied(Model m) {
		m.addAttribute("errorMsg", "접근 불가능한 페이지입니다.");
		return "common/errorPage";
	}
	
	
	//회원 가입 페이지 
	@GetMapping("/insert")
	public String enroll(@ModelAttribute Member member) {
		
			/*
				@ModelAttribute
					- 커맨드객체 바인딩시 사용
					- model 영역에 커맨드객체를 저
			
			 */
		
		//log.info("bcrypto : {}", passwordEncoder);
		return "member/memberEnrollForm";
	}
	
	
	/*
		InitBinder
			- 현재 컨트롤러에서 Binding 작업을 수행할 때 실행되는 객체
			- @ModelAttribute로 지정한 커맨드객체에 대해 바인딩 작업을 수행한다
			
		처리 순서
			1) 클라이언트의 요청 파라미터를 커맨드 객체 필드에 바인딩시도
			2) 바인딩 과정에서 WebDataBinder가 필요한 경우 타입변환 및 유효성검사를 수행
			3) 유효성 검사 결과를 저장한다.(BindingResult)
		
	 */
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(new MemberValidator());
		
		// 타입변환 수행
		// 문자열 형태의 날짜값을 Date로 변환하기. (birthday)
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
		dateFormat.setLenient(false);  	//yyMMdd 형식이 아닌경우 에러발생
		
		binder.registerCustomEditor(Date.class, "birthday",
				new CustomDateEditor(dateFormat, true));
		// 날짜형태의 값이 들어오는경우 자동으로 date 자료형으로 변환해주는 커슽텀에디터
	}
	
	@PostMapping("/insert")
	public String register(
		@Validated @ModelAttribute Member member,
		BindingResult bindingResult,
		//BindingResult
		// - 유형성 검사결과를 저장하는 객체
		// - forward 시 자동으로 jsp에게 전달되며, form 태그내부에
		//	에러내용을 바인딩하기 위해 사용된다.
		RedirectAttributes ra
	) {
		// 유효성 검사에 따른 결과값
		
		// 실패
		if(bindingResult.hasErrors()) {
			return "member/memberEnrollForm";
		}
		
		
		// 성공시 비밀번호 암호화하여 회원가입 진행
		String encryotedPassword
			= passwordEncoder.encode(member.getUserPwd());
		member.setUserPwd(encryotedPassword);
		
		mService.insertMember(member);
		
		return "redirect:/member/login";
	}
	
	/*
		Authentication
			- Principal : 인증에 사용된 사용자 객체
			- Credentials : 인증에 필요한 비밀번호에 대한 정보를 가진 객체
			- Authorities : 사용자가 가진 권한을 저장하는 객체
	
	 */
	
	@GetMapping("/myPage")
	public String myPage(
			Authentication auth,
			Principal principal,
			Model model
			) {
		// 인증된 사용자 정보 가져오는 방법
		// 1. ArgumentResolver를 이용한 자동바인딩  
		log.debug("auth = {}" , auth);
		log.debug("principal = {}" , principal);
		
		
		// 2. SecurityContextHolder를 이용
		Authentication auth2 = SecurityContextHolder.getContext().getAuthentication();
		MemberExt loginUser = (MemberExt) auth2.getPrincipal();
		//MemberExt loginUser = (MemberExt) prin;
		
		model.addAttribute("loginUser", loginUser);
		
		return "member/myPage";
	}
	
	@PostMapping("/update")
	public String update(
			@Validated @ModelAttribute  MemberExt loginUser,
			BindingResult bindResult,
			Authentication auth, // 로그인한 사용자 인증정보
			RedirectAttributes ra
			) {
		
		if(bindResult.hasErrors()) {
			return "redirect:/security/myPage";
		}
		
		// 비지니스 로직
		// 		1. 전달받은 member 데이터를 바탕으로 db 수정요청
		int result = mService.updateMember(loginUser);
		
		// 		2. 내정보 수정이 성공했다면, 변경된 회원정보를 DB에서 다시 조회한 후
		//		새로운 인증정보를 생성하여 SecurityContext에 저장.
		if(result > 0) {
			Authentication newAuth = 
					// principal, credentials, authorities
					new UsernamePasswordAuthenticationToken(
							loginUser, 
							auth.getCredentials(),
							auth.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(newAuth);
			ra.addFlashAttribute("alertMsg", "내 정보 수정 성공");
			
			 return "redirect:/security/myPage";
		}
		else {
			throw new RuntimeException("회원정보 수정 오류.");
		}
	}
	
}
