external interface IStack<T> {
    var stack: List<T>
    fun getStack(): List<T>
    fun pop(): T?
    fun push(t: T)
    fun poll(t: T)
    fun peek(): T?
    fun contains(t: T): Boolean
    fun isEmpty(): Boolean
    fun isNotEmpty(): Boolean
    fun removeAll()
}

data class Stack(override var stack: List<Node> = mutableListOf<Node>()): IStack<Node> {

    override fun getStack(): List<Node> = this.stack

    override fun pop(): Node? {
        val node = this.stack.lastOrNull()
        if(this.stack.isNotEmpty()) {
            this.stack -= this.stack.last()
        }
        return node
    }

    override fun push(t: Node) {
        this.stack += t
    }

    override fun poll(t: Node) {
        if(this.contains(t)) this.stack -= t
    }

    override fun peek(): Node? = this.stack.lastOrNull()

    override fun contains(t: Node): Boolean = this.stack.contains(t)

    override fun isEmpty(): Boolean = this.stack.isEmpty()

    override fun isNotEmpty(): Boolean = this.stack.isNotEmpty()

    override fun removeAll() {
        while(this.isNotEmpty())
            this.pop()
    }
}