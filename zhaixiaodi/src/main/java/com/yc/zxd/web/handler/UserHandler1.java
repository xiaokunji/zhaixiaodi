package com.yc.zxd.web.handler;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.yc.zxd.entity.Zusers;
import com.yc.zxd.service.UserService;
import com.yc.zxd.service.impl.UserServiceImpl;

@Controller // 标识为控制器
@RequestMapping("/user") // 请求处理的前缀,可以过滤处理 (相当于servlet时的注解类似)
public class UserHandler1 {

	@Autowired
	private UserService userService;

	@Autowired
	private Zusers zusers;

	@Autowired
	private JavaMailSender mailSender;

	private String code = "";

	public UserHandler1() {
		userService = new UserServiceImpl();
	}

	@RequestMapping("/check") // 检查用户是否存在
	@ResponseBody
	public boolean check(String tel) {
		boolean result = false;
		LogManager.getLogger().debug("进入UserHandler 处理isExistUN,检查用户是否存在,username:" + tel);
		result = userService.isExistUN(tel);// 检查用户是否存在
		return result;
	}

	@RequestMapping("/verifyCode") // 检查用户是否存在
	@ResponseBody
	public boolean verifyCode(String code, ServletRequest request) {
		boolean result = false;
		HttpSession session = ((HttpServletRequest) request).getSession();
		String realCode = (String) session.getAttribute("rand"); // 取到原始验证码
		LogManager.getLogger().debug("进入UserHandler 处理verifyCode,检查验证码是否正确,code:" + code + "\t真的验证码:" + realCode);
		if (realCode != null && code != null) {
			if (realCode.intern() == code.intern()) {
				result = true;
			}
		}
		return result;
	}

	@RequestMapping("/editPwd") // 修改密码
	public String editPwdByTel(String tel, String password, ServletRequest request) {
		boolean result = false;
		LogManager.getLogger().debug("进入UserHandler 处理editPwd,检查验证码是否正确,tel:" + tel + "\t密码:" + password);
		if(tel.length()>0 && password.length()>0){			
			result=userService.editPwdByTel(tel,password);
		}else{
			result=false;
		}
		if(result){
			return "redirect:/page/Login.jsp";
		}else{
			return "redirect:/htm/findPwd3.html?tel="+tel;
		}
		
	}

	@RequestMapping("/getMail") // 得到邮箱
	@ResponseBody
	public String getUser(String tel) {
		String email="";
		LogManager.getLogger().debug("进入UserHandler 处理getMail,根据电话得到邮箱,tel:" + tel);
		if (tel != null) {
			email = userService.getMail(tel);
		}

		return new Gson().toJson(email);
	}

	@RequestMapping("/sendMail") // 发送邮件
	@ResponseBody
	public String sendMail( String email, HttpServletRequest request) {
		LogManager.getLogger().debug("请求UserHandler 进行sendMail操作");
		LogManager.getLogger().debug("请求数据  email:" + email);
		if (email == null) {
			LogManager.getLogger().debug(" 要发送的邮件为空 ");
			return "";
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

			helper.setFrom("13237343452@163.com");
			helper.setTo(email);
			helper.setSubject("找回密码");
//			String hrefStr = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
//					+ request.getContextPath() + "/user/getpassword?username=" + username;
			// String hrefStr = request.getRequestURL()+
			// "user/getpassword?username=" + username;
			code = new ImageCodeServelt().verifyCode(request);
			helper.setText("宅小递的验证码:" + code, true);
			System.out.println("真的验证码:"+code);
//			mailSender.send(message);

		} catch (MessagingException e) {
			e.printStackTrace();
			return new Gson().toJson("false");
		}

		return new Gson().toJson("true");
	}

}
