📄 README — Simulador de Sistema de Arquivos (I-Node)
Simulador de Sistema de Arquivos baseado em I-Nodes

Projeto acadêmico — Sistemas Operacionais

📌 Descrição do Projeto

Este projeto implementa um simulador de sistema de arquivos inspirado na estrutura tradicional de i-nodes, utilizada por sistemas Unix e derivados.
O objetivo é permitir que o usuário compreenda, na prática, como funciona a organização interna de diretórios, arquivos, alocação de blocos e gerenciamento de referências dentro de um disco virtual.

O simulador opera por meio de uma interface de linha de comando simples (shell), permitindo ao usuário criar, remover, mover e listar arquivos e diretórios dentro de um sistema de arquivos totalmente simulado.

🎯 Objetivos de Aprendizagem

Com este simulador, é possível:

Entender como um sistema de arquivos baseado em i-nodes funciona internamente.

Visualizar a relação entre diretórios, arquivos e blocos.

Compreender problemas reais, como referências perdidas, blocos órfãos e inconsistências.

Observar na prática a implementação de conceitos como:
✔ i-node
✔ diretórios como listas de entradas
✔ blocos de dados
✔ alocação e desalocação
✔ persistência do disco em arquivo binário

🧠 Resultados Esperados

Ao utilizar o simulador, o aluno deve ser capaz de:

Entender o papel do i-node e sua estrutura de metadados.

Identificar claramente como um diretório referencia seus arquivos.

Observar o comportamento de operações reais do SO, como

mkdir

touch

rm

mv

ls

Relacionar o funcionamento interno com o que ocorre em sistemas reais.

Desenvolver visão crítica sobre problemas de integridade e consistência.

🖥️ Funcionalidades Implementadas

O shell do simulador suporta os seguintes comandos:

📁 mkdir <nome>

Cria um diretório no diretório atual.

📄 touch <nome> <conteúdo>

Cria um arquivo e escreve conteúdo.

📂 ls

Lista os arquivos e diretórios existentes no diretório atual, exibindo:

tipo (arquivo/diretório)

nome

número do i-node

tamanho

data de modificação

🗑 rm <nome>

Remove arquivo ou diretório (com verificação de referência e persistência).

✏ mv <antigo> <novo>

Renomeia uma entrada do diretório atual.

🚪 exit

Fecha o programa salvando o estado do sistema de arquivos.

🧱 Arquitetura do Projeto

O simulador contém os seguintes componentes principais:

🔹 Inode

Representa metadados de arquivos/diretórios:

tamanho

blocos diretos

timestamp

flag de tipo

permissões (opcional)

🔹 DirectoryEntry

Representa itens apontados por um diretório → nome + inodeId.

🔹 VirtualDisk

Simula:

vetor de i-nodes

vetor de blocos

persistência em arquivo binário (disk.bin)

🔹 FileManagerService

Contém toda lógica de:

criação

remoção

atualização

leitura de diretórios

alocação de blocos

manutenção da persistência

🔹 ShellApp

Simula um terminal interativo e processa os comandos do usuário.

🔧 Como Executar o Projeto

Instalar JDK 17+

Instalar Quarkus CLI (opcional)

Executar com:

./mvnw clean package


Ou, se usar @QuarkusMain:

java -jar target/quarkus-app/quarkus-run.jar


Ao iniciar, o terminal aparecerá:

=== SIMULADOR I-NODE (v2.0) ===
Comandos: mkdir, touch, ls, rm, mv, exit
$ root/>

🔗 Link para o GitHub

📎 Adicionar aqui quando o repositório estiver publicado:
👉 https://github.com/seu-usuario/seu-repositorio

🧑‍🤝‍🧑 Integrantes da Dupla

 — João Lucas Melo / 2010433
