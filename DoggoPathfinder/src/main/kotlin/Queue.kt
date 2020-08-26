external interface IQueue<T> {
    var queue: List<T>
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

data class Queue(override var queue: List<Node> = mutableListOf<Node>()): IQueue<Node> {

    override fun getQueue(): List<Node> = this.queue

    override fun deque(): Node? {
        if(this.queue.isNotEmpty()) {
            val node = this.queue[0]
            this.queue -= this.queue[0]
            return node
        }
        return null
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

    override fun removeAll() {
        while(this.isNotEmpty())
            this.deque()
    }
}