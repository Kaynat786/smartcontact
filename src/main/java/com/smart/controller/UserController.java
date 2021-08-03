package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
//import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	//method  for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m, Principal principal)
	{
		String userName = principal.getName();
		System.out.println("USERNAME"+userName);
		
		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER"+user);
		
		m.addAttribute("user", user);
	}
	
	//dash board home
	@RequestMapping("/index")
	public String dashboard(Model m,Principal principal)
	{
		/*
		 * String userName = principal.getName();
		 * System.out.println("USERNAME"+userName);
		 * 
		 * User user = userRepository.getUserByUserName(userName);
		 * System.out.println("USER"+user);
		 * 
		 * m.addAttribute("user", user);
		 * 
		 * //get the user by userName(email)
		 */	
		m.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open form handler
	
	@GetMapping("/add_contact")
	public String openAddContactForm(Model m)
	{
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
	
	//processing add contact form
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") 
	MultipartFile file,Principal principal,HttpSession session)
	{
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		//processing and uploading file
		
		if(file.isEmpty())
		{
			//if the file is empty then try our message 
			
			System.out.println("File is empty");
			contact.setImage("default.png");
			
		}
		else
		{
			// file the file to folder and update the name to contact
			
			contact.setImage(file.getOriginalFilename());
			
			
			
			File file2 = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("Image is uploaded successfully");
			
			
		}
		
		
		contact.setUser(user);
		 user.getContacts().add(contact);
		 this.userRepository.save(user);
		 
			/*
			 * throw error
			 * if(3>2) { throw new Exception(); }
			 */
		
		System.out.println("DATA"+contact);
		System.out.println("Added to database");
		
		//message success.....
		
		session.setAttribute("message", new Message("Your contact is added !!!","success"));
		
	}catch(Exception e)
	{
		System.out.println("ERROR"+e.getMessage()); 
		e.printStackTrace();
		//error messages
		session.setAttribute("message", new Message("Some thing went wrong Try again !!!","danger"));

	}
		
		return "normal/add_contact_form";
	}
	
	//show contacts handler
	//per page =5[n]
	//current page=0[page]
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page,Model m, Principal principal)
	{
		m.addAttribute("title", "Show user contacts");
		
		/*
		 * String userName = principal.getName();
		 * 
		 * User user = this.userRepository.getUserByUserName(userName);
		 * 
		 * List<Contact> contacts = user.getContacts();
		 */
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//current Page-page
		//Contact Per page- 5
		
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findConatctsByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
	//show particular contact details
	
	@RequestMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId,
			Model m, Principal principal)
	
	{
		System.out.println("CID"+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		Contact contact=contactOptional.get();
		
		String userName = principal.getName();
		User userByUserName = this.userRepository.getUserByUserName(userName);
		
		if(userByUserName.getId()==contact.getUser().getId())
		{
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
		
		}
		return "normal/contact-detail";
	}
	
	//delete contact handler
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,
			Model m, Principal principal,HttpSession session)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		Contact contact=contactOptional.get();
		
		String name = principal.getName();
		
		User user = this.userRepository.getUserByUserName(name);
		
		//check...Assignment
		
		
		
		if(user.getId()== contact.getUser().getId())
		{
			//contact.setUser(null);
			//this.contactRepository.delete(contact);
			user.getContacts().remove(contact);
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Contact deleted..", "success"));
			
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
	
	//open update handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid,Model m)
	{
		m.addAttribute("title", "update contact");
		Contact contact = this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	//update contact handler
	
	@RequestMapping(value="/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Model m,HttpSession session,
			Principal principal)
	{
		
		try {
			
			//fetching old contact details
			
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			//image
			if(!file.isEmpty())
			{
				//file work
				
				//rewrite
				
				//delete old photo
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				
				File file1= new File(deleteFile, oldContactDetail.getImage());
				
				file1.delete();
				
				
				//update new photo
				
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			
			else
			{
				contact.setImage(oldContactDetail.getImage());
			}
			
			User user=this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", 
					new Message("Your Contact is updated..", "success"));
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
		System.out.println("Contact Name"+ contact.getName());
		System.out.println("Contact Name"+ contact.getcId());
		
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	//your profile handler
	
	@GetMapping("/profile")
	public String yourProfile(Model m)
	{
		m.addAttribute("title", "Profile Page");
		
		return "normal/profile";
	}
	
	
	//open settings handler
	
	@GetMapping("/settings")
	public String openSettings()
	{
		return "normal/settings";
	}
	
	//change password handler
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,
			Principal principal,HttpSession session)
	{
		System.out.println("OLD PASSWORD "+oldPassword);
		System.out.println("NEW PASSWORD "+newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println("user"+currentUser.getPassword());
		
		if(this.bcryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
			
		{
			//change the password...
			
			currentUser.setPassword(this.bcryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", 
					new Message("Your Password is updated..", "success"));
			
		}
		else
		{
			//error...
			session.setAttribute("message", 
					new Message("Wrong Old Password..", "danger"));
			return "redirect:/user/settings";
			
		}
		
		return "redirect:/user/index";
	}

}
