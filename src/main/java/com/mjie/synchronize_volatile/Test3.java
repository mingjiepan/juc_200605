package com.mjie.synchronize_volatile;

/**
 * jvm的同步是基于进入与退出监视器对象（管程对象）（Monitor）来实现的，每个对象实例都会有一个Monitor对象，Monitor对象会和Java对象一同创建并销毁
 * Monitor对象是由C++来实现的。
 *
 * 当多个线程同时访问一段同步代码时，这些线程会被放到一个EntryList集合中，处于阻塞状态的线程都会被放到该列表当中。接下来，当线程获取到对象的
 * Monitor时，Monitor是依赖于底层操作系统的mutex lock来实现互斥的，线程获取mutex成功，则会持有该mutex，这时其他线程就无法再获取到该mutex
 *
 * 如果线程调用了wait方法，那么该线程就会释放掉所持有的mutex，并且该线程会进入到WaitSet集合（等待集合）中，等待下一次被其他线程调用notify/notifyAll唤醒。
 * 如果当前线程顺利执行完毕方法，那么它也会释放掉所持有的mutex
 *
 * 总结一下：同步锁在这种实现方式当中，因为monitor是依赖于底层的操作系统实现，这样就存在用户态与内核态之间的切换，所以会增加性能开销。
 *
 * 通过对象互斥锁的概念来保证共享数据操作的完整性。每个对象都对应于一个可称为"互斥锁"的标记，这个标记用于保证在任何时刻，只能有一个线程访问该对象
 *
 * 那些处于EntityList与WaitSet中的线程均处于阻塞状态，阻塞操作是由操作系统来完成的，在Linux下是通过pthread_mutex_lock函数实现的。线程被阻塞后便会进入到内核调度状态
 * 这会导致系统在用户态与内核态之间来回切换，严重影响锁的性能
 *
 * 解决上述问题的办法便是自旋，其原理是：当发生对Monitor的争用时，若Owner能够在很短的时间内释放掉锁，则那些正在争用的线程就可以稍微等待一下（即所谓的自旋），在Owner线程释放锁之后
 * 争用线程可能会立刻取到锁，从而避免了系统阻塞。
 *
 * 不过，当Owner运行的时间超过了临界值后，争用线程自旋一段时间后依然无法获取到锁，这时争用线程则会停止自旋而进入到阻塞状态。
 * 所以总体的思想是：先自旋，不成功再进行阻塞，尽量降低阻塞的可能性，这对那些执行时间很短的代码块来说有极大的性能提升。显然，自旋在多处理器（多核心）上才有意义。
 *
 * monitor 底层c++代码实现为ObjectMonitor对象，Object.wait() 为 ObjectMonitor.wait()
 * ObjectWaiter双向链表（WaitSet和EntryList）
 * WaitSet 当调用wait方法后，线程会进入WaitSet
 * EntryList 争用monitor的线程会进入到EntryList中
 *
 *
 *
 * 线程获取对象的锁，通过系统调用获取mutex（针对synchronized）
 *
 *
 *
 * 互斥锁的属性
 * 1，pthread_mutex_timed_np: 这是缺省值，也就是普通锁。当一个线程加锁后，其余请求锁的线程将会形成一个等待队列，并且在解锁后按照优先级获取到锁。这种策略可以确保资源分配的公平性。
 * 2，pthread_mutex_recursive_np: 嵌套锁。允许一个线程对同一把锁成功获取多次，并通过unlock解锁。如果是不同线程请求，则在加锁线程解锁时重新进行竞争。
 * 3，pthread_mutex_errorcheck_np: 检错锁。如果一个线程请求了同一个锁，则返回edeadlk，否则与pthread_mutex_timed_np类型动作相同，这样就保证了当不允许多次加锁时不会出现最简单情况下的死锁（不太理解）
 * 4，pthread_mutex_adaptive_np: 适应锁，动作最简单的锁类型，仅仅等待解锁后重新竞争。
 */
public class Test3 {
    public static synchronized void method() {
        System.out.println("hello world");
    }
}
