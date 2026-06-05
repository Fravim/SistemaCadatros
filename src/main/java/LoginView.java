import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;

public class LoginView {
    // --- COMPONENTES VINCULADOS AO DESIGNER (GUI FORMS) ---
    private JPanel mainPanel;
    private JLabel LblTitulo;
    private JTextField TxtUsuario;
    private JPasswordField TxtSenha;
    private JButton BtnEntrar;
    private JButton BtnNovoUsuario;
    private JButton BtnEsqueciSenha;

    // Configurações do Banco de Dados
    private final String URL = "jdbc:mysql://localhost:3306/sistemacadastro";
    private final String USER = "root";
    private final String PASS = "";

    // Senha mestra externa para permitir a criação de Super Usuários
    private static final String SENHA_MESTRA = "SENHAMESTRA123";

    /**
     * CONSTRUTOR: Configura os listeners dos botões com tratamento limpo
     */
    public LoginView() {
        // Evento do botão Entrar
        BtnEntrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                efetuarLogin();
            }
        });

        // Evento do botão Novo Usuário
        BtnNovoUsuario.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                criarNovoUsuario();
            }
        });

        // Evento do botão Esqueci a Senha
        BtnEsqueciSenha.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recuperarSenha();
            }
        });
    }

    /**
     * Autentica o usuário no banco de dados e gerencia a sessão
     */
    private void efetuarLogin() {
        String usuario = TxtUsuario.getText().trim();
        String senha = new String(TxtSenha.getPassword()).trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor, preencha todos os campos!");
            return;
        }

        String sql = "SELECT perfil FROM usuarios WHERE username = ? AND senha = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String perfil = rs.getString("perfil");

                    // Registra os dados do usuário na memória do SessionManager
                    SessionManager.login(usuario, perfil);

                    JOptionPane.showMessageDialog(null, "Bem-vindo, " + usuario + "!");
                    redirecionarParaConsulta();
                } else {
                    JOptionPane.showMessageDialog(null, "Usuário ou senha incorretos.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro de conexão com o banco: " + ex.getMessage());
        }
    }

    /**
     * Cria novos usuários controlando o nível de acesso (Padrão ou Super)
     */
    private void criarNovoUsuario() {
        String usuario = JOptionPane.showInputDialog(null, "Digite o nome do novo usuário:");
        if (usuario == null || usuario.trim().isEmpty()) return;

        String senha = JOptionPane.showInputDialog(null, "Digite a senha para a nova conta:");
        if (senha == null || senha.trim().isEmpty()) return;

        String[] perfis = {"PADRAO (Apenas Consulta)", "SUPER (CRUD Completo)"};
        int escolhaPerfil = JOptionPane.showOptionDialog(null, "Selecione o perfil do usuário:",
                "Perfil de Acesso", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, perfis, perfis[0]);

        String perfilSelecionado = (escolhaPerfil == 1) ? "SUPER" : "PADRAO";

        // Validação da senha mestra para criação de administradores
        if (perfilSelecionado.equals("SUPER")) {
            String confirmacaoMestra = JOptionPane.showInputDialog(null,
                    "Atenção: Digite a SENHA MESTRA para autorizar um Super Usuário:");

            if (confirmacaoMestra == null || !confirmacaoMestra.equals(SENHA_MESTRA)) {
                JOptionPane.showMessageDialog(null, "Senha Mestra incorreta! Operação cancelada.");
                return;
            }
        }

        String sql = "INSERT INTO usuarios (username, senha, perfil) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.trim());
            stmt.setString(2, senha.trim());
            stmt.setString(3, perfilSelecionado);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Usuário '" + usuario + "' criado com sucesso!");

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(null, "Este nome de usuário já existe no sistema.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar: " + ex.getMessage());
        }
    }

    /**
     * Altera a senha caso o nome de usuário seja correspondente
     */
    private void recuperarSenha() {
        String usuario = JOptionPane.showInputDialog(null, "Digite seu nome de usuário para verificação:");
        if (usuario == null || usuario.trim().isEmpty()) return;

        String sqlVerifica = "SELECT id FROM usuarios WHERE username = ?";
        String sqlUpdate = "UPDATE usuarios SET senha = ? WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmtVerifica = conn.prepareStatement(sqlVerifica)) {

            stmtVerifica.setString(1, usuario.trim());

            try (ResultSet rs = stmtVerifica.executeQuery()) {
                if (rs.next()) {
                    String novaSenha = JOptionPane.showInputDialog(null, "Usuário validado! Digite sua nova senha:");
                    if (novaSenha == null || novaSenha.trim().isEmpty()) return;

                    try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate)) {
                        stmtUpdate.setString(1, novaSenha.trim());
                        stmtUpdate.setString(2, usuario.trim());
                        stmtUpdate.executeUpdate();

                        JOptionPane.showMessageDialog(null, "Senha atualizada com sucesso!");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Usuário não localizado no banco de dados.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Erro no processo de recuperação: " + ex.getMessage());
        }
    }

    /**
     * Realiza a transição de telas e repassa o controle de segurança do encerramento
     */
    private void redirecionarParaConsulta() {
        JFrame loginFrame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);

        ConsultaPetView consultaView = new ConsultaPetView();
        JFrame mainFrame = new JFrame("Sistema de Cadastro de Pets - Dashboard");

        mainFrame.setContentPane(consultaView.getPanelPrincipal());
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                String[] opcoes = {"Apenas Fechar", "Fazer Logoff e Sair", "Cancelar"};
                int escolha = JOptionPane.showOptionDialog(mainFrame,
                        "Você deseja deslogar do sistema antes de fechar o aplicativo?",
                        "Encerrar Aplicação", JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, opcoes, opcoes[0]);

                if (escolha == 0) {
                    System.exit(0);
                } else if (escolha == 1) {
                    SessionManager.logout();
                    System.exit(0);
                }
            }
        });

        mainFrame.pack();
        mainFrame.setSize(900, 600);
        mainFrame.setLocationRelativeTo(null);

        if (loginFrame != null) {
            loginFrame.dispose();
        }
        mainFrame.setVisible(true);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}