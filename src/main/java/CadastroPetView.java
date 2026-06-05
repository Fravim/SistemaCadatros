import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CadastroPetView {
    // --- COMPONENTES VINCULADOS AO DESIGNER (GUI FORMS) ---
    private JPanel mainPanel;
    private JLabel LblCadastroPet;
    private JLabel LblPet;
    private JLabel LblDono;
    private JLabel LblSexo;
    private JLabel LblPeso;
    private JLabel LblRaca;
    private JLabel TxtTipo;
    private JLabel TxtIdade;

    private JTextField TxtPet;
    private JComboBox<String> TxtDono; // Convertido para JComboBox para busca dinâmica
    private JComboBox<String> CmbSexo;
    private JSpinner SpinIdade;
    private JComboBox<String> CmbTipo;
    private JTextField TxtRaca;
    private JTextField TxtPeso;

    private JButton BtnVoltar;
    private JButton BtnSalvar;
    private JButton BtnLimpar;

    // Configurações do Banco de Dados para busca direta de donos
    private final String URL = "jdbc:mysql://localhost:3306/sistemacadastro";
    private final String USER = "root";
    private final String PASS = "";

    // Controle de estado: -1 = Novo Cadastro | > -1 = Edição
    private int idEdicao = -1;
    private List<String> listaDonosExistentes = new ArrayList<>();

    /**
     * CONSTRUTOR 1: Novo Pet
     */
    public CadastroPetView() {
        inicializarComponentes();
    }

    /**
     * CONSTRUTOR 2: Edição de Pet
     */
    public CadastroPetView(Pet pet) {
        inicializarComponentes();

        this.idEdicao = pet.getId();
        TxtPet.setText(pet.getNomePet());

        // Seta o dono atual no ComboBox editável
        TxtDono.setSelectedItem(pet.getNomeDono());

        CmbTipo.setSelectedItem(pet.getTipo());
        CmbSexo.setSelectedItem(pet.getSexo());
        SpinIdade.setValue(pet.getIdade());
        TxtPeso.setText(String.valueOf(pet.getPeso()));
        TxtRaca.setText(pet.getRaca());

        LblCadastroPet.setText("Editando Pet: " + pet.getNomePet());
        BtnSalvar.setText("Atualizar");
    }

    private void inicializarComponentes() {
        SpinIdade.setModel(new SpinnerNumberModel(0, 0, 100, 1));

        // Torna o ComboBox de donos totalmente editável (campo de texto + lista)
        TxtDono.setEditable(true);
        carregarSugestoesDonos();

        BtnSalvar.addActionListener(e -> salvarOuAtualizar());
        BtnLimpar.addActionListener(e -> limparFormulario());
        BtnVoltar.addActionListener(e -> SwingUtilities.getWindowAncestor(mainPanel).dispose());
    }

    /**
     * Busca os donos já cadastrados na tabela para preencher as sugestões
     */
    private void carregarSugestoesDonos() {
        TxtDono.removeAllItems();
        TxtDono.addItem(""); // Item padrão em branco
        listaDonosExistentes.clear();

        String sql = "SELECT DISTINCT nome_dono FROM pets WHERE nome_dono IS NOT NULL AND nome_dono != '' ORDER BY nome_dono";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String dono = rs.getString("nome_dono");
                TxtDono.addItem(dono);
                listaDonosExistentes.add(dono.toLowerCase().trim());
            }

        } catch (SQLException ex) {
            System.out.println("Erro ao carregar sugestões de donos: " + ex.getMessage());
        }
    }

    private void salvarOuAtualizar() {
        String nomePet = TxtPet.getText().trim();

        // Captura o texto digitado ou selecionado no JComboBox do Dono
        Object donoSelecionado = TxtDono.getEditor().getItem();
        String nomeDono = (donoSelecionado != null) ? donoSelecionado.toString().trim() : "";

        if (nomePet.isEmpty() || nomeDono.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Preencha o nome do pet e do dono.");
            return;
        }

        // Validação inteligente: verifica correspondência exata
        if (!listaDonosExistentes.contains(nomeDono.toLowerCase())) {
            int resposta = JOptionPane.showConfirmDialog(null,
                    "O dono '" + nomeDono + "' não foi localizado no sistema.\n" +
                            "Dica: Certifique-se de que a grafia está correta para manter o padrão do banco.\n\n" +
                            "Deseja cadastrar assim mesmo?",
                    "Aviso de Novo Proprietário",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (resposta != JOptionPane.YES_OPTION) {
                return; // Para o salvamento para o usuário corrigir o nome
            }
        }

        try {
            Pet p = new Pet();
            p.setNomePet(nomePet);
            p.setNomeDono(nomeDono);
            p.setTipo(CmbTipo.getSelectedItem().toString());
            p.setSexo(CmbSexo.getSelectedItem().toString());
            p.setIdade((int) SpinIdade.getValue());
            p.setRaca(TxtRaca.getText().trim());

            String pesoTexto = TxtPeso.getText().replace(",", ".");
            p.setPeso(pesoTexto.isEmpty() ? 0.0 : Double.parseDouble(pesoTexto));

            PetDAO dao = new PetDAO();

            if (this.idEdicao == -1) {
                dao.cadastrar(p);
            } else {
                p.setId(this.idEdicao);
                dao.atualizarCompleto(p);
            }

            SwingUtilities.getWindowAncestor(mainPanel).dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Por favor, insira um peso válido.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erro inesperado: " + ex.getMessage());
        }
    }

    private void limparFormulario() {
        TxtPet.setText("");
        TxtDono.setSelectedIndex(0);
        TxtDono.getEditor().setItem("");
        TxtPeso.setText("");
        TxtRaca.setText("");
        SpinIdade.setValue(0);
        CmbTipo.setSelectedIndex(0);
        CmbSexo.setSelectedIndex(0);
        TxtPet.requestFocus();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}