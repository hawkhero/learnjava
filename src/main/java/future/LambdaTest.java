package future;

import static java.lang.System.out;

import java.util.*;
import java.util.stream.Stream;

import org.junit.Test;

public class LambdaTest {

	@Test
	public void compareThreadLambda(){
		Runnable r1 = () -> System.out.println("r1: " + this.getClass());
		
		Runnable r2 = new Runnable(){
			public void run(){
				System.out.println("r2: " + this.getClass());
			}
		};

		Runnable r3 = () -> {
			HelloFuture.pauseSecond(2);
			System.out.println("r3: " + this.getClass());
		};
		
		new Thread(r3).start();
		new Thread(r2).start();
		new Thread(r1).start();
	}
	
	@Test
	void consumer(){
		Arrays.asList("Justin", "Monica", "Irene")
		.forEach(System.out::println); //pass a method reference, lambda 
	}
	
	//receive a parameter and return
	@Test
	void function(){
		out.println(Optional.of("my name").map(String::toUpperCase).get());
	}
	
	@Test
	void predicate(){
		Stream.of("Albee","Justin", "Monica", "Irene").filter(name -> name.endsWith("e"))
			.forEach(out::println);
	}
	
	@Test
	void supplier(){
		Stream.generate(Math::random).limit(5).forEach(out::println);
	}
}
