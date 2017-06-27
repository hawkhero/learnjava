package future;

import java.util.concurrent.*;

import org.junit.Test;

import static java.lang.System.out;
import static java.lang.Thread.*;

public class HelloFuture {
	static ExecutorService threadPool = Executors.newCachedThreadPool();
	

	@Test
	public void handleError() throws InterruptedException, ExecutionException {
		CompletableFuture.runAsync(() -> {
				throw new RuntimeException("on purpose");
		}).whenComplete((result, throwable) -> {
			System.out.println("world");
		}).exceptionally((error) -> {
			System.out.println(error);
			return null;
		});
		out.println(">>handle error");
		pauseSecond(1);
	}

	@Test
	public void combine() throws InterruptedException, ExecutionException{
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

	@Test
	private void incorrect() throws InterruptedException, ExecutionException{
		CompletableFuture.runAsync(() -> {
			try {
				Thread.sleep(1000);
				out.println("hello");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).thenAccept(result -> {
			//不會印出結果，因為前一步是 runAsync()， run 開頭的 method 是沒有回傳值，因此接收回傳值的 accept method 不會被呼叫
			System.out.println(result); 
		});
		pauseSecond(1);
	}
	
	@Test
	void supplyAsync() throws InterruptedException{
		CompletableFuture.supplyAsync(() -> {
			try {
				out.println(Thread.currentThread());
				out.println("produce data");
				Thread.sleep(5);
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
			return "data";
		}).thenApply((o) -> {
			out.println(o);
			return o+" processed";
		}).thenAccept(System.out::println);
		
		out.println(">>supplyAsync "+Thread.currentThread());
		pauseSecond(1);
	}
	
	//
	/**
	 * 參考： http://codingjunkie.net/completable-futures-part1/
	 * By default, CompletableFuture runs in the ForkJoinPool.commonPool(). This pool is statically constructed; 
	 * its run state is unaffected by attempts to shutdown() or shutdownNow(). 
	 *  However this pool and any ongoing processing are automatically terminated upon program System.exit(int). 
	 *  Any program that relies on asynchronous task processing to complete before program termination should invoke commonPool().awaitQuiescence, before exit.
	 *  所以如果沒有將 thread 暫停，程式直接終止就來不急跑傳給 runAsync() 的 runnable lambda
	 *  
	 */
	@Test
	void runAsync(){
		CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> System.out.println("running async task"));
		pauseSecond(1);
	}
	
	/**
	 * this case runs without an pause
	 * 這個例子跑在另一個 thread pool 中，因此不會受到原有 main thread終止的影響
	 */
	@Test
	void runAsyncExecutor(){
		 CompletableFuture.runAsync(() -> System.out.println("running async task in another pool"), threadPool);
	}
	
	static void pauseSecond(int sec){
		try{
			Thread.sleep(1000 * sec);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	/**
	 * get() blocks the  thread.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	void sync() throws InterruptedException, ExecutionException {
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
