package br.com.alura.codechella.user;

import br.com.alura.codechella.security.JwtUtil;
import br.com.alura.codechella.user.dto.LoginRequest;
import br.com.alura.codechella.user.dto.TokenResponse;
import br.com.alura.codechella.user.dto.RegistroUsuarioDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Mono<User> register(RegistroUsuarioDto dto) {
        return userRepository.findByEmail(dto.email())
                .flatMap(u -> Mono.<User>error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado")))
                .switchIfEmpty(Mono.defer(() -> {
                    User user = new User();
                    user.setEmail(dto.email());
                    user.setPasswordHash(passwordEncoder.encode(dto.senha()));
                    user.setRole("USER");
                    return userRepository.save(user);
                }));
    }

    public Mono<TokenResponse> login(LoginRequest dto) {
        return userRepository.findByEmail(dto.email())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(dto.senha(), user.getPasswordHash())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));
                    }
                    String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                    return Mono.just(new TokenResponse(token));
                });
    }
}