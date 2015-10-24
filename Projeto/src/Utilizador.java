import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Utilizador implements Serializable {
	String nome;
	String password;
	
	// hashmap que liga IDs dos projetos às doações do utilizador
	HashMap <Integer, Float> listaDoacoesUser = new HashMap<Integer, Float>();
	
	// lista de recompensas
	ArrayList <Integer> listaIDsRecompensas = new ArrayList <Integer> ();
	
	//lista de projetos criados
	ArrayList <Integer> listaIDsProjeto = new ArrayList <Integer>();
	
	//inbox
	
	float saldo;
	int id; // posição na lista de Utilizadores
	
	public Utilizador() {
		
	}
	
	public Utilizador(String nome, String password, float saldo) {
		this.nome = nome;
		this.password = password;
		this.saldo=saldo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public HashMap<Integer, Float> getListaDoacoesUser() {
		return listaDoacoesUser;
	}

	public void setListaDoacoesUser(HashMap<Integer, Float> listaDoacoesUser) {
		this.listaDoacoesUser = listaDoacoesUser;
	}

	public ArrayList<Integer> getListaIDsRecompensas() {
		return listaIDsRecompensas;
	}

	public void setListaIDsRecompensas(ArrayList<Integer> listaIDsRecompensas) {
		this.listaIDsRecompensas = listaIDsRecompensas;
	}

	public ArrayList<Integer> getListaIDsProjeto() {
		return listaIDsProjeto;
	}

	public void setListaIDsProjeto(ArrayList<Integer> listaIDsProjeto) {
		this.listaIDsProjeto = listaIDsProjeto;
	}

	public float getSaldo() {
		return saldo;
	}

	public void setSaldo(float saldo) {
		this.saldo = saldo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean contemDoacao(int projID) {
		return listaDoacoesUser.containsKey(projID);
	}
	
}
