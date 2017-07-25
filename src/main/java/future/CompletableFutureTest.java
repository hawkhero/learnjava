package future;

import static java.lang.System.out;
import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.is;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.junit.*;

/**
 * runYYY(Runnable), a method with no argument, no return
 * supplyYYY(Supplier), a method returns an object with no argument 
 * applyYYY(Function), a method returns a value with an argument
 * @author hawk
 *
 */
public class CompletableFutureTest {
	static ExecutorService threadPool = Executors.newCachedThreadPool();
	
	/**
	 * get() 會等 CompletableFuture 做完才執行下一行
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void sync() throws InterruptedException, ExecutionException {
		CompletableFuture<Void> future =
				CompletableFuture.runAsync(() -> {
					sleepSecond(2);
					System.out.println("hello");
				});

		future.get(); //the thread waits here
		System.out.println("world");
	}
	
	/**
	 * world 先印出 
	 */
	@Test
	public void asyncHello() {
		CompletableFuture.runAsync(() -> System.out.println("hello"));
		System.out.println("world");
	}	
	
	
	/**
	 * 參考： http://codingjunkie.net/completable-futures-part1/
	 * By default, CompletableFuture runs in the ForkJoinPool.commonPool(). This pool is statically constructed; 
	 * its run state is unaffected by attempts to shutdown() or shutdownNow(). 
	 *  However this pool and any ongoing processing are automatically terminated upon program System.exit(int). 
	 *  Any program that relies on asynchronous task processing to complete before program termination should invoke commonPool().awaitQuiescence, before exit.
	 *  所以如果沒有將 main thread 暫停，程式直接終止，傳給 runAsync() 的 Runnable lambda 就來不及執行
	 */
	@Test
	public void runAsync(){
		CompletableFuture.runAsync(() -> System.out.println("running async task"));
//		out.println(">>runAsync "+Thread.currentThread());
		sleepSecond(1);
	}
	

	/**
	 * this case runs without an pause
	 * 這個例子跑在另一個 thread pool 中，因此不會受到原有 main thread終止的影響
	 */
	@Test
	public void runAsyncExecutor(){
		 CompletableFuture.runAsync(() -> System.out.println("running async task in another pool"), threadPool);
	}
		

	/**
	 * similar to get() but throw unchecked exception. Can be used a lambda better than get() which throws checked exception.
	 */
	@Test
	public void runAsyncJoin(){
		CompletableFuture.runAsync(() -> {
			sleepSecond(2);
			System.out.println("running async task");
		})
		.join();
		System.out.println(">>>async join");
	}

