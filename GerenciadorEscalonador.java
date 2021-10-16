import java.io.IOException;
import java.lang.Runnable;
import java.util.List;
import java.util.*;

public class GerenciadorEscalonador {

	//////////////////////////////////////////////
	//             ATRIBUTOS CLASSE             //
	//////////////////////////////////////////////	

	private int quantum, instrucoesExecutadas, trocas, totalProgramas;
	private Instrucoes instrucao = null;
	private String log;
	
	
	//Instancia de List com objetos do tipo Processos
  protected List<BlocoDeControleDeProcessos> listaDeProntos = new ArrayList<>();
  protected List<BlocoDeControleDeProcessos> listaDeBloqueados = new ArrayList<>();

  //A tabela de processos possui um ponteiro para o BCP.
  protected List<BlocoDeControleDeProcessos> tabelaDeProcessos = new ArrayList<>();


	
	//////////////////////////////////////////////
	//            	 CONSTRUTORES 	            //
	//////////////////////////////////////////////	

	public GerenciadorEscalonador(int quantum){
		this.quantum = quantum;
	}

	public GerenciadorEscalonador(String quantumFile){
		this.quantum = LeituraPrograma.getQuantumFile(quantumFile);
	}



	//////////////////////////////////////////////
	//             			MÉTODOS  			          //
	//////////////////////////////////////////////	

  /**
   * Metodos para gerenciar os processos e determinar seus estados
   * Usar o algoritmo de Round-Robin, dividir o processador de acordo
   * com a quantidade do Quantum.
  */	

  public void carregandoProgramas(){
    
		tabelaDeProcessos = LeituraPrograma.programasLidos();
		totalProgramas = tabelaDeProcessos.size();
      
		//Inicializa todos na lista de Prontos
		for(BlocoDeControleDeProcessos processo : tabelaDeProcessos){
			
			listaDeProntos.add(processo);		   

			String log = "Carregando " + processo.getNomePrograma();
			Log.gravarArquivoLog(quantum, log);

		}

  }



  //Recebe uma lista de programas que foram lidos, cada array e um programa.
  public void executaRoundRobin(){ 

		System.out.println("INICIANDO ROUND ROBIN - QUANTUM = " + quantum);

		//1° Executa os programas na ordem enquanto estiver processos na lista de Prontos e bloqueados
		while(listaDeProntos.size() > 0 || listaDeBloqueados.size() > 0){ 

  		try{

				//Comeca a processar pelo primeiro que esta na fila de prontos
				if(listaDeProntos.size() > 0){

					BlocoDeControleDeProcessos programa = listaDeProntos.get(0);
					instrucao = new Instrucoes(programa, quantum);

					//2° Verifica se o programa terminou
					if(!instrucao.terminou()){

						log = "Executando " + programa.getNomePrograma();
						Log.gravarArquivoLog(quantum, log);			

						programa.setEstadoProcesso("Executando");
						instrucao.processaInstrucoes();

						instrucoesExecutadas += instrucao.getInstrucoesExecutadas();
						trocas++;//toda vez que chama o processaInstrucao e uma troca de contexto								

						log = "Interrompendo " + programa.getNomePrograma() + " após " + instrucao.getInstrucoesExecutadas() + " instruções";
						Log.gravarArquivoLog(quantum, log);

						programa.setEstadoProcesso("Pronto");
						listaDeProntos.add(listaDeProntos.remove(0));	//Remove do comeco e coloca no final da fila de prontos
						decrementaTempoEspera_Bloqueados();
						
					}

				}
				else{
					
					//	4. Se nao houver nenhum processo em condicao de ser executado
					//	deve-se decrementar os tempos de espra de todos os processos na fila de bloqueados

					decrementaTempoEspera_Bloqueados();			

				}
			
			}    
			catch(Exception e){ 
				
				//3. Se, durante a execucao de um quantum, o processo fazer uma entrada ou saida (instrucao E/S")
				if (e.getMessage().equals("E/S")) executaES();			
				
				//5. Ao encontrar o comando SAIDA, o escalonador deve remover o processo em execucao da fila apropriada e da tabela de processos.
				if (e.getMessage().equals("SAIDA"))	executaSaida();	

				decrementaTempoEspera_Bloqueados();			
				
			}
			finally{
				
				imprimeListaDeProntos();
				imprimeListaDeBloqueados();
				imprimeTabelaProcessos();

			}
	
		}
   
  }



