package classes

import Node

external interface IQueue<T> {
    fun getQueue(): List<T>
    fun deque(): T?
    fun enqueue(t: T)
    fun poll(t: T)
    fun peek(): T?
    fun contains(t: T): Boolean
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean
    fun removeAll()
}

data class Queue(var queue: List<Node> = mutableListOf<Node>()): IQueue<Node> {

    override fun getQueue(): List<Node> = this.queue

    override fun deque(): Node? {
        val node = this.queue.firstOrNull()
        if(this.queue.isNotEmpty()) {
            this.queue -= this.queue[0]
        }
        return node
    }

    override fun enqueue(t: Node) {
        this.queue += t
    }

    override fun poll(t: Node) {
        if(this.contains(t)) this.queue -= t
    }

    override fun peek(): Node? = this.queue.firstOrNull()

    override fun contains(t: Node): Boolean = this.queue.contains(t)

    override fun isEmpty(): Boolean = this.queue.isEmpty()

    override fun isNotEmpty(): Boolean = this.queue.isNotEmpty()

    override fun removeAll() {
        while(this.isNotEmpty()) this.deque()
    }
}