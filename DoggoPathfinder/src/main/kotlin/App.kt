import react.*
import react.dom.*
import kotlinx.html.js.onClickFunction

const val NUMBER_OF_ROWS = 15
const val NUMBER_OF_COLS = 25

external interface AppState: RState {
    var nodes: ArrayList<Node>
    var walls: Set<Node>
    var startNode: Node
    var endNode: Node
    var startEndInitialized: Boolean
    var selectingWalls: Boolean
    var selectingStart: Boolean
    var selectingEnd: Boolean
    var visitedNodes: Queue
    var paths: HashMap<Node, Node>
}

class App: RComponent<RProps,AppState>() {

    override fun AppState.init() {
        nodes = arrayListOf<Node>()
        walls = mutableSetOf<Node>()
        startEndInitialized = false
        visitedNodes = Queue()
        paths = hashMapOf<Node, Node>()
    }

    private fun visitNeighbors() {
        setState {
            val neighbors = findNeighbors(this.startNode)
            this.visitedNodes.enqueue(this.startNode)
            while(this.visitedNodes.isNotEmpty()) {

                for(neighbor in neighbors) {
                    this.visitedNodes.enqueue(neighbor)
                }
            }

        }
    }

    private fun findNeighbors(node: Node): List<Node> {
        var neighbors = listOfNotNull<Node>()
        val index = state.nodes.indexOf(node)
        val up = index - NUMBER_OF_COLS
        val down = index + NUMBER_OF_COLS
        val left = index - 1
        val right = index + 1

        if (up >= 0 && !state.walls.contains(state.nodes[up]))
            neighbors += state.nodes[index-NUMBER_OF_COLS]

        if (down < state.nodes.size && !state.walls.contains(state.nodes[down]))
            neighbors += state.nodes[index+NUMBER_OF_COLS]

        if (left >= 0 && !state.walls.contains(state.nodes[left]))
            neighbors += state.nodes[index-1]

        if (right < state.nodes.size && !state.walls.contains(state.nodes[right]))
            neighbors += state.nodes[index+1]

        return neighbors
    }

    private fun initializeStartEnd() {
        state.startNode = state.nodes[180]
        state.endNode = state.nodes[194]
    }

    private fun resetNodes() {
        setState {
            for(wall in this.walls) {
                this.walls -= wall
            }
            this.selectingWalls = false
            this.selectingStart = false
            this.selectingEnd = false
        }
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
                                                    this.selectingWalls ->
                                                        if(!this.walls.contains(node)) {
                                                            if(node != this.startNode && node != this.endNode)
                                                                this.walls += node
                                                        } else {
                                                            this.walls -= node
                                                        }
                                                    this.selectingStart -> {
                                                        if(node != this.endNode && !this.walls.contains(node))
                                                            this.startNode = node
                                                        this.startEndInitialized = true
                                                    }
                                                    this.selectingEnd -> {
                                                        if(node != this.startNode && !this.walls.contains(node))
                                                            this.endNode = node
                                                        this.startEndInitialized = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    when {
                                        state.walls.contains(node) -> +"\uD83C\uDF33"
                                        state.startNode == node -> +"🐶"
                                        state.endNode == node -> +"⚾"
                                    }
                                }
                            }
                        }
                    }
                    if(!state.startEndInitialized) initializeStartEnd()
                }
            }
            button {
                attrs {
                    onClickFunction = {
                        resetNodes()
                    }
                }
                +"reset"
            }
            button {
                attrs {
                    onClickFunction = {
                        setState {
                            this.selectingWalls = true
                            this.selectingStart = false
                            this.selectingEnd = false
                        }
                    }
                }
                +"walls"
            }
            button {
                attrs {
                    onClickFunction = {
                        setState {
                            this.selectingWalls = false
                            this.selectingStart = true
                            this.selectingEnd = false
                        }
                    }
                }
                +"start"
            }
            button {
                attrs {
                    onClickFunction = {
                        setState {
                            this.selectingWalls = false
                            this.selectingStart = false
                            this.selectingEnd = true
                        }
                    }
                }
                +"end"
            }
            button {
                attrs {
                    onClickFunction = {
                        for(node in state.walls) {
                            println("node-${node.row}-${node.col}")
                        }
                    }
                }
                +"show walls"
            }
        }

    }
}
