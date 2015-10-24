import java.util.ArrayList;

public class BaseDeDados {

	public static ArrayList <Utilizador> listaUtilizadores = new ArrayList <Utilizador>();
	public static ArrayList <Projeto> listaProjetos = new ArrayList <Projeto>();
	
	public BaseDeDados() {
		
	}

	public static ArrayList<Utilizador> getListaUtilizadores() {
		return listaUtilizadores;
	}

	public static void setListaUtilizadores(ArrayList<Utilizador> listaUtilizadores) {
		BaseDeDados.listaUtilizadores = listaUtilizadores;
	}

	public static ArrayList<Projeto> getListaProjetos() {
		return listaProjetos;
	}

	public static void setListaProjetos(ArrayList<Projeto> listaProjetos) {
		BaseDeDados.listaProjetos = listaProjetos;
	}
	
}
