import react.*
import react.dom.*
import kotlinx.html.js.onClickFunction

const val NUMBER_OF_ROWS = 15
const val NUMBER_OF_COLS = 25

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
        setState {
            queuing.enqueue(start)
            while(queuing.isNotEmpty()) {
                var node = queuing.deque()
                val neighbors = getNeighbors(node)
                if(node == end) {
                    path += node
                    while(node != start) {
                        path += visited[node]
                        node = visited[node]
                    }
                }
                for(neighbor in neighbors) {
                    if(!visited.containsKey(neighbor)) {
                        queuing.enqueue(neighbor)
                        visited[neighbor] = node
                    }
                }
            }
            println("start node = node-${start.row}-${start.col}")
            println("end node = node-${end.row}-${end.col}")
            for(node in path) {
                println("node-${node?.row}-${node?.col}")
            }
        }
    }

    private fun getNeighbors(node: Node?): List<Node> {
        var neighbors = listOfNotNull<Node>()
        val i = state.nodes.indexOf(node)
        val up = i - NUMBER_OF_COLS
        val down = i + NUMBER_OF_COLS
        val left = i - 1
        val right = i + 1

        if (right < state.nodes.size && !state.walls.contains(state.nodes[right]) && state.nodes[right] != state.start)
            neighbors += state.nodes[i+1]

        if (down < state.nodes.size && !state.walls.contains(state.nodes[down]) && state.nodes[down] != state.start)
            neighbors += state.nodes[i+NUMBER_OF_COLS]

        if (i % NUMBER_OF_COLS != 0 && left >= 0 && !state.walls.contains(state.nodes[left]) &&
            state.nodes[left] != state.start)
            neighbors += state.nodes[i-1]

        if (i % NUMBER_OF_COLS != NUMBER_OF_COLS-1 && up >= 0 && !state.walls.contains(state.nodes[up]) &&
            state.nodes[up] != state.start)
            neighbors += state.nodes[i-NUMBER_OF_COLS]

        return neighbors
    }

    private fun initializeStartEnd() {
        state.start = state.nodes[180]
        state.end = state.nodes[194]
    }

    private fun resetNodes() {
        setState {
            for(wall in walls) {
                walls -= wall
            }
            for(node in path) {
                path -= node
            }
            queuing.removeAll()
            visited.clear()
            selectingWalls = false
            selectingStart = false
            selectingEnd = false
        }
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
                                        state.walls.contains(node) -> +"\uD83C\uDF33"
                                        state.start == node -> +"🐶"
                                        state.end == node -> +"⚾"
                                        state.path.contains(node) -> +"\uD83D\uDC3E"
                                    }
                                }
                            }
                        }
                    }
                    if(!state.startEndInitialized) initializeStartEnd()
                }
            }
            button {
                attrs { onClickFunction = { resetNodes() } }
                +"reset"
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

    }
}
