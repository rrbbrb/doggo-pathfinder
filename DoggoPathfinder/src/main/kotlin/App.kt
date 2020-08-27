import kotlinx.browser.document
import kotlinx.css.img
import kotlinx.html.dom.create
import kotlinx.html.img
import kotlinx.html.js.div
import react.*
import react.dom.*
import kotlinx.html.js.onClickFunction

const val NUMBER_OF_ROWS = 15
const val NUMBER_OF_COLS = 25
const val TOTAL_CELLS = NUMBER_OF_ROWS * NUMBER_OF_COLS

external interface AppState: RState {
    var nodes: ArrayList<Node>
    var walls: Set<Node>
    var start: Node
    var end: Node
    var startEndInitialized: Boolean
    var selectingWalls: Boolean
    var selectingStart: Boolean
    var selectingEnd: Boolean
    var queuing: Queue
    var visited: HashMap<Node, Node?>
    var path: List<Node?>
}

class App: RComponent<RProps,AppState>() {

    override fun AppState.init() {
        nodes = arrayListOf<Node>()
        walls = mutableSetOf<Node>()
        startEndInitialized = false
        queuing = Queue()
        visited = hashMapOf<Node, Node?>()
        path = listOf<Node?>()
    }

    private fun bfs() {
        clearPath()
        var count = 1
        setState {
            queuing.enqueue(start)
            visited[start] = start
            animateVisited(start, count++)
            while(queuing.isNotEmpty()) {
                var node = queuing.deque()
                val neighbors = getNeighbors(node)
                if(node == end) {
                    path += node
                    while(node != start) {
                        path += visited[node]
                        node = visited[node]
                    }
                    path = path.reversed()
                    break
                }
                for(neighbor in neighbors) {
                    if(!visited.containsKey(neighbor)) {
                        queuing.enqueue(neighbor)
                        animateVisited(neighbor, count++)
                        visited[neighbor] = node
                    }
                }
            }
            animateWholePath(path, count)
        }
    }

    private fun animateWholePath(path: List<Node?>, count: Int) {
        fun animate() = path.forEach { node -> animatePath(node, path.indexOf(node)) }
        val animate = animate()
        js("setTimeout(animate, 20 * count)")
    }

    private fun animatePath(node: Node?, index: Int) {
        val id = "node-${node?.row}-${node?.col}"
        val start = state.start
        val end = state.end
        val pathNode = document.create.div("path") {
            img {
                alt = "path"
                src = "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/apple/237/paw-prints_1f43e.png"
            }
        }
        js("setTimeout( function() {" +
                "if(node != start && node != end) {document.getElementById(id).appendChild(pathNode)}" +
                "}, 50 * index)")
    }

    private fun animateVisited(node: Node, index: Int) {
        val id = "node-${node.row}-${node.col}"
        val start = state.start
        val end = state.end
        val visited = "visited"
        js("setTimeout( function() {" +
                "document.getElementById(id).className = visited;" +
                "}, 20 * index)")
    }

    private fun clearVisitedAnimations() {
        for(node in state.visited.keys) {
            val id = "node-${node.row}-${node.col}"
            val visited = "visited"
            js("var node = document.getElementById(id);" +
                    "node.classList.remove(visited);")
        }
    }

    private fun getNeighbors(node: Node?): List<Node> {
        var neighbors = listOfNotNull<Node>()
        val i = state.nodes.indexOf(node)
        val up = i - NUMBER_OF_COLS
        val down = i + NUMBER_OF_COLS
        val left = i - 1
        val right = i + 1
        fun isWall(node: Node): Boolean = state.walls.contains(node)
        fun isStart(node: Node): Boolean = node == state.start
        fun withinRange(index: Int): Boolean = index in 0 until TOTAL_CELLS
        fun leftEdge(): Boolean = i % NUMBER_OF_COLS == 0
        fun rightEdge(): Boolean = i % NUMBER_OF_COLS == NUMBER_OF_COLS-1
        fun notVisited(node: Node): Boolean = !state.visited.containsKey(node)
        fun qualify(index: Int): Boolean = withinRange(index) && !isWall(state.nodes[index]) && !isStart(state.nodes[index]) && notVisited(state.nodes[index])

        if(qualify(right) && !rightEdge()) neighbors += state.nodes[right]
        if(qualify(down)) neighbors += state.nodes[down]
        if(qualify(left) && !leftEdge()) neighbors += state.nodes[left]
        if(qualify(up)) neighbors += state.nodes[up]

        return neighbors
    }

