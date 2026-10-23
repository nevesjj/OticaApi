package api.otica.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

/** Acesso ao usuário autenticado da requisição atual. */
public final class UsuarioLogado {

    private UsuarioLogado() {
    }

    public static String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "sistema";
    }

    public static String nome() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt && jwt.getClaimAsString("nome") != null) {
            return jwt.getClaimAsString("nome");
        }
        return login();
    }
}
