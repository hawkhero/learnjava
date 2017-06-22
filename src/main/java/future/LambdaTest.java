package future;

import static java.lang.System.out;

import java.util.*;
import java.util.stream.Stream;

public class LambdaTest {

	public static void main(String[] args) {
//		new LambdaTest().compareThreadLambda();
//		consumer();
//		function();
//		predicate();
		supplier();
	}
	
	public void compareThreadLambda(){
		Runnable r1 = () -> System.out.println("r1: " + this.getClass());
		
		Runnable r2 = new Runnable(){
			public void run(){
				System.out.println("r2: " + this.getClass());
			}
		};
		
		new Thread(r1).start();
		new Thread(r2).start();
	}
	
	static void consumer(){
		Arrays.asList("Justin", "Monica", "Irene")
		.forEach(System.out::println); //pass a method reference, lambda 
	}
	
	//receive a parameter and return
	static void function(){
		out.println(Optional.of("my name").map(String::toUpperCase).get());
	}
	
	static void predicate(){
		Stream.of("Albee","Justin", "Monica", "Irene").filter(name -> name.endsWith("e"))
			.forEach(out::println);
	}
	
	static void supplier(){
		Stream.generate(Math::random).limit(5).forEach(out::println);
	}
}
