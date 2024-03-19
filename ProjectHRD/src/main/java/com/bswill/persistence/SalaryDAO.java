package com.bswill.persistence;

import java.util.List;

import com.bswill.domain.SalaryCriteria;
import com.bswill.domain.EmployeeVO;
import com.bswill.domain.SalaryListVO;
import com.bswill.domain.SalaryVO;

public interface SalaryDAO {
	
	public List<EmployeeVO> empListSelect() throws Exception;
	
	public List<SalaryVO> salaryListSelect() throws Exception;
	
	public List<SalaryListVO> salarySeachSelect(SalaryCriteria cri) throws Exception;
	
}