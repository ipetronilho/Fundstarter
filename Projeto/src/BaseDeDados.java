import java.util.ArrayList;

public class BaseDeDados {

	public ArrayList <Utilizador> listaUtilizadores = new ArrayList <Utilizador>();
	public ArrayList <Projeto> listaProjetos = new ArrayList <Projeto>();
	
	public BaseDeDados() {
		
	}

	
	
	public ArrayList<Utilizador> getListaUtilizadores() {
		return listaUtilizadores;
	}

	public void setListaUtilizadores(ArrayList<Utilizador> listaUtilizadores) {
		this.listaUtilizadores = listaUtilizadores;
	}

	public ArrayList<Projeto> getListaProjetos() {
		return listaProjetos;
	}

	public void setListaProjetos(ArrayList<Projeto> listaProjetos) {
		this.listaProjetos = listaProjetos;
	}
	
}
