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
    var cancellingWall: Boolean
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
        cancellingWall = false
    }

    private fun bfs() {
        clearPath()
        state.cancellingWall = false
        disablePointer()
        var count = 1
        var found = false
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
                    found = true
                    break
                }
                for(neighbor in neighbors) {
                    if(!visited.containsKey(neighbor)) {
                        queuing.enqueue(neighbor)
                        visited[neighbor] = node
                    }
                }
            }
            if(found) path.forEach { node -> animatePath(node, path.indexOf(node), count) }
            else showNoPathDialog(count)
            reenablePointer(path.size, count)
        }
    }

    private fun disablePointer() {
        document.getElementById("board")?.setAttribute("style", "pointer-events: none;")
        document.getElementById("control-panel")?.setAttribute("style", "pointer-events: none;")
    }

    private fun reenablePointer(length: Int, count: Int) {
        val board = document.getElementById("board")
        val controlPanel = document.getElementById("control-panel")
        js("setTimeout(function() {board.removeAttribute('style'); controlPanel.removeAttribute('style');}, 80*length + 20*count)")
    }

    private fun animatePath(node: Node?, i: Int, count: Int) {
        val pathNode = document.getElementById("node-${node?.row}-${node?.col}")
        val pathTrail = document.create.div("path") {
            img {
                alt = "path"
                src = "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/paw-emoji.png"
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
        val row = node!!.row
        val col = node.col
        val left = if(col-1 >= 1) state.nodes[state.nodes.indexOf(Node(row, col-1))] else Node(0,0)
        val right = if(col+1 <= NUMBER_OF_COLS) state.nodes[state.nodes.indexOf(Node(row, col+1))] else Node(0, 0)
        val up = if(row-1 >= 1) state.nodes[state.nodes.indexOf(Node(row-1, col))] else Node(0, 0)
        val down = if(row+1 <= NUMBER_OF_ROWS) state.nodes[state.nodes.indexOf(Node(row+1, col))] else Node(0, 0)
        var neighbors = listOf<Node>(left, right, up, down)
        neighbors.forEach { neighbor ->
            if(neighbor.row == 0 || neighbor == state.start || state.walls.contains(neighbor) || state.visited.containsKey(neighbor))
                neighbors -= neighbor
        }
        return neighbors
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
        setState { walls.forEach { wall -> walls -= wall } }
        clearPath()
    }

    private fun cancellingWall() {
        setState {
            wallMouseDown = false
            startMouseDown = false
            endMouseDown = false
            wallTouchDown = false
            startTouchDown = false
            endTouchDown = false
            cancellingWall = !cancellingWall
        }
    }

    private fun showNoPathDialog(count: Int) {
        val dialog = document.getElementById("no-path-bg")
        js("setTimeout( function() { dialog.setAttribute('style', 'display: flex;') }, 20 * count + 200)")
    }

    private fun hideDialog(id: String) {
        document.getElementById(id)?.setAttribute("style", "display: none;")
    }

    override fun RBuilder.render() {
        div("grid-container") {
            attrs {
                onMouseUpFunction = { state.wallMouseDown = false; state.startMouseDown = false; state.endMouseDown = false }
            }
            div("logo") {
                img {
                    attrs {
                        src = "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/logo.png"
                    }
                }
            }
            div{
                attrs["id"] = "control-panel"
                attrs {
                    onMouseUpFunction = { state.wallMouseDown = false; state.startMouseDown = false; state.endMouseDown = false }
                }
                button {
                    attrs { onClickFunction = { resetAll() } }
                    +"reset"
                }
                button {
                    attrs { onClickFunction = { clearPath() } }
                    +"clear path"
                }
                button {
                    attrs { onClickFunction = { cancellingWall() } }
                    if(!state.cancellingWall) +"remove a tree" else +"add a tree"
                }
                button {
                    attrs { onClickFunction = { bfs() } }
                    +"breadth-first search"
                }
            }
            div {
                attrs["id"] = "no-path-bg"
                div("no-path") {
                    img {
                        attrs {
                            alt = "dog crying"
                            src = "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/dog-cry.png"
                        }
                    }
                    p {
                        +"there is no path!"
                    }
                    button {
                        attrs { onClickFunction = { hideDialog("no-path-bg") } }
                        +"try again"
                    }
                }
            }
            table {
                attrs["id"] = "board"
                tbody {
                    attrs {
                        onMouseUpFunction = { state.wallMouseDown = false; state.startMouseDown = false; state.endMouseDown = false }
                    }
                    for(i in 1..NUMBER_OF_ROWS) {
                        tr {
                            for(j in 1..NUMBER_OF_COLS) {
                                val node = Node(i,j)
                                state.nodes.add(node)
                                td {
                                    attrs["id"] = "node-${i}-${j}"
                                    if(!state.startEndInitialized && node.row == 8) {
                                        when(node.col) {
                                            6 -> state.start = node
                                            20 -> state.end = node
                                        }
                                    }
                                    attrs {
                                        fun addToWalls(node: Node) {
                                            if(!state.walls.contains(node)) state.walls += node
                                        }
                                        fun removeFromWalls(node: Node) {
                                            if(state.walls.contains(node)) state.walls -= node
                                        }
                                        fun clearOldPath() {
                                            val td = document.getElementById("node-${i}-${j}")
                                            if(td!!.hasChildNodes()) {
                                                js("if(td.childNodes[0].className == 'path'){td.removeChild(td.childNodes[0])}")
                                            }
                                        }

                                        onMouseDownFunction = {
                                            setState { startEndInitialized = true }
                                            if(!state.cancellingWall) {
                                                clearOldPath()
                                                when(node) {
                                                    state.start -> state.startMouseDown = true
                                                    state.end -> state.endMouseDown = true
                                                    else -> {
                                                        state.wallMouseDown = true
                                                        addToWalls(node)
                                                    }
                                                }
                                            } else {
                                                if(node != state.start && node != state.end) removeFromWalls(node)
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
                                            setState{
                                                startEndInitialized = true
                                                if(!state.cancellingWall) {
                                                    when (node) {
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
                                                                    if (!walls.contains(node) && node != start) {
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
                                                } else {
                                                    if(node != start && node != end) removeFromWalls(node)
                                                }
                                            }
                                        }
                                    }
                                    when {
                                        state.walls.contains(node) -> {
                                            img(classes = "board-element", alt = "tree") {
                                                attrs {
                                                    src = "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/tree-emoji.png"
                                                }
                                            }
                                        }
                                        state.start == node -> {
                                            img(classes = "board-element", alt = "dog") {
                                                attrs {
                                                    src = "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/dog-emoji.png"
                                                }
                                            }
                                        }
                                        state.end == node -> {
                                            img(classes = "board-element", alt = "ball") {
                                                attrs {
                                                    src = "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/baseball-emoji.png"
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
