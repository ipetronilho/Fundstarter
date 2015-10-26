import java.util.ArrayList;
import java.util.HashMap;

public class Mensagem {
	int idUser;
	int idProjeto;
	String comentario;
	
	public Mensagem(int idUser, int idProjeto, String mensagem) {
		this.idUser=idUser;
		this.idProjeto=idProjeto;
		this.comentario=mensagem;
	}
}
