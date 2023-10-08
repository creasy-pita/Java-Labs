package com.creasypita;

import java.util.concurrent.locks.*;

/**
 * 需求：资源由多个线程生产，多个线程消费，只有一个生产车间，所以同一时间只允许一个消费和生产线程在工作
 * 资源中没有产品时，生产产品；有产品时，生产线程先进入阻塞，等待消费线程消费完产品时再唤醒，生产线程重新获取执行权后生产产品；
 *
 */
class ProducerConsumerDemo
{
    public static void main(String[] args) throws InterruptedException {
        Resource r = new Resource();

        Producer pro = new Producer(r);
        Consumer con = new Consumer(r);

        Thread t1 = new Thread(pro);
//        Thread t2 = new Thread(pro);
//        Thread t3 = new Thread(con);
        Thread t4 = new Thread(con);

        t1.start();
        Thread.sleep(10);
//        t2.start();
//        Thread.sleep(10);
//        t3.start();
        t4.start();

    }
}

/*
JDK1.5 中提供了多线程升级解决方案。
将同步Synchronized替换成现实Lock操作。
将Object中的wait，notify notifyAll，替换了Condition对象。
该对象可以Lock锁 进行获取。
该示例中，实现了本方只唤醒对方操作。

Lock:替代了Synchronized
	lock
	unlock
	newCondition()

Condition：替代了Object wait notify notifyAll
	await();
	signal();
	signalAll();




*/
class Resource
{
    private String name;
    private int count = 1;
    private boolean hasProduct = false;
    //  t1    t2
    private Lock lock = new ReentrantLock();

    private Condition condition_pro = lock.newCondition();
    private Condition condition_con = lock.newCondition();



    public  void set(String name)throws InterruptedException
    {//t2
        lock.lock();
        try
        {
//            Thread.sleep(10000);
            while(hasProduct)
                condition_pro.await();
            this.name = name+"--"+count++;

            System.out.println(Thread.currentThread().getName()+"...生产者.."+this.name);
            hasProduct = true;//t1
            condition_con.signal();
        }
        finally
        {
            lock.unlock();//释放锁的动作一定要执行。
        }
    }


    //  t3   t4
    public  void out()throws InterruptedException
    {
        lock.lock();
        try
        {
            while(!hasProduct)
                condition_con.await();
            System.out.println(Thread.currentThread().getName()+"...消费者........."+this.name);
            hasProduct = false;
            condition_pro.signal();
        }
        finally
        {
            lock.unlock();
        }

    }
}

class Producer implements Runnable
{
    private Resource res;

    Producer(Resource res)
    {
        this.res = res;
    }
    public void run()
    {
        while(true)
        {
            try
            {
                res.set("+商品+");
            }
            catch (InterruptedException e)
            {
            }

        }
    }
}

class Consumer implements Runnable
{
    private Resource res;

    Consumer(Resource res)
    {
        this.res = res;
    }
    public void run()
    {
        while(true)
        {
            try
            {
                res.out();
            }
            catch (InterruptedException e)
            {
            }
        }
    }
}
