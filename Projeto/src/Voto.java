import java.io.Serializable;
import java.util.ArrayList;

public class Voto implements Serializable {

	String nome;
	int contador=0;
	ArrayList <Integer> listaUsers = new ArrayList <Integer>();
	
	public Voto(String nome) {
		this.nome = nome;
	}
	
	public String imprime(int i) {
		return i+"-"+nome+"\n";
	}
	
}
