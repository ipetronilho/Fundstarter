import java.rmi.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

public interface InterfaceRMI extends Remote {

	/* CONSULTAR DADOS */
	public String listaProjetosActuais() throws RemoteException;
	public String listaProjetosAntigos() throws RemoteException;
	
	/* LOGIN */
	public int registaConta(String nomeUser, String password) throws RemoteException;
	public int verificaLogin(String nomeUser, String password) throws RemoteException;
	
	/* CONSULTA */
	public String consultarSaldo(int userID) throws RemoteException;
	public String consultarProjetos(int userID) throws RemoteException;
	public String imprimeDoacoesUser(int userID) throws RemoteException;
	public String consultarRecompensas(int userID) throws RemoteException;
	public int procuraProjeto(String nome) throws RemoteException;
	public String procuraRecompensa(Projeto proj, int userID) throws RemoteException;
	public String listaRecompensas(int projID) throws RemoteException;
	public String imprimeDetalhesProjeto(int projID) throws RemoteException;
	
	/* OPERAÇÕES DE PROJETOS */
	public void criaProjeto(int userID, String nome, float valor_objetivo, Calendar dataInicial, Calendar dataFinal)throws RemoteException;
	public boolean validaDoacao(int userID, float dinheiro);
	public void doarDinheiro(int userID, int ProjID, float dinheiro) throws RemoteException;
	public void eliminaProjeto(int userID, int projID) throws RemoteException;
	
	/* OPERAÇÕES DE RECOMPENSAS */
	public void addRecompensa(int userID, int projID, String nome, float valor) throws RemoteException;
	public boolean escolherRecompensa(int userID, int projID, float dinheiro, int indexRecompensa) throws RemoteException;
	public void removeRecompensa(int userID, int projID, String nome) throws RemoteException;
	
	public int verificaAdministrador(int userID, int projID) throws RemoteException;

	// arranjar forma de responder aos backers
	// fim do projeto
}
