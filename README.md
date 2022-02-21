## Escalonador de Processos

O objetivo deste Exercício Programa é implementar um escalonador de processos baseado na política de Time Sharing *(algoritmo de Round Robin)* em uma máquina com um único processador, permitindo assim um ambiente de multiprogramação.


<br>

---
### Estrutura das Classes

- **Escalonador**
    - Classe que contem o método `main` e valida se existem argumentos para se rodar uma simulação completa ou executar o processo com base nas instruções do arquivo `quantum.txt`, dentro da pasta `programas`. 
    
    <br>


- **BlocoDeControleDeProcessos**
    -  O BCP (Bloco de Controle de Processos) representa unitariamente o processo que está na tabela de processos e serve para armazenar informações como o estado do processo, o nome do processo, etc. A tabela de processos será um ArrayList que guardará esses objetos/blocos.

    <br>

- **GerenciadorEscalonador**
    - Classe responsável por "orquestrar" toda a dinâmica da política de escalonamento Round Robin.

    <br>

- **Instrucoes**
    - Nesta classe, estão as instruções que serão executadas pelo processo, além do gerenciamento de tempo e controle do estado dos registradores.

    <br>

- **LeituraPrograma**
    - Esta classe é responsável por ler os arquivos "txt" e instanciar os objetos que representam os processos (ou seja, os BCPs). Ela retorna um ArrayList de objetos do tipo `BlocoDeControleDeProcessos` conforme informado anteriormente.

    <br>

- **Log**
    - Classe que armazena os resultados das simulações nos arquivos invididuas de log, conforme o quantum.

    <br>


- **Simulacao**
    - Responsável por executar a simulação com diferentes quantuns (1 à 21) e armazenar os resultados para analise posterior.

    <br>


---
### Compilação e Execução
Para compilar o código, basta executar o comando: `javac Escalonador.java`

Para execucar o programa, basta executar o comando: `java Escalonador` [args]

<br>

---
**Créditos**

Professor: Norton Trevisan Roman

Aluno: Luiz Fernando Conceição dos Santos

Aluna: Camila Felinto Suriao 
