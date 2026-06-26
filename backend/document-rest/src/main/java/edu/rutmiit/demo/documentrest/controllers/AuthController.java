package edu.rutmiit.demo.documentrest.controllers;

import edu.rutmiit.demo.documentsapicontract.dto.AuthResponse;
import edu.rutmiit.demo.documentsapicontract.dto.EmployeeResponse;
import edu.rutmiit.demo.documentsapicontract.dto.LoginRequest;
import edu.rutmiit.demo.documentrest.service.AuthService;
import edu.rutmiit.demo.documentrest.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final EmployeeService employeeService;

    public AuthController(AuthService authService, EmployeeService employeeService) {
        this.authService = authService;
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.login(), request.password());
    }

    @GetMapping("/me")
    public EmployeeResponse me(@RequestHeader("X-User-Id") Long userId) {
        return authService.requireUser(userId);
    }

    @GetMapping("/employees")
    public List<EmployeeResponse> employees(@RequestHeader("X-User-Id") Long userId) {
        authService.requireUser(userId);
        return employeeService.findAll(0, 100).content();
    }
}
