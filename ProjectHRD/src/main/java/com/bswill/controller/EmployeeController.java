package com.bswill.controller;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import java.util.Calendar;

import javax.inject.Inject;

import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.bswill.domain.AppointmentListVO;
import com.bswill.domain.AppointmentVO;
import com.bswill.domain.EmployeeVO;
import com.bswill.domain.EventVO;
import com.bswill.domain.LicenseListVO;
import com.bswill.domain.LicenseVO;
import com.bswill.domain.NotificationVO;
import com.bswill.security.CustomNoopPasswordEncoder;
import com.bswill.service.AppointmentService;
import com.bswill.service.EmployeeService;
import com.bswill.service.EventService;
import com.bswill.service.LicenseService;

@Controller
@RequestMapping(value = "/emp/*")
public class EmployeeController {

	private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

	@Inject
	private EmployeeService eService;

	@Inject
	private LicenseService lService;

	@Inject
	private AppointmentService aService;

	@Inject
	private EventService vService;

	// http://localhost:8088/emp/registEmp
	@RequestMapping(value = "/registEmp", method = RequestMethod.GET)
	public void registEmpGET(Model model) throws Exception {
		logger.debug("registEmpGET() 호출");
	}

	@RequestMapping(value = "/emp/registEmp", method = RequestMethod.POST)
	public String registEmpPOST(EmployeeVO evo, @ModelAttribute(value = "LicenseListVO") LicenseListVO lList,
			@ModelAttribute(value = "AppointmentListVO") AppointmentListVO aList, @RequestParam("profile") MultipartFile profile, Model model)
			throws Exception {
		logger.debug("registEmpPOST() 호출");

		logger.debug("evo:" + evo);
		logger.debug("lList:" + lList);
		logger.debug("aList" + aList);
		logger.debug("profile: " + profile);

		// 입사일자에서 년도 추출
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(evo.getStart_date());
		int year = calendar.get(Calendar.YEAR);

		int empno = eService.countEmpNo(year);

		// employee_id = 입사년도 + 입사부서 + (해당년도 입사순번 + 100)
		String employee_id = "" + year + evo.getDEPTID() + empno;
		logger.debug("emp:" + employee_id);
		evo.setEmployee_id(Integer.parseInt(employee_id));

		// 최초 PASSWD = 생년월일(yyyyMMdd) + 입사일자(yyyyMMdd)
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String birthFormat = evo.getBirth().replaceAll("-", "");
		logger.debug("birth:" + evo.getBirth());
		String startFormat = sdf.format(evo.getStart_date());
		String PASSWD = birthFormat + startFormat;
		logger.debug("PASSWD:" + PASSWD);

		// 비밀번호 암호화(단방향)
		CustomNoopPasswordEncoder encoder = new CustomNoopPasswordEncoder();
		String password = encoder.encode(PASSWD);
		logger.debug("password:" + password);
		evo.setPASSWD(password);

		String profileName = saveProfile(evo.getEmployee_id(), profile);
		logger.debug("profileName:" + profileName);
		evo.setPROFIL(profileName);

		eService.registEmp(evo);

		logger.debug("lList" + lList.toString());

		if (!lList.toString().equals("LicenseListVO(licenseList=null)")) {
			for (LicenseVO lvo : lList.getLicenseList()) {
				if (lvo.getLicense() != null) {
					lvo.setEmployee_id(evo.getEmployee_id());
					lService.registLicense(lvo);
				}
			}
		}

		if (aList.toString().equals("AppointmentListVO(appointmentList=null")) {
			for (AppointmentVO avo : aList.getAppointmentList()) {
				if (avo.getApp_issue() != null) {
					avo.setEmployee_id(evo.getEmployee_id());
					aService.registAppointment(avo);
				}
			}
		}

		NotificationVO nvo = new NotificationVO();
		nvo.setEmployee_id(evo.getEmployee_id());
		nvo.setNoti_title(evo.getEmp_name() + "님 입사를 축하합니다.");

		vService.notifyModification(nvo);

		model.addAttribute("employee_id", evo.getEmployee_id());

		return "redirect:/emp/viewEmp";
	}

	private String saveProfile(int employee_id, MultipartFile profile) throws Exception {
		String uploadDir = "D://upload/profile/";
		String profileName = employee_id + "." + profile.getOriginalFilename().split("\\.")[1];
		String profilePath = uploadDir + profileName;

		try (OutputStream os = new FileOutputStream(profilePath)) {
			os.write(profile.getBytes());
		}

		return profileName;
	}

	// http://localhost:8088/emp/listEmp
	@RequestMapping(value = "listEmp", method = RequestMethod.GET)
	public void listEmpGET(Model model, HttpSession session) throws Exception {
		logger.debug("listEmpGET()");

		model.addAttribute("listEmp", eService.listEmp());

	}

	// http://localhost:8088/emp/viewEmp
	@RequestMapping(value = "viewEmp", method = RequestMethod.GET)
	public void viewEmpGET(Model model) throws Exception {
		logger.debug("viewEmpGET() 호출");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		int employee_id = Integer.parseInt(authentication.getName());

		logger.debug("viewEmpVO: " + eService.viewEmp(employee_id));
		logger.debug("viewEmpLicenseVO: " + lService.viewEmpLicense(employee_id));
		logger.debug("viewEmpAppointmentVO: " + aService.viewEmpAppointment(employee_id));

		model.addAttribute("viewEmpVO", eService.viewEmp(employee_id));
		model.addAttribute("viewEmpLicenseVO", lService.viewEmpLicense(employee_id));
		model.addAttribute("viewEmpAppointmentVO", aService.viewEmpAppointment(employee_id));
	}

