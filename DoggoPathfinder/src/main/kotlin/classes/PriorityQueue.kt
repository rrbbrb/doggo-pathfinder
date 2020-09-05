package classes

import Node

data class PriorityQueue(var PQ: HashMap<Node, Int> = hashMapOf<Node, Int>()): IQueue<Node> {

    private fun getMin(): Node? = this.PQ.minByOrNull { it -> it.value }?.key

    override fun deque(): Node? {
        val node = getMin()
        if(this.isNotEmpty()) node?.let { this.poll(it) }
        return node
    }

    fun getPriority(node: Node) = this.PQ[node]

    override fun enqueue(t: Node) { }

    fun enqueue(t: Node, priority: Int) {
        this.PQ[t] = priority
    }

    override fun poll(t: Node) {
        if(this.contains(t)) this.PQ.remove(t)
    }

    override fun peek(): Node? = getMin()

    override fun contains(t: Node): Boolean = this.PQ.containsKey(t)

    override fun isEmpty(): Boolean = this.PQ.isEmpty()

    override fun isNotEmpty(): Boolean = this.PQ.isNotEmpty()

    override fun removeAll() {
        while(this.isNotEmpty()) this.deque()
    }
}