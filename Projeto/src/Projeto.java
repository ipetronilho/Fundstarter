import java.io.Serializable;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Projeto implements Serializable {
	
	// todo: valor_objetivo2 em que se passar tem de ter uma extra recompensa
	ArrayList <Mensagem> inboxProjeto = new ArrayList <Mensagem>();

	ArrayList <Recompensa> listaRecompensas = new ArrayList <Recompensa>();
	ArrayList <Voto> listaVotos = new ArrayList <Voto>();
	// implementar hashmap
	AtomicInteger contadorIDRecompensa = new AtomicInteger(0);
	
	Utilizador admin;
	float valor_objetivo;
	String descricao;
	String nome;
	float percentagem;
	
	Calendar dataInicial = new GregorianCalendar();
	Calendar dataFinal = new GregorianCalendar();
	// Calendar user = new GregorianCalendar(2012, Calendar.MAY, 17);
	int id;
	
	// hashmap que liga IDs dos utilizadores backers às doações
	HashMap <Integer, Float> listaDoacoes = new HashMap<Integer, Float>();
	
	//inbox que liga os IDs dos users às mensagens que eles escrevem
	 
	
	
	public Projeto(Utilizador admin, String nome, float valor_objetivo, int id, Calendar dataInicial, Calendar dataFinal) {
		this.admin=admin;
		this.nome=nome;
		this.valor_objetivo=valor_objetivo;
		this.id=id;
		this.dataInicial=dataInicial;
		this.dataFinal=dataFinal;
		//System.out.println("Dia do mes: "+dataInicial.get(dataInicial.DAY_OF_MONTH)); - check!
		// pedir o gregorianCalendar e depois verificar validade
	}
	
	// devolve 1 se for actual e 0 se for antigo
	public boolean verificaValidade() {
		Calendar diadeHoje= new GregorianCalendar();
		return (dataFinal.compareTo(diadeHoje)>=0);	// true: actual. false: antigo
	}

	
	public String imprime() {
		DecimalFormat df = new DecimalFormat("#.##");
		float valor_recolhido = getValorRecolhido();
		percentagem = valor_recolhido/valor_objetivo*100;
		return "Projeto: "+this.nome+"\nValor Recolhido:"+valor_recolhido+"("+df.format(percentagem)+"%)"+"\nValor objetivo: "+valor_objetivo+"Id: "+id+"\n\n";
	}
	
	public float getValorRecolhido() {
		Set set = listaDoacoes.entrySet();
		Iterator i= set.iterator();
		float totalRecolhido=0;
		while (i.hasNext()) {
			Map.Entry mentry = (Map.Entry)i.next();
			System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
	        System.out.println(mentry.getValue());
	        totalRecolhido += (float) mentry.getValue();
		}
		return totalRecolhido;
	}

	
	public String imprimeDetalhes(Projeto proj) {
		String str="";
		str=str.concat("Id:"+this.id+"Projeto: "+this.nome+"\nDescricao:"+this.descricao+"\nValor objetivo: "+valor_objetivo+"\n");
		str = str.concat(imprimeRecompensas(proj));
		return str;
	}
	
	public String imprimeRecompensas(Projeto proj) {
		int i;
		String str="";
		for (i=0;i<proj.listaRecompensas.size();i++) {
			str=str.concat(proj.listaRecompensas.get(i).imprimeRecompensa(i));
		}
		return str;
	}
	
	public void addRecompensa(Projeto proj, Recompensa rec) {
		proj.listaRecompensas.add(rec);
	}
	
	

}

