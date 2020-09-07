import classes.PriorityQueue
import classes.Queue
import classes.Stack
import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.img
import kotlinx.html.js.*
import org.w3c.dom.get
import react.*
import react.dom.*
import kotlin.math.abs
import kotlin.random.Random

const val NUMBER_OF_ROWS = 15
const val NUMBER_OF_COLS = 25
const val TOTAL_CELLS = NUMBER_OF_ROWS * NUMBER_OF_COLS

external interface AppState: RState {
    var nodes: ArrayList<Node>
    var walls: Set<Node>
    var start: Node
    var end: Node
    var startEndInitialized: Boolean
    var queue: Queue
    var stack: Stack
    var parentVisited: HashMap<Node, Node?>
    var path: List<Node?>
    var wallMouseDown: Boolean
    var startMouseDown: Boolean
    var endMouseDown: Boolean
    var wallTouchDown: Boolean
    var startTouchDown: Boolean
    var endTouchDown: Boolean
    var cancellingWall: Boolean
    var visited: Set<Node>
    var minPQ: PriorityQueue
    var costs: HashMap<Node, Int>
}

class App: RComponent<RProps,AppState>() {

    override fun AppState.init() {
        nodes = arrayListOf<Node>()
        walls = mutableSetOf<Node>()
        startEndInitialized = false
        queue = Queue()
        stack = Stack()
        parentVisited = hashMapOf<Node, Node?>()
        path = listOf<Node?>()
        wallMouseDown = false
        startMouseDown = false
        endMouseDown = false
        wallTouchDown = false
        startTouchDown = false
        endTouchDown = false
        cancellingWall = false
        visited = setOf<Node>()
        minPQ = PriorityQueue()
        costs = hashMapOf<Node, Int>()
    }

    private fun bfs() {
        clearPath()
        state.cancellingWall = false
        disablePointer()
        var count = 1
        var hasPath = false
        setState {
            queue.enqueue(start)
            parentVisited[start] = start
            while(queue.isNotEmpty()) {
                var node = queue.deque()
                animateVisited(node, count++)
                val neighbors = getNeighbors(node)
                if(node == end) {
                    while(node != start) {
                        if(parentVisited[node] != start) path += parentVisited[node]
                        node = parentVisited[node]
                    }
                    path = path.reversed()
                    hasPath = true
                    break
                }
                for(neighbor in neighbors) {
                    if(!parentVisited.containsKey(neighbor)) {
                        queue.enqueue(neighbor)
                        parentVisited[neighbor] = node
                    }
                }
            }
            val speed = 60
            if(hasPath) path.forEach { node -> animatePath(node, path.indexOf(node), count, speed) }
            else showNoPathDialog(count)
            reenablePointer(path.size, count, speed)
        }
    }
    private fun dfs() {
        clearPath()
        state.cancellingWall = false
        disablePointer()
        var count = 1
        var hasPath = false
        setState {
            stack.push(start)
            parentVisited[start] = start
            while(stack.isNotEmpty()) {
                var node = stack.pop()
                visited += node!!
                animateVisited(node, count++)
                if(node == end) {
                    while(node != start) {
                        if(parentVisited[node] != start) path += parentVisited[node]
                        node = parentVisited[node]
                    }
                    path = path.reversed()
                    hasPath = true
                    break
                }
                val neighbors = getNeighbors(node).asReversed()
                for(neighbor in neighbors) {
                    if(!visited.contains(neighbor)) {
                        stack.push(neighbor)
                        parentVisited[neighbor] = node
                    }
                }
            }
            val speed = 60
            if(hasPath) path.forEach { node -> animatePath(node, path.indexOf(node), count, speed) }
            else showNoPathDialog(count)
            reenablePointer(path.size, count, speed)
        }
    }

    private fun dijkstra() {
        clearPath()
        state.cancellingWall = false
        disablePointer()
        var count = 1
        var hasPath = false
        setState {
            costs[start] = 0
            minPQ.enqueue(start, 0)
            nodes.forEach { node -> if(node != start) costs[node] = Int.MAX_VALUE }
            while(minPQ.isNotEmpty()) {
                var minNode = minPQ.deque()
                visited += minNode!!
                animateVisited(minNode, count++)
                if(minNode == end) {
                    while(minNode != start) {
                        if(parentVisited[minNode] != start) path += parentVisited[minNode]
                        minNode = parentVisited[minNode]
                    }
                    path = path.reversed()
                    hasPath = true
                    break
                }
                val neighbors = getNeighbors(minNode)
                for(neighbor in neighbors) {
                    if(!visited.contains(neighbor)) {
                        val altPath = costs[minNode]?.plus(1)
                        if (altPath != null) {
                            if(altPath < costs[neighbor]!!) {
                                costs[neighbor] = altPath
                                parentVisited[neighbor] = minNode
                                minPQ.enqueue(neighbor, altPath)
                            }
                        }
                    }
                }
            }
            val speed = 60
            if(hasPath) path.forEach { node -> animatePath(node, path.indexOf(node), count, speed) }
            else showNoPathDialog(count)
            reenablePointer(path.size, count, speed)
        }
    }

