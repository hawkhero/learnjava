package future;

import static java.lang.System.out;

import java.util.*;
import java.util.stream.Stream;

import org.junit.Test;

public class LambdaTest {

	/**
	 * r2 是屬於匿名 inner class
	 */
	@Test
	public void compareThreadLambda(){
		Runnable r1 = () -> System.out.println("r1: " + this.getClass());
		
		Runnable r2 = new Runnable(){
			public void run(){
				System.out.println("r2: " + this.getClass());
			}
		};

		Runnable r3 = () -> {
			CompletableFutureTest.sleepSecond(2);
			System.out.println("r3: " + this.getClass());
		};
		
		new Thread(r3).start();
		new Thread(r2).start();
		new Thread(r1).start();
	}
	
	// 以下是4種函數介面
	
	@Test
	public void consumer(){
		Arrays.asList("Justin", "Monica", "Irene")
		.forEach(System.out::println); //pass a method reference, lambda 
	}
	
	@Test
	public void function(){
		out.println(Optional.of("my name").map(String::toUpperCase).get());
	}
	
	@Test
	public void predicate(){
		Stream.of("Albee","Justin", "Monica", "Irene").filter(name -> name.endsWith("e"))
			.forEach(out::println);
	}
	
	@Test
	public void supplier(){
		Stream.generate(Math::random).limit(5).forEach(out::println);
	}
}
