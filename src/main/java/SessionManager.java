public class SessionManager {
    // Guarda o nome do usuário que acabou de fazer login
    private static String usuarioLogado = null;
    // Guarda o perfil do usuário ('SUPER' ou 'PADRAO')
    private static String perfilLogado = null;

    // Registra a sessão quando o login dá certo
    public static void login(String username, String perfil) {
        usuarioLogado = username;
        perfilLogado = perfil;
    }

    // Limpa a sessão no logoff
    public static void logout() {
        usuarioLogado = null;
        perfilLogado = null;
    }

    // Getters para o sistema saber quem está logado e o que ele pode fazer
    public static String getUsuarioLogado() {
        return usuarioLogado;
    }

    public static String getPerfilLogado() {
        return perfilLogado;
    }

    // Verifica se existe alguém conectado
    public static boolean isLogado() {
        return usuarioLogado != null;
    }
} 