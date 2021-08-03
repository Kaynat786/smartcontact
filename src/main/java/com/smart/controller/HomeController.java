package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	/*
	 * @GetMapping("/test")
	 * 
	 * @ResponseBody public String test() { User user = new User();
	 * 
	 * user.setName("Kaynat Siddiqui"); user.setEmail("mdkaynat384@gmail.com");
	 * userRepository.save(user); return "working"; }
	 */
	
	@RequestMapping("/")
	public String home(Model m)
	{
		m.addAttribute("title", "Home- smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model m)
	{
		m.addAttribute("title", "About- smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model m)
	{
		m.addAttribute("title", "Register- smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	
	//handler for registering user
	
	@RequestMapping(value="/do_register", method = RequestMethod.POST)
	public String registerSuer(@Valid @ModelAttribute("user") User user,BindingResult result1, @RequestParam(value="agreement",defaultValue="false") boolean agreement, Model m, 
			HttpSession session)
	{
		
		try {
			
			if(!agreement)
			{
				System.out.println("You are not accepts terms and condition");
				throw new Exception("You are not accepts terms and condition");
			}
			
			if(result1.hasErrors())
			{
				System.out.println("ERROR"+result1.toString());
				m.addAttribute("user", user);
				return "signup";
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			
			System.out.println("Agreement"+agreement);
			System.out.println("User"+user);
			
			User result = this.userRepository.save(user);
			
			m.addAttribute("user", new User());
			
			session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
			return "signup";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong"+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
		
	}
	
	//handler for custom login
	
	@GetMapping("/signin")
	public String customLogin(Model m)
	{
		m.addAttribute("title", "Login Page");
		return "login";
	}
	
	@GetMapping("/login-fail")
	public String failureLogin(Model m)
	{
		m.addAttribute("title", "failure page");
		
		return "login-fail";
	}
}
