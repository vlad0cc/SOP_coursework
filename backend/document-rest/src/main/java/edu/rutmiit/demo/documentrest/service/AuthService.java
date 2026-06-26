package edu.rutmiit.demo.documentrest.service;

import edu.rutmiit.demo.documentsapicontract.dto.AuthResponse;
import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;
import edu.rutmiit.demo.documentrest.storage.InMemoryStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {
    private final InMemoryStorage storage;

    public AuthService(InMemoryStorage storage) {
        this.storage = storage;
    }

    public AuthResponse login(String login, String password) {
        String expectedPassword = storage.passwords.get(login);
        if (expectedPassword == null || !expectedPassword.equals(password)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Неверный логин или пароль");
        }
        Long employeeId = storage.userEmployees.get(login);
        EmployeeResponse employee = storage.employees.get(employeeId);
        return new AuthResponse(employee.getId(), login, employee.getFullName(), employee.getPosition());
    }

    public EmployeeResponse requireUser(Long userId) {
        EmployeeResponse employee = userId == null ? null : storage.employees.get(userId);
        if (employee == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Требуется авторизация");
        }
        return employee;
    }
}
