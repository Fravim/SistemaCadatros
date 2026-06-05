import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Define o Look and Feel do sistema operacional para a tela ficar mais moderna
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Inicia a aplicação na thread de eventos do Swing
        SwingUtilities.invokeLater(() -> {
            inicializarSistema();
        });
    }

    private static void inicializarSistema() {
        // 1. Instanciamos a nova classe de Login que servirá como porta de entrada
        LoginView loginView = new LoginView();

        // 2. Criamos a janela principal do sistema
        JFrame frame = new JFrame("Módulo de Acesso - Segurança de Sistema");

        // 3. Definimos o painel da tela de login como o conteúdo inicial da janela
        frame.setContentPane(loginView.getMainPanel()); // Certifique-se de criar o método getMainPanel() na sua LoginView

        // 4. CONFIGURAÇÃO CRÍTICA DE FECHAMENTO: Dizemos para o Swing não fechar a janela sozinho
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // 5. Tratamento do evento de fechamento ("X" da janela)
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                // Criamos as opções personalizadas para a caixa de diálogo
                String[] opcoes = {"Apenas Fechar", "Fazer Logoff e Sair", "Cancelar"};

                int escolha = JOptionPane.showOptionDialog(
                        frame,
                        "Você deseja deslogar do sistema antes de fechar o aplicativo?",
                        "Encerrar Aplicação",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opcoes,
                        opcoes[0]
                );

                if (escolha == 0) {
                    // Usuário escolheu apenas fechar (permanece conectado na lógica de sessão)
                    System.exit(0);
                } else if (escolha == 1) {
                    // Usuário escolheu fazer logoff explicitamente antes de sair
                    SessionManager.logout();
                    System.exit(0);
                }
                // Se escolher 'Cancelar' (escolha == 2) ou fechar o diálogo, o programa continua aberto
            }
        });

        // 6. Configurações visuais da janela
        frame.pack();
        frame.setSize(450, 350); // Um tamanho menor e mais adequado para uma tela de Login
        frame.setLocationRelativeTo(null); // Centraliza a tela de login no monitor
        frame.setVisible(true); // Exibe a tela de login
    }
}