    private fun aStar() {
        clearPath()
        state.cancellingWall = false
        disablePointer()
        var count = 1
        var hasPath = false
        fun manhattan(node: Node, end: Node) = abs(node.row - end.row) + abs(node.col - end.col)
        setState {
            val fOfStart = 0 + manhattan(start, end)
            minPQ.enqueue(start, fOfStart)
            parentVisited[start] = start
            while(minPQ.isNotEmpty()) {
                val node = minPQ.peek()!!
                val fOfNode = minPQ.getPriority(node)!!
                val gOfNode = fOfNode - manhattan(node, end)
                minPQ.deque()
                costs[node] = fOfNode
                animateVisited(node, count++)
                val neighbors = getNeighbors(node)
                for(neighbor in neighbors) {
                    animateVisited(neighbor, count++)
                    if(neighbor == end) {
                        parentVisited[neighbor] = node
                        var pathNode = neighbor
                        while(pathNode != start) {
                            if(parentVisited[pathNode] != start) path += parentVisited[pathNode]
                            pathNode = parentVisited[pathNode]!!
                        }
                        path = path.reversed()
                        hasPath = true
                        break
                    }
                    val fOfNeighbor = (gOfNode + 1) + manhattan(neighbor, end)
                    if(minPQ.contains(neighbor) && minPQ.getPriority(neighbor)!! <= fOfNeighbor) {
                        continue
                    }
                    if(costs.containsKey(neighbor) && costs[neighbor]!! <= fOfNeighbor) {
                        continue
                    } else {
                        minPQ.enqueue(neighbor, fOfNeighbor)
                        parentVisited[neighbor] = node
                    }
                }
                if(hasPath) break
            }
            val speed = 60
            if(hasPath) path.forEach { node -> animatePath(node, path.indexOf(node), count, speed) }
            else showNoPathDialog(count)
            reenablePointer(path.size, count, speed)
        }
    }

    private fun generateMaze() {
        resetAll()
        val orientation = if(Random.nextBoolean()) "horizontal" else "vertical"

        fun recursiveDivision(orientation: String, startRow: Int, endRow: Int, startCol: Int, endCol: Int,
                              columnPassage: Int, rowPassage: Int) {
            val nextOrientation =
                if(orientation == "horizontal") {
                    if(endRow - startRow == 1) "horizontal"
                    else "vertical"
                } else {
                    if(endCol - startCol == 1) "vertical"
                    else "horizontal"
                }
            when (orientation) {
                "horizontal" -> {
                    when {
                        endCol == startCol || endRow == startRow-> return
                        else -> {
                            val wallColumn : Int? =
                                if(rowPassage == 0 || endRow - startRow == 1) {
                                    (startCol + 1 until endCol).filter { it -> it != rowPassage }.randomOrNull()
                                } else {
                                    (rowPassage-1..rowPassage+1).filter { it -> it != rowPassage && it > startCol && it < endCol }.randomOrNull()
                                }
                            val passageInCol : Int =
                                if(endRow - startRow == 2) {
                                    if(Random.nextBoolean()) endRow else startRow
                                } else {
                                    (startRow..endRow).random()
                                }
                            setState {
                                if(wallColumn != null) {
                                    for (wallRow in startRow..endRow) {
                                        if (wallRow != passageInCol) {
                                            val node = nodes[nodes.indexOf(Node(wallRow, wallColumn))]
                                            if(node != start && node != end) walls += node
                                        }
                                    }
                                }
                            }
                            if (wallColumn != null) {
                                recursiveDivision(nextOrientation, startRow, endRow, startCol, wallColumn-1, passageInCol, rowPassage)
                                recursiveDivision(nextOrientation, startRow, endRow, wallColumn+1, endCol, passageInCol, rowPassage)
                            }
                        }
                    }
                }
                "vertical" -> {
                    when {
                        endRow == startRow || endCol == startCol -> return
                        else -> {
                            val wallRow : Int? =
                                if(columnPassage == 0 || endCol - startCol == 1) {
                                    (startRow+1 until endRow).filter { it -> it != columnPassage }.randomOrNull()
                                } else {
                                    (columnPassage-1..columnPassage+1).filter { it -> it != columnPassage && it > startRow && it < endRow }.randomOrNull()
                                }
                            val passageInRow : Int =
                                if(endCol - startCol == 2) {
                                    if(Random.nextBoolean()) endCol else startCol
                                } else {
                                    (startCol..endCol).random()
                                }
                            setState {
                                if(wallRow != null) {
                                    for(wallColumn in startCol..endCol) {
                                        if(wallColumn != passageInRow) {
                                            val node = nodes[nodes.indexOf(Node(wallRow, wallColumn))]
                                            if(node != start && node != end) walls += node
                                        }
                                    }
                                }
                            }
                            if (wallRow != null) {
                                recursiveDivision(nextOrientation, startRow, wallRow-1, startCol, endCol, columnPassage, passageInRow)
                                recursiveDivision(nextOrientation, wallRow+1, endRow, startCol, endCol, columnPassage, passageInRow)
                            }
                        }
                    }
                }
            }
        }
        recursiveDivision(orientation, 1, NUMBER_OF_ROWS, 1, NUMBER_OF_COLS, 0, 0)
    }