	/**
	 * 呈現 supply, apply, accept 串接的用法，但要注意是否有回傳值。如最後一個 accept 是不會收到回傳值的
	 * @throws InterruptedException
	 */
	@Test
	public void supplyAsync() throws InterruptedException{
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
			out.println("applying "+ o);
			return o+" applied";
		}).thenAccept(System.out::println)
		.thenAccept(System.out::println);//just get null
		
		out.println(">>supplyAsync "+Thread.currentThread());
		sleepSecond(1);
	}
	
	/**
	 * 非同步、照順序
	 * then 開頭 method, thenYYY(),  會等前面執行完再執行
	 * 如果用 runAsync() 就不會等前一個
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void runOneByOne() throws InterruptedException, ExecutionException{
		CompletableFuture<Void> future = CompletableFuture
		.runAsync(() -> {
			try{
				sleep(1000);
				System.out.println("1st");
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		})
		.thenRunAsync(() -> {
			try{
				sleep(1000);
				System.out.println("2nd");
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		})
		.thenRunAsync(() -> {
			try{
				sleep(1000);
				System.out.println("3rd");
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		})
		.whenComplete((r, ex) -> System.out.println("done"));
		
		sleepSecond(5);
	}
	

	@Test
	public void handleError() throws InterruptedException, ExecutionException {
		CompletableFuture.runAsync(() -> {
				throw new RuntimeException("on purpose");
		}).exceptionally((error) -> {
			error.printStackTrace();
			return null;
		});
		sleepSecond(1);
	}	
	
	
	/**
	 * whenComplete() is a flexible way to handle result/exception
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void whenComplete() throws InterruptedException, ExecutionException {
		CompletableFuture.supplyAsync(() -> {
			Random random = new Random();
			if (random.nextBoolean()){
				return "success";
			}else{
				//if an error happens, the next stage will not be invoked
				throw new RuntimeException("error");
			}
		}).thenApply(result -> {
			System.out.println(result);
			return result;
		})
		.whenComplete((result, throwable) -> {
			if (result == null){
				throwable.printStackTrace();
			}else{
				System.out.println("complete: "+result);
			}
		});
		out.println(">>handle errors or result");
		sleepSecond(1);
	}
	
	/**
	 * 將一個 CompletableFuture 結果傳給另一個 CompletableFuture 做輸入，兩者要照順序執行
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public void compose() throws InterruptedException, ExecutionException {
		//1st Future
		 CompletableFuture<Integer> summedNumbers = CompletableFuture.supplyAsync(() ->{
			return Arrays.asList(1,2,3,4,5,6,7,8,9,10);
		})
		.thenComposeAsync((numbers) -> {
			//需回傳 2nd Future
			return CompletableFuture.supplyAsync(() -> numbers.stream().mapToInt(Integer::intValue).sum());
		});

		Assert.assertThat(summedNumbers.get(), is(55));
	}

	@Test
	public void compose2() throws InterruptedException, ExecutionException {
		CompletableFuture<String> completableFuture = 
			CompletableFuture.supplyAsync(() -> "Hello")
		    .thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World"));
		 
		Assert.assertEquals("Hello World", completableFuture.get());

	}

	/**
	 * 將 2 個獨立執行的 CompletableFuture 的結果合併
	 * @throws Exception
	 */
	@Test
	public void combine() throws Exception {
		CompletableFuture<String> firstTask = CompletableFuture.supplyAsync(() -> "combine all");
		CompletableFuture<String> secondTask = CompletableFuture.supplyAsync(() ->  "task results");

		CompletableFuture<String> combined = firstTask.thenCombineAsync(secondTask, (f, s) -> f + " " + s);

		Assert.assertThat(combined.get(), is("combine all task results"));
	}

	
	
	@Test
	public void templateMethodCase1() throws InterruptedException, ExecutionException {
		CompletableFuture<String> async = CompletableFuture.supplyAsync(() -> {
			System.out.println("async operation");
			sleepSecond(1);
			return "DONE";});

		async.thenCompose(templateMethod(this::updateUi))
		.whenComplete((val, ex) -> ex.printStackTrace());

		sleepSecond(2);
	}

	public void updateUi(String value) {
		System.out.println(value);
//		throw new RuntimeException("problem");
	}

	public <T>Function<T, CompletionStage<Void>> templateMethod(Consumer<T> consumer) {
		return (value) -> CompletableFuture.runAsync(() -> System.out.println("activate"))
				.thenRun(() -> consumer.accept(value))
				.whenComplete((res, err) -> System.out.println("deactivate"));
	}
	
	
	@Test
	public void templateMethodCase2() throws InterruptedException, ExecutionException {
		CompletableFuture<String> async = CompletableFuture.supplyAsync(() -> {
			System.out.println("async operation");
			sleepSecond(1);
			return "DONE";});
		templateMethod(this::updateUi);
//		templateMethod2(this::updateUi, "");
//		async.thenCompose((range) -> templateMethod2(this::updateUi, range))
//		.whenComplete((val, ex) -> ex.printStackTrace());

		sleepSecond(2);
	}
	
	public CompletableFuture templateMethod2(Consumer consumer, Object data) {
		return CompletableFuture.runAsync(() -> System.out.println("activate"))
				.thenRun(() -> consumer.accept(data))
				.whenComplete((res, err) -> System.out.println("deactivate"));
	}
	
	static void sleepSecond(int sec){
		try{
			TimeUnit.SECONDS.sleep(sec);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
}
