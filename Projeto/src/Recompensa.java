import java.io.Serializable;

public class Recompensa implements Serializable {
	
	String nome;
	float valor;
	Projeto proj;
	int id;
	Utilizador user;
	// data estimada
	public Recompensa(Projeto proj, String nome, float valor, int id) {
		this.nome = nome;
		this.valor = valor;
		this.proj = proj;
		this.id = id;
		/* id é sempre o indice em que está na lista de recompensas do projeto */
	}
	
	
	public String imprimeRecompensa(int i) {
		return (i+"-"+"Recompensa: "+this.nome+"\nValor: "+this.valor+"id: "+id+"\n");
	}
}
