import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultaPetView {
    // Componentes vinculados ao arquivo .form
    private JPanel panel1;
    private JTable table1;
    private JButton BtnCadastrar;
    private JButton BtnDeletar;
    private JButton BtnEditar;
    private JButton BtnLogoff;

    // Novos componentes de controle e filtro
    private JLabel LblSessao;
    private JLabel LblContador;
    private JComboBox<String> CmbFiltroTipo;
    private JComboBox<String> CmbFiltroSexo;
    private JSpinner SpinFiltroIdade;

    // Configurações locais de banco de dados
    private final String URL = "jdbc:mysql://localhost:3306/sistemacadastro";
    private final String USER = "root";
    private final String PASS = "";

    public ConsultaPetView() {
        // 1. Configura as informações da sessão ativa pós-login
        String usuarioLogado = SessionManager.getUsuarioLogado();
        String perfilLogado = SessionManager.getPerfilLogado();

        LblSessao.setText("Usuário ativo: " + usuarioLogado + " (" + perfilLogado + ")");

        // 2. CONTROLE DE ACESSO (RBAC): Oculta componentes se for usuário PADRAO
        if ("PADRAO".equals(perfilLogado)) {
            BtnCadastrar.setVisible(false);
            BtnEditar.setVisible(false);
            BtnDeletar.setVisible(false);
        }

        // 3. Inicializa os filtros e carrega a tabela customizada
        SpinFiltroIdade.setModel(new SpinnerNumberModel(100, 0, 100, 1)); // Valor padrão máximo
        configurarTabela();

        // Listeners para ativação dos filtros dinâmicos
        CmbFiltroTipo.addActionListener(e -> configurarTabela());
        CmbFiltroSexo.addActionListener(e -> configurarTabela());
        SpinFiltroIdade.addChangeListener(e -> configurarTabela());

        // Eventos dos botões operacionais
        BtnCadastrar.addActionListener(e -> abrirTelaCadastro());
        BtnEditar.addActionListener(e -> prepararEdicao());
        BtnDeletar.addActionListener(e -> executarExclusao());

        // Evento do botão de Logoff exposto na interface
        BtnLogoff.addActionListener(e -> efetuarLogoff());
    }

    /**
     * Busca dados aplicando regras de escopo por papel e filtros da tela
     */
    public void configurarTabela() {
        // Definição exata da ordem dos cabeçalhos solicitados
        String[] colunas = {"ID", "Nome", "Nome do Dono", "Tipo de Pet", "Raça", "Idade"};

        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Impede edição direta nas células
            }
        };

        // Captura os valores ativos nos filtros visuais
        String filtroTipo = CmbFiltroTipo.getSelectedItem().toString();
        String filtroSexo = CmbFiltroSexo.getSelectedItem().toString();
        int filtroIdadeMax = (int) SpinFiltroIdade.getValue();

        // Início da query base
        StringBuilder sql = new StringBuilder("SELECT * FROM pets WHERE idade_aproximada <= ?");

        // Se for usuário PADRAO, restringe a consulta apenas aos registros vinculados a ele por String/Dono
        if ("PADRAO".equals(SessionManager.getPerfilLogado())) {
            sql.append(" AND LOWER(nome_dono) = LOWER(?)");
        }

        if (!filtroTipo.equals("Todos os Tipos")) {
            sql.append(" AND tipo_pet = ?");
        }

        if (!filtroSexo.equals("Todos os Sexos")) {
            sql.append(" AND sexo = ?");
        }

        int contadorRegistros = 0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int parametroIndex = 1;
            stmt.setInt(parametroIndex++, filtroIdadeMax);

            if ("PADRAO".equals(SessionManager.getPerfilLogado())) {
                stmt.setString(parametroIndex++, SessionManager.getUsuarioLogado());
            }

            if (!filtroTipo.equals("Todos os Tipos")) {
                stmt.setString(parametroIndex++, filtroTipo);
            }

            if (!filtroSexo.equals("Todos os Sexos")) {
                stmt.setString(parametroIndex++, filtroSexo);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] linha = {
                            rs.getInt("id"),
                            rs.getString("nome_sobrenome_pet"),
                            rs.getString("nome_dono"),
                            rs.getString("tipo_pet"),
                            rs.getString("raca"),
                            rs.getInt("idade_aproximada")
                    };
                    model.addRow(linha);
                    contadorRegistros++;
                }
            }

            table1.setModel(model);
            LblContador.setText("Total de Pets listados: " + contadorRegistros);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao carregar os dados: " + e.getMessage());
        }
    }

    private void abrirTelaCadastro() {
        CadastroPetView cadastro = new CadastroPetView();
        exibirJanelaFormulario(cadastro, "Cadastrar Novo Pet");
    }

    private void prepararEdicao() {
        int row = table1.getSelectedRow();
        if (row != -1) {
            int id = Integer.parseInt(table1.getValueAt(row, 0).toString());
            Pet petParaEditar = new PetDAO().buscarPorId(id);

            if (petParaEditar != null) {
                CadastroPetView telaEdicao = new CadastroPetView(petParaEditar);
                exibirJanelaFormulario(telaEdicao, "Modificar Registro de Pet");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Por favor, selecione uma linha para efetuar a alteração.");
        }
    }

    private void executarExclusao() {
        int row = table1.getSelectedRow();
        if (row != -1) {
            int id = Integer.parseInt(table1.getValueAt(row, 0).toString());
            String nomePet = table1.getValueAt(row, 1).toString();

            int confirmacao = JOptionPane.showConfirmDialog(null,
                    "Confirmar a remoção definitiva do registro de: " + nomePet + "?",
                    "Aviso do Sistema", JOptionPane.YES_NO_OPTION);

            if (confirmacao == JOptionPane.YES_OPTION) {
                new PetDAO().deletar(id);
                configurarTabela();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Por favor, selecione um registro na tabela para remover.");
        }
    }

    private void exibirJanelaFormulario(CadastroPetView view, String titulo) {
        JFrame frameForm = new JFrame(titulo);
        frameForm.setContentPane(view.getMainPanel());
        frameForm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameForm.pack();
        frameForm.setLocationRelativeTo(null);
        frameForm.setVisible(true);

        frameForm.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                configurarTabela();
            }
        });
    }

    /**
     * Zera o gerenciamento de sessão e joga o fluxo de volta para a LoginView
     */
    private void efetuarLogoff() {
        int confirmacao = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja encerrar sua sessão?", "Logoff do Sistema",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            SessionManager.logout(); // Destrói o login ativo [cite: 30]

            // Destrói o painel de consulta atual
            JFrame dashboardFrame = (JFrame) SwingUtilities.getWindowAncestor(panel1);
            dashboardFrame.dispose();

            // Recarrega de forma limpa a tela de Login
            JFrame frameLogin = new JFrame("Módulo de Acesso - Segurança de Sistema");
            frameLogin.setContentPane(new LoginView().getMainPanel());
            frameLogin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frameLogin.pack();
            frameLogin.setSize(450, 350);
            frameLogin.setLocationRelativeTo(null);
            frameLogin.setVisible(true);
        }
    }

    public JPanel getPanelPrincipal() {
        return panel1;
    }
}