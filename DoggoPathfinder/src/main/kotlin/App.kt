import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.img
import kotlinx.html.js.*
import react.*
import react.dom.*

const val NUMBER_OF_ROWS = 15
const val NUMBER_OF_COLS = 25
const val TOTAL_CELLS = NUMBER_OF_ROWS * NUMBER_OF_COLS

external interface AppState: RState {
    var nodes: ArrayList<Node>
    var walls: Set<Node>
    var start: Node
    var end: Node
    var startEndInitialized: Boolean
    var queuing: Queue
    var visited: HashMap<Node, Node?>
    var path: List<Node?>
    var wallMouseDown: Boolean
    var startMouseDown: Boolean
    var endMouseDown: Boolean
    var wallTouchDown: Boolean
    var startTouchDown: Boolean
    var endTouchDown: Boolean
}

class App: RComponent<RProps,AppState>() {

    override fun AppState.init() {
        nodes = arrayListOf<Node>()
        walls = mutableSetOf<Node>()
        startEndInitialized = false
        queuing = Queue()
        visited = hashMapOf<Node, Node?>()
        path = listOf<Node?>()
        wallMouseDown = false
        startMouseDown = false
        endMouseDown = false
        wallTouchDown = false
        startTouchDown = false
        endTouchDown = false
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
                animateVisited(node, count++)
                val neighbors = getNeighbors(node)
                if(node == end) {
                    while(node != start) {
                        if(visited[node] != start) path += visited[node]
                        node = visited[node]
                    }
                    path = path.reversed()
                    break
                }
                for(neighbor in neighbors) {
                    if(!visited.containsKey(neighbor)) {
                        queuing.enqueue(neighbor)
                        visited[neighbor] = node
                    }
                }
            }
            path.forEach { node -> animatePath(node, path.indexOf(node), count) }
        }
    }

    private fun animatePath(node: Node?, i: Int, count: Int) {
        val pathNode = document.getElementById("node-${node?.row}-${node?.col}")
        val pathTrail = document.create.div("path") {
            img {
                alt = "path"
                src = "https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/apple/237/paw-prints_1f43e.png"
            }
        }
        js("setTimeout(function() { pathNode.appendChild(pathTrail) }, 80 * i + 20 * count)")
    }

    private fun clearPathAnimations() {
        val paths = document.getElementsByClassName("path")
        js("while(paths.length > 0) { paths[0].parentNode.removeChild(paths[0]) };")
    }

    private fun animateVisited(node: Node?, i: Int) {
        val visitedNode = document.getElementById("node-${node?.row}-${node?.col}")
        js("setTimeout( function() { visitedNode.className = 'visited'; }, 20 * i)")
    }

    private fun clearVisitedAnimations() {
        for(node in state.visited.keys) {
            val visitedNode = document.getElementById("node-${node.row}-${node.col}")
            js("visitedNode.classList.remove('visited');")
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
        setState { walls.forEach { wall -> walls -= wall } }
    }

    private fun clearPath() {
        clearVisitedAnimations()
        clearPathAnimations()
        setState {
            path.forEach { pathNode -> path -= pathNode }
            queuing.removeAll()
            visited.clear()
        }
    }

    private fun resetAll() {
        clearWalls()
        clearPath()
    }

    override fun RBuilder.render() {
        div("grid-container") {
            attrs {
                onMouseUpFunction = { state.wallMouseDown = false; state.startMouseDown = false; state.endMouseDown = false }
                onMouseOverFunction = { if(!state.startEndInitialized) initializeStartEnd() }
            }
            div("control-panel"){
                attrs {
                    onMouseUpFunction = { state.wallMouseDown = false; state.startMouseDown = false; state.endMouseDown = false }
                }
                button {
                    attrs { onClickFunction = { resetAll() } }
                    +"reset all"
                }
                button {
                    attrs { onClickFunction = { clearPath() } }
                    +"clear path"
                }
                button {
                    attrs { onClickFunction = { bfs() } }
                    +"BFS"
                }
            }
            table {
                tbody {
                    attrs {
                        onMouseUpFunction = { state.wallMouseDown = false; state.startMouseDown = false; state.endMouseDown = false }
                    }
                    for(i in 1..NUMBER_OF_ROWS) {
                        tr {
                            for(j in 1..NUMBER_OF_COLS) {
                                val node = Node(i,j)
                                state.nodes.add(node)
                                td("empty") {
                                    attrs["id"] = "node-${i}-${j}"
                                    attrs {
                                        fun addToWalls(node: Node) {
                                            if(!state.walls.contains(node)) state.walls += node
                                        }
                                        fun clearOldPath() {
                                            val td = document.getElementById("node-${i}-${j}")
                                            if(td!!.hasChildNodes()) {
                                                js("if(td.childNodes[0].className == 'path'){td.removeChild(td.childNodes[0])}")
                                            }
                                        }
                                        onMouseDownFunction = {
                                            setState { startEndInitialized = true }
                                            clearOldPath()
                                            when(node) {
                                                state.start -> state.startMouseDown = true
                                                state.end -> state.endMouseDown = true
                                                else -> {
                                                    state.wallMouseDown = true
                                                    addToWalls(node)
                                                }
                                            }
                                        }
                                        onMouseOverFunction = {
                                            setState { startEndInitialized = true }
                                            when {
                                                state.startMouseDown && !state.walls.contains(node) && node != state.end -> {
                                                    state.start = node
                                                    clearOldPath()
                                                }
                                                state.endMouseDown && !state.walls.contains(node) && node != state.start -> {
                                                    state.end = node
                                                    clearOldPath()
                                                }
                                                state.wallMouseDown && node != state.start && node != state.end -> {
                                                    addToWalls(node)
                                                    clearOldPath()
                                                }
                                            }
                                        }
                                        onMouseUpFunction = {
                                            when {
                                                state.startMouseDown -> state.startMouseDown = false
                                                state.endMouseDown -> state.endMouseDown = false
                                                state.wallMouseDown -> state.wallMouseDown = false
                                            }
                                        }
                                        onTouchStartFunction = {
                                            if(!state.startEndInitialized) initializeStartEnd()
                                            setState{
                                                startEndInitialized = true
                                                when(node) {
                                                    start -> startTouchDown = !startTouchDown
                                                    end -> endTouchDown = !endTouchDown
                                                    else -> {
                                                        when {
                                                            startTouchDown -> {
                                                                if (!walls.contains(node) && node != end) {
                                                                    start = node
                                                                    clearOldPath()
                                                                }
                                                                startTouchDown = false
                                                            }
                                                            endTouchDown -> {
                                                                if(!walls.contains(node) && node != start) {
                                                                    end = node
                                                                    clearOldPath()
                                                                }
                                                                endTouchDown = false
                                                            }
                                                            else -> {
                                                                if (node != start && node != end) addToWalls(node)
                                                            }
                                                        }
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
