package future;

import java.util.concurrent.*;


import static java.lang.Thread.*;

public class HelloFuture {
	public static void main(String[] args) throws Exception {
//		sync();
		listen();
//		combine();
//		handleError();
	}

	private static void handleError() throws InterruptedException,
			ExecutionException {
		CompletableFuture.runAsync(() -> {
				throw new RuntimeException("on purpose");
		}).whenComplete((result, throwable) -> {
			System.out.println("world");
		}).exceptionally((error) -> {
			System.out.println(error);
			return null;
		}).get();
	}

	private static void combine() throws InterruptedException, ExecutionException{
		CompletableFuture<Void> future = CompletableFuture
		.runAsync(() -> {
			try{
				sleep(1000);
				System.out.println("1st");
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		})
		.runAsync(() -> {
			try{
				sleep(1000);
				System.out.println("2nd");
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		})
		.runAsync(() -> {
			try{
				sleep(1000);
				System.out.println("3rd");
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		})
		.whenComplete((r, ex) -> System.out.println("done"));
		
		future.get();
	}

	private static void listen() throws InterruptedException, ExecutionException{
		CompletableFuture.runAsync(() -> {
			try {
				Thread.sleep(1000);
				System.out.println("hello");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).thenAccept(result -> {
			System.out.println(result);
		});
	}

	/**
	 * get() blocks the  thread.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	private static void sync() throws InterruptedException, ExecutionException {
		CompletableFuture<Void> future =
				CompletableFuture.runAsync(() -> {
					try {
						Thread.sleep(1000);
						System.out.println("hello");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				});

		future.get(); //the thread waits here
		System.out.println("world");
	}

}