    private fun reenableDropdown() {
        document.getElementsByClassName("dropdown")[0]?.removeAttribute("style")
    }

    private fun hideDropdown() {
        document.getElementsByClassName("dropdown")[0]?.setAttribute("style", "display: none;")
    }

    private fun disablePointer() {
        document.getElementById("board")?.setAttribute("style", "pointer-events: none;")
        document.getElementById("control-panel")?.setAttribute("style", "pointer-events: none;")
    }

    private fun reenablePointer(length: Int, count: Int, speed: Int) {
        val board = document.getElementById("board")
        val controlPanel = document.getElementById("control-panel")
        js("setTimeout(function() {board.removeAttribute('style'); controlPanel.removeAttribute('style'); }, speed*length + 20*count)")
    }

    private fun animatePath(node: Node?, i: Int, count: Int, speed: Int) {
        val pathNode = document.getElementById("node-${node?.row}-${node?.col}")
        val pathTrail = document.create.div("path") {
            img {
                alt = "path"
                src = "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/paw-emoji.png"
            }
        }
        js("setTimeout(function() { pathNode.appendChild(pathTrail) }, speed * i + 20 * count)")
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
        val allVisited = state.parentVisited.keys.toSet() + state.visited
        for(node in allVisited) {
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
        var neighbors = listOf<Node>(up, right, down, left)
        neighbors.forEach { neighbor ->
            if(neighbor.row == 0 || neighbor == state.start || state.walls.contains(neighbor))
                neighbors -= neighbor
        }
        return neighbors
    }

    private fun clearPath() {
        clearVisitedAnimations()
        clearPathAnimations()
        setState {
            path.forEach { pathNode -> path -= pathNode }
            queue.removeAll()
            stack.removeAll()
            parentVisited.clear()
            visited.forEach { visitedNode -> visited -= visitedNode }
            minPQ.removeAll()
            costs.clear()
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
                    attrs { onClickFunction = { generateMaze() } }
                    +"generate maze"
                }
                button {
                    attrs { onClickFunction = { cancellingWall() } }
                    +"remove tree"
                    i {
                        img {
                            attrs {
                                src = if(!state.cancellingWall) "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/off-switch.png"
                                else "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/on-switch.png"
                            }
                        }
                    }
                }
                var shownDropdown = false
                div("algorithms") {
                    button {
                        attrs {
                            onClickFunction = {
                                if(shownDropdown) hideDropdown()
                                else reenableDropdown()
                                shownDropdown = !shownDropdown
                            }
                            onMouseOverFunction = {
                                reenableDropdown()
                            }
                        }
                        +"visualize algorithm"
                        i {
                            img {
                                attrs {
                                    src = "https://raw.githubusercontent.com/rrbbrb/doggo-pathfinder/media/caret.png"
                                }
                            }
                        }
                    }
                    div("dropdown") {
                        p {
                            attrs {
                                onClickFunction = {
                                    bfs()
                                    hideDropdown()
                                    shownDropdown = false
                                }
                            }
                            +"breadth-first search"
                        }
                        p {
                            attrs {
                                onClickFunction = {
                                    dfs()
                                    hideDropdown()
                                    shownDropdown = false
                                }
                            }
                            +"depth-first search"
                        }
                        p {
                            attrs {
                                onClickFunction = {
                                    dijkstra()
                                    hideDropdown()
                                    shownDropdown = false
                                }
                            }
                            +"dijsktra's algorithm"
                        }
                        p {
                            attrs {
                                onClickFunction = {
                                    aStar()
                                    hideDropdown()
                                    shownDropdown = false
                                }
                            }
                            +"a* search"
                        }
                    }
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
