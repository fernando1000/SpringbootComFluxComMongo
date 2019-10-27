package com.x10d.FluxComMongo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TesteDesempenho {
	
	private static int NUMERO_THREADS = 100;
	private static int QTD_ITENS_LISTA = 5;
	
	public static List<NovaLoja> getLista() {
		
		List<NovaLoja> lista = new ArrayList<NovaLoja>();
		
		for(int i=0; i<QTD_ITENS_LISTA; i++) {			
			lista.add(new NovaLoja("A"+i));
		}
        return lista;
    }
	
    public static void main(String[] args) {
    	
        List<NovaLoja> lojas = getLista();
       
        assincrono(lojas);
        System.out.println("##############");
        sequencial(lojas);
    }
    
    private static void assincrono(List<NovaLoja> lojas) {

        long start = System.currentTimeMillis();
        
        final Executor executor = Executors.newFixedThreadPool(Math.min(lojas.size(), NUMERO_THREADS), r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
       
        CompletableFuture[] completableFutures = acharPrecosStream(lojas, executor)
        											.map(f -> f.thenAccept(s -> System.out.println(s+"(finalizado em: "+(System.currentTimeMillis() - start)+")")))
        											.toArray(CompletableFuture[]::new);
        
        CompletableFuture.allOf(completableFutures).join();
        
        System.out.println("Tempo total MAIN THREAD: " + (System.currentTimeMillis() - start) + " ms");
    }

    public static double calculoComDelay() {
     	delay();
        return ThreadLocalRandom.current().nextDouble() * 100;
    }

	 public static void delay() {
	        try {
	            int delay = ThreadLocalRandom.current().nextInt(500, 2000);
	            TimeUnit.MILLISECONDS.sleep(delay);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	    }
	 
    private static List<String> sequencial(List<NovaLoja> lojas) {
    	
        System.out.println("Stream sequencial");
        
        long start = System.currentTimeMillis();
        
        List<String> collect = lojas.stream()
        							.map(NovaLoja::getPrecoComDelay)
							        .map((nomePreco) -> Orcamento.fromStringToOrcamento(nomePreco))
							        .map((orcamento) -> Desconto.calcularDescontoComDelay(orcamento))
							        .collect(Collectors.toList());
							        
        System.out.println(collect);
        System.out.println("Tempo total: " + (System.currentTimeMillis() - start) + " ms");
        return collect;
    }

    private static Stream<CompletableFuture<String>> acharPrecosStream(List<NovaLoja> lojas, Executor executor) {
    	
        System.out.println("Completable Future Async Stream");
        
        long start = System.currentTimeMillis();
        
        Stream<CompletableFuture<String>> completableFutureStream = lojas.stream()
        		// Pegando o preco original de forma async
                .map(loja -> CompletableFuture.supplyAsync(loja::getPrecoComDelay, executor))
                
                // Transforma a string em um Orcamento no momento em que ele se torna disponivel
                .map(future -> future.thenApply((nomePreco) -> Orcamento.fromStringToOrcamento(nomePreco)))
                
                // Compoem o primeiro future com mais uma chamada async, para pegar os descontos no momento que a primeira requisicao async estiver disponivel
                .map(future -> future.thenCompose(orcamento -> CompletableFuture.supplyAsync(() -> Desconto.calcularDescontoComDelay(orcamento), executor)));
        
        System.out.println("Tempo total: " + (System.currentTimeMillis() - start) + " ms");
        
        return completableFutureStream;
    }

}

class NovaLoja {
    private String nome;
    public NovaLoja(String nome) {
        this.nome = nome;
    }
    public String getPrecoComDelay() {
        double preco = TesteDesempenho.calculoComDelay();
        return String.format("%s:%.2f", nome, preco);
    }
    public String getNome() {
        return nome;
    }
}

class Desconto {
    public static String calcularDescontoComDelay(Orcamento orcamento) {
        return String.format("%s PRI: %.2f," + " SEG: %.2f", orcamento.getNomeLoja(), orcamento.getPreco(), TesteDesempenho.calculoComDelay());
    }
}

class Orcamento {
    private final String nomeLoja;
    private final double preco;
    private Orcamento(String nomeLoja, double preco) {
        this.nomeLoja = nomeLoja;
        this.preco = preco;
    }
    public static Orcamento fromStringToOrcamento(String nomePreco) {
        String[] split = nomePreco.split(":");
        String nomeLoja = split[0];
        String virgula = split[1];
        String ponto = virgula.replace(",", ".");
        double preco = Double.parseDouble(ponto);
        return new Orcamento(nomeLoja, preco);
    }
    public String getNomeLoja() {
        return nomeLoja;
    }
    public double getPreco() {
        return preco;
    }
}