	@RequestMapping(value = "viewEmp", method = RequestMethod.POST)
	public String viewEmpPOST(@RequestParam("emp_tel") String emp_tel, @RequestParam("emp_mail") String emp_mail, Model model) throws Exception {
		logger.debug("viewEmpPOST() 호출");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		int employee_id = Integer.parseInt(authentication.getName());

		eService.modifyEmpTelAndEmail(employee_id, emp_tel, emp_mail);

		return "redirect:/emp/viewEmp?employee_id=" + employee_id;
	}

	////////////////////////////////////////////////////////////////////////////////////
	// http://localhost:8088/emp/applyEvent
	@RequestMapping(value = "/applyEvent", method = RequestMethod.GET)
	public void applyEventGET(Model model) throws Exception {
		logger.debug("applyEventGET() 호출");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		int employee_id = Integer.parseInt(authentication.getName());

		logger.debug("employee_id: " + employee_id);

		model.addAttribute("employee_id", employee_id);
	}

	@RequestMapping(value = "/applyEvent", method = RequestMethod.POST)
	public String applyEventPOST(EventVO vvo) throws Exception {
		logger.debug("applyEventPOST() 호출");

		logger.debug("evo: " + vvo);

		vService.applyEvent(vvo);

		logger.debug("evo: " + vvo);

		return "redirect:/emp/viewEvent?&searchType=eve_class&keyword=";
	}

	// http://localhost:8088/emp/viewEvent?searchType=eve_class&keyword=
	@RequestMapping(value = "viewEvent", method = RequestMethod.GET)
	public void viewEventGET(@RequestParam(name = "searchType", required = false) String searchType,
			@RequestParam(name = "keyword", required = false) String keyword, Model model, HttpSession session) throws Exception {
		logger.debug("viewEventGET() 호출");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		int employee_id = Integer.parseInt(authentication.getName());

		logger.debug("employee_id: " + employee_id);

		if (searchType == null) {
			searchType = "''";
		}

		if (keyword == null) {
			keyword = "''";
		}

		logger.debug("searchType: " + searchType);
		logger.debug("keyword: " + keyword);

		model.addAttribute("listEmpEvent", vService.viewEmpEventList(employee_id, searchType, keyword));
	}

	// http://localhost:8088/emp/listEvent?searchType=eve_class&keyword=
	@RequestMapping(value = "/listEvent", method = RequestMethod.GET)
	public void listEventGET(Model model, @RequestParam(name = "searchType", required = false) String searchType,
			@RequestParam(name = "keyword", required = false) String keyword) throws Exception {
		logger.debug("listEventGET() 호출");

		if (searchType == null) {
			searchType = "''";
		}

		if (keyword == null) {
			keyword = "''";
		}

		model.addAttribute("listEvent", vService.listAllEvent(searchType, keyword));
	}

	@RequestMapping(value = "/viewEmpAccountInfo", method = RequestMethod.GET)
	public void viewEmpAccountInfoGET(Model model, @RequestParam("employee_id") int employee_id) throws Exception {
		logger.debug("viewEmpAccountInfoGET() 호출");

		logger.debug("employee_id: " + employee_id);
		logger.debug("employee_id: " + vService.viewEmpSalary(employee_id));

		model.addAttribute("viewSalary", vService.viewEmpSalary(employee_id));
	}

	@RequestMapping(value = "/approveEmpEvent", method = RequestMethod.POST)
	public String approveEmpEvent(EventVO vvo) throws Exception {
		logger.debug("approveEmpEvent() 호출");

		logger.debug("vvo:" + vvo);

		vService.modifyEventAuthApprove(vvo);

		NotificationVO nvo = new NotificationVO();
		nvo.setEmployee_id(vvo.getEmployee_id());
		nvo.setNoti_title("요청하신 " + vvo.getEve_subject() + "씨의 " + vvo.getEve_class() + " 경조비 신청이 승인되었습니다.");
		nvo.setNoti_link("http://localhost:8088/emp/viewEvent?searchType=eve_auth&keyword=%EC%8A%B9%EC%9D%B8");

		vService.notifyModification(nvo);

		return "redirect:/emp/listEvent?searchType=employee_id&keyword=";
	}

	@RequestMapping(value = "/rejectEmpEvent", method = RequestMethod.POST)
	public String rejectEmpEvent(EventVO vvo) throws Exception {
		logger.debug("rejectEmpEvent() 호출");

		logger.debug("vvo:" + vvo);

		vService.modifyEventAuthReject(vvo);

		NotificationVO nvo = new NotificationVO();
		nvo.setEmployee_id(vvo.getEmployee_id());
		nvo.setNoti_title("요청하신 " + vvo.getEve_subject() + "씨의 " + vvo.getEve_class() + " 경조비 신청이 거부되었습니다.");
		nvo.setNoti_link("http://localhost:8088/emp/viewEvent?searchType=eve_auth&keyword=%EA%B1%B0%EB%B6%80");

		vService.notifyModification(nvo);

		return "redirect:/emp/listEvent?searchType=employee_id&keyword=";
	}

}
