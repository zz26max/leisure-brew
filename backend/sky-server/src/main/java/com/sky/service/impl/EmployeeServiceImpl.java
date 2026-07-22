package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final String PASSWORD_ALPHABET =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int INITIAL_PASSWORD_LENGTH = 12;

    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public EmployeeServiceImpl(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public Employee login(EmployeeLoginDTO loginDTO) {
        Employee employee = employeeMapper.getByUsername(loginDTO.getUsername());
        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!passwordMatches(loginDTO.getPassword(), employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        if (employee.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        upgradeLegacyPassword(employee, loginDTO.getPassword());
        return employee;
    }

    @Override
    public String save(EmployeeDTO employeeDTO) {
        String initialPassword = newInitialPassword();
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(passwordEncoder.encode(initialPassword));
        employeeMapper.insert(employee);
        return initialPassword;
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO query) {
        PageHelper.startPage(query.getPage(), query.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(query);
        List<Employee> records = page.getResult();
        records.forEach(employee -> employee.setPassword(null));
        return new PageResult(page.getTotal(), records);
    }

    @Override
    public void startOrstop(Integer status, Long id) {
        employeeMapper.update(Employee.builder().status(status).id(id).build());
    }

    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        if (employee != null) {
            employee.setPassword(null);
        }
        return employee;
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employeeMapper.update(employee);
    }

    @Override
    public void editPassword(PasswordEditDTO passwordEditDTO) {
        Long employeeId = BaseContext.getCurrentId();
        Employee employee = employeeMapper.getById(employeeId);
        if (employee == null
                || !passwordMatches(passwordEditDTO.getOldPassword(), employee.getPassword())) {
            throw new PasswordEditFailedException("原密码不正确");
        }
        String newPassword = passwordEditDTO.getNewPassword();
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 64) {
            throw new PasswordEditFailedException("新密码需为 8 到 64 位");
        }
        employeeMapper.update(Employee.builder()
                .id(employeeId)
                .password(passwordEncoder.encode(newPassword))
                .build());
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        String legacyHash = DigestUtils.md5DigestAsHex(
                rawPassword.getBytes(StandardCharsets.UTF_8));
        return legacyHash.equalsIgnoreCase(storedPassword);
    }

    private void upgradeLegacyPassword(Employee employee, String rawPassword) {
        if (!employee.getPassword().startsWith("$2")) {
            employeeMapper.update(Employee.builder()
                    .id(employee.getId())
                    .password(passwordEncoder.encode(rawPassword))
                    .build());
        }
    }

    private String newInitialPassword() {
        StringBuilder password = new StringBuilder(INITIAL_PASSWORD_LENGTH);
        for (int index = 0; index < INITIAL_PASSWORD_LENGTH; index++) {
            password.append(PASSWORD_ALPHABET.charAt(
                    secureRandom.nextInt(PASSWORD_ALPHABET.length())));
        }
        return password.toString();
    }
}
