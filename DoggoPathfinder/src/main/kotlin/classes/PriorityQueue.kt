package classes

import Node

data class PriorityQueue(var queue: List<Node> = mutableListOf<Node>(),
                         var priority: HashMap<Node, Int> = hashMapOf<Node, Int>()): IQueue<Node> {

    override fun getQueue(): List<Node> = this.queue

    private fun getMin(): Node? = this.priority.minByOrNull { it -> it.value }?.key

    private fun getMax(): Node? = this.priority.maxByOrNull { it -> it.value }?.key

    override fun deque(): Node? {
        val node = this.queue[this.queue.indexOf(getMin())]
        if(this.queue.isNotEmpty()) {
            this.queue -= node
            this.priority.remove(node)
        }
        return node
    }

    override fun enqueue(t: Node) { }

    fun enqueue(t: Node, priority: Int) {
        this.queue += t
        this.priority[t] = priority
    }

    fun updatePriority(t: Node, newPriority: Int) {
        if(this.contains(t)) this.priority[t] = newPriority
    }

    override fun poll(t: Node) {
        if(this.contains(t))
            this.queue -= t
            this.priority.remove(t)
    }

    override fun peek(): Node? = this.queue[this.queue.indexOf(getMin())]

    override fun contains(t: Node): Boolean = this.queue.contains(t)

    override fun isEmpty(): Boolean = this.queue.isEmpty()

    override fun isNotEmpty(): Boolean = this.queue.isNotEmpty()

    override fun removeAll() {
        while(this.isNotEmpty()) this.deque()
    }
}