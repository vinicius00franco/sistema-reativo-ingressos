package br.com.alura.codechella.user;

import br.com.alura.codechella.user.dto.LoginRequest;
import br.com.alura.codechella.user.dto.RegistroUsuarioDto;
import br.com.alura.codechella.user.dto.TokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<User> register(@RequestBody RegistroUsuarioDto dto) {
        return service.register(dto);
    }

    @PostMapping("/login")
    public Mono<TokenResponse> login(@RequestBody LoginRequest dto) {
        return service.login(dto);
    }
}