    private fun initializeStartEnd() {
        state.start = state.nodes[180]
        state.end = state.nodes[194]
    }

    private fun clearWalls() {
        setState {
            for(wall in walls) {
                walls -= wall
            }
        }
    }

    private fun clearPath() {
        clearVisitedAnimations()
        setState {
            for(node in path) {
                path -= node
            }
            queuing.removeAll()
            visited.clear()
            selectingWalls = true
            selectingStart = false
            selectingEnd = false
        }
    }

    private fun resetAll() {
        clearWalls()
        clearPath()
    }

    private fun selectingWalls() {
        setState { selectingWalls = true; selectingStart = false; selectingEnd = false }
    }

    private fun selectingStart() {
        setState { selectingWalls = false; selectingStart = true; selectingEnd = false }
    }

    private fun selectingEnd() {
        setState { selectingWalls = false; selectingStart = false; selectingEnd = true }
    }

    override fun RBuilder.render() {
        div("grid-container") {
            div("control-panel"){
                button {
                    attrs { onClickFunction = { resetAll() } }
                    +"reset"
                }
                button {
                    attrs { onClickFunction = { clearWalls() } }
                    +"clear board"
                }
                button {
                    attrs { onClickFunction = { selectingWalls() } }
                    +"walls"
                }
                button {
                    attrs { onClickFunction = { selectingStart() } }
                    +"start"
                }
                button {
                    attrs { onClickFunction = { selectingEnd() } }
                    +"end"
                }
                button {
                    attrs { onClickFunction = { bfs() } }
                    +"BFS"
                }
            }
            table {
                tbody {
                    for(i in 1..NUMBER_OF_ROWS) {
                        tr {
                            for(j in 1..NUMBER_OF_COLS) {
                                val node = Node(i,j)
                                state.nodes.add(node)
                                td {
                                    attrs["id"] = "node-${i}-${j}"
                                    attrs {
                                        onClickFunction = {
                                            setState {
                                                when {
                                                    selectingWalls ->
                                                        if(!walls.contains(node)) {
                                                            if(node != start && node != end)
                                                                walls += node
                                                        } else {
                                                            walls -= node
                                                        }
                                                    selectingStart -> {
                                                        if(node != end && !walls.contains(node))
                                                            start = node
                                                        startEndInitialized = true
                                                    }
                                                    selectingEnd -> {
                                                        if(node != start && !walls.contains(node))
                                                            end = node
                                                        startEndInitialized = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    when {
                                        state.walls.contains(node) -> {
                                            img(classes = "board-element", alt = "tree") {
                                                attrs {
                                                    src = "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/microsoft/209/deciduous-tree_1f333.png"
                                                }
                                            }
                                        }
                                        state.start == node -> {
                                            img(classes = "board-element", alt = "dog") {
                                                attrs {
                                                    src = "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/microsoft/209/dog-face_1f436.png"
                                                }
                                            }
                                        }
                                        state.end == node -> {
                                            img(classes = "board-element", alt = "ball") {
                                                attrs {
                                                    src = "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/microsoft/209/baseball_26be.png"
                                                }
                                            }
                                        }
//                                        state.path.contains(node) -> {
//                                            img(classes = "board-element", alt = "path") {
//                                                attrs {
//                                                    src = "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/apple/237/paw-prints_1f43e.png"
//                                                }
//                                            }
//                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(!state.startEndInitialized) initializeStartEnd()
                }
            }
        }
    }
}
