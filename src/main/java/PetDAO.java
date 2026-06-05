import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class PetDAO {

    private final String URL = "jdbc:mysql://localhost:3306/sistemacadastro";
    private final String USER = "root";
    private final String PASS = "";

    /**
     * INSERT: Salva um novo Pet no banco de dados
     */
    public void cadastrar(Pet pet) {
        String sql = "INSERT INTO pets (nome_sobrenome_pet, nome_dono, tipo_pet, sexo, idade_aproximada, peso_aproximado, raca) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pet.getNomePet());
            stmt.setString(2, pet.getNomeDono());
            stmt.setString(3, pet.getTipo());
            stmt.setString(4, pet.getSexo());
            stmt.setInt(5, pet.getIdade());
            stmt.setDouble(6, pet.getPeso());
            stmt.setString(7, pet.getRaca());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Pet cadastrado com sucesso!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao cadastrar: " + e.getMessage());
        }
    }

    /**
     * SELECT ALL: Busca todos os registros para a tabela
     */
    public List<Pet> listarTodos() {
        String sql = "SELECT * FROM pets";
        List<Pet> lista = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Pet p = extrairPetDoResultSet(rs);
                lista.add(p);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar: " + e.getMessage());
        }
        return lista;
    }

    /**
     * NOVO - SELECT BY ID: Busca um pet específico para preencher a tela de edição
     */
    public Pet buscarPorId(int id) {
        String sql = "SELECT * FROM pets WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extrairPetDoResultSet(rs);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao buscar pet: " + e.getMessage());
        }
        return null;
    }

    /**
     * NOVO - UPDATE COMPLETO: Atualiza todos os campos do pet de uma vez
     */
    public void atualizarCompleto(Pet pet) {
        String sql = "UPDATE pets SET nome_sobrenome_pet = ?, nome_dono = ?, tipo_pet = ?, " +
                "sexo = ?, idade_aproximada = ?, peso_aproximado = ?, raca = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pet.getNomePet());
            stmt.setString(2, pet.getNomeDono());
            stmt.setString(3, pet.getTipo());
            stmt.setString(4, pet.getSexo());
            stmt.setInt(5, pet.getIdade());
            stmt.setDouble(6, pet.getPeso());
            stmt.setString(7, pet.getRaca());
            stmt.setInt(8, pet.getId()); // O ID vai no WHERE

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Pet atualizado com sucesso!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + e.getMessage());
        }
    }

    /**
     * DELETE: Remove um pet pelo ID
     */
    public void deletar(int id) {
        String sql = "DELETE FROM pets WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Removido com sucesso!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Erro ao deletar: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para evitar repetição de código (Refatoração)
     */
    private Pet extrairPetDoResultSet(ResultSet rs) throws SQLException {
        Pet p = new Pet();
        p.setId(rs.getInt("id"));
        p.setNomePet(rs.getString("nome_sobrenome_pet"));
        p.setNomeDono(rs.getString("nome_dono"));
        p.setTipo(rs.getString("tipo_pet"));
        p.setSexo(rs.getString("sexo"));
        p.setIdade(rs.getInt("idade_aproximada"));
        p.setPeso(rs.getDouble("peso_aproximado"));
        p.setRaca(rs.getString("raca"));
        return p;
    }
}