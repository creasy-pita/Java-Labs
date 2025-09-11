## future 和 contdownlatch 区别

future.get() 的性能开销：

阻塞：future.get() 是一个阻塞方法，主线程会在此方法上停顿，直到相应的任务完成。这会导致主线程等待每个任务的结束。

同步开销：future.get() 需要从线程池中的工作线程中获取结果或等待任务完成的信号。在任务执行过程中，主线程不断地向每个 Future 对象请求完成信号，因此有一定的同步开销。每个 get() 调用都会导致一次线程切换（即主线程等待某个任务线程完成），增加了系统负担。

Exception Handling：每个 future.get() 都可能抛出 InterruptedException 或 ExecutionException，需要捕获和处理异常。这些处理本身也需要额外的时间。

与 future.get() 的不同 CountDownLatch：

非阻塞等待：CountDownLatch 通过 countDown() 来标记每个子任务的完成状态，主线程通过调用 await() 等待所有子任务完成。与 future.get() 不同，await() 仅仅是等待信号，而不需要去获取任务的返回结果。

性能优势：CountDownLatch 会一次性等待所有任务完成，不像 future.get() 一样需要逐个等待每个线程的完成。这样主线程的等待时间会更短，且无额外的异常处理成本。

可组合性差：CountDownLatch 适用于一组任务完成后的等待，而 Future 可以用于处理任务的结果（返回值）。