	private void executaES(){

		BlocoDeControleDeProcessos programaES = listaDeProntos.get(0);

		log = "Interrompendo " + programaES.getNomePrograma() + " após " + instrucao.getInstrucoesExecutadas() + " instruções";
		System.out.println(log);
		Log.gravarArquivoLog(quantum, log);
		
		log = "E/S iniciada em " + programaES.getNomePrograma();
		System.out.println(log);
		Log.gravarArquivoLog(quantum, log);			

		instrucoesExecutadas += instrucao.getInstrucoesExecutadas();
		trocas++;		//toda vez que chama o processaInstrucao e uma troca de contexto			

		programaES.setTempoEspera(2);
		programaES.setEstadoProcesso("Bloqueado");

		listaDeBloqueados.add(listaDeProntos.remove(0));		//Remove da fila de Prontos e coloca no final da fila de Bloqueados
		
	}


	private void executaSaida(){
								
		BlocoDeControleDeProcessos programaSaida = listaDeProntos.remove(0);

		int posicao = tabelaDeProcessos.indexOf(programaSaida);
		tabelaDeProcessos.remove(posicao);

		System.out.println(programaSaida.getNomePrograma() + " REMOVIDO da lista de PRONTOS e da TABELA PROCESSOS!");
		
		instrucoesExecutadas += instrucao.getInstrucoesExecutadas();
		trocas++;

		log = programaSaida.getNomePrograma() + " terminado. X=" + programaSaida.getRegistradorX() + ". Y=" + programaSaida.getRegistradorY();
		Log.gravarArquivoLog(quantum, log);

	}



  /////////////////////////////////////////////////////////////////////////////////////
 //              CALCULAR O TEMPO: MÉDIAS DE TROCAS, INSTRUCOES E PROGRAMAS           //
 //////////////////////////////////////////////////////////////////////////////////////

  public double getMediaTrocasContexto(){
		System.out.println("MEDIA DE TROCAS:  " + (trocas/totalProgramas) + " / QTD DE TROCAS: " + trocas + " / QTD PROGRAMAS: " + totalProgramas);  
		return ((double) trocas/totalProgramas);		
  }


	public double getMediaInstrucoes(){
		System.out.println("MEDIA DE INSTRUCOES:  " + (instrucoesExecutadas/trocas) + " / QTD DE INSTRUCOES: " + instrucoesExecutadas + " / QTD TROCAS: " + trocas); 
		return ((double) instrucoesExecutadas/trocas);
	}





	public void decrementaTempoEspera_Bloqueados(){

		for(BlocoDeControleDeProcessos processo : listaDeBloqueados)
			processo.decrementaTempoEspera();

		retiraZerados_Bloqueados();
			
	}


	//	3 (e) - Quando o tempo de espera de algum processo bloqueado chegar a zero
	public void retiraZerados_Bloqueados(){

		for(BlocoDeControleDeProcessos processo : listaDeBloqueados){

			if (processo.getTempoEspera() == 0){			
				
				//	recebe o status de pronto, remove da fila de bloqueados e inserido ao final da fila de processos prontos

				int posicao = listaDeBloqueados.indexOf(processo);
				listaDeBloqueados.get(posicao).setEstadoProcesso("Pronto");
				listaDeProntos.add(listaDeBloqueados.remove(posicao));	
				retiraZerados_Bloqueados();

			}

			if (listaDeBloqueados.size() == 0) return;

		}

	}


	//////////////////////////////////////////////
	//              IMPRESSÃO	                  //
	//////////////////////////////////////////////	

  public void imprimeTabelaProcessos(){

		System.out.println("\n\n---- TABELA DE PROCESSOS");
		for(BlocoDeControleDeProcessos processo : tabelaDeProcessos)
			System.out.print(processo);

  }  

	
	public void imprimeListaDeProntos(){
  
		System.out.println("\n\n---- LISTA DE PRONTOS");
		for(BlocoDeControleDeProcessos processo : listaDeProntos)
			System.out.print(processo);

  }  


	public void imprimeListaDeBloqueados(){
		
		System.out.println("\n---- LISTA DE BLOQUEADOS");
		for(BlocoDeControleDeProcessos processo : listaDeBloqueados)
			System.out.print(processo);

  }  


	//////////////////////////////////////////////
	//             			LOG                 		//
	//////////////////////////////////////////////

	public void logFinal(){

		String log;

		log = "MEDIA DE TROCAS: " + getMediaTrocasContexto();
		Log.gravarArquivoLog(quantum, log);

		log = "MEDIA DE INSTRUCOES: " + getMediaInstrucoes();
		Log.gravarArquivoLog(quantum, log);

		log = "QUANTUM: " + quantum;
		Log.gravarArquivoLog(quantum, log);

	}


}