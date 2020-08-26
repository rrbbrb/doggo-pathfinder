external interface IQueue<T> {
    var queue: List<T>
    fun getQueue(): List<T>
    fun deque()
    fun enqueue(t: T)
    fun poll(t: T)
    fun peek(): T?
    fun contains(t: T): Boolean
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean
}

data class Queue(override var queue: List<Node> = mutableListOf<Node>()): IQueue<Node> {

    override fun getQueue(): List<Node> = this.queue

    override fun deque() {
        if(this.queue.isNotEmpty()) this.queue -= this.queue[0]
    }

    override fun enqueue(t: Node) {
        this.queue += t
    }

    override fun poll(t: Node) {
        if(this.contains(t)) this.queue -= t
    }

    override fun peek(): Node? {
        if(this.queue.isNotEmpty())
            return this.queue[0]
        return null
    }

    override fun contains(t: Node): Boolean = this.queue.contains(t)

    override fun isEmpty(): Boolean = this.queue.isEmpty()

    override fun isNotEmpty(): Boolean = this.queue.isNotEmpty()
}