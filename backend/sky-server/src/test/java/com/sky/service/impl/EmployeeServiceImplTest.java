package com.sky.service.impl;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.mapper.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeMapper employeeMapper;

    @Test
    void upgradesLegacyPasswordAfterSuccessfulLogin() {
        Employee employee = Employee.builder()
                .id(7L)
                .username("tea-maker")
                .password(DigestUtils.md5DigestAsHex("old-password".getBytes(StandardCharsets.UTF_8)))
                .status(1)
                .build();
        when(employeeMapper.getByUsername("tea-maker")).thenReturn(employee);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper);

        EmployeeLoginDTO login = new EmployeeLoginDTO();
        login.setUsername("tea-maker");
        login.setPassword("old-password");
        assertEquals(employee, service.login(login));

        ArgumentCaptor<Employee> update = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper).update(update.capture());
        assertEquals(7L, update.getValue().getId());
        assertTrue(new BCryptPasswordEncoder().matches("old-password", update.getValue().getPassword()));
    }

    @Test
    void createsUniqueStyleInitialPasswordAndStoresOnlyItsHash() {
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper);
        EmployeeDTO employee = new EmployeeDTO();
        employee.setUsername("new-partner");
        employee.setName("新伙伴");

        String initialPassword = service.save(employee);

        ArgumentCaptor<Employee> inserted = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper).insert(inserted.capture());
        assertEquals(12, initialPassword.length());
        assertEquals(1, inserted.getValue().getStatus());
        assertTrue(new BCryptPasswordEncoder().matches(initialPassword, inserted.getValue().getPassword()));
    }
